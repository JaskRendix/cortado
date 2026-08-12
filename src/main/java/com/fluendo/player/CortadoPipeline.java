/* Cortado - a video player java applet
 * Copyright (C) 2005 Fluendo S.L.
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

package com.fluendo.player;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fluendo.jst.Caps;
import com.fluendo.jst.CapsListener;
import com.fluendo.jst.Clock;
import com.fluendo.jst.Element;
import com.fluendo.jst.ElementFactory;
import com.fluendo.jst.Format;
import com.fluendo.jst.Message;
import com.fluendo.jst.Pad;
import com.fluendo.jst.PadListener;
import com.fluendo.jst.Pipeline;
import com.fluendo.jst.Query;
import com.fluendo.utils.Debug;

public class CortadoPipeline extends Pipeline implements PadListener, CapsListener {
    private String url;
    private String userId;
    private String password;
    private boolean enableAudio = true;
    private boolean enableVideo = true;
    private boolean keepAspect;
    private boolean ignoreAspect;
    private int enableKate = -1;
    private String enableKateLanguage = "";
    private String enableKateCategory = "";
    private Component component;
    private int bufferSize = -1;
    private int bufferLow = -1;
    private int bufferHigh = -1;
    private URL documentBase = null;
    private Element httpsrc;
    private Element buffer;
    private Element demux;
    private Element videodec;
    private Element audiodec;
    private Element videosink;
    private Element audiosink;
    private Element v_queue, v_queue2, a_queue = null;
    private Element overlay;
    private Pad asinkpad, ovsinkpad, oksinkpad;
    private Pad apad, vpad;
    private final List<Element> katedec = new ArrayList<>();
    private final List<Element> k_queue = new ArrayList<>();
    private Element kselector = null;
    public boolean usingJavaX = false;

    public CortadoPipeline() {
        super("pipeline");
    }

    private boolean setupVideoDec(String name) {
        videodec = ElementFactory.makeByName(name, "videodec");
        if (videodec == null) {
            noSuchElement(name);
            return false;
        }
        add(videodec);
        return true;
    }

    @Override
    public void padAdded(Pad pad) {
        Caps caps = pad.getCaps();
        if (caps == null) {
            Debug.log(Debug.INFO, "pad added without caps: " + pad);
            return;
        }
        Debug.log(Debug.INFO, "pad added " + pad);
        String mime = caps.getMime();
        switch (mime) {
            case "audio/x-vorbis" -> {
                if (!enableAudio)
                    return;
                if (a_queue != null) {
                    Debug.log(Debug.INFO, "More than one audio stream detected, ignoring all except first one");
                    return;
                }
                a_queue = ElementFactory.makeByName("queue", "a_queue");
                if (a_queue == null) {
                    noSuchElement("queue");
                    return;
                }
                if (v_queue != null) {
                    v_queue.setProperty("leaky", "2");
                }
                audiodec = ElementFactory.makeByName("vorbisdec", "audiodec");
                if (audiodec == null) {
                    noSuchElement("vorbisdec");
                    return;
                }
                a_queue.setProperty("maxBuffers", "100");
                add(a_queue);
                add(audiodec);
                pad.link(a_queue.getPad("sink"));
                a_queue.getPad("src").link(audiodec.getPad("sink"));
                if (!audiodec.getPad("src").link(asinkpad)) {
                    postMessage(Message.newError(this, "audiosink already linked"));
                    return;
                }
                apad = pad;
                audiodec.setState(PAUSE);
                a_queue.setState(PAUSE);
            }
            case "video/x-theora" -> {
                if (!enableVideo)
                    return;
                v_queue = ElementFactory.makeByName("queue", "v_queue");
                v_queue2 = ElementFactory.makeByName("queue", "v_queue2");
                if (v_queue == null) {
                    noSuchElement("queue");
                    return;
                }
                if (!setupVideoDec("theoradec"))
                    return;
                if (a_queue != null) {
                    v_queue.setProperty("leaky", "2");
                }
                v_queue.setProperty("maxBuffers", "175");
                v_queue2.setProperty("maxBuffers", "1");
                add(v_queue);
                add(v_queue2);
                pad.link(v_queue.getPad("sink"));
                v_queue.getPad("src").link(videodec.getPad("sink"));
                videodec.getPad("src").link(v_queue2.getPad("sink"));
                if (!v_queue2.getPad("src").link(ovsinkpad)) {
                    postMessage(Message.newError(this, "videosink already linked"));
                    return;
                }
                vpad = pad;
                videodec.setState(PAUSE);
                v_queue.setState(PAUSE);
                v_queue2.setState(PAUSE);
            }
            case "image/jpeg" -> {
                if (!enableVideo)
                    return;
                if (!setupVideoDec("jpegdec"))
                    return;
                videodec.setProperty("component", component);
                pad.link(videodec.getPad("sink"));
                if (!videodec.getPad("src").link(ovsinkpad)) {
                    postMessage(Message.newError(this, "videosink already linked"));
                    return;
                }
                videodec.setState(PAUSE);
            }
            case "video/x-smoke" -> {
                if (!enableVideo)
                    return;
                if (!setupVideoDec("smokedec"))
                    return;
                videodec.setProperty("component", component);
                pad.link(videodec.getPad("sink"));
                if (!videodec.getPad("src").link(ovsinkpad)) {
                    postMessage(Message.newError(this, "videosink already linked"));
                    return;
                }
                vpad = pad;
                videodec.setState(PAUSE);
            }
            case "application/x-kate" -> {
                if (!enableVideo)
                    return;
                int kateIndex = katedec.size();
                Debug.debug("Found Kate stream, setting up pipeline branch");
                Element tmpKQueue = ElementFactory.makeByName("queue", "k_queue" + kateIndex);
                if (tmpKQueue == null) {
                    noSuchElement("queue");
                    return;
                }
                Element tmpKatedec = ElementFactory.makeByName("katedec", "katedec" + kateIndex);
                if (tmpKatedec == null) {
                    noSuchElement("katedec");
                    return;
                }
                if (kselector == null) {
                    Debug.debug("No Kate selector yet, creating one");
                    if (videodec != null) {
                        ovsinkpad.unlink();
                        videodec.getPad("src").unlink();
                        overlay = ElementFactory.makeByName("kateoverlay", "overlay");
                        if (overlay == null) {
                            noSuchElement("overlay");
                            return;
                        }
                        ovsinkpad = overlay.getPad("videosink");
                        oksinkpad = overlay.getPad("katesink");
                        if (!videodec.getPad("src").link(ovsinkpad)) {
                            postMessage(Message.newError(this, "Failed linking video decoder to overlay"));
                            return;
                        }
                        add(overlay);
                        overlay.setProperty("component", component);
                        overlay.getPad("videosrc").link(videosink.getPad("sink"));
                    } else {
                        Element fakesink = ElementFactory.makeByName("fakesink", "fakesink");
                        if (fakesink == null) {
                            noSuchElement("fakesink");
                            return;
                        }
                        oksinkpad = fakesink.getPad("sink");
                        add(fakesink);
                        fakesink.setState(PAUSE);
                    }
                    kselector = ElementFactory.makeByName("selector", "selector");
                    if (kselector == null) {
                        noSuchElement("selector");
                        return;
                    }
                    add(kselector);
                    if (!kselector.getPad("src").link(oksinkpad)) {
                        postMessage(Message.newError(this, "Failed linking Kate selector to overlay"));
                        return;
                    }
                    kselector.setState(PAUSE);
                }
                add(tmpKQueue);
                add(tmpKatedec);
                if (!pad.link(tmpKQueue.getPad("sink"))) {
                    postMessage(Message.newError(this, "Failed to link new Kate stream to queue"));
                    return;
                }
                if (!tmpKQueue.getPad("src").link(tmpKatedec.getPad("sink"))) {
                    postMessage(Message.newError(this, "Failed to link new Kate queue to decoder"));
                    return;
                }
                Pad newSelectorPad = kselector.requestSinkPad(tmpKatedec.getPad("src"));
                if (!tmpKatedec.getPad("src").link(newSelectorPad)) {
                    postMessage(Message.newError(this, "kate sink already linked"));
                    return;
                }
                tmpKatedec.setState(PAUSE);
                tmpKQueue.setState(PAUSE);
                katedec.add(tmpKatedec);
                k_queue.add(tmpKQueue);
                if (enableKate == katedec.size() - 1) {
                    doEnableKateIndex(enableKate);
                } else if (enableKate < 0 && (!enableKateLanguage.isEmpty() || !enableKateCategory.isEmpty())) {
                    String language = caps.getFieldString("language", "");
                    String category = caps.getFieldString("category", "");
                    boolean matchingLanguage = enableKateLanguage.isEmpty() || enableKateLanguage.equals(language);
                    boolean matchingCategory = enableKateCategory.isEmpty() || enableKateCategory.equals(category);
                    if (matchingLanguage && matchingCategory) {
                        doEnableKateIndex(katedec.size() - 1);
                    }
                }
            }
            default -> postMessage(Message.newError(this, "unknown type: " + mime));
        }
    }

    @Override
    public void padRemoved(Pad pad) {
        pad.unlink();
        if (pad == vpad) {
            Debug.log(Debug.INFO, "video pad removed " + pad);
            if (ovsinkpad != null)
                ovsinkpad.unlink();
            vpad = null;
        } else if (pad == apad) {
            Debug.log(Debug.INFO, "audio pad removed " + pad);
            if (asinkpad != null)
                asinkpad.unlink();
            apad = null;
        }
    }

    public void noMorePads() {
        boolean changed = false;
        Debug.log(Debug.INFO, "all streams detected");
        if (apad == null && enableAudio && audiosink != null) {
            Debug.log(Debug.INFO, "file has no audio, remove audiosink");
            audiosink.setState(STOP);
            remove(audiosink);
            audiosink = null;
            changed = true;
            if (videosink != null) {
                videosink.setProperty("max-lateness", String.valueOf(Long.MAX_VALUE));
            }
        }
        if (vpad == null && enableVideo && videosink != null) {
            Debug.log(Debug.INFO, "file has no video, remove videosink");
            videosink.setState(STOP);
            if (overlay != null) {
                overlay.setState(STOP);
            }
            remove(videosink);
            remove(overlay);
            videosink = null;
            overlay = null;
            changed = true;
        }
        if (changed) {
            scheduleReCalcState();
        }
    }

    public void setUrl(String anUrl) {
        url = anUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUserId(String aUserId) {
        userId = aUserId;
    }

    public void setKeepAspect(boolean keep) {
        keepAspect = keep;
    }

    public void setIgnoreAspect(boolean ignore) {
        ignoreAspect = ignore;
    }

    public void setPassword(String aPassword) {
        password = aPassword;
    }

    public void enableAudio(boolean b) {
        enableAudio = b;
    }

    public boolean isAudioEnabled() {
        return enableAudio;
    }

    public void enableVideo(boolean b) {
        enableVideo = b;
    }

    public boolean isVideoEnabled() {
        return enableVideo;
    }

    private int findKateStream(String language, String category) {
        if (!language.isEmpty() || !category.isEmpty()) {
            for (int n = 0; n < katedec.size(); ++n) {
                Element e = katedec.get(n);
                if (e != null) {
                    String eLanguage = String.valueOf(e.getProperty("language"));
                    String eCategory = String.valueOf(e.getProperty("category"));
                    if (language.equalsIgnoreCase(eLanguage) && (category.isEmpty() || category.equals(eCategory))) {
                        return n;
                    }
                }
            }
        }
        return -1;
    }

    public void enableKateStream(int idx, String language, String category) {
        int targetIdx = idx;
        if (targetIdx < 0) {
            enableKateLanguage = language;
            enableKateCategory = category;
            targetIdx = findKateStream(language, category);
        }
        if (targetIdx == enableKate)
            return;
        doEnableKateIndex(targetIdx);
    }

    private void doEnableKateIndex(int idx) {
        if (kselector != null) {
            Debug.info("Switching Kate streams from " + enableKate + " to " + idx);
            kselector.setProperty("selected", idx);
        } else {
            Debug.warning("Switching Kate stream request, but no Kate selector exists");
        }
        enableKate = idx;
    }

    public int getEnabledKateIndex() {
        return enableKate;
    }

    public void setComponent(Component c) {
        component = c;
    }

    public Component getComponent() {
        return component;
    }

    public void setDocumentBase(URL base) {
        documentBase = base;
    }

    public URL getDocumentBase() {
        return documentBase;
    }

    public void setBufferSize(int size) {
        bufferSize = size;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferLow(int size) {
        bufferLow = size;
    }

    public int getBufferLow() {
        return bufferLow;
    }

    public void setBufferHigh(int size) {
        bufferHigh = size;
    }

    public int getBufferHigh() {
        return bufferHigh;
    }

    public void resize(Dimension d) {
        if (videosink == null || d == null)
            return;
        videosink.setProperty("bounds", new Rectangle(d));
    }

    public boolean buildOgg() {
        demux = ElementFactory.makeByName("oggdemux", "demux");
        if (demux == null) {
            noSuchElement("oggdemux");
            return false;
        }
        buffer = ElementFactory.makeByName("queue", "buffer");
        if (buffer == null) {
            demux = null;
            noSuchElement("queue");
            return false;
        }
        buffer.setProperty("isBuffer", true);
        if (bufferSize != -1)
            buffer.setProperty("maxSize", bufferSize * 1024);
        if (bufferLow != -1)
            buffer.setProperty("lowPercent", bufferLow);
        if (bufferHigh != -1)
            buffer.setProperty("highPercent", bufferHigh);
        add(demux);
        add(buffer);
        httpsrc.getPad("src").link(buffer.getPad("sink"));
        buffer.getPad("src").link(demux.getPad("sink"));
        demux.addPadListener(this);
        buffer.setState(PAUSE);
        demux.setState(PAUSE);
        return true;
    }

    public boolean buildMultipart() {
        demux = ElementFactory.makeByName("multipartdemux", "demux");
        if (demux == null) {
            noSuchElement("multipartdemux");
            return false;
        }
        add(demux);
        httpsrc.getPad("src").link(demux.getPad("sink"));
        demux.addPadListener(this);
        return true;
    }

    @Override
    public void capsChanged(Caps caps) {
        String mime = caps.getMime();
        switch (mime) {
            case "application/ogg" -> buildOgg();
            case "multipart/x-mixed-replace" -> buildMultipart();
            default -> postMessage(Message.newError(this, "unknown type: " + mime));
        }
    }

    private void noSuchElement(String elemName) {
        postMessage(Message.newError(this, "no such element: " + elemName + " (check plugins.ini)"));
    }

    private boolean build() {
        String vendor = System.getProperty("java.vendor");
        httpsrc = ElementFactory.makeByName("httpsrc", "httpsrc");
        if (httpsrc == null) {
            noSuchElement("httpsrc");
            return false;
        }
        httpsrc.setProperty("url", url);
        httpsrc.setProperty("userId", userId);
        httpsrc.setProperty("password", password);
        
        String vendorPrefix = (vendor != null && vendor.contains(" ")) ? vendor.substring(0, vendor.indexOf(" ")) : "Java";
        String userAgent = "Cortado/1.2.0 " + vendorPrefix + "/" + System.getProperty("java.version");
        
        String extra = "(" + System.getProperty("os.name") + " " + System.getProperty("os.version") + ")";
        try {
            String agent = System.getProperty("http.agent");
            if (agent != null)
                extra = agent;
        } catch (Exception ignored) {
        }
        userAgent += " " + extra;
        Debug.log(Debug.INFO, "setting User-Agent " + userAgent);
        httpsrc.setProperty("userAgent", userAgent);
        httpsrc.setProperty("documentBase", documentBase);
        add(httpsrc);
        httpsrc.getPad("src").addCapsListener(this);
        if (enableAudio) {
            audiosink = newAudioSink();
            if (audiosink == null) {
                enableAudio = false;
            } else {
                asinkpad = audiosink.getPad("sink");
                add(audiosink);
            }
        }
        if (enableVideo) {
            videosink = ElementFactory.makeByName("videosink", "videosink");
            if (videosink == null) {
                noSuchElement("videosink");
                return false;
            }
            videosink.setProperty("keep-aspect", keepAspect ? "true" : "false");
            videosink.setProperty("ignore-aspect", ignoreAspect ? "true" : "false");
            videosink.setProperty("component", component);
            if (component != null) {
                resize(component.getSize());
            }
            videosink.setProperty("max-lateness", String.valueOf(enableAudio ? Clock.MSECOND * 20 : Long.MAX_VALUE));
            add(videosink);
            ovsinkpad = videosink.getPad("sink");
        }
        if (audiosink == null && videosink == null) {
            postMessage(Message.newError(this, "Both audio and video are disabled, can't play anything"));
            return false;
        }
        return true;
    }

    protected Element newAudioSink() {
        com.fluendo.plugin.AudioSink s;
        try {
            Class.forName("javax.sound.sampled.AudioSystem");
            Class.forName("javax.sound.sampled.DataLine");
            usingJavaX = true;
            s = (com.fluendo.plugin.AudioSink) ElementFactory.makeByName("audiosinkj2", "audiosink");
            Debug.log(Debug.INFO, "using high quality javax.sound backend");
        } catch (Throwable e) {
            s = null;
            Debug.log(Debug.INFO, "No audio backend available");
        }
        if (s == null) {
            Debug.warn("Failed to create an audio sink, continuing anyway");
            return null;
        }
        return s.test() ? s : null;
    }

    private boolean cleanup() {
        Debug.log(Debug.INFO, "cleanup");
        if (httpsrc != null) {
            remove(httpsrc);
            httpsrc = null;
        }
        if (audiosink != null) {
            remove(audiosink);
            audiosink = null;
            asinkpad = null;
        }
        if (videosink != null) {
            remove(videosink);
            videosink = null;
        }
        if (overlay != null) {
            remove(overlay);
            overlay = null;
            ovsinkpad = null;
            oksinkpad = null;
        }
        if (buffer != null) {
            remove(buffer);
            buffer = null;
        }
        if (demux != null) {
            demux.removePadListener(this);
            remove(demux);
            demux = null;
        }
        if (v_queue != null) {
            remove(v_queue);
            v_queue = null;
        }
        if (v_queue2 != null) {
            remove(v_queue2);
            v_queue2 = null;
        }
        if (a_queue != null) {
            remove(a_queue);
            a_queue = null;
        }
        if (videodec != null) {
            remove(videodec);
            videodec = null;
        }
        if (audiodec != null) {
            remove(audiodec);
            audiodec = null;
        }
        for (Element queue : k_queue) {
            if (queue != null)
                remove(queue);
        }
        for (Element dec : katedec) {
            if (dec != null)
                remove(dec);
        }
        k_queue.clear();
        katedec.clear();
        if (kselector != null) {
            remove(kselector);
            kselector = null;
        }
        return true;
    }

    @Override
    protected int changeState(int transition) {
        if (transition == STOP_PAUSE) {
            if (!build())
                return FAILURE;
        }
        int res = super.changeState(transition);
        if (transition == PAUSE_STOP) {
            cleanup();
        }
        return res;
    }

    @Override
    protected boolean doSendEvent(com.fluendo.jst.Event event) {
        if (event.getType() != com.fluendo.jst.Event.Type.SEEK)
            return false;
        if (event.parseSeekFormat() != Format.PERCENT)
            return false;
        if (httpsrc == null)
            return false;
        boolean res = httpsrc.getPad("src").sendEvent(event);
        getState(null, null, -1);
        return res;
    }

    protected long getPosition() {
        Query q = Query.newPosition(Format.TIME);
        if (super.query(q)) {
            return q.parsePositionValue();
        }
        return 0;
    }

    protected int getNumKateStreams() {
        return katedec.size();
    }

    protected String getKateStreamCategory(int idx) {
        if (idx < 0 || idx >= katedec.size())
            return "";
        return String.valueOf(katedec.get(idx).getProperty("category"));
    }

    protected String getKateStreamLanguage(int idx) {
        if (idx < 0 || idx >= katedec.size())
            return "";
        return String.valueOf(katedec.get(idx).getProperty("language"));
    }
}
