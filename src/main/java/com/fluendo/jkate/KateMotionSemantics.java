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

public class KateMotionSemantics {
    public static final KateMotionSemantics KMS_TIME = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_Z = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_REGION_POSITION = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_REGION_SIZE = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_TEXT_ALIGNMENT_INT = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_TEXT_ALIGNMENT_EXT = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_TEXT_POSITION = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_TEXT_SIZE = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_MARKER1_POSITION = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_MARKER2_POSITION = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_MARKER3_POSITION = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_MARKER4_POSITION = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_GLYPH_POINTER_1 = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_GLYPH_POINTER_2 = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_GLYPH_POINTER_3 = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_GLYPH_POINTER_4 = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_TEXT_COLOR_RG = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_TEXT_COLOR_BA = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_BACKGROUND_COLOR_RG = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_BACKGROUND_COLOR_BA = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_DRAW_COLOR_RG = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_DRAW_COLOR_BA = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_STYLE_MORPH = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_TEXT_PATH = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_TEXT_PATH_SECTION = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_DRAW = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_TEXT_VISIBLE_SECTION = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_HORIZONTAL_MARGINS = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_VERTICAL_MARGINS = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_BITMAP_POSITION = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_BITMAP_SIZE = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_MARKER1_BITMAP = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_MARKER2_BITMAP = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_MARKER3_BITMAP = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_MARKER4_BITMAP = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_GLYPH_POINTER_1_BITMAP = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_GLYPH_POINTER_2_BITMAP = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_GLYPH_POINTER_3_BITMAP = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_GLYPH_POINTER_4_BITMAP = new KateMotionSemantics();
    public static final KateMotionSemantics KMS_DRAW_WIDTH = new KateMotionSemantics();

    // Legacy references for backward compatibility
    public static final KateMotionSemantics kms_time = KMS_TIME;
    public static final KateMotionSemantics kms_z = KMS_Z;
    public static final KateMotionSemantics kms_region_position = KMS_REGION_POSITION;
    public static final KateMotionSemantics kms_region_size = KMS_REGION_SIZE;
    public static final KateMotionSemantics kms_text_alignment_int = KMS_TEXT_ALIGNMENT_INT;
    public static final KateMotionSemantics kms_text_alignment_ext = KMS_TEXT_ALIGNMENT_EXT;
    public static final KateMotionSemantics kms_text_position = KMS_TEXT_POSITION;
    public static final KateMotionSemantics kms_text_size = KMS_TEXT_SIZE;
    public static final KateMotionSemantics kms_marker1_position = KMS_MARKER1_POSITION;
    public static final KateMotionSemantics kms_marker2_position = KMS_MARKER2_POSITION;
    public static final KateMotionSemantics kms_marker3_position = KMS_MARKER3_POSITION;
    public static final KateMotionSemantics kms_marker4_position = KMS_MARKER4_POSITION;
    public static final KateMotionSemantics kms_glyph_pointer_1 = KMS_GLYPH_POINTER_1;
    public static final KateMotionSemantics kms_glyph_pointer_2 = KMS_GLYPH_POINTER_2;
    public static final KateMotionSemantics kms_glyph_pointer_3 = KMS_GLYPH_POINTER_3;
    public static final KateMotionSemantics kms_glyph_pointer_4 = KMS_GLYPH_POINTER_4;
    public static final KateMotionSemantics kms_text_color_rg = KMS_TEXT_COLOR_RG;
    public static final KateMotionSemantics kms_text_color_ba = KMS_TEXT_COLOR_BA;
    public static final KateMotionSemantics kms_background_color_rg = KMS_BACKGROUND_COLOR_RG;
    public static final KateMotionSemantics kms_background_color_ba = KMS_BACKGROUND_COLOR_BA;
    public static final KateMotionSemantics kms_draw_color_rg = KMS_DRAW_COLOR_RG;
    public static final KateMotionSemantics kms_draw_color_ba = KMS_DRAW_COLOR_BA;
    public static final KateMotionSemantics kms_style_morph = KMS_STYLE_MORPH;
    public static final KateMotionSemantics kms_text_path = KMS_TEXT_PATH;
    public static final KateMotionSemantics kms_text_path_section = KMS_TEXT_PATH_SECTION;
    public static final KateMotionSemantics kms_draw = KMS_DRAW;
    public static final KateMotionSemantics kms_text_visible_section = KMS_TEXT_VISIBLE_SECTION;
    public static final KateMotionSemantics kms_horizontal_margins = KMS_HORIZONTAL_MARGINS;
    public static final KateMotionSemantics kms_vertical_margins = KMS_VERTICAL_MARGINS;
    public static final KateMotionSemantics kms_bitmap_position = KMS_BITMAP_POSITION;
    public static final KateMotionSemantics kms_bitmap_size = KMS_BITMAP_SIZE;
    public static final KateMotionSemantics kms_marker1_bitmap = KMS_MARKER1_BITMAP;
    public static final KateMotionSemantics kms_marker2_bitmap = KMS_MARKER2_BITMAP;
    public static final KateMotionSemantics kms_marker3_bitmap = KMS_MARKER3_BITMAP;
    public static final KateMotionSemantics kms_marker4_bitmap = KMS_MARKER4_BITMAP;
    public static final KateMotionSemantics kms_glyph_pointer_1_bitmap = KMS_GLYPH_POINTER_1_BITMAP;
    public static final KateMotionSemantics kms_glyph_pointer_2_bitmap = KMS_GLYPH_POINTER_2_BITMAP;
    public static final KateMotionSemantics kms_glyph_pointer_3_bitmap = KMS_GLYPH_POINTER_3_BITMAP;
    public static final KateMotionSemantics kms_glyph_pointer_4_bitmap = KMS_GLYPH_POINTER_4_BITMAP;
    public static final KateMotionSemantics kms_draw_width = KMS_DRAW_WIDTH;

    private static final KateMotionSemantics[] LIST = {
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
        KMS_DRAW_WIDTH,
    };

    private KateMotionSemantics() {
    }

    /**
     * Create a KateMotionSemantics object from an integer.
     */
    public static KateMotionSemantics CreateMotionSemantics(int idx) throws KateException {
        if (idx < 0 || idx >= LIST.length) {
            throw new KateException("Motion semantics " + idx + " out of bounds");
        }
        return LIST[idx];
    }
}
