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

import java.util.Objects;

/**
 * A region definition, for placement of text/images.
 */
public class Region {
    public KateSpaceMetric metric;
    public int x;
    public int y;
    public int w;
    public int h;
    public int style;
    public boolean clip;

    public Region() {
        this.metric = null;
        this.x = 0;
        this.y = 0;
        this.w = 0;
        this.h = 0;
        this.style = 0;
        this.clip = false;
    }

    public Region(KateSpaceMetric metric, int x, int y, int w, int h, int style, boolean clip) {
        this.metric = metric;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.style = style;
        this.clip = clip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Region region = (Region) o;
        return x == region.x &&
                y == region.y &&
                w == region.w &&
                h == region.h &&
                style == region.style &&
                clip == region.clip &&
                metric == region.metric;
    }

    @Override
    public int hashCode() {
        return Objects.hash(metric, x, y, w, h, style, clip);
    }

    @Override
    public String toString() {
        return "Region{" +
                "metric=" + metric +
                ", x=" + x +
                ", y=" + y +
                ", w=" + w +
                ", h=" + h +
                ", style=" + style +
                ", clip=" + clip +
                '}';
    }
}
