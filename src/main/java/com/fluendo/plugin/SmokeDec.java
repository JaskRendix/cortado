/* Smoke Codec
 * Copyright (C) <2004> Wim Taymans <wim@fluendo.com>
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

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import com.fluendo.codecs.SmokeCodec;
import com.fluendo.jst.Element;
import com.fluendo.jst.Pad;
import com.fluendo.jst.Caps;

public class SmokeDec extends Element {
    private static final Logger LOGGER = Logger.getLogger(SmokeDec.class.getName());

    private Component component;
    private SmokeCodec smoke;
    private int width;
    private int height;

    private final Pad srcPad = new Pad(Pad.SRC, "src") {
        @Override
        protected boolean eventFunc(com.fluendo.jst.Event event) {
            return sinkPad.pushEvent(event);
        }
    };

    private final Pad sinkPad = new Pad(Pad.SINK, "sink") {
        @Override
        protected boolean eventFunc(com.fluendo.jst.Event event) {
            boolean result;

            switch (event.getType()) {
                case FLUSH_START:
                    result = srcPad.pushEvent(event);
                    synchronized (streamLock) {
                        LOGGER.info("synced " + this);
                    }
                    break;
                case FLUSH_STOP:
                case EOS:
                case NEWSEGMENT:
                default:
                    result = srcPad.pushEvent(event);
                    break;
            }
            return result;
        }

        @Override
        protected int chainFunc(com.fluendo.jst.Buffer buf) {
            int ret;

            BufferedImage img = smoke.decode(buf.data, buf.offset, buf.length);

            if (img != null) {
                int imgW = img.getWidth();
                int imgH = img.getHeight();

                if (imgW != width || imgH != height) {
                    width = imgW;
                    height = imgH;

                    LOGGER.info("smoke frame: " + width + "," + height);

                    caps = new Caps("video/raw");
                    caps.setFieldInt("width", width);
                    caps.setFieldInt("height", height);
                    caps.setFieldInt("aspect_x", 1);
                    caps.setFieldInt("aspect_y", 1);
                }
                buf.object = img;
                buf.caps = caps;

                ret = srcPad.push(buf);
            } else {
                if ((smoke.getFlags() & SmokeCodec.KEYFRAME) != 0) {
                    LOGGER.warning("could not decode jpeg image");
                }
                buf.free();
                ret = OK;
            }
            return ret;
        }
    };

    public SmokeDec() {
        super();
        addPad(srcPad);
        addPad(sinkPad);
        this.smoke = new SmokeCodec();
    }

    @Override
    public boolean setProperty(String name, java.lang.Object value) {
        if (name.equals("component")) {
            component = (Component) value;
        } else {
            return false;
        }
        return true;
    }

    @Override
    public java.lang.Object getProperty(String name) {
        if (name.equals("component")) {
            return component;
        }
        return null;
    }

    @Override
    public String getFactoryName() {
        return "smokedec";
    }

    @Override
    public String getMime() {
        return "video/x-smoke";
    }

    @Override
    public int typeFind(byte[] data, int offset, int length) {
        if (data != null && length - offset > 1 && data[offset + 1] == 0x73) {
            return 10;
        }
        return -1;
    }
}
