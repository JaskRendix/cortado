/* JKate
 * Copyright (C) 2008 ogg.k.ogg.k <ogg.k.ogg.k@googlemail.com>
 *
 * Parts of JKate are based on code by Wim Taymans <wim@fluendo.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.fluendo.jkate;

public class KateBitmapType {
    public static final KateBitmapType KATE_BITMAP_TYPE_PALETTED = new KateBitmapType();
    public static final KateBitmapType KATE_BITMAP_TYPE_PNG = new KateBitmapType();

    // Legacy references for backward compatibility
    public static final KateBitmapType kate_bitmap_type_paletted = KATE_BITMAP_TYPE_PALETTED;
    public static final KateBitmapType kate_bitmap_type_png = KATE_BITMAP_TYPE_PNG;

    private static final KateBitmapType[] LIST = {
        KATE_BITMAP_TYPE_PALETTED,
        KATE_BITMAP_TYPE_PNG,
    };

    private KateBitmapType() {
    }

    /**
     * Create a KateBitmapType object from an integer.
     */
    public static KateBitmapType CreateBitmapType(int idx) throws KateException {
        if (idx < 0 || idx >= LIST.length) {
            throw new KateException("Bitmap type " + idx + " out of bounds");
        }
        return LIST[idx];
    }
}