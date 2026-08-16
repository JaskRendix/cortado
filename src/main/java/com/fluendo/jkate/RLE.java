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

import com.jcraft.jogg.Buffer;
import java.util.Arrays;

/** RLE decoding routines. */
public final class RLE {

  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC = 4;
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC_IN_DELTA = 3;
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC_STARTEND = 3;
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC_STOP = 6;
  private static final int KATE_RLE_RUN_LENGTH_BITS_DELTA = 6;
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC_STARTEND_START = 9;
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC_STARTEND_END = 8;
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC_STOP_START = 8;
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC_IN_DELTA_STOP = 3;
  private static final int KATE_RLE_RUN_LENGTH_BITS_DELTA_STOP = 5;
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC_ZERO = 8;
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC_NON_ZERO = 3;

  private static final int KATE_RLE_TYPE_EMPTY = 0;
  private static final int KATE_RLE_TYPE_BASIC = 1;
  private static final int KATE_RLE_TYPE_DELTA = 2;
  private static final int KATE_RLE_TYPE_BASIC_STOP = 3;
  private static final int KATE_RLE_TYPE_BASIC_STARTEND = 4;
  private static final int KATE_RLE_TYPE_DELTA_STOP = 5;
  private static final int KATE_RLE_TYPE_BASIC_ZERO = 6;

  private static final int KATE_RLE_TYPE_BITS = 3;

  // Private constructor to prevent utility class instantiation
  private RLE() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  private static int decodeLineEmpty(
      Buffer opb, int width, byte[] pixels, int offset, int bits, byte zero) {
    Arrays.fill(pixels, offset, offset + width, zero);
    return 0;
  }

  private static int decodeLineBasic(
      Buffer opb, int width, byte[] pixels, int offset, int bits, byte zero) {
    final int runLengthBits = KATE_RLE_RUN_LENGTH_BITS_BASIC;
    int p = 0;
    int count = width;
    while (count > 0) {
      int runLength = 1 + opb.read(runLengthBits);
      if (runLength <= 0 || runLength > count) return -1;
      byte value = (byte) opb.read(bits);
      Arrays.fill(pixels, offset + p, offset + p + runLength, value);
      p += runLength;
      count -= runLength;
    }
    return 0;
  }

  private static int decodeLineDelta(
      Buffer opb, int width, byte[] pixels, int offset, int bits, byte zero) {
    final int runLengthDeltaBits = KATE_RLE_RUN_LENGTH_BITS_DELTA;
    final int runLengthBasicBits = KATE_RLE_RUN_LENGTH_BITS_BASIC_IN_DELTA;
    int p = 0;
    int count = width;
    while (count > 0) {
      int type = opb.read1();
      if (type != 0) {
        int runLength = 1 + opb.read(runLengthDeltaBits);
        if (runLength <= 0 || runLength > count) return -1;
        if (offset > 0) {
          for (int n = 0; n < runLength; ++n) {
            pixels[offset + p] = pixels[offset + p - width];
            ++p;
          }
        } else {
          Arrays.fill(pixels, offset + p, offset + p + runLength, zero);
          p += runLength;
        }
        count -= runLength;
      } else {
        int runLength = 1 + opb.read(runLengthBasicBits);
        if (runLength <= 0 || runLength > count) return -1;
        byte value = (byte) opb.read(bits);
        Arrays.fill(pixels, offset + p, offset + p + runLength, value);
        p += runLength;
        count -= runLength;
      }
    }
    return 0;
  }

  private static int decodeLineBasicStartEnd(
      Buffer opb, int width, byte[] pixels, int offset, int bits, byte zero) {
    final int runLengthBits = KATE_RLE_RUN_LENGTH_BITS_BASIC_STARTEND;
    int count = width;
    int p = 0;

    int runLength = opb.read(KATE_RLE_RUN_LENGTH_BITS_BASIC_STARTEND_START);
    if (runLength > 0) {
      if (runLength > count) return -1;
      Arrays.fill(pixels, offset + p, offset + p + runLength, zero);
      p += runLength;
      count -= runLength;
    }

    runLength = opb.read(KATE_RLE_RUN_LENGTH_BITS_BASIC_STARTEND_END);
    if (runLength > 0) {
      if (runLength > count) return -1;
      for (int n = 0; n < runLength; ++n) pixels[offset + width - 1 - n] = zero;
      count -= runLength;
    }

    while (count > 0) {
      runLength = 1 + opb.read(runLengthBits);
      if (runLength <= 0 || runLength > count) return -1;
      byte value = (byte) opb.read(bits);
      Arrays.fill(pixels, offset + p, offset + p + runLength, value);
      p += runLength;
      count -= runLength;
    }

    return 0;
  }

