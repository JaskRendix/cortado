/* Copyright (C) <2004> Wim Taymans <wim@fluendo.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package com.fluendo.plugin;

import java.util.*;
import com.fluendo.jst.*;
import com.fluendo.utils.*;

public class Queue extends Element {
    /* Leaky types */
    public static final int NO_LEAK = 0;
    public static final int LEAK_UPSTREAM = 1;
    public static final int LEAK_DOWNSTREAM = 2;

    private static final int DEFAULT_MAX_BUFFERS = 100;
    private static final int DEFAULT_MAX_SIZE = -1;
    private static final boolean DEFAULT_IS_BUFFER = false;
    private static final int DEFAULT_LOW_PERCENT = 10;
    private static final int DEFAULT_HIGH_PERCENT = 70;
    private static final int DEFAULT_LEAKY = NO_LEAK;

    private final List<java.lang.Object> queue = new ArrayList<>();
    private int srcResult = Pad.WRONG_STATE;
    private int size;
    private boolean isBuffering;
    private boolean isEOS;
    private boolean headNeedsDiscont = false;
    private boolean tailNeedsDiscont = false;

    private int maxBuffers = DEFAULT_MAX_BUFFERS;
    private int maxSize = DEFAULT_MAX_SIZE;
    private boolean isBuffer = DEFAULT_IS_BUFFER;
    private int lowPercent = DEFAULT_LOW_PERCENT;
    private int highPercent = DEFAULT_HIGH_PERCENT;
    private int leaky = DEFAULT_LEAKY;

    private boolean isFilled() {
        if (maxSize != -1) {
            return size >= maxSize;
        } else {
            return queue.size() >= maxBuffers;
        }
    }

    private boolean isEmpty() {
        return queue.isEmpty();
    }

    private void clearQueue() {
        for (java.lang.Object obj : queue) {
            if (obj instanceof Buffer) {
                ((Buffer) obj).free();
            }
        }
        queue.clear();
        size = 0;
        isBuffering = true;
    }

    private void updateBuffering() {
        if (!isBuffer || srcResult != Pad.OK)
            return;
        if (isEOS) {
            if (isBuffering) {
                isBuffering = false;
                postMessage(Message.newBuffering(this, false, 0));
            }
            return;
        }

        /* Figure out the percentage we are filled */
        int percent = size * 100 / maxSize;
        if (percent > 100)
            percent = 100;

        if (isBuffering) {
            if (percent >= highPercent) {
                isBuffering = false;
            }
            postMessage(Message.newBuffering(this, isBuffering, percent));
        } else {
            if (percent < lowPercent) {
                isBuffering = true;
            }
        }
    }

    private void leakDownstream() {
        /* For as long as the queue is filled, dequeue an item and discard it */
        java.lang.Object leak;
        while (isFilled()) {
            synchronized (queue) {
                leak = queue.get(queue.size() - 1);
                if (leak == null) {
                    Debug.error("There is nothing to dequeue and the queue is still filled. This should not happen.");
                }
                queue.remove(queue.size() - 1);
                if (leak instanceof Buffer) {
                    ((Buffer) leak).free();
                }
                headNeedsDiscont = true;
                queue.notifyAll();
            }
        }
    }

    private final Pad srcpad = new Pad(Pad.SRC, "src") {
        @Override
        protected void taskFunc() {
            java.lang.Object obj;
            int res;

            synchronized (queue) {
                if (srcResult != OK)
                    return;

                while (isEmpty()) {
                    try {
                        queue.wait();
                        if (srcResult != OK)
                            return;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                obj = queue.remove(queue.size() - 1);
                queue.notifyAll();
            }

            if (obj instanceof Event) {
                Event event = (Event) obj;
                pushEvent(event);
                res = OK;
                if (event.getType() == Event.Type.EOS) {
                    postMessage(Message.newStreamStatus(this, false, OK, "flow stopped, EOS"));
                    pauseTask();
                }
            } else {
                Buffer buf = (Buffer) obj;

                if (headNeedsDiscont) {
                    buf.setFlag(Buffer.FLAG_DISCONT, true);
                    headNeedsDiscont = false;
                }

                size -= buf.length;

                Debug.log(Debug.DEBUG, parent.getName() + " >>> " + buf);
                res = push(buf);
                if (maxSize == -1) {
                    Debug.log(Debug.DEBUG, parent.getName() + " count = " + queue.size() + "/" + maxBuffers);
                } else {
                    Debug.log(Debug.DEBUG, parent.getName() + " size = " + size + "/" + maxSize);
                }
            }
            synchronized (queue) {
                if (res != OK) {
                    srcResult = res;
                    if (isFlowFatal(res)) {
                        pushEvent(Event.newEOS());
                    }
                    postMessage(Message.newStreamStatus(this, false, res, "flow stopped"));
                    pauseTask();
                }
                updateBuffering();
            }
        }

        @Override
        protected boolean activateFunc(int mode) {
            boolean res = true;

            switch (mode) {
                case MODE_NONE -> {
                    synchronized (queue) {
                        clearQueue();
                        srcResult = WRONG_STATE;
                        queue.notifyAll();
                    }
                    // Cancel buffering status
                    if (isBuffer && isBuffering) {
                        isBuffering = false;
                        postMessage(Message.newBuffering(this, false, 0));
                    }
                    postMessage(Message.newStreamStatus(this, false, Pad.WRONG_STATE, "stopping"));
                    res = stopTask();
                }
                case MODE_PUSH -> {
                    isEOS = false;
                    synchronized (queue) {
                        srcResult = OK;
                        /*
                         * If we buffer, we start when we are hitting the
                         * high watermark
                         */
                        if (!isBuffer) {
                            isBuffering = false;
                        } else {
                            isBuffering = true;
                            postMessage(Message.newBuffering(this, true, 0));
                        }
                        postMessage(Message.newStreamStatus(this, true, Pad.OK, "activating"));
                        res = startTask("cortado-Queue-Stream-" + Debug.genId());
                    }
                }
                default -> {
                    synchronized (queue) {
                        srcResult = WRONG_STATE;
                    }
                    res = false;
                }
            }
            return res;
        }
    };

    private final Pad sinkpad = new Pad(Pad.SINK, "sink") {
        @Override
        protected boolean eventFunc(Event event) {
            Event.Type type = event.getType();
            boolean doQueue = true;

            switch (type) {
                case FLUSH_START -> {
                    srcpad.pushEvent(event);
                    synchronized (queue) {
                        srcResult = WRONG_STATE;
                        queue.notifyAll();
                    }
                    synchronized (streamLock) {
                        Debug.log(Debug.DEBUG, this + " synced");
                    }
                    postMessage(Message.newStreamStatus(srcpad, false, Pad.WRONG_STATE, "flush start"));
                    srcpad.pauseTask();
                    doQueue = false;
                }
                case FLUSH_STOP -> {
                    srcpad.pushEvent(event);
                    isEOS = false;
                    synchronized (queue) {
                        clearQueue();
                        srcResult = OK;
                        queue.notifyAll();
                    }
                    if (isBuffer) {
                        isBuffering = true;
                        postMessage(Message.newBuffering(this, true, 0));
                    }
                    postMessage(Message.newStreamStatus(srcpad, true, Pad.OK, "restart after flush"));
                    srcpad.startTask("cortado-Queue-Stream-" + Debug.genId());
                    doQueue = false;
                }
                case EOS -> {
                    isEOS = true;
                    Debug.log(Debug.INFO, "got EOS: " + this);
                    if (isBuffer) {
                        if (isBuffering) {
                            isBuffering = false;
                            postMessage(Message.newBuffering(this, isBuffering, 100));
                        }
                    }
                }
                default -> {}
            }
            if (doQueue) {
                synchronized (queue) {
                    queue.add(0, event);
                    queue.notifyAll();
                }
            }
            return true;
        }

        @Override
        protected int chainFunc(Buffer buf) {
            synchronized (queue) {
                if (srcResult != OK) {
                    buf.free();
                    return srcResult;
                }

                while (isFilled()) {
                    switch (leaky) {
                        case LEAK_UPSTREAM -> {
                            tailNeedsDiscont = true;
                            Debug.debug(parent.getName() + " is full, leaking buffer on upstream end");
                            buf.free();
                            queue.notifyAll();
                            return OK;
                        }
                        case LEAK_DOWNSTREAM -> {
                            leakDownstream();
                        }
                        case NO_LEAK -> {
                            try {
                                Debug.debug(parent.getName() + " full, waiting...");
                                queue.wait();
                                if (srcResult != OK) {
                                    buf.free();
                                    return srcResult;
                                }
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                buf.free();
                                return WRONG_STATE;
                            }
                        }
                        default -> {
                            Debug.warn("Unknown leaky type, using default");
                            try {
                                Debug.debug(parent.getName() + " full, waiting...");
                                queue.wait();
                                if (srcResult != OK) {
                                    buf.free();
                                    return srcResult;
                                }
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                buf.free();
                                return WRONG_STATE;
                            }
                        }
                    }
                }

                if (tailNeedsDiscont) {
                    buf.setFlag(Buffer.FLAG_DISCONT, true);
                    tailNeedsDiscont = false;
                }

                size += buf.length;
                updateBuffering();

                Debug.log(Debug.DEBUG, parent.getName() + " <<< " + buf);
                queue.add(0, buf);
                if (maxSize == -1) {
                    Debug.log(Debug.DEBUG, parent.getName() + " count = " + queue.size() + "/" + maxBuffers);
                } else {
                    Debug.log(Debug.DEBUG, parent.getName() + " size = " + size + "/" + maxSize);
                }
                queue.notifyAll();
            }
            return OK;
        }
    };

    public Queue() {
        super();
        addPad(srcpad);
        addPad(sinkpad);
    }

    @Override
    public String getFactoryName() {
        return "queue";
    }

    @Override
    public boolean setProperty(String name, java.lang.Object value) {
        switch (name) {
            case "maxBuffers" -> maxBuffers = Integer.parseInt(value.toString());
            case "maxSize" -> maxSize = Integer.parseInt(value.toString());
            case "isBuffer" -> isBuffer = Boolean.parseBoolean(value.toString());
            case "lowPercent" -> lowPercent = Integer.parseInt(value.toString());
            case "highPercent" -> highPercent = Integer.parseInt(value.toString());
            case "leaky" -> leaky = Integer.parseInt(value.toString());
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public java.lang.Object getProperty(String name) {
        return switch (name) {
            case "maxBuffers" -> Integer.valueOf(maxBuffers);
            case "maxSize" -> Integer.valueOf(maxSize);
            case "isBuffer" -> isBuffer ? "true" : "false";
            case "lowPercent" -> Integer.valueOf(lowPercent);
            case "highPercent" -> Integer.valueOf(highPercent);
            case "leaky" -> Integer.valueOf(leaky);
            default -> null;
        };
    }
}
