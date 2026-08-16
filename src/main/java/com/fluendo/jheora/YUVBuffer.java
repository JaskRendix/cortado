/* Jheora
 * Copyright (C) 2004 Fluendo S.L.
 *
 * Written by: 2004 Wim Taymans <wim@fluendo.com>
 *
 * Many thanks to
 *   The Xiph.Org Foundation http://www.xiph.org/
 * Jheora was based on their Theora reference decoder.
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
package com.fluendo.jheora;

import java.awt.image.*;

public final class YUVBuffer implements ImageProducer {

  public int yWidth;
  public int yHeight;
  public int yStride;
  public int uvWidth;
  public int uvHeight;
  public int uvStride;
  public short[] data;
  public int yOffset;
  public int uOffset;
  public int vOffset;

  private int[] pixels;
  private int pixSize;
  private boolean newPixels = true;
  private final ColorModel colorModel = ColorModel.getRGBdefault();
  private ImageProducer filteredThis;
  private int cropX;
  private int cropY;
  private int cropW;
  private int cropH;

  @Override
  public void addConsumer(ImageConsumer ic) {}

  @Override
  public boolean isConsumer(ImageConsumer ic) {
    return false;
  }

  @Override
  public void removeConsumer(ImageConsumer ic) {}

  @Override
  public void requestTopDownLeftRightResend(ImageConsumer ic) {}

  @Override
  public void startProduction(ImageConsumer ic) {
    ic.setColorModel(colorModel);
    ic.setHints(
        ImageConsumer.TOPDOWNLEFTRIGHT
            | ImageConsumer.COMPLETESCANLINES
            | ImageConsumer.SINGLEFRAME
            | ImageConsumer.SINGLEPASS);
    ic.setDimensions(yWidth, yHeight);
    prepareRgbData(0, 0, yWidth, yHeight);
    ic.setPixels(0, 0, yWidth, yHeight, colorModel, pixels, 0, yWidth);
    ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
  }

  private synchronized void prepareRgbData(int x, int y, int width, int height) {
    if (!newPixels) {
      return;
    }

    int size = width * height;

    try {
      if (size != pixSize) {
        pixels = new int[size];
        pixSize = size;
      }
      /* rely on the buffer size being set correctly, and the only allowed
      video formats being Theora's video formats */
      if (uvHeight < yHeight) {
        yuv420ToRgb(x, y, width, height);
      } else if (uvWidth == yWidth) {
        yuv444ToRgb(x, y, width, height);
      } else {
        yuv422ToRgb(x, y, width, height);
      }
    } catch (Throwable t) {
      /* ignore */
    }
    newPixels = false;
  }

  public synchronized void newPixels() {
    newPixels = true;
  }

  // cropping code provided by Benjamin Schwartz
  public Object getObject(int x, int y, int width, int height) {
    if (x == 0 && y == 0 && width == yWidth && height == yHeight) {
      return this;
    } else {
      if (x != cropX || y != cropY || width != cropW || height != cropH) {
        cropX = x;
        cropY = y;
        cropW = width;
        cropH = height;
        CropImageFilter cropFilter = new CropImageFilter(cropX, cropY, cropW, cropH);
        filteredThis = new FilteredImageSource(this, cropFilter);
      }
      return filteredThis;
    }
  }

  private void yuv420ToRgb(int x, int y, int width, int height) {
    /*
     * this modified version of the original YUVtoRGB was
     * provided by Ilan and Yaniv Ben Hagai.
     *
     * additional thanks to Gumboot for helping with making this
     * code perform better.
     */

    // Set up starting values for YUV pointers
    int yPtr = yOffset + x + y * yStride;
    int yPtr2 = yPtr + yStride;
    int uPtr = uOffset + x / 2 + (y / 2) * uvStride;
    int vPtr = vOffset + x / 2 + (y / 2) * uvStride;
    int rgbPtr = 0;
    int rgbPtr2 = width;
    int width2 = width / 2;
    int height2 = height / 2;

    // Set the line step for the Y and UV planes and YPtr2
    int yStep = yStride * 2 - width2 * 2;
    int uvStep = uvStride - width2;
    int rgbStep = width;

    for (int i = 0; i < height2; i++) {
      for (int j = 0; j < width2; j++) {
        int d, e, r, g, b, t1, t2, t3, t4;

        d = data[uPtr++];
        e = data[vPtr++];

        t1 = 298 * (data[yPtr] - 16);
        t2 = 409 * e - 409 * 128 + 128;
        t3 = (100 * d) + (208 * e) - 100 * 128 - 208 * 128 - 128;
        t4 = 516 * d - 516 * 128 + 128;

        r = (t1 + t2);
        g = (t1 - t3);
        b = (t1 + t4);

        // retrieve data for next pixel now, hide latency?
        t1 = 298 * (data[yPtr + 1] - 16);

        // pack pixel
        pixels[rgbPtr] = (clamp65280(r) << 8) | clamp65280(g) | (clamp65280(b) >> 8) | 0xff000000;

        r = (t1 + t2);
        g = (t1 - t3);
        b = (t1 + t4);

        // retrieve data for next pixel now, hide latency?
        t1 = 298 * (data[yPtr2] - 16);

        // pack pixel
        pixels[rgbPtr + 1] =
            (clamp65280(r) << 8) | clamp65280(g) | (clamp65280(b) >> 8) | 0xff000000;

        r = (t1 + t2);
        g = (t1 - t3);
        b = (t1 + t4);

        // retrieve data for next pixel now, hide latency?
        t1 = 298 * (data[yPtr2 + 1] - 16);

        // pack pixel
        pixels[rgbPtr2] = (clamp65280(r) << 8) | clamp65280(g) | (clamp65280(b) >> 8) | 0xff000000;

        r = (t1 + t2);
        g = (t1 - t3);
        b = (t1 + t4);

        // pack pixel
        pixels[rgbPtr2 + 1] =
            (clamp65280(r) << 8) | clamp65280(g) | (clamp65280(b) >> 8) | 0xff000000;

        yPtr += 2;
        yPtr2 += 2;
        rgbPtr += 2;
        rgbPtr2 += 2;
      }

      // Increment the various pointers
      yPtr += yStep;
      yPtr2 += yStep;
      uPtr += uvStep;
      vPtr += uvStep;
      rgbPtr += rgbStep;
      rgbPtr2 += rgbStep;
    }
  }

  private static int clamp65280(int val) {
    /* 65280 == 255 << 8 == 0x0000FF00 */
    /* This function is just like clamp255, but only acting on the top
    24 bits (bottom 8 are zero'd).  This allows val, initially scaled
    to 65536, to be clamped without shifting, thereby saving one shift.
    (RGB packing must be aware that the info is in the second-lowest
    byte.) */
    return (~(val >> 31)) & 65280 & (val | ((65280 - val) >> 31));
  }

  private void yuv444ToRgb(int x, int y, int width, int height) {
    for (int j = 0; j < height; j++) {
      for (int i = 0; i < width; i++) {
        int d, e, r, g, b, t1, t2, t3, t4, p;
        p = x + i + (j + y) * yStride;

        d = data[uOffset + p];
        e = data[vOffset + p];

        t1 = 298 * (data[yOffset + p] - 16);
        t2 = 409 * e - 409 * 128 + 128;
        t3 = (100 * d) + (208 * e) - 100 * 128 - 208 * 128 - 128;
        t4 = 516 * d - 516 * 128 + 128;

        r = (t1 + t2);
        g = (t1 - t3);
        b = (t1 + t4);

        // pack pixel
        pixels[i + j * width] =
            (clamp65280(r) << 8) | clamp65280(g) | (clamp65280(b) >> 8) | 0xff000000;
      }
    }
  }

  private void yuv422ToRgb(int x, int y, int width, int height) {
    int x2 = x / 2;
    int width2 = width / 2;
    for (int j = 0; j < height; j++) {
      for (int i = 0; i < width2; i++) {
        int d, e, r, g, b, t1, t2, t3, t4, p;
        p = x2 + i + (y + j) * uvStride;

        d = data[uOffset + p];
        e = data[vOffset + p];

        p = yOffset + 2 * (x2 + i) + (y + j) * yStride;
        t1 = 298 * (data[p] - 16);
        t2 = 409 * e - 409 * 128 + 128;
        t3 = (100 * d) + (208 * e) - 100 * 128 - 208 * 128 - 128;
        t4 = 516 * d - 516 * 128 + 128;

        r = (t1 + t2);
        g = (t1 - t3);
        b = (t1 + t4);

        p++;
        t1 = 298 * (data[p] - 16);

        // pack pixel
        p = 2 * i + j * width;
        pixels[p] = (clamp65280(r) << 8) | clamp65280(g) | (clamp65280(b) >> 8) | 0xff000000;

        r = (t1 + t2);
        g = (t1 - t3);
        b = (t1 + t4);
        p++;

        // pack pixel
        pixels[p] = (clamp65280(r) << 8) | clamp65280(g) | (clamp65280(b) >> 8) | 0xff000000;
      }
    }
  }
}
