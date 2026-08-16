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

import java.util.Arrays;
import java.util.Objects;

/** A curve definition, splines, segments, etc. */
public class Curve {
  public KateCurveType type;
  public int npts;
  public double[][] pts;

  public Curve() {}

  public Curve(KateCurveType type, int npts, double[][] pts) {
    this.type = type;
    this.npts = npts;
    this.pts = pts;
  }

  public KateCurveType getType() {
    return type;
  }

  public void setType(KateCurveType type) {
    this.type = type;
  }

  public int getNpts() {
    return npts;
  }

  public void setNpts(int npts) {
    this.npts = npts;
  }

  public double[][] getPts() {
    return pts;
  }

  public void setPts(double[][] pts) {
    this.pts = pts;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Curve curve = (Curve) o;
    return npts == curve.npts
        && Objects.equals(type, curve.type)
        && Arrays.deepEquals(pts, curve.pts);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(type, npts);
    result = 31 * result + Arrays.deepHashCode(pts);
    return result;
  }

  @Override
  public String toString() {
    return "Curve{" + "type=" + type + ", npts=" + npts + ", pts=" + Arrays.deepToString(pts) + '}';
  }
}
