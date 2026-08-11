/* Smoke Codec
 * Copyright (C) <2004> Wim Taymans <wim@fluendo.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package com.fluendo.codecs;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class SmokeCodec {
  private static final Logger LOGGER = Logger.getLogger(SmokeCodec.class.getName());

  private static final int IDX_TYPE = 0;
  private static final int IDX_WIDTH = 1;
  private static final int IDX_HEIGHT = 3;
  private static final int IDX_FPS_NUM = 5;
  private static final int IDX_FPS_DENOM = 9;
  private static final int IDX_FLAGS = 13;
  private static final int IDX_NUM_BLOCKS = 14;
  private static final int IDX_SIZE = 16;
  private static final int IDX_BLOCKS = 18;
  private static final int OFFS_PICT = 18;

  public static final int KEYFRAME = (1);

  private BufferedImage reference;
  private CodecHeader header;

  /**
   * Immutable record representing the header metadata (Java 14+).
   */
  public record CodecHeader(
      int type,
      int width,
      int height,
      int fpsNum,
      int fpsDenom,
      int flags,
      int size,
      int blocks) {
    public boolean isKeyframe() {
      return (flags & KEYFRAME) != 0;
    }
  }

  public SmokeCodec() {
    // Decoupled from AWT Component and MediaTracker
  }

  /**
   * Parses the header using java.nio.ByteBuffer.
   */
  public CodecHeader parseHeader(byte[] in, int offset, int length) {
    if (in == null || length - offset < OFFS_PICT) {
      return null;
    }

    var buffer = ByteBuffer.wrap(in, offset, length).order(ByteOrder.BIG_ENDIAN);

    int type = buffer.get(IDX_TYPE) & 0xFF;
    int width = buffer.getShort(IDX_WIDTH) & 0xFFFF;
    int height = buffer.getShort(IDX_HEIGHT) & 0xFFFF;
    int fpsNum = buffer.getInt(IDX_FPS_NUM);
    int fpsDenom = buffer.getInt(IDX_FPS_DENOM);
    int flags = buffer.get(IDX_FLAGS) & 0xFF;
    int size = buffer.getShort(IDX_SIZE) & 0xFFFF;
    int blocks = buffer.getShort(IDX_NUM_BLOCKS) & 0xFFFF;

    this.header = new CodecHeader(type, width, height, fpsNum, fpsDenom, flags, size, blocks);
    return this.header;
  }

  /**
   * Helper method to directly get flags matching the modern header structure.
   */
  public int getFlags() {
    return this.header != null ? this.header.flags() : 0;
  }

  /**
   * Returns the current active header.
   */
  public CodecHeader getHeader() {
    return this.header;
  }

  /**
   * Decodes the frame into a BufferedImage using modern Java I/O.
   */
  public BufferedImage decode(byte[] in, int offset, int length) {
    var currentHeader = parseHeader(in, offset, length);
    if (currentHeader == null) {
      return null;
    }

    if (reference == null && !currentHeader.isKeyframe()) {
      return null;
    }

    int imgoff = currentHeader.blocks() * 2 + OFFS_PICT;
    if (length - imgoff < 0) {
      return null;
    }

    BufferedImage src = null;
    try (var bis = new ByteArrayInputStream(in, imgoff + offset, length - imgoff)) {
      src = ImageIO.read(bis);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to decode frame image", e);
    }

    if (src == null) {
      return null;
    }

    if (reference == null || currentHeader.isKeyframe()) {
      reference = src;
    } else {
      if (currentHeader.blocks() > 0) {
        int srcW = src.getWidth();
        int srcH = src.getHeight();
        int blockPtr = 0;
        int blockOffset = offset + IDX_BLOCKS;

        var newRef = new BufferedImage(currentHeader.width(), currentHeader.height(), BufferedImage.TYPE_INT_RGB);
        Graphics2D refGfx = newRef.createGraphics();
        refGfx.drawImage(reference, 0, 0, null);

        for (int i = 0; i < srcH; i += 16) {
          for (int j = 0; j < srcW; j += 16) {
            int pos = blockPtr * 2 + blockOffset;
            if (pos + 1 >= offset + length) {
              break;
            }

            int b1 = in[pos] & 0xFF;
            int b2 = in[pos + 1] & 0xFF;
            int blockPos = (b1 << 8) | b2;

            int div = currentHeader.width() / 16;
            if (div == 0) {
              div = 1;
            }

            int x = (blockPos % div) * 16;
            int y = (blockPos / div) * 16;

            refGfx.drawImage(src,
                x, y, x + 16, y + 16,
                j, i, j + 16, i + 16,
                null);

            blockPtr++;
            if (blockPtr >= currentHeader.blocks()) {
              break;
            }
          }
        }
        refGfx.dispose();
        reference = newRef;
      }
    }
    return reference;
  }
}
