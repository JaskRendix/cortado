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

import com.fluendo.utils.MemUtils;

public final class DCTDecode {

  private static final int PUR = 8;
  private static final int PU = 4;
  private static final int PUL = 2;
  private static final int PL = 1;

  private final short[] dequantMatrix = new short[64];
  private static final int[] MODE_USES_MC = {0, 0, 1, 1, 1, 0, 1, 1};

  private static final short[][] PC = {
    {0, 0, 0, 0, 0, 0},
    {1, 0, 0, 0, 0, 0},
    {1, 0, 0, 0, 0, 0},
    {1, 0, 0, 0, 0, 0},
    {1, 0, 0, 0, 0, 0},
    {1, 1, 0, 0, 1, 1},
    {0, 1, 0, 0, 0, 0},
    {29, -26, 29, 0, 5, 31},
    {1, 0, 0, 0, 0, 0},
    {75, 53, 0, 0, 7, 127},
    {1, 1, 0, 0, 1, 1},
    {75, 0, 53, 0, 7, 127},
    {1, 0, 0, 0, 0, 0},
    {75, 0, 53, 0, 7, 127},
    {3, 10, 3, 0, 4, 15},
    {29, -26, 29, 0, 5, 31}
  };

  private static final int[] BC_MASK = {
    PUR | PU | PUL | PL, PUR | PU, PL, 0, PU | PUL | PL, PU, PL, 0
  };

  private static final short[] MODE_2_FRAME = {1, 0, 1, 1, 1, 2, 2, 1};

  private final short[] reconDataBuffer = new short[64];
  private final int[] v = new int[4];
  private final int[] fn = new int[4];
  private final short[] last = new short[3];
  private final iDCT idct = new iDCT();

  private void expandKfBlock(Playback pbi, int fragmentNumber) {
    int reconPixelsPerLine;
    int reconPixelIndex;
    short[] dequantCoeffs;
    int qi = pbi.FragQs[fragmentNumber];

    if (fragmentNumber < (int) pbi.YPlaneFragments) {
      reconPixelsPerLine = pbi.YStride;
      dequantCoeffs = pbi.info.dequant_tables[0][0][pbi.frameQIS[qi]];
      dequantMatrix[0] = pbi.info.dequant_tables[0][0][pbi.frameQIS[0]][0];
    } else if (fragmentNumber < pbi.YPlaneFragments + pbi.UVPlaneFragments) {
      reconPixelsPerLine = pbi.UVStride;
      dequantCoeffs = pbi.info.dequant_tables[0][1][pbi.frameQIS[qi]];
      dequantMatrix[0] = pbi.info.dequant_tables[0][1][pbi.frameQIS[0]][0];
    } else {
      reconPixelsPerLine = pbi.UVStride;
      dequantCoeffs = pbi.info.dequant_tables[0][2][pbi.frameQIS[qi]];
      dequantMatrix[0] = pbi.info.dequant_tables[0][2][pbi.frameQIS[0]][0];
    }

    System.arraycopy(dequantCoeffs, 1, dequantMatrix, 1, 63);
    short[] quantizedList = pbi.QFragData[fragmentNumber];

    switch (pbi.FragCoefEOB[fragmentNumber]) {
      case 0, 1 -> idct.IDct1(quantizedList, dequantMatrix, reconDataBuffer);
      case 2, 3, 4, 5, 6, 7, 8, 9, 10 -> idct.IDct10(quantizedList, dequantMatrix, reconDataBuffer);
      default -> idct.IDctSlow(quantizedList, dequantMatrix, reconDataBuffer);
    }

    reconPixelIndex = pbi.recon_pixel_index_table[fragmentNumber];
    Recon.reconIntra(pbi.ThisFrameRecon, reconPixelIndex, reconDataBuffer, reconPixelsPerLine);
  }

