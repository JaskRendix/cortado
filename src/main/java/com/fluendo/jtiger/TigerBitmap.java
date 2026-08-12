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

import com.fluendo.jkate.Bitmap;
import com.fluendo.jkate.Palette;
import com.fluendo.utils.Debug;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.util.Objects;

public final class TigerBitmap {

    private final Image image;
    private Image scaledImage;

    /**
     * Creates a new TigerBitmap from a Kate bitmap and optional palette.
     */
    public TigerBitmap(Component component, Bitmap bitmap, Palette palette) {
        Objects.requireNonNull(component, "Component cannot be null");

        Image loadedImage = null;
        if (bitmap != null) {
            if (bitmap.bpp == 0) {
                loadedImage = createPngBitmap(component, bitmap, palette);
            } else if (palette != null && palette.colors != null) {
                loadedImage = createPalettedBitmap(component, bitmap, palette);
            }
        }

        if (loadedImage == null) {
            // Fallback to a 1x1 transparent image to avoid corner cases
            loadedImage = component.getToolkit().createImage(
                new MemoryImageSource(1, 1, new int[]{0}, 0, 1)
            );
        }
        this.image = loadedImage;
    }

    /**
     * Returns a scaled version of the image, recalculating only when dimensions change.
     */
    public Image getScaled(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be greater than zero");
        }

        int currentWidth = scaledImage != null ? scaledImage.getWidth(null) : -1;
        int currentHeight = scaledImage != null ? scaledImage.getHeight(null) : -1;

        if (scaledImage == null || width != currentWidth || height != currentHeight) {
            scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
        return scaledImage;
    }

    /**
     * Creates an image from bytes representing a PNG image.
     */
    private Image createPngBitmap(Component component, Bitmap bitmap, Palette palette) {
        Debug.warning("PNG bitmaps not supported yet");
        return null;
    }

    /**
     * Creates a paletted image based on the provided bitmap and color palette.
     */
    private Image createPalettedBitmap(Component component, Bitmap bitmap, Palette palette) {
        if (palette.colors == null || bitmap.pixels == null) {
            return null;
        }

        byte[] colorMap = new byte[4 * palette.colors.length];
        for (int i = 0; i < palette.colors.length; ++i) {
            if (palette.colors[i] != null) {
                colorMap[i * 4]     = palette.colors[i].r;
                colorMap[i * 4 + 1] = palette.colors[i].g;
                colorMap[i * 4 + 2] = palette.colors[i].b;
                colorMap[i * 4 + 3] = palette.colors[i].a;
            }
        }

        IndexColorModel colorModel = new IndexColorModel(bitmap.bpp, palette.colors.length, colorMap, 0, true);
        return component.createImage(new MemoryImageSource(
            bitmap.width, bitmap.height, colorModel, bitmap.pixels, 0, bitmap.width
        ));
    }
}
