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

public class KateMarkupType {
    public static final KateMarkupType KATE_MARKUP_NONE = new KateMarkupType();
    public static final KateMarkupType KATE_MARKUP_SIMPLE = new KateMarkupType();

    // Legacy references for backward compatibility
    public static final KateMarkupType kate_markup_none = KATE_MARKUP_NONE;
    public static final KateMarkupType kate_markup_simple = KATE_MARKUP_SIMPLE;

    private static final KateMarkupType[] LIST = {
        KATE_MARKUP_NONE,
        KATE_MARKUP_SIMPLE,
    };

    private KateMarkupType() {
    }

    /**
     * Create a KateMarkupType object from an integer.
     */
    public static KateMarkupType CreateMarkupType(int idx) throws KateException {
        if (idx < 0 || idx >= LIST.length) {
            throw new KateException("Markup type " + idx + " out of bounds");
        }
        return LIST[idx];
    }
}