  private void expandBlock(Playback pbi, int fragmentNumber) {
    short[] lastFrameRecPtr;
    int reconPixelsPerLine;
    int reconPixelIndex;
    int reconPtr2Offset;
    int mvOffset;
    int mvShiftX;
    int mvShiftY;
    int mvModMaskX;
    int mvModMaskY;
    short[] dequantCoeffs;
    CodingMode codingMode;

    int qi = pbi.FragQs[fragmentNumber];
    if (pbi.getFrameType() == Constants.BASE_FRAME) {
      codingMode = CodingMode.CODE_INTRA;
    } else {
      codingMode = pbi.FragCodingMethod[fragmentNumber];
    }

    if (fragmentNumber < (int) pbi.YPlaneFragments) {
      reconPixelsPerLine = pbi.YStride;
      mvShiftX = 1;
      mvShiftY = 1;
      mvModMaskX = 0x00000001;
      mvModMaskY = 0x00000001;
      if (codingMode == CodingMode.CODE_INTRA) {
        dequantCoeffs = pbi.info.dequant_tables[0][0][pbi.frameQIS[qi]];
        dequantMatrix[0] = pbi.info.dequant_tables[0][0][pbi.frameQIS[0]][0];
      } else {
        dequantCoeffs = pbi.info.dequant_tables[1][0][pbi.frameQIS[qi]];
        dequantMatrix[0] = pbi.info.dequant_tables[1][0][pbi.frameQIS[0]][0];
      }
    } else {
      reconPixelsPerLine = pbi.UVStride;
      mvShiftX = pbi.UVShiftX + 1;
      mvShiftY = pbi.UVShiftY + 1;
      mvModMaskX = 0x00000003;
      mvModMaskY = 0x00000003;
      if (mvShiftX == 1) mvModMaskX = 0x00000001;
      if (mvShiftY == 1) mvModMaskY = 0x00000001;

      if (fragmentNumber < pbi.YPlaneFragments + pbi.UVPlaneFragments) {
        if (codingMode == CodingMode.CODE_INTRA) {
          dequantCoeffs = pbi.info.dequant_tables[0][1][pbi.frameQIS[qi]];
          dequantMatrix[0] = pbi.info.dequant_tables[0][1][pbi.frameQIS[0]][0];
        } else {
          dequantCoeffs = pbi.info.dequant_tables[1][1][pbi.frameQIS[qi]];
          dequantMatrix[0] = pbi.info.dequant_tables[1][1][pbi.frameQIS[0]][0];
        }
      } else {
        if (codingMode == CodingMode.CODE_INTRA) {
          dequantCoeffs = pbi.info.dequant_tables[0][2][pbi.frameQIS[qi]];
          dequantMatrix[0] = pbi.info.dequant_tables[0][2][pbi.frameQIS[0]][0];
        } else {
          dequantCoeffs = pbi.info.dequant_tables[1][2][pbi.frameQIS[qi]];
          dequantMatrix[0] = pbi.info.dequant_tables[1][2][pbi.frameQIS[0]][0];
        }
      }
    }

    System.arraycopy(dequantCoeffs, 1, dequantMatrix, 1, 63);
    short[] quantizedList = pbi.QFragData[fragmentNumber];

    switch (pbi.FragCoefEOB[fragmentNumber]) {
      case 0, 1 -> idct.IDct1(quantizedList, dequantMatrix, reconDataBuffer);
      case 2, 3, 4, 5, 6, 7, 8, 9, 10 -> idct.IDct10(quantizedList, dequantMatrix, reconDataBuffer);
      default -> idct.IDctSlow(quantizedList, dequantMatrix, reconDataBuffer);
    }

    reconPixelIndex = pbi.recon_pixel_index_table[fragmentNumber];

    if (codingMode == CodingMode.CODE_INTER_NO_MV) {
      Recon.reconInter(
          pbi.ThisFrameRecon,
          reconPixelIndex,
          pbi.LastFrameRecon,
          reconPixelIndex,
          reconDataBuffer,
          reconPixelsPerLine);
    } else if (MODE_USES_MC[codingMode.getValue()] != 0) {
      int dir;
      reconPtr2Offset = 0;
      mvOffset = 0;

      dir = pbi.FragMVect[fragmentNumber].x;
      if (dir > 0) {
        mvOffset = dir >> mvShiftX;
        if ((dir & mvModMaskX) != 0) reconPtr2Offset = 1;
      } else if (dir < 0) {
        mvOffset = -((-dir) >> mvShiftX);
        if (((-dir) & mvModMaskX) != 0) reconPtr2Offset = -1;
      }

      dir = pbi.FragMVect[fragmentNumber].y;
      if (dir > 0) {
        mvOffset += (dir >> mvShiftY) * reconPixelsPerLine;
        if ((dir & mvModMaskY) != 0) reconPtr2Offset += reconPixelsPerLine;
      } else if (dir < 0) {
        mvOffset -= ((-dir) >> mvShiftY) * reconPixelsPerLine;
        if (((-dir) & mvModMaskY) != 0) reconPtr2Offset -= reconPixelsPerLine;
      }

      int lastFrameRecOffset = reconPixelIndex + mvOffset;
      if (codingMode == CodingMode.CODE_GOLDEN_MV) {
        lastFrameRecPtr = pbi.GoldenFrame;
      } else {
        lastFrameRecPtr = pbi.LastFrameRecon;
      }

      if (reconPtr2Offset == 0) {
        Recon.reconInter(
            pbi.ThisFrameRecon,
            reconPixelIndex,
            lastFrameRecPtr,
            lastFrameRecOffset,
            reconDataBuffer,
            reconPixelsPerLine);
      } else {
        Recon.reconInterHalfPixel2(
            pbi.ThisFrameRecon,
            reconPixelIndex,
            lastFrameRecPtr,
            lastFrameRecOffset,
            lastFrameRecPtr,
            lastFrameRecOffset + reconPtr2Offset,
            reconDataBuffer,
            reconPixelsPerLine);
      }
    } else if (codingMode == CodingMode.CODE_USING_GOLDEN) {
      Recon.reconInter(
          pbi.ThisFrameRecon,
          reconPixelIndex,
          pbi.GoldenFrame,
          reconPixelIndex,
          reconDataBuffer,
          reconPixelsPerLine);
    } else {
      Recon.reconIntra(pbi.ThisFrameRecon, reconPixelIndex, reconDataBuffer, reconPixelsPerLine);
    }
  }

