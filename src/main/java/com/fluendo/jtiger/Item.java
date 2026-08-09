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

import java.awt.*;
import java.nio.charset.StandardCharsets;
import com.fluendo.jkate.Tracker;
import com.fluendo.utils.*;

public class Item {
    private Tracker kin = null;
    private boolean active = false;
    private Font font = null;
    private int font_size = 0;
    private String text = null;
    private TigerBitmap background_image = null;

    private int width = -1;
    private int height = -1;

    private final Rectangle region = new Rectangle();

    private boolean dirty = true;

    private static final TextRenderer textRenderer = detectTextRenderer();

    private static TextRenderer detectTextRenderer() {
        TextRenderer tr = null;
        try {
            Class<?> c = Class.forName("com.fluendo.jtiger.BasicTextRenderer");
            tr = (TextRenderer) c.getDeclaredConstructor().newInstance();
            Debug.info("jtiger.Item: detecting Graphics2D");
            Class.forName("java.awt.Graphics2D");
            Debug.info("jtiger.Item: detecting TextLayout");
            Class.forName("java.awt.font.TextLayout");
            Debug.info("jtiger.Item: detecting AttributedString");
            Class.forName("java.text.AttributedString");
            c = Class.forName("com.fluendo.jtiger.FancyTextRenderer");
            tr = (TextRenderer) c.getDeclaredConstructor().newInstance();
            Debug.info("jtiger.Item: We can use the fancy text renderer");
        } catch (Throwable e) {
            if (tr == null) {
                Debug.info("jtiger.Item: We cannot use any text renderer: " + e);
            } else {
                Debug.info("jtiger.Item: We have to use the basic text renderer: " + e);
            }
        }
        return tr;
    }

    /**
     * Create a new item from a Kate event.
     */
    public Item(com.fluendo.jkate.Event ev) {
        if (ev != null) {
            this.kin = new Tracker(ev);
            if (ev.text != null && ev.text.length > 0) {
                text = new String(ev.text, StandardCharsets.UTF_8);
            }
        }
        dirty = false; /* not dirty yet, inactive */
    }

    /**
     * Create a font suitable for displaying on the given component
     */
    protected void createFont(Component c, Image img) {
        if (img != null) {
            font_size = img.getWidth(null) / 32;
        }
        if (font_size < 12) {
            font_size = 12;
        }
        font = new Font("sansserif", Font.BOLD, font_size);
    }

    /**
     * Regenerate any cached data to match any relevant changes in the
     * given component
     */
    protected void updateCachedData(Component c, Image img) {
        if (img == null) return;
        int img_width = img.getWidth(null);
        int img_height = img.getHeight(null);

        if (img_width == width && img_height == height)
            return;

        createFont(c, img);

        width = img_width;
        height = img_height;

        dirty = true;
    }

    /**
     * Updates the item at the given time.
     * returns false if the item should be destroyed, true otherwise.
     */
    public boolean update(Component c, Dimension d, double t) {
        if (kin == null) return false;
        com.fluendo.jkate.Event ev = kin.ev;
        if (ev == null) return false;

        /* early out if we're not within the lifetime of the event */
        if (t < ev.start_time) return true;
        if (t >= ev.end_time) {
            active = false;
            dirty = true;
            return false; /* we're done, and will get destroyed */
        }

        if (!active) {
            active = true;
            dirty = true;
        }

        return kin.update(t - ev.start_time, d, d);
    }

    /**
     * Set up the region.
     */
    public void setupRegion(Component c, Image img) {
        if (kin != null && kin.has[Tracker.has_region]) {
            region.x = (int) (kin.region_x + 0.5f);
            region.y = (int) (kin.region_y + 0.5f);
            region.width = (int) (kin.region_w + 0.5f);
            region.height = (int) (kin.region_h + 0.5f);
        } else if (img != null) {
            int w = img.getWidth(null);
            int h = img.getHeight(null);
            region.x = (int) (w * 0.1f + 0.5f);
            region.y = (int) (h * 0.8f + 0.5f);
            region.width = (int) (w * 0.8f + 0.5f);
            region.height = (int) (h * 0.1f + 0.5f);
        }
    }

    /**
     * Renders the item on the given image.
     */
    public void render(Component c, Image img) {
        if (!active || img == null)
            return;

        updateCachedData(c, img);
        setupRegion(c, img);
        renderBackground(c, img);
        renderText(img);

        dirty = false;
    }

    /**
     * Render a background for the item, if appropriate.
     * The background may be a color, or an image.
     */
    public void renderBackground(Component c, Image img) {
        if (kin != null && kin.ev != null && kin.ev.bitmap != null && img != null) {
            if (background_image == null) {
                background_image = new TigerBitmap(c, kin.ev.bitmap, kin.ev.palette);
            }
            
            Graphics g = img.getGraphics();
            if (g != null) {
                g.drawImage(background_image.getScaled(region.width, region.height), region.x, region.y, null);
                g.dispose();
            }
        }
    }

    /**
     * Render text for the item, if appropriate.
     */
    public void renderText(Image img) {
        if (text == null || img == null || textRenderer == null)
            return;

        Graphics g = img.getGraphics();
        if (g != null) {
            textRenderer.renderText(g, region, font, text);
            g.dispose();
        }
    }

    public boolean isDirty() {
        return dirty;
    }
    public boolean isActive() {
        return active;
    }
}
