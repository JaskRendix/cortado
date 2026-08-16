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

import java.util.Arrays;
import java.util.Objects;

/** A motion definition, composed of a series of curves */
public class Motion {
  public Curve[] curves;
  public double[] durations;
  public KateMotionMapping x_mapping;
  public KateMotionMapping y_mapping;
  public KateMotionSemantics semantics;
  public boolean periodic;

  public Motion() {
    this.curves = new Curve[0];
    this.durations = new double[0];
    this.x_mapping = null;
    this.y_mapping = null;
    this.semantics = null;
    this.periodic = false;
  }

  public Motion(
      Curve[] curves,
      double[] durations,
      KateMotionMapping x_mapping,
      KateMotionMapping y_mapping,
      KateMotionSemantics semantics,
      boolean periodic) {
    this.curves = curves != null ? curves : new Curve[0];
    this.durations = durations != null ? durations : new double[0];
    this.x_mapping = x_mapping;
    this.y_mapping = y_mapping;
    this.semantics = semantics;
    this.periodic = periodic;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Motion motion = (Motion) o;
    return periodic == motion.periodic
        && Arrays.equals(curves, motion.curves)
        && Arrays.equals(durations, motion.durations)
        && x_mapping == motion.x_mapping
        && y_mapping == motion.y_mapping
        && semantics == motion.semantics;
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(x_mapping, y_mapping, semantics, periodic);
    result = 31 * result + Arrays.hashCode(curves);
    result = 31 * result + Arrays.hashCode(durations);
    return result;
  }

  @Override
  public String toString() {
    return "Motion{"
        + "curvesCount="
        + (curves != null ? curves.length : 0)
        + ", durationsCount="
        + (durations != null ? durations.length : 0)
        + ", x_mapping="
        + x_mapping
        + ", y_mapping="
        + y_mapping
        + ", semantics="
        + semantics
        + ", periodic="
        + periodic
        + '}';
  }
}