  private void updateUMVHBorders(Playback pbi, short[] destReconPtr, int planeFragOffset) {
    int i;
    int pixelIndex;
    int planeStride;
    int blockVStep;
    int planeFragments;
    int lineFragments;
    int planeBorderWidth;
    int planeBorderHeight;
    short[] srcPtr1;
    int srcOff1;
    short[] srcPtr2;
    int srcOff2;
    short[] destPtr1;
    int destOff1;
    short[] destPtr2;
    int destOff2;

    if (planeFragOffset == 0) {
      blockVStep = (pbi.YStride * (Constants.VFRAGPIXELS - 1));
      planeStride = pbi.YStride;
      planeBorderWidth = Constants.UMV_BORDER;
      planeBorderHeight = Constants.UMV_BORDER;
      planeFragments = pbi.YPlaneFragments;
      lineFragments = pbi.HFragments;
    } else {
      blockVStep = (pbi.UVStride * (Constants.VFRAGPIXELS - 1));
      planeStride = pbi.UVStride;
      planeBorderWidth = Constants.UMV_BORDER >> pbi.UVShiftX;
      planeBorderHeight = Constants.UMV_BORDER >> pbi.UVShiftY;
      planeFragments = pbi.UVPlaneFragments;
      lineFragments = pbi.HFragments >> pbi.UVShiftX;
    }

    pixelIndex = pbi.recon_pixel_index_table[planeFragOffset];
    srcPtr1 = destReconPtr;
    srcOff1 = pixelIndex - planeBorderWidth;
    destPtr1 = srcPtr1;
    destOff1 = srcOff1 - (planeBorderHeight * planeStride);

    pixelIndex =
        pbi.recon_pixel_index_table[planeFragOffset + planeFragments - lineFragments] + blockVStep;
    srcPtr2 = destReconPtr;
    srcOff2 = pixelIndex - planeBorderWidth;
    destPtr2 = srcPtr2;
    destOff2 = srcOff2 + planeStride;

    for (i = 0; i < planeBorderHeight; i++) {
      System.arraycopy(srcPtr1, srcOff1, destPtr1, destOff1, planeStride);
      System.arraycopy(srcPtr2, srcOff2, destPtr2, destOff2, planeStride);
      destOff1 += planeStride;
      destOff2 += planeStride;
    }
  }

