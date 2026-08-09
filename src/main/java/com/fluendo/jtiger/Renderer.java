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
import java.util.ArrayList;
import java.util.List;

public class Renderer {
    private final List<Item> items = new ArrayList<>();
    private boolean dirty = true;

    /**
     * Add a new event to the renderer.
     */
    public synchronized void add(com.fluendo.jkate.Event ev) {
        if (ev != null) {
            items.add(new Item(ev));
            dirty = true;
        }
    }

    /**
     * Update the renderer, and all the events it tracks.
     * Returns 1 if there is nothing to draw, as an optimization.
     */
    public synchronized int update(Component c, Dimension d, double t) {
        int nactive = 0;
        for (int n = 0; n < items.size(); ++n) {
            Item item = items.get(n);
            boolean ret = item.update(c, d, t);
            if (!ret) {
                items.remove(n);
                dirty = true;
                --n;
            } else {
                if (item.isDirty()) {
                    dirty = true;
                }
                if (item.isActive()) {
                    ++nactive;
                }
            }
        }
        if (nactive == 0) {
            return 1;
        }
        return 0;
    }

    /**
     * Renders onto the given image.
     */
    public synchronized Image render(Component c, Image img) {
        if (c == null || img == null) {
            return img;
        }

        int width = img.getWidth(null);
        int height = img.getHeight(null);
        if (width <= 0 || height <= 0) {
            return img;
        }

        Image copy = c.createImage(width, height);
        if (copy != null) {
            Graphics g = copy.getGraphics();
            if (g != null) {
                g.drawImage(img, 0, 0, null);
                g.dispose();
            }
            img = copy;
        }

        for (int n = 0; n < items.size(); ++n) {
            items.get(n).render(c, img);
        }

        dirty = false;
        return img;
    }

    /**
     * Flushes all events.
     */
    public synchronized void flush() {
        items.clear();
        dirty = true;
    }

    public synchronized boolean isDirty() {
        return dirty;
    }
}
