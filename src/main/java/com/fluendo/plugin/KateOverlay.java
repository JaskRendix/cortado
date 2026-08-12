/* Copyright (C) <2008> ogg.k.ogg.k <ogg.k.ogg.k@googlemail.com>
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

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import com.fluendo.jst.*;
import com.fluendo.jtiger.Renderer;
import com.fluendo.utils.*;

/* This element renders a Kate stream on incoming video */
public class KateOverlay extends Overlay {
    private Font font = null;
    private String text = null;
    private final Renderer tr = new Renderer();
    private Dimension image_dimension = null;

    /* This class allows lazy rendering, which may not even happen
       if the buffer is late, saving cycles, and ensuring buffers are
       not delayed on their way to the sink */
    private class OverlayProducer implements ImageProducer, ImageConsumer {
        private final List<ImageConsumer> consumers;

        private final Component component;
        private final Renderer tr;
        private final Buffer buf;
        private final java.lang.Object object;

        OverlayProducer(Component c, Renderer tr, Buffer b) {
            consumers = new ArrayList<>();
            component = c;
            this.tr = tr;
            this.buf = b;
            object = buf.object;
        }

        @Override
        public void addConsumer(ImageConsumer ic) {
            if (!isConsumer(ic)) {
                consumers.add(ic);
            }
        }

        @Override
        public boolean isConsumer(ImageConsumer ic) {
            return consumers.contains(ic);
        }

        @Override
        public void removeConsumer(ImageConsumer ic) {
            consumers.remove(ic);
            ImageProducer ip = (ImageProducer) object;
            for (int n = 0; n < consumers.size(); ++n) {
                ip.removeConsumer(ic);
            }
        }

        @Override
        public void requestTopDownLeftRightResend(ImageConsumer ic) {
        }

        @Override
        public void startProduction(ImageConsumer ic) {
            Image img = null;

            addConsumer(ic);

            if (image_dimension == null) {
                img = getImage(object);
                if (img == null) {
                    sendError();
                    return;
                }
                image_dimension = new Dimension(img.getWidth(null), img.getHeight(null));
            }

            /* before rendering, we update the state of the events; for now this
               just weeds out old ones, but at some point motions could be tracked. */
            int ret = tr.update(component, image_dimension, buf.timestamp / (double) Clock.SECOND);

            if (ret < 0) {
                Debug.log(Debug.WARNING, "Failed to update jtiger renderer");
                sendOriginalImage();
                return;
            }

            /* if the renderer is empty and the buffer is not a duplicate, we leave the
               video alone */
            if (!buf.duplicate && ret > 0) {
                Debug.log(Debug.DEBUG, "Video frame is not a dupe and we have nothing to overlay.");
                sendOriginalImage();
                return;
            }

            /* if the renderer isn't dirty and the image hasn't changed, we don't need
               to do anything, as the result image would be the same */
            if (buf.duplicate && !tr.isDirty()) {
                Debug.log(Debug.DEBUG, "Video frame is a dupe and we're not dirty. Yeah.");
                sendOriginalImage();
                return;
            }

            /* render Kate stream on top */
            if (img == null) {
                img = getImage(object);
            }
            img = tr.render(component, img);

            /* We need to draw a new overlay, so we need to get the buffer to update,
               as it might have a previous overlay on top of it */
            buf.duplicate = false;

            sendImage(img);
        }

        private Image getImage(java.lang.Object object) {
            Image img;
            if (object instanceof ImageProducer imageProducer) {
                img = component.createImage(imageProducer);
            } else if (object instanceof Image image) {
                img = image;
            } else {
                System.out.println(this + ": unknown buffer received " + object);
                img = null;
            }
            return img;
        }

        /* tells the consumers there was an error producing the image */
        private void sendError() {
            Debug.log(Debug.WARNING, "Sending image error notification");
            for (ImageConsumer ic : consumers) {
                ic.imageComplete(ImageConsumer.IMAGEERROR);
            }
        }

        /* sends the original image, unmodified, to the consumers, by forwarding all
           ImageConsumer calls from the original image to our own consumers */
        private void sendOriginalImage() {
            ImageProducer ip = (ImageProducer) object;
            ip.startProduction(this);
        }