  private void updateUMVVBorders(Playback pbi, short[] destReconPtr, int planeFragOffset) {
    int i;
    int pixelIndex;
    int planeStride;
    int lineFragments;
    int planeBorderWidth;
    int planeHeight;
    short[] srcPtr1;
    int srcOff1;
    short[] srcPtr2;
    int srcOff2;
    short[] destPtr1;
    int destOff1;
    short[] destPtr2;
    int destOff2;

    if (planeFragOffset == 0) {
      planeStride = pbi.YStride;
      planeBorderWidth = Constants.UMV_BORDER;
      lineFragments = pbi.HFragments;
      planeHeight = pbi.info.height;
    } else {
      planeStride = pbi.UVStride;
      planeBorderWidth = Constants.UMV_BORDER >> pbi.UVShiftX;
      lineFragments = pbi.HFragments >> pbi.UVShiftX;
      planeHeight = pbi.info.height >> pbi.UVShiftY;
    }

    pixelIndex = pbi.recon_pixel_index_table[planeFragOffset];
    srcPtr1 = destReconPtr;
    srcOff1 = pixelIndex;
    destPtr1 = destReconPtr;
    destOff1 = pixelIndex - planeBorderWidth;

    pixelIndex =
        pbi.recon_pixel_index_table[planeFragOffset + lineFragments - 1]
            + (Constants.HFRAGPIXELS - 1);
    srcPtr2 = destReconPtr;
    srcOff2 = pixelIndex;
    destPtr2 = destReconPtr;
    destOff2 = pixelIndex + 1;

    for (i = 0; i < planeHeight; i++) {
      MemUtils.set(destPtr1, destOff1, srcPtr1[srcOff1], planeBorderWidth);
      MemUtils.set(destPtr2, destOff2, srcPtr2[srcOff2], planeBorderWidth);
      destOff1 += planeStride;
      destOff2 += planeStride;
      srcOff1 += planeStride;
      srcOff2 += planeStride;
    }
  }

  private void updateUMVBorder(Playback pbi, short[] destReconPtr) {
    int planeFragOffset = 0;
    updateUMVVBorders(pbi, destReconPtr, planeFragOffset);
    updateUMVHBorders(pbi, destReconPtr, planeFragOffset);

    planeFragOffset = pbi.YPlaneFragments;
    updateUMVVBorders(pbi, destReconPtr, planeFragOffset);
    updateUMVHBorders(pbi, destReconPtr, planeFragOffset);

    planeFragOffset = pbi.YPlaneFragments + pbi.UVPlaneFragments;
    updateUMVVBorders(pbi, destReconPtr, planeFragOffset);
    updateUMVHBorders(pbi, destReconPtr, planeFragOffset);
  }

  private void copyRecon(Playback pbi, short[] destReconPtr, short[] srcReconPtr) {
    int i;
    int planeLineStep;
    int pixelIndex;

    planeLineStep = pbi.YStride;
    for (i = 0; i < pbi.YPlaneFragments; i++) {
      if (pbi.display_fragments[i] != 0) {
        pixelIndex = pbi.recon_pixel_index_table[i];
        Recon.copyBlock(srcReconPtr, destReconPtr, pixelIndex, planeLineStep);
      }
    }

    planeLineStep = pbi.UVStride;
    for (i = pbi.YPlaneFragments; i < pbi.UnitFragments; i++) {
      if (pbi.display_fragments[i] != 0) {
        pixelIndex = pbi.recon_pixel_index_table[i];
        Recon.copyBlock(srcReconPtr, destReconPtr, pixelIndex, planeLineStep);
      }
    }
  }

  private void copyNotRecon(Playback pbi, short[] destReconPtr, short[] srcReconPtr) {
    int i;
    int planeLineStep;
    int pixelIndex;

    planeLineStep = pbi.YStride;
    for (i = 0; i < pbi.YPlaneFragments; i++) {
      if (pbi.display_fragments[i] == 0) {
        pixelIndex = pbi.recon_pixel_index_table[i];
        Recon.copyBlock(srcReconPtr, destReconPtr, pixelIndex, planeLineStep);
      }
    }

    planeLineStep = pbi.UVStride;
    for (i = pbi.YPlaneFragments; i < pbi.UnitFragments; i++) {
      if (pbi.display_fragments[i] == 0) {
        pixelIndex = pbi.recon_pixel_index_table[i];
        Recon.copyBlock(srcReconPtr, destReconPtr, pixelIndex, planeLineStep);
      }
    }
  }

