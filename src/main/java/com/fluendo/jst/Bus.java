/* Cortado - a video player java applet
 * Copyright (C) 2004 Fluendo S.L.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Street #330, Boston, MA 02111-1307, USA.
 */

package com.fluendo.jst;

import java.util.ArrayList;
import java.util.List;
import com.fluendo.utils.Debug;

public class Bus {
    private final List<Message> queue;
    private final List<BusHandler> handlers;
    private boolean flushing;
    private BusSyncHandler syncHandler;

    public Bus() {
        queue = new ArrayList<>();
        handlers = new ArrayList<>();
        flushing = false;
    }

    public synchronized void addHandler(BusHandler handler) {
        if (handler != null && !handlers.contains(handler)) {
            handlers.add(handler);
        }
    }

    public synchronized void removeHandler(BusHandler handler) {
        handlers.remove(handler);
    }

    public synchronized void setSyncHandler(BusSyncHandler handler) {
        syncHandler = handler;
    }

    private void notifyHandlers(List<BusHandler> targetHandlers, Message message) {
        Debug.debug("Bus.notifyHandlers: " + message);
        for (BusHandler handler : targetHandlers) {
            Debug.debug("Notifying " + handler);
            handler.handleMessage(message);
        }
    }

    public void post(Message message) {
        boolean post;
        BusSyncHandler handler;

        Debug.debug("Bus.post: " + message);

        synchronized (this) {
            if (flushing) {
                return;
            }
            handler = syncHandler;
        }
        
        post = (handler == null || handler.handleSyncMessage(message) == BusSyncHandler.PASS);

        synchronized (this) {
            if (post && !flushing) {
                queue.add(message);
                notifyAll();
            }
        }
    }

    public synchronized Message peek() {
        if (queue.isEmpty() || flushing) {
            return null;
        }
        return queue.get(0);
    }

    public synchronized Message pop() {
        if (queue.isEmpty() || flushing) {
            return null;
        }
        return queue.remove(0);
    }

    public synchronized Message poll(long timeout) {
        if (queue.isEmpty() && !flushing) {
            try {
                wait(timeout);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return pop();
    }

    public synchronized void setFlushing(boolean flush) {
        flushing = flush;
        queue.clear();
        notifyAll();
    }

    public void waitAndDispatch() {
        Message msg = poll(0);
        if (msg != null) {
            notifyHandlers(handlers, msg);
        }
    }
}
