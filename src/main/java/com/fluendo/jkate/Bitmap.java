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

/** A Bitmap definition. */
public class Bitmap {
  public int width;
  public int height;
  public int bpp;
  public KateBitmapType type;
  public int palette;
  public byte[] pixels;
  public int size;
  public int x_offset;
  public int y_offset;

  public Bitmap() {}

  public Bitmap(
      int width,
      int height,
      int bpp,
      KateBitmapType type,
      int palette,
      byte[] pixels,
      int size,
      int x_offset,
      int y_offset) {
    this.width = width;
    this.height = height;
    this.bpp = bpp;
    this.type = type;
    this.palette = palette;
    this.pixels = pixels;
    this.size = size;
    this.x_offset = x_offset;
    this.y_offset = y_offset;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getBpp() {
    return bpp;
  }

  public void setBpp(int bpp) {
    this.bpp = bpp;
  }

  public KateBitmapType getType() {
    return type;
  }

  public void setType(KateBitmapType type) {
    this.type = type;
  }

  public int getPalette() {
    return palette;
  }

  public void setPalette(int palette) {
    this.palette = palette;
  }

  public byte[] getPixels() {
    return pixels;
  }

  public void setPixels(byte[] pixels) {
    this.pixels = pixels;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getX_offset() {
    return x_offset;
  }

  public void setX_offset(int x_offset) {
    this.x_offset = x_offset;
  }

  public int getY_offset() {
    return y_offset;
  }

  public void setY_offset(int y_offset) {
    this.y_offset = y_offset;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Bitmap bitmap = (Bitmap) o;
    return width == bitmap.width
        && height == bitmap.height
        && bpp == bitmap.bpp
        && palette == bitmap.palette
        && size == bitmap.size
        && x_offset == bitmap.x_offset
        && y_offset == bitmap.y_offset
        && Objects.equals(type, bitmap.type)
        && Arrays.equals(pixels, bitmap.pixels);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(width, height, bpp, type, palette, size, x_offset, y_offset);
    result = 31 * result + Arrays.hashCode(pixels);
    return result;
  }

  @Override
  public String toString() {
    return "Bitmap{"
        + "width="
        + width
        + ", height="
        + height
        + ", bpp="
        + bpp
        + ", type="
        + type
        + ", palette="
        + palette
        + ", pixels="
        + Arrays.toString(pixels)
        + ", size="
        + size
        + ", x_offset="
        + x_offset
        + ", y_offset="
        + y_offset
        + '}';
  }
}