  public void expandToken(
      short[] expandedBlock, byte[] coeffIndex, int fragIndex, int token, int extraBits) {
    if (token >= Huffman.DCT_RUN_CATEGORY1) {
      if (token < Huffman.DCT_RUN_CATEGORY2) {
        if (token < Huffman.DCT_RUN_CATEGORY1B) {
          coeffIndex[fragIndex] += (byte) ((token - Huffman.DCT_RUN_CATEGORY1) + 1);
          expandedBlock[coeffIndex[fragIndex]] = (short) -(((extraBits & 0x01) << 1) - 1);
        } else if (token == Huffman.DCT_RUN_CATEGORY1B) {
          coeffIndex[fragIndex] += (6 + (extraBits & 0x03));
          expandedBlock[coeffIndex[fragIndex]] = (short) -(((extraBits & 0x04) >> 1) - 1);
        } else {
          coeffIndex[fragIndex] += (10 + (extraBits & 0x07));
          expandedBlock[coeffIndex[fragIndex]] = (short) -(((extraBits & 0x08) >> 2) - 1);
        }
      } else {
        if (token == Huffman.DCT_RUN_CATEGORY2) {
          coeffIndex[fragIndex] += 1;
          expandedBlock[coeffIndex[fragIndex]] =
              (short) ((2 + (extraBits & 0x01)) * -((extraBits & 0x02) - 1));
        } else {
          coeffIndex[fragIndex] += 2 + (extraBits & 0x01);
          expandedBlock[coeffIndex[fragIndex]] =
              (short) ((2 + ((extraBits & 0x02) >> 1)) * -(((extraBits & 0x04) >> 1) - 1));
        }
      }
      coeffIndex[fragIndex] += 1;
    } else if (token == Huffman.DCT_SHORT_ZRL_TOKEN || token == Huffman.DCT_ZRL_TOKEN) {
      coeffIndex[fragIndex] += extraBits + 1;
    } else if (token < Huffman.LOW_VAL_TOKENS) {
      switch (token) {
        case Huffman.ONE_TOKEN -> expandedBlock[coeffIndex[fragIndex]] = 1;
        case Huffman.MINUS_ONE_TOKEN -> expandedBlock[coeffIndex[fragIndex]] = -1;
        case Huffman.TWO_TOKEN -> expandedBlock[coeffIndex[fragIndex]] = 2;
        case Huffman.MINUS_TWO_TOKEN -> expandedBlock[coeffIndex[fragIndex]] = -2;
      }
      coeffIndex[fragIndex] += 1;
    } else {
      if (token < Huffman.DCT_VAL_CATEGORY3) {
        token = token - Huffman.LOW_VAL_TOKENS;
        expandedBlock[coeffIndex[fragIndex]] =
            (short) ((token + Huffman.DCT_VAL_CAT2_MIN) * -(((extraBits) << 1) - 1));
      } else if (token == Huffman.DCT_VAL_CATEGORY3) {
        expandedBlock[coeffIndex[fragIndex]] =
            (short) ((Huffman.DCT_VAL_CAT3_MIN + (extraBits & 0x01)) * -(((extraBits & 0x02)) - 1));
      } else if (token == Huffman.DCT_VAL_CATEGORY4) {
        expandedBlock[coeffIndex[fragIndex]] =
            (short)
                ((Huffman.DCT_VAL_CAT4_MIN + (extraBits & 0x03))
                    * -(((extraBits & 0x04) >> 1) - 1));
      } else if (token == Huffman.DCT_VAL_CATEGORY5) {
        expandedBlock[coeffIndex[fragIndex]] =
            (short)
                ((Huffman.DCT_VAL_CAT5_MIN + (extraBits & 0x07))
                    * -(((extraBits & 0x08) >> 2) - 1));
      } else if (token == Huffman.DCT_VAL_CATEGORY6) {
        expandedBlock[coeffIndex[fragIndex]] =
            (short)
                ((Huffman.DCT_VAL_CAT6_MIN + (extraBits & 0x0F))
                    * -(((extraBits & 0x10) >> 3) - 1));
      } else if (token == Huffman.DCT_VAL_CATEGORY7) {
        expandedBlock[coeffIndex[fragIndex]] =
            (short)
                ((Huffman.DCT_VAL_CAT7_MIN + (extraBits & 0x1F))
                    * -(((extraBits & 0x20) >> 4) - 1));
      } else if (token == Huffman.DCT_VAL_CATEGORY8) {
        expandedBlock[coeffIndex[fragIndex]] =
            (short)
                ((Huffman.DCT_VAL_CAT8_MIN + (extraBits & 0x1FF))
                    * -(((extraBits & 0x200) >> 8) - 1));
      }
      coeffIndex[fragIndex] += 1;
    }
  }

