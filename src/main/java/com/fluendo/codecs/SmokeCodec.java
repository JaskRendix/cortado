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

import java.awt.*;

public class SmokeCodec {
  private static final int IDX_TYPE       = 0;
  private static final int IDX_WIDTH      = 1;
  private static final int IDX_HEIGHT     = 3;
  private static final int IDX_FPS_NUM    = 5;
  private static final int IDX_FPS_DENOM  = 9;
  private static final int IDX_FLAGS      = 13;
  private static final int IDX_NUM_BLOCKS = 14;
  private static final int IDX_SIZE       = 16;
  private static final int IDX_BLOCKS     = 18;
  private static final int OFFS_PICT      = 18;

  private Image reference;
  private final MediaTracker mt;
  private final Component component;
  private final Toolkit toolkit;

  public static final int KEYFRAME = (1 << 0);

  public int type;
  public int width, height;
  public int fps_num, fps_denom;
  public int flags;
  public int size;
  public int blocks;

  public SmokeCodec(Component comp, MediaTracker tracker) {
    this.component = comp;
    this.toolkit = comp.getToolkit();
    this.mt = tracker;
  }

  public int parseHeader(byte[] in, int offset, int length) {
    if (in == null || length - offset < OFFS_PICT) {
      return -1;
    }

    type = in[IDX_TYPE + offset] & 0xFF;

    width = ((in[IDX_WIDTH + offset] & 0xFF) << 8) | 
            (in[IDX_WIDTH + 1 + offset] & 0xFF);
            
    height = ((in[IDX_HEIGHT + offset] & 0xFF) << 8) | 
             (in[IDX_HEIGHT + 1 + offset] & 0xFF);

    fps_num = ((in[IDX_FPS_NUM + offset] & 0xFF) << 24) | 
              ((in[IDX_FPS_NUM + 1 + offset] & 0xFF) << 16) | 
              ((in[IDX_FPS_NUM + 2 + offset] & 0xFF) << 8) | 
              (in[IDX_FPS_NUM + 3 + offset] & 0xFF);

    fps_denom = ((in[IDX_FPS_DENOM + offset] & 0xFF) << 24) | 
                ((in[IDX_FPS_DENOM + 1 + offset] & 0xFF) << 16) | 
                ((in[IDX_FPS_DENOM + 2 + offset] & 0xFF) << 8) | 
                (in[IDX_FPS_DENOM + 3 + offset] & 0xFF);

    flags = in[IDX_FLAGS + offset] & 0xFF;
    
    // Note: original code checked IDX_SIZE twice for b2; fixing to IDX_SIZE + 1
    size = ((in[IDX_SIZE + offset] & 0xFF) << 8) | 
           (in[IDX_SIZE + 1 + offset] & 0xFF);

    blocks = ((in[IDX_NUM_BLOCKS + offset] & 0xFF) << 8) | 
             (in[IDX_NUM_BLOCKS + 1 + offset] & 0xFF);

    return 0;
  }
  
  public Image decode(byte[] in, int offset, int length) {
    if (parseHeader(in, offset, length) < 0) {
      return null;
    }

    boolean keyframe = ((flags & KEYFRAME) != 0);

    if (reference == null && !keyframe) {
      return null;
    }

    int imgoff = blocks * 2 + OFFS_PICT;
    if (length - imgoff < 0) {
      return null;
    }
    
    Image src = null;
    try {
      src = toolkit.createImage(in, imgoff + offset, length - imgoff);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (src == null) {
      return null;
    }
      
    try {
      mt.addImage(src, 0);
      mt.waitForID(0);
      mt.removeImage(src, 0);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (reference == null || keyframe) {
      reference = src;
    } else {
      if (blocks > 0) {
        int src_w = src.getWidth(null);
        int src_h = src.getHeight(null);
        int blockptr = 0;
        int pos, i, j, x, y;

        int blockOffset = offset + IDX_BLOCKS;

        Image newref = component.createImage(width, height);
        Graphics refgfx = newref.getGraphics();
        refgfx.drawImage(reference, 0, 0, null);
        reference = newref;

        for (i = 0; i < src_h; i += 16) {
          for (j = 0; j < src_w; j += 16) {
            pos = blockptr * 2 + blockOffset;
            if (pos + 1 >= offset + length) {
              break;
            }
            int b1 = in[pos] & 0xFF;
            int b2 = in[pos + 1] & 0xFF;
            pos = (b1 << 8) | b2;

            int div = width / 16;
            if (div == 0) div = 1; // Prevent division by zero safeguard

            x = (pos % div) * 16;
            y = (pos / div) * 16;

            refgfx.drawImage(src, 
               x, y, x + 16, y + 16, 
               j, i, j + 16, i + 16, 
               null);

            blockptr++;
            if (blockptr >= blocks) {
              break;
            }
          }
        }
        refgfx.dispose();
      }
    }
    return reference;
  }
}
