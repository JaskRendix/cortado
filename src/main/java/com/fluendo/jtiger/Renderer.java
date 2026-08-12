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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

public final class Renderer {

    private final List<Item> items = new ArrayList<>();
    private boolean dirty = true;

    /**
     * Adds a new event to the renderer.
     */
    public synchronized void add(Event event) {
        if (event != null) {
            items.add(new Item(event));
            dirty = true;
        }
    }

    /**
     * Updates the renderer and all the events it tracks.
     * 
     * @return 1 if there is nothing active/to draw (as an optimization), 0 otherwise.
     */
    public synchronized int update(Component component, Dimension dimension, double time) {
        int activeCount = 0;
        
        // Using an explicit iterator removal loop or safely iterating backwards/removing
        // to handle collection mutation cleanly in modern Java.
        var iterator = items.listIterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            boolean isStillValid = item.update(component, dimension, time);
            
            if (!isStillValid) {
                iterator.remove();
                dirty = true;
            } else {
                if (item.isDirty()) {
                    dirty = true;
                }
                if (item.isActive()) {
                    activeCount++;
                }
            }
        }

        return activeCount == 0 ? 1 : 0;
    }

    /**
     * Renders all active items onto the given image.
     */
    public synchronized Image render(Component component, Image image) {
        if (component == null || image == null) {
            return image;
        }

        int width = image.getWidth(null);
        int height = image.getHeight(null);
        if (width <= 0 || height <= 0) {
            return image;
        }

        Image copy = component.createImage(width, height);
        if (copy != null) {
            Graphics graphics = copy.getGraphics();
            if (graphics != null) {
                graphics.drawImage(image, 0, 0, null);
                graphics.dispose();
            }
            image = copy;
        }

        for (Item item : items) {
            item.render(component, image);
        }

        dirty = false;
        return image;
    }

    /**
     * Flushes and clears all tracked items.
     */
    public synchronized void flush() {
        items.clear();
        dirty = true;
    }

    /**
     * Checks if the renderer state is dirty and requires a redraw.
     */
    public synchronized boolean isDirty() {
        return dirty;
    }
}