  public void reconRefFrames(Playback pbi) {
    int i;
    int j, k, m, n;
    int pcount;
    short wpc;
    short predictedDC;
    int fragsAcross = pbi.HFragments;
    int fromFragment;
    int fragsDown = pbi.VFragments;
    int whichFrame;
    int whichCase;
    boolean isBaseFrame;

    isBaseFrame = pbi.getFrameType() == Constants.BASE_FRAME;
    pbi.filter.setupLoopFilter(pbi.FrameQIndex);

    for (j = 0; j < 3; j++) {
      switch (j) {
        case 0 -> {
          fromFragment = 0;
          fragsAcross = pbi.HFragments;
          fragsDown = pbi.VFragments;
        }
        case 1 -> {
          fromFragment = pbi.YPlaneFragments;
          fragsAcross = pbi.HFragments >> pbi.UVShiftX;
          fragsDown = pbi.VFragments >> pbi.UVShiftY;
        }
        default -> {
          fromFragment = pbi.YPlaneFragments + pbi.UVPlaneFragments;
          fragsAcross = pbi.HFragments >> pbi.UVShiftX;
          fragsDown = pbi.VFragments >> pbi.UVShiftY;
        }
      }

      for (k = 0; k < 3; k++) last[k] = 0;

      i = fromFragment;
      for (m = 0; m < fragsDown; m++) {
        for (n = 0; n < fragsAcross; n++, i++) {
          if ((pbi.display_fragments[i] != 0) || (pbi.getFrameType() == Constants.BASE_FRAME)) {
            whichFrame = MODE_2_FRAME[pbi.FragCodingMethod[i].getValue()];
            whichCase =
                (n == 0 ? 1 : 0) + ((m == 0 ? 1 : 0) << 1) + ((n + 1 == fragsAcross ? 1 : 0) << 2);
            fn[0] = i - 1;
            fn[1] = i - fragsAcross - 1;
            fn[2] = i - fragsAcross;
            fn[3] = i - fragsAcross + 1;

            for (k = pcount = wpc = 0; k < 4; k++) {
              int pflag = 1 << k;
              if ((BC_MASK[whichCase] & pflag) != 0
                  && pbi.display_fragments[fn[k]] != 0
                  && (MODE_2_FRAME[pbi.FragCodingMethod[fn[k]].getValue()] == whichFrame)) {
                v[pcount] = pbi.QFragData[fn[k]][0];
                wpc |= pflag;
                pcount++;
              }
            }

            if (wpc == 0) {
              pbi.QFragData[i][0] += last[whichFrame];
            } else {
              predictedDC = (short) (PC[wpc][0] * v[0]);
              for (k = 1; k < pcount; k++) {
                predictedDC += PC[wpc][k] * v[k];
              }
              if (PC[wpc][4] != 0) {
                if (predictedDC < 0) predictedDC += PC[wpc][5];
                predictedDC >>= PC[wpc][4];
              }
              if ((wpc & (PU | PUL | PL)) == (PU | PUL | PL)) {
                if (Math.abs(predictedDC - v[2]) > 128) {
                  predictedDC = (short) v[2];
                } else if (Math.abs(predictedDC - v[0]) > 128) {
                  predictedDC = (short) v[0];
                } else if (Math.abs(predictedDC - v[1]) > 128) {
                  predictedDC = (short) v[1];
                }
              }
              pbi.QFragData[i][0] += predictedDC;
            }

            last[whichFrame] = pbi.QFragData[i][0];
            if (isBaseFrame) expandKfBlock(pbi, i);
            else expandBlock(pbi, i);
          }
        }
      }
    }

    if (pbi.CodedBlockIndex > (int) (pbi.UnitFragments >> 1)) {
      short[] swapReconBuffersTemp = pbi.ThisFrameRecon;
      pbi.ThisFrameRecon = pbi.LastFrameRecon;
      pbi.LastFrameRecon = swapReconBuffersTemp;
      copyNotRecon(pbi, pbi.LastFrameRecon, pbi.ThisFrameRecon);
    } else {
      copyRecon(pbi, pbi.LastFrameRecon, pbi.ThisFrameRecon);
    }

    pbi.filter.loopFilter(pbi);
    updateUMVBorder(pbi, pbi.LastFrameRecon);
    if (isBaseFrame) {
      copyRecon(pbi, pbi.GoldenFrame, pbi.LastFrameRecon);
      updateUMVBorder(pbi, pbi.GoldenFrame);
    }
  }
}