        /* sends the given image to the consumers */
        private void sendImage(Image img) {
            PixelGrabber pg = new PixelGrabber(img, 0, 0, -1, -1, false);
            try {
                if (pg.grabPixels(0)) {
                    int[] pixels = (int[]) pg.getPixels();
                    if (pixels == null) {
                        Debug.log(Debug.WARNING, "pixels are null!");
                        sendError();
                    } else {
                        for (ImageConsumer ic : consumers) {
                            ic.setHints(ImageConsumer.TOPDOWNLEFTRIGHT |
                                    ImageConsumer.COMPLETESCANLINES |
                                    ImageConsumer.SINGLEFRAME |
                                    ImageConsumer.SINGLEPASS);
                            ic.setDimensions(image_dimension.width, image_dimension.height);
                            ic.setPixels(0, 0, image_dimension.width, image_dimension.height, pg.getColorModel(), pixels, 0, image_dimension.width);
                            ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
                        }
                    }
                } else {
                    Debug.log(Debug.WARNING, "Failed to grab pixels");
                    sendError();
                }
            } catch (Exception e) {
                Debug.log(Debug.WARNING, "Failed to grab pixels: " + e);
                sendError();
            }
        }

        /* ImageConsumer interface, to redirect calls from the original image */

        @Override
        public void imageComplete(int status) {
            for (ImageConsumer ic : consumers) {
                ic.imageComplete(status);
            }
        }

        @Override
        public void setColorModel(ColorModel cm) {
            for (ImageConsumer ic : consumers) {
                ic.setColorModel(cm);
            }
        }

        @Override
        public void setDimensions(int w, int h) {
            for (ImageConsumer ic : consumers) {
                ic.setDimensions(w, h);
            }
        }

        @Override
        public void setHints(int hints) {
            for (ImageConsumer ic : consumers) {
                ic.setHints(hints);
            }
        }

        @Override
        public void setProperties(Hashtable<?, ?> props) {
            for (ImageConsumer ic : consumers) {
                ic.setProperties(props);
            }
        }

        @Override
        public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
            for (ImageConsumer ic : consumers) {
                ic.setPixels(x, y, w, h, model, pixels, off, scansize);
            }
        }

        @Override
        public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
            for (ImageConsumer ic : consumers) {
                ic.setPixels(x, y, w, h, model, pixels, off, scansize);
            }
        }
    }

    private final Pad kateSinkPad = new Pad(Pad.SINK, "katesink") {
        @Override
        protected boolean eventFunc(com.fluendo.jst.Event event) {
            /* don't propagate, the video sink is the master */
            switch (event.getType()) {
                case FLUSH_START, FLUSH_STOP, NEWSEGMENT -> onFlush();
                default -> {}
            }
            return true;
        }

        /**
         * This pad receives Kate events, and add them to the renderer.
         * They will be removed from it as they become inactive.
         */
        @Override
        protected synchronized int chainFunc(com.fluendo.jst.Buffer buf) {
            addKateEvent((com.fluendo.jkate.Event) buf.object);
            return Pad.OK;
        }
    };

    /**
     * Create a new Kate overlay
     */
    public KateOverlay() {
        super();
        addPad(kateSinkPad);
    }

    /**
     * Add a new Kate event to the renderer.
     * This needs locking so the Kate events are not changed while the
     * overlay is rendering them to an image.
     */
    protected synchronized void addKateEvent(com.fluendo.jkate.Event ev) {
        tr.add(ev);
        Debug.log(Debug.DEBUG, "Kate overlay got Kate event: " + new String(ev.text));
    }

    /**
     * Upon a flushing event, remove any existing event, now obsolete.
     * This needs locking so the Kate events are not changed while the
     * overlay is rendering them to an image.
     */
    protected synchronized void onFlush() {
        tr.flush();
        image_dimension = null;
        Debug.log(Debug.DEBUG, "Kate overlay flushing");
    }

    /**
     * Overlay the Kate renderer onto the given image.
     */
    @Override
    protected synchronized void overlay(com.fluendo.jst.Buffer buf) {
        buf.object = new OverlayProducer(component, tr, buf);
    }

    @Override
    public String getFactoryName() {
        return "kateoverlay";
    }
}
