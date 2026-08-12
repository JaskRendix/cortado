/* JTiger
 * Copyright (C) 2008 ogg.k.ogg.k <ogg.k.ogg.k@googlemail.com>
 *
 * Parts of JTiger are based on code by Wim Taymans <wim@fluendo.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.fluendo.jtiger;

import com.fluendo.jkate.Event;
import com.fluendo.jkate.Tracker;
import com.fluendo.utils.Debug;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.nio.charset.StandardCharsets;

public final class Item {

    private Tracker tracker = null;
    private boolean active = false;
    private Font font = null;
    private int fontSize = 0;
    private String text = null;
    private TigerBitmap backgroundImage = null;

    private int width = -1;
    private int height = -1;

    private final Rectangle region = new Rectangle();
    private boolean dirty = true;

    private static final TextRenderer TEXT_RENDERER = detectTextRenderer();

    private static TextRenderer detectTextRenderer() {
        TextRenderer renderer = null;
        try {
            Class<?> basicClass = Class.forName("com.fluendo.jtiger.BasicTextRenderer");
            renderer = (TextRenderer) basicClass.getDeclaredConstructor().newInstance();
            Debug.info("jtiger.Item: detecting Graphics2D");
            
            Class.forName("java.awt.Graphics2D");
            Debug.info("jtiger.Item: detecting TextLayout");
            
            Class.forName("java.awt.font.TextLayout");
            Debug.info("jtiger.Item: detecting AttributedString");
            
            Class.forName("java.text.AttributedString");
            Class<?> fancyClass = Class.forName("com.fluendo.jtiger.FancyTextRenderer");
            renderer = (TextRenderer) fancyClass.getDeclaredConstructor().newInstance();
            Debug.info("jtiger.Item: We can use the fancy text renderer");
        } catch (Throwable e) {
            if (renderer == null) {
                Debug.info("jtiger.Item: We cannot use any text renderer: " + e);
            } else {
                Debug.info("jtiger.Item: We have to use the basic text renderer: " + e);
            }
        }
        return renderer;
    }

    /**
     * Creates a new item from a Kate event.
     */
    public Item(Event event) {
        if (event != null) {
            this.tracker = new Tracker(event);
            if (event.text != null && event.text.length > 0) {
                this.text = new String(event.text, StandardCharsets.UTF_8);
            }
        }
        this.dirty = false; // not dirty yet, inactive
    }

    /**
     * Creates a font suitable for displaying on the given component/image.
     */
    protected void createFont(Component component, Image image) {
        if (image != null) {
            fontSize = image.getWidth(null) / 32;
        }
        if (fontSize < 12) {
            fontSize = 12;
        }
        font = new Font(Font.SANS_SERIF, Font.BOLD, fontSize);
    }

    /**
     * Regenerates any cached data to match any relevant changes in the given image.
     */
    protected void updateCachedData(Component component, Image image) {
        if (image == null) {
            return;
        }
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);

        if (imageWidth == width && imageHeight == height) {
            return;
        }

        createFont(component, image);

        width = imageWidth;
        height = imageHeight;
        dirty = true;
    }

    /**
     * Updates the item at the given time.
     * 
     * @return false if the item should be destroyed, true otherwise.
     */
    public boolean update(Component component, Dimension dimension, double time) {
        if (tracker == null) {
            return false;
        }
        Event event = tracker.ev;
        if (event == null) {
            return false;
        }

        // Early out if we're not within the lifetime of the event
        if (time < event.start_time) {
            return true;
        }
        if (time >= event.end_time) {
            active = false;
            dirty = true;
            return false; // we're done, and will get destroyed
        }

        if (!active) {
            active = true;
            dirty = true;
        }

        return tracker.update(time - event.start_time, dimension, dimension);
    }

    /**
     * Sets up the rendering region.
     */
    public void setupRegion(Component component, Image image) {
        if (tracker != null && tracker.has[Tracker.HAS_REGION]) {
            region.x = (int) (tracker.regionX + 0.5f);
            region.y = (int) (tracker.regionY + 0.5f);
            region.width = (int) (tracker.regionW + 0.5f);
            region.height = (int) (tracker.regionH + 0.5f);
        } else if (image != null) {
            int imgWidth = image.getWidth(null);
            int imgHeight = image.getHeight(null);
            region.x = (int) (imgWidth * 0.1f + 0.5f);
            region.y = (int) (imgHeight * 0.8f + 0.5f);
            region.width = (int) (imgWidth * 0.8f + 0.5f);
            region.height = (int) (imgHeight * 0.1f + 0.5f);
        }
    }

    /**
     * Renders the item on the given image.
     */
    public void render(Component component, Image image) {
        if (!active || image == null) {
            return;
        }

        updateCachedData(component, image);
        setupRegion(component, image);
        renderBackground(component, image);
        renderText(image);

        dirty = false;
    }

    /**
     * Renders a background for the item, if appropriate.
     */
    public void renderBackground(Component component, Image image) {
        if (tracker != null && tracker.ev != null && tracker.ev.bitmap != null && image != null) {
            if (backgroundImage == null) {
                backgroundImage = new TigerBitmap(component, tracker.ev.bitmap, tracker.ev.palette);
            }

            Graphics graphics = image.getGraphics();
            if (graphics != null) {
                graphics.drawImage(
                    backgroundImage.getScaled(region.width, region.height), 
                    region.x, 
                    region.y, 
                    null
                );
                graphics.dispose();
            }
        }
    }

    /**
     * Renders text for the item, if appropriate.
     */
    public void renderText(Image image) {
        if (text == null || image == null || TEXT_RENDERER == null) {
            return;
        }

        Graphics graphics = image.getGraphics();
        if (graphics != null) {
            TEXT_RENDERER.renderText(graphics, region, font, text);
            graphics.dispose();
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isActive() {
        return active;
    }
}