  private static int decodeLineBasicStop(
      Buffer opb, int width, byte[] pixels, int offset, int bits, byte zero) {
    final int runLengthBits = KATE_RLE_RUN_LENGTH_BITS_BASIC_STOP;
    int count = width;
    int p = 0;

    int runLength = opb.read(KATE_RLE_RUN_LENGTH_BITS_BASIC_STOP_START);
    if (runLength > 0) {
      if (runLength > count) return -1;
      Arrays.fill(pixels, offset + p, offset + p + runLength, zero);
      p += runLength;
      count -= runLength;
    }

    while (count > 0) {
      runLength = opb.read(runLengthBits);
      if (runLength > count) return -1;
      if (runLength == 0) {
        break;
      }
      byte value = (byte) opb.read(bits);
      Arrays.fill(pixels, offset + p, offset + p + runLength, value);
      p += runLength;
      count -= runLength;
    }

    return 0;
  }

  private static int decodeLineDeltaStop(
      Buffer opb, int width, byte[] pixels, int offset, int bits, byte zero) {
    final int runLengthDeltaBits = KATE_RLE_RUN_LENGTH_BITS_DELTA_STOP;
    final int runLengthBasicBits = KATE_RLE_RUN_LENGTH_BITS_BASIC_IN_DELTA_STOP;
    int count = width;
    int p = 0;

    while (count > 0) {
      int type = opb.read1();
      int runLength;
      if (type != 0) {
        runLength = 1 + opb.read(runLengthDeltaBits);
        if (runLength <= 0 || runLength > count) return -1;
        if (offset > 0) {
          for (int n = 0; n < runLength; ++n) {
            pixels[offset + p] = pixels[offset + p - width];
            ++p;
          }
        } else {
          Arrays.fill(pixels, offset + p, offset + p + runLength, zero);
          p += runLength;
        }
      } else {
        runLength = opb.read(runLengthBasicBits);
        if (runLength == 0) {
          break;
        }
        if (runLength > count) return -1;
        byte value = (byte) opb.read(bits);
        Arrays.fill(pixels, offset + p, offset + p + runLength, value);
        p += runLength;
      }
      count -= runLength;
    }

    return 0;
  }

  private static int decodeLineBasicZero(
      Buffer opb, int width, byte[] pixels, int offset, int bits, byte zero) {
    final int runLengthBitsZero = KATE_RLE_RUN_LENGTH_BITS_BASIC_ZERO;
    final int runLengthBitsNonZero = KATE_RLE_RUN_LENGTH_BITS_BASIC_NON_ZERO;
    int count = width;
    int p = 0;

    while (count > 0) {
      byte value = (byte) opb.read(bits);
      int runLength;
      if (value == zero) {
        runLength = 1 + opb.read(runLengthBitsZero);
      } else {
        runLength = 1 + opb.read(runLengthBitsNonZero);
      }
      if (runLength <= 0 || runLength > count) return -1;
      Arrays.fill(pixels, offset + p, offset + p + runLength, value);
      p += runLength;
      count -= runLength;
    }

    return 0;
  }

  public static byte[] decodeRLE(Buffer opb, int width, int height, int bpp) {
    if (width <= 0 || height <= 0 || bpp <= 0) {
      return null;
    }
    byte[] pixels = new byte[width * height];
    int offset = 0;
    byte zero = (byte) opb.read(bpp);

    while (height > 0) {
      int type = opb.read(KATE_RLE_TYPE_BITS);
      int ret;
      switch (type) {
        case KATE_RLE_TYPE_EMPTY:
          ret = decodeLineEmpty(opb, width, pixels, offset, bpp, zero);
          break;
        case KATE_RLE_TYPE_DELTA:
          ret = decodeLineDelta(opb, width, pixels, offset, bpp, zero);
          break;
        case KATE_RLE_TYPE_BASIC:
          ret = decodeLineBasic(opb, width, pixels, offset, bpp, zero);
          break;
        case KATE_RLE_TYPE_BASIC_STARTEND:
          ret = decodeLineBasicStartEnd(opb, width, pixels, offset, bpp, zero);
          break;
        case KATE_RLE_TYPE_BASIC_STOP:
          ret = decodeLineBasicStop(opb, width, pixels, offset, bpp, zero);
          break;
        case KATE_RLE_TYPE_DELTA_STOP:
          ret = decodeLineDeltaStop(opb, width, pixels, offset, bpp, zero);
          break;
        case KATE_RLE_TYPE_BASIC_ZERO:
          ret = decodeLineBasicZero(opb, width, pixels, offset, bpp, zero);
          break;
        default:
          return null;
      }
      if (ret != 0) return null;
      offset += width;
      --height;
    }
    return pixels;
  }
}
