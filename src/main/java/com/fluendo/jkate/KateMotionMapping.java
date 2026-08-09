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

public class KateMotionMapping {
    public static final KateMotionMapping KMM_NONE = new KateMotionMapping();
    public static final KateMotionMapping KMM_FRAME = new KateMotionMapping();
    public static final KateMotionMapping KMM_WINDOW = new KateMotionMapping();
    public static final KateMotionMapping KMM_REGION = new KateMotionMapping();
    public static final KateMotionMapping KMM_EVENT_DURATION = new KateMotionMapping();
    public static final KateMotionMapping KMM_BITMAP_SIZE = new KateMotionMapping();

    // Legacy references for backward compatibility
    public static final KateMotionMapping kmm_none = KMM_NONE;
    public static final KateMotionMapping kmm_frame = KMM_FRAME;
    public static final KateMotionMapping kmm_window = KMM_WINDOW;
    public static final KateMotionMapping kmm_region = KMM_REGION;
    public static final KateMotionMapping kmm_event_duration = KMM_EVENT_DURATION;
    public static final KateMotionMapping kmm_bitmap_size = KMM_BITMAP_SIZE;

    private static final KateMotionMapping[] LIST = {
        KMM_NONE,
        KMM_FRAME,
        KMM_WINDOW,
        KMM_REGION,
        KMM_EVENT_DURATION,
        KMM_BITMAP_SIZE,
    };

    private KateMotionMapping() {
    }

    /**
     * Create a KateMotionMapping object from an integer.
     */
    public static KateMotionMapping CreateMotionMapping(int idx) throws KateException {
        if (idx < 0 || idx >= LIST.length) {
            throw new KateException("Motion mapping " + idx + " out of bounds");
        }
        return LIST[idx];
    }
}
