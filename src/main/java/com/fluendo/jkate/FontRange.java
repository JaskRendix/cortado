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

import java.util.Objects;

/**
 * A code point range mapping to a set of bitmaps.
 */
public class FontRange {
    public int first_code_point;
    public int last_code_point;
    public int first_bitmap;

    public FontRange() {
    }

    public FontRange(int firstCodePoint, int lastCodePoint, int firstBitmap) {
        this.first_code_point = firstCodePoint;
        this.last_code_point = lastCodePoint;
        this.first_bitmap = firstBitmap;
    }

    public int getFirstCodePoint() {
        return first_code_point;
    }

    public void setFirstCodePoint(int firstCodePoint) {
        this.first_code_point = firstCodePoint;
    }

    public int getLastCodePoint() {
        return last_code_point;
    }

    public void setLastCodePoint(int lastCodePoint) {
        this.last_code_point = lastCodePoint;
    }

    public int getFirstBitmap() {
        return first_bitmap;
    }

    public void setFirstBitmap(int firstBitmap) {
        this.first_bitmap = firstBitmap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FontRange fontRange = (FontRange) o;
        return first_code_point == fontRange.first_code_point &&
                last_code_point == fontRange.last_code_point &&
                first_bitmap == fontRange.first_bitmap;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first_code_point, last_code_point, first_bitmap);
    }

    @Override
    public String toString() {
        return "FontRange{" +
                "first_code_point=" + first_code_point +
                ", last_code_point=" + last_code_point +
                ", first_bitmap=" + first_bitmap +
                '}';
    }
}
