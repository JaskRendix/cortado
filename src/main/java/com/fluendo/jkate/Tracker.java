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

import java.awt.Dimension;
import java.util.Arrays;
import java.util.Objects;
import com.fluendo.utils.Debug;

/**
 * Tracks event coordinates, regions, and styles during rendering or layout calculations.
 */
public final class Tracker {

    private Dimension window;
    private Dimension frame;

    public Event ev;

    public boolean[] has = new boolean[64];
    public static final int has_region = 0;
    public static final int has_text_alignment_int = 1;
    public static final int has_text_alignment_ext = 2;

    public float region_x;
    public float region_y;
    public float region_w;
    public float region_h;

    public Tracker() {
        this.ev = null;
        this.window = null;
        this.frame = null;
        this.region_x = 0.0f;
        this.region_y = 0.0f;
        this.region_w = 0.0f;
        this.region_h = 0.0f;
    }

    public Tracker(Event ev) {
        this.ev = ev;
        this.window = null;
        this.frame = null;
        this.region_x = 0.0f;
        this.region_y = 0.0f;
        this.region_w = 0.0f;
        this.region_h = 0.0f;
    }

    /**
     * Update the tracker at the given time for the given image's dimensions.
     */
    public boolean update(double t, Dimension window, Dimension frame) {
        this.window = window;
        this.frame = frame;

        if (ev == null) {
            Debug.debug("Tracker update failed: Event is null");
            return false;
        }

        if (frame == null && (ev.kr != null && ev.kr.metric != KateSpaceMetric.kate_metric_pixels)) {
            Debug.debug("Tracker update failed: Frame dimension is required for non-pixel metrics");
            return false;
        }

        /* find current region and style, if any */
        Region kr = ev.kr;
        Style ks = ev.ks;
        if (ks == null && kr != null && kr.style >= 0) {
            if (ev.ki != null && ev.ki.styles != null && kr.style < ev.ki.styles.length) {
                ks = ev.ki.styles[kr.style];
            }
        }

        /* start with nothing */
        Arrays.fill(has, false);

        /* define region */
        if (kr != null) {
            if (kr.metric == KateSpaceMetric.kate_metric_percentage) {
                if (frame == null) return false;
                region_x = kr.x * frame.width / 100.0f;
                region_y = kr.y * frame.height / 100.0f;
                region_w = kr.w * frame.width / 100.0f;
                region_h = kr.h * frame.height / 100.0f;
            } else if (kr.metric == KateSpaceMetric.kate_metric_millionths) {
                if (frame == null) return false;
                region_x = kr.x * frame.width / 1000000.0f;
                region_y = kr.y * frame.height / 1000000.0f;
                region_w = kr.w * frame.width / 1000000.0f;
                region_h = kr.h * frame.height / 1000000.0f;
            } else if (kr.metric == KateSpaceMetric.kate_metric_pixels) {
                region_x = kr.x;
                region_y = kr.y;
                region_w = kr.w;
                region_h = kr.h;
            } else {
                Debug.debug("Invalid metrics");
                return false;
            }
            has[has_region] = true;
        }

        return true;
    }

    public Dimension getWindow() {
        return window;
    }

    public Dimension getFrame() {
        return frame;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tracker tracker = (Tracker) o;
        return Float.compare(tracker.region_x, region_x) == 0 &&
                Float.compare(tracker.region_y, region_y) == 0 &&
                Float.compare(tracker.region_w, region_w) == 0 &&
                Float.compare(tracker.region_h, region_h) == 0 &&
                Objects.equals(window, tracker.window) &&
                Objects.equals(frame, tracker.frame) &&
                Objects.equals(ev, tracker.ev) &&
                Arrays.equals(has, tracker.has);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(window, frame, ev, region_x, region_y, region_w, region_h);
        result = 31 * result + Arrays.hashCode(has);
        return result;
    }

    @Override
    public String toString() {
        return "Tracker{" +
                "region_x=" + region_x +
                ", region_y=" + region_y +
                ", region_w=" + region_w +
                ", region_h=" + region_h +
                '}';
    }
}
