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

public class KateWrapMode {
    public static final KateWrapMode KATE_WRAP_WORD = new KateWrapMode();
    public static final KateWrapMode KATE_WRAP_NONE = new KateWrapMode();

    // Legacy references for backward compatibility
    public static final KateWrapMode kate_wrap_word = KATE_WRAP_WORD;
    public static final KateWrapMode kate_wrap_none = KATE_WRAP_NONE;

    private static final KateWrapMode[] LIST = {
        KATE_WRAP_WORD,
        KATE_WRAP_NONE,
    };

    private KateWrapMode() {
    }

    /**
     * Create a KateWrapMode object from an integer.
     */
    public static KateWrapMode CreateWrapMode(int idx) throws KateException {
        if (idx < 0 || idx >= LIST.length) {
            throw new KateException("Wrap mode " + idx + " out of bounds");
        }
        return LIST[idx];
    }
}
