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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.fluendo.jkate;

public enum KateMotionSemantics {
    KMS_TIME,
    KMS_Z,
    KMS_REGION_POSITION,
    KMS_REGION_SIZE,
    KMS_TEXT_ALIGNMENT_INT,
    KMS_TEXT_ALIGNMENT_EXT,
    KMS_TEXT_POSITION,
    KMS_TEXT_SIZE,
    KMS_MARKER1_POSITION,
    KMS_MARKER2_POSITION,
    KMS_MARKER3_POSITION,
    KMS_MARKER4_POSITION,
    KMS_GLYPH_POINTER_1,
    KMS_GLYPH_POINTER_2,
    KMS_GLYPH_POINTER_3,
    KMS_GLYPH_POINTER_4,
    KMS_TEXT_COLOR_RG,
    KMS_TEXT_COLOR_BA,
    KMS_BACKGROUND_COLOR_RG,
    KMS_BACKGROUND_COLOR_BA,
    KMS_DRAW_COLOR_RG,
    KMS_DRAW_COLOR_BA,
    KMS_STYLE_MORPH,
    KMS_TEXT_PATH,
    KMS_TEXT_PATH_SECTION,
    KMS_DRAW,
    KMS_TEXT_VISIBLE_SECTION,
    KMS_HORIZONTAL_MARGINS,
    KMS_VERTICAL_MARGINS,
    KMS_BITMAP_POSITION,
    KMS_BITMAP_SIZE,
    KMS_MARKER1_BITMAP,
    KMS_MARKER2_BITMAP,
    KMS_MARKER3_BITMAP,
    KMS_MARKER4_BITMAP,
    KMS_GLYPH_POINTER_1_BITMAP,
    KMS_GLYPH_POINTER_2_BITMAP,
    KMS_GLYPH_POINTER_3_BITMAP,
    KMS_GLYPH_POINTER_4_BITMAP,
    KMS_DRAW_WIDTH;

    private static final KateMotionSemantics[] VALUES = values();

    /**
     * Create a KateMotionSemantics object from an integer index.
     */
    public static KateMotionSemantics createMotionSemantics(int idx) throws KateException {
        if (idx < 0 || idx >= VALUES.length) {
            throw new KateException("Motion semantics " + idx + " out of bounds");
        }
        return VALUES[idx];
    }
}
