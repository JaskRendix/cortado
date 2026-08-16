/* Jheora
 * Copyright (C) 2004 Fluendo S.L.
 *
 * Written by: 2004 Wim Taymans <wim@fluendo.com>
 *
 * Parts ported from the new Theora C reference encoder, which was mostly
 * written by Timothy B. Terriberry
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

import com.fluendo.utils.*;
import com.jcraft.jogg.Buffer;

interface ExtractMVectorComponent {
  int extract(Buffer opb);
}

class ExtractMVectorComponentA implements ExtractMVectorComponent {
  @Override
  public int extract(Buffer opb) {
    return switch (opb.readB(3)) {
      case 0 -> 0;
      case 1 -> 1;
      case 2 -> -1;
      case 3 -> 2 - (4 * opb.readB(1));
      case 4 -> 3 - (6 * opb.readB(1));
      case 5 -> (4 + opb.readB(2)) * -((opb.readB(1) << 1) - 1);
      case 6 -> (8 + opb.readB(3)) * -((opb.readB(1) << 1) - 1);
      case 7 -> (16 + opb.readB(4)) * -((opb.readB(1) << 1) - 1);
      default -> 0;
    };
  }
}

class ExtractMVectorComponentB implements ExtractMVectorComponent {
  @Override
  public int extract(Buffer opb) {
    return (opb.readB(5)) * -((opb.readB(1) << 1) - 1);
  }
}

public final class Decode {
  private static final ExtractMVectorComponent MVA = new ExtractMVectorComponentA();
  private static final ExtractMVectorComponent MVB = new ExtractMVectorComponentB();

  private static final CodingMode[][] modeAlphabet = {
    {
      CodingMode.CODE_INTER_LAST_MV, CodingMode.CODE_INTER_PRIOR_LAST,
      CodingMode.CODE_INTER_PLUS_MV, CodingMode.CODE_INTER_NO_MV,
      CodingMode.CODE_INTRA, CodingMode.CODE_USING_GOLDEN,
      CodingMode.CODE_GOLDEN_MV, CodingMode.CODE_INTER_FOURMV
    },
    {
      CodingMode.CODE_INTER_LAST_MV, CodingMode.CODE_INTER_PRIOR_LAST,
      CodingMode.CODE_INTER_NO_MV, CodingMode.CODE_INTER_PLUS_MV,
      CodingMode.CODE_INTRA, CodingMode.CODE_USING_GOLDEN,
      CodingMode.CODE_GOLDEN_MV, CodingMode.CODE_INTER_FOURMV
    },
    {
      CodingMode.CODE_INTER_LAST_MV, CodingMode.CODE_INTER_PLUS_MV,
      CodingMode.CODE_INTER_PRIOR_LAST, CodingMode.CODE_INTER_NO_MV,
      CodingMode.CODE_INTRA, CodingMode.CODE_USING_GOLDEN,
      CodingMode.CODE_GOLDEN_MV, CodingMode.CODE_INTER_FOURMV
    },
    {
      CodingMode.CODE_INTER_LAST_MV, CodingMode.CODE_INTER_PLUS_MV,
      CodingMode.CODE_INTER_NO_MV, CodingMode.CODE_INTER_PRIOR_LAST,
      CodingMode.CODE_INTRA, CodingMode.CODE_USING_GOLDEN,
      CodingMode.CODE_GOLDEN_MV, CodingMode.CODE_INTER_FOURMV
    },
    {
      CodingMode.CODE_INTER_NO_MV, CodingMode.CODE_INTER_LAST_MV,
      CodingMode.CODE_INTER_PRIOR_LAST, CodingMode.CODE_INTER_PLUS_MV,
      CodingMode.CODE_INTRA, CodingMode.CODE_USING_GOLDEN,
      CodingMode.CODE_GOLDEN_MV, CodingMode.CODE_INTER_FOURMV
    },
    {
      CodingMode.CODE_INTER_NO_MV, CodingMode.CODE_USING_GOLDEN,
      CodingMode.CODE_INTER_LAST_MV, CodingMode.CODE_INTER_PRIOR_LAST,
      CodingMode.CODE_INTER_PLUS_MV, CodingMode.CODE_INTRA,
      CodingMode.CODE_GOLDEN_MV, CodingMode.CODE_INTER_FOURMV
    },
    {
      CodingMode.CODE_INTER_NO_MV, CodingMode.CODE_USING_GOLDEN,
      CodingMode.CODE_INTER_LAST_MV, CodingMode.CODE_INTER_PRIOR_LAST,
      CodingMode.CODE_INTER_PLUS_MV, CodingMode.CODE_INTRA,
      CodingMode.CODE_GOLDEN_MV, CodingMode.CODE_INTER_FOURMV
    }
  };

  private int blocksToDecode;
  private int eobRun;
  private final DCTDecode dctDecode = new DCTDecode();
  private final byte[] fragCoeffs;
  private MotionVector lastInterMV = new MotionVector();
  private MotionVector priorLastInterMV = new MotionVector();
  private final Playback pbi;

  public Decode(Playback pbi) {
    this.fragCoeffs = new byte[pbi.UnitFragments];
    this.pbi = pbi;
  }

  private int longRunBitStringDecode() {
    Buffer opb = pbi.opb;
    int bits = opb.readB(1);
    if (bits == 0) return 1;

    bits = opb.readB(2);
    if ((bits & 2) == 0) return 2 + bits;
    if ((bits & 1) == 0) {
      bits = opb.readB(1);
      return 4 + bits;
    }

    bits = opb.readB(3);
    if ((bits & 4) == 0) return 6 + bits;
    if ((bits & 2) == 0) {
      int ret = 10 + ((bits & 1) << 2);
      bits = opb.readB(2);
      return ret + bits;
    }
    if ((bits & 1) == 0) {
      bits = opb.readB(4);
      return 18 + bits;
    }

    bits = opb.readB(12);
    return 34 + bits;
  }

  private void decodeBlockLevelQi() {
    int nCodedFrags = pbi.CodedBlockIndex;
    if (nCodedFrags <= 0) return;

    if (pbi.frameNQIS == 1) {
      for (int codedFrag = 0; codedFrag < nCodedFrags; ++codedFrag) {
        pbi.FragQs[pbi.CodedBlockList[codedFrag]] = 0;
      }
    } else {
      Buffer opb = pbi.opb;
      int val = opb.readB(1);
      int flag = val;
      int nqi0 = 0;
      int codedFrag = 0;

      while (codedFrag < nCodedFrags) {
        int runCount = longRunBitStringDecode();
        boolean fullRun = (runCount >= 4129);
        do {
          pbi.FragQs[pbi.CodedBlockList[codedFrag++]] = (byte) flag;
          if (flag < 1) ++nqi0;
        } while (--runCount > 0 && codedFrag < nCodedFrags);

        if (fullRun && codedFrag < nCodedFrags) {
          val = opb.readB(1);
          flag = (int) val;
        } else {
          flag = (flag != 0) ? 0 : 1;
        }
      }

      if (pbi.frameNQIS == 3 && nqi0 < nCodedFrags) {
        codedFrag = 0;
        while (codedFrag < nCodedFrags && pbi.FragQs[pbi.CodedBlockList[codedFrag]] == 0) {
          ++codedFrag;
        }
        val = opb.readB(1);
        flag = val;

        while (codedFrag < nCodedFrags) {
          int runCount = longRunBitStringDecode();
          boolean fullRun = runCount >= 4129;
          for (; codedFrag < nCodedFrags; ++codedFrag) {
            if (pbi.FragQs[pbi.CodedBlockList[codedFrag]] == 0) continue;
            if (runCount-- <= 0) break;
            pbi.FragQs[pbi.CodedBlockList[codedFrag]] += (byte) flag;
          }
          if (fullRun && codedFrag < nCodedFrags) {
            val = opb.readB(1);
            flag = val;
          } else {
            flag = (flag != 0) ? 0 : 1;
          }
        }
      }
    }
  }

  private int loadFrame() {
    Buffer opb = pbi.opb;
    pbi.FrameType = (byte) opb.readB(1);
    int dctQMask = (int) opb.readB(6);
    pbi.frameQIS[0] = dctQMask;
    pbi.frameNQIS = 1;

    int moreQs = opb.readB(1);
    if (moreQs > 0) {
      pbi.frameQIS[1] = (int) opb.readB(6);
      pbi.frameNQIS = 2;
      moreQs = opb.readB(1);
      if (moreQs > 0) {
        pbi.frameQIS[2] = (int) opb.readB(6);
        pbi.frameNQIS = 3;
      }
    }

    if (pbi.FrameType == Constants.BASE_FRAME) {
      pbi.KeyFrameType = (byte) opb.readB(1);
      opb.readB(2);
    }

    pbi.frArray.quadDecodeDisplayFragments(pbi);
    return 1;
  }

  private void decodeModes(int sbRows, int sbCols) {
    CodingMode[] fragCodingMethod = pbi.FragCodingMethod;

    if (pbi.getFrameType() == Constants.BASE_FRAME) {
      MemUtils.set(fragCodingMethod, 0, CodingMode.CODE_INTRA, pbi.UnitFragments);
    } else {
      MemUtils.set(fragCodingMethod, 0, CodingMode.CODE_INTER_NO_MV, pbi.UnitFragments);
      CodingMode[] modeList;
      long ret = pbi.opb.readB(Constants.MODE_METHOD_BITS);
      int codingScheme = (int) ret;

      if (codingScheme == 0) {
        CodingMode[] customModeAlphabet = new CodingMode[Constants.MAX_MODES];
        for (int i = 0; i < Constants.MAX_MODES; i++) {
          ret = pbi.opb.readB(Constants.MODE_BITS);
          customModeAlphabet[(int) ret] = CodingMode.MODES[i];
        }
        modeList = customModeAlphabet;
      } else {
        modeList = modeAlphabet[codingScheme - 1];
      }

      int sb = 0;
      int mbListIndex = 0;
      for (int sbRow = 0; sbRow < sbRows; sbRow++) {
        for (int sbCol = 0; sbCol < sbCols; sbCol++) {
          for (int mb = 0; mb < 4; mb++) {
            int fragIndex = pbi.BlockMap.quadMapToMBTopLeft(sb, mb);
            if (fragIndex >= 0) {
              if (pbi.MBCodedFlags[mbListIndex++] != 0) {
                CodingMode codingMethod;
                if (codingScheme == (Constants.MODE_METHODS - 1)) {
                  ret = pbi.opb.readB(Constants.MODE_BITS);
                  codingMethod = CodingMode.MODES[(int) ret];
                } else {
                  CodingMode modeEntry = pbi.frArray.unpackMode(pbi.opb);
                  codingMethod = modeList[modeEntry.getValue()];
                }

                fragCodingMethod[fragIndex] = codingMethod;
                fragCodingMethod[fragIndex + 1] = codingMethod;
                fragCodingMethod[fragIndex + pbi.HFragments] = codingMethod;
                fragCodingMethod[fragIndex + pbi.HFragments + 1] = codingMethod;

                if (pbi.UVShiftX == 1 && pbi.UVShiftY == 1) {
                  int uvRow = (fragIndex / (pbi.HFragments * 2));
                  int uvColumn = (fragIndex % pbi.HFragments) / 2;
                  int uvFragOffset = (uvRow * (pbi.HFragments / 2)) + uvColumn;
                  fragCodingMethod[pbi.YPlaneFragments + uvFragOffset] = codingMethod;
                  fragCodingMethod[pbi.YPlaneFragments + pbi.UVPlaneFragments + uvFragOffset] =
                      codingMethod;
                } else if (pbi.UVShiftX == 0) {
                  int tempIdx = fragIndex + pbi.YPlaneFragments;
                  fragCodingMethod[tempIdx] =
                      fragCodingMethod[tempIdx + 1] =
                          fragCodingMethod[tempIdx + pbi.HFragments] =
                              fragCodingMethod[tempIdx + pbi.HFragments + 1] = codingMethod;
                  tempIdx += pbi.UVPlaneFragments;
                  fragCodingMethod[tempIdx] =
                      fragCodingMethod[tempIdx + 1] =
                          fragCodingMethod[tempIdx + pbi.HFragments] =
                              fragCodingMethod[tempIdx + pbi.HFragments + 1] = codingMethod;
                } else {
                  int tempIdx = pbi.YPlaneFragments + fragIndex / 2;
                  fragCodingMethod[tempIdx] =
                      fragCodingMethod[tempIdx + pbi.HFragments / 2] = codingMethod;
                  tempIdx += pbi.UVPlaneFragments;
                  fragCodingMethod[tempIdx] =
                      fragCodingMethod[tempIdx + pbi.HFragments / 2] = codingMethod;
                }
              }
            }
          }
          sb++;
        }
      }
    }
  }

  private void decodeMVectors(int sbRows, int sbCols) {
    Buffer opb = pbi.opb;
    if (pbi.getFrameType() == Constants.BASE_FRAME) {
      return;
    }

    MotionVector dummy = new MotionVector();
    lastInterMV.x = 0;
    lastInterMV.y = 0;
    priorLastInterMV.x = 0;
    priorLastInterMV.y = 0;

    ExtractMVectorComponent mvc = (opb.readB(1) == 0) ? MVA : MVB;

    int sb = 0;
    int mbListIndex = 0;
    for (int sbRow = 0; sbRow < sbRows; sbRow++) {
      for (int sbCol = 0; sbCol < sbCols; sbCol++) {
        for (int mb = 0; mb < 4; mb++) {
          int fragIndex = pbi.BlockMap.quadMapToMBTopLeft(sb, mb);
          if (fragIndex >= 0) {
            if (pbi.MBCodedFlags[mbListIndex++] != 0) {
              CodingMode codingMethod = pbi.FragCodingMethod[fragIndex];
              MotionVector mvect0 = pbi.FragMVect[fragIndex];
              MotionVector mvect1 = pbi.FragMVect[fragIndex + 1];
              MotionVector mvect2 = pbi.FragMVect[fragIndex + pbi.HFragments];
              MotionVector mvect3 = pbi.FragMVect[fragIndex + pbi.HFragments + 1];

              int uvRow = (fragIndex / (pbi.HFragments << pbi.UVShiftY));
              int uvColumn = (fragIndex % pbi.HFragments) >> pbi.UVShiftX;
              int uvFragOffset = (uvRow * (pbi.HFragments >> pbi.UVShiftX)) + uvColumn;

              MotionVector mvectU0 = pbi.FragMVect[pbi.YPlaneFragments + uvFragOffset];
              MotionVector mvectV0 =
                  pbi.FragMVect[pbi.YPlaneFragments + pbi.UVPlaneFragments + uvFragOffset];
              MotionVector mvectU1 = dummy;
              MotionVector mvectV1 = dummy;
              MotionVector mvectU2 = dummy;
              MotionVector mvectV2 = dummy;
              MotionVector mvectU3 = dummy;
              MotionVector mvectV3 = dummy;

              if (pbi.UVShiftY == 0) {
                mvectU2 =
                    pbi.FragMVect[
                        pbi.YPlaneFragments + uvFragOffset + (pbi.HFragments >> pbi.UVShiftX)];
                mvectV2 =
                    pbi.FragMVect[
                        pbi.YPlaneFragments
                            + pbi.UVPlaneFragments
                            + uvFragOffset
                            + (pbi.HFragments >> pbi.UVShiftX)];
                if (pbi.UVShiftX == 0) {
                  mvectU1 = pbi.FragMVect[pbi.YPlaneFragments + uvFragOffset + 1];
                  mvectV1 =
                      pbi.FragMVect[pbi.YPlaneFragments + pbi.UVPlaneFragments + uvFragOffset + 1];
                  mvectU3 = pbi.FragMVect[pbi.YPlaneFragments + uvFragOffset + pbi.HFragments + 1];
                  mvectV3 =
                      pbi.FragMVect[
                          pbi.YPlaneFragments
                              + pbi.UVPlaneFragments
                              + uvFragOffset
                              + pbi.HFragments
                              + 1];
                }
              }

              if (codingMethod == CodingMode.CODE_INTER_PLUS_MV) {
                priorLastInterMV.x = lastInterMV.x;
                priorLastInterMV.y = lastInterMV.y;

                int extractedX = mvc.extract(opb);
                int extractedY = mvc.extract(opb);

                lastInterMV.x =
                    mvect0.x =
                        mvect1.x =
                            mvect2.x =
                                mvect3.x =
                                    mvectU0.x =
                                        mvectV0.x =
                                            mvectU1.x =
                                                mvectV1.x =
                                                    mvectU2.x =
                                                        mvectV2.x =
                                                            mvectU3.x = mvectV3.x = extractedX;

                lastInterMV.y =
                    mvect0.y =
                        mvect1.y =
                            mvect2.y =
                                mvect3.y =
                                    mvectU0.y =
                                        mvectV0.y =
                                            mvectU1.y =
                                                mvectV1.y =
                                                    mvectU2.y =
                                                        mvectV2.y =
                                                            mvectU3.y = mvectV3.y = extractedY;

              } else if (codingMethod == CodingMode.CODE_GOLDEN_MV) {
                int extractedX = mvc.extract(opb);
                int extractedY = mvc.extract(opb);

                mvect0.x =
                    mvect1.x =
                        mvect2.x =
                            mvect3.x =
                                mvectU0.x =
                                    mvectV0.x =
                                        mvectU1.x =
                                            mvectV1.x =
                                                mvectU2.x =
                                                    mvectV2.x = mvectU3.x = mvectV3.x = extractedX;

                mvect0.y =
                    mvect1.y =
                        mvect2.y =
                            mvect3.y =
                                mvectU0.y =
                                    mvectV0.y =
                                        mvectU1.y =
                                            mvectV1.y =
                                                mvectU2.y =
                                                    mvectV2.y = mvectU3.y = mvectV3.y = extractedY;

              } else if (codingMethod == CodingMode.CODE_INTER_FOURMV) {
                priorLastInterMV.x = lastInterMV.x;
                priorLastInterMV.y = lastInterMV.y;

                int x = 0, y = 0;
                if (pbi.display_fragments[fragIndex] != 0) {
                  x = mvect0.x = mvc.extract(opb);
                  y = mvect0.y = mvc.extract(opb);
                  lastInterMV.x = mvect0.x;
                  lastInterMV.y = mvect0.y;
                } else {
                  mvect0.x = 0;
                  mvect0.y = 0;
                }

                if (pbi.display_fragments[fragIndex + 1] != 0) {
                  x += mvect1.x = mvc.extract(opb);
                  y += mvect1.y = mvc.extract(opb);
                  lastInterMV.x = mvect1.x;
                  lastInterMV.y = mvect1.y;
                } else {
                  mvect1.x = 0;
                  mvect1.y = 0;
                }

                if (pbi.display_fragments[fragIndex + pbi.HFragments] != 0) {
                  x += mvect2.x = mvc.extract(opb);
                  y += mvect2.y = mvc.extract(opb);
                  lastInterMV.x = mvect2.x;
                  lastInterMV.y = mvect2.y;
                } else {
                  mvect2.x = 0;
                  mvect2.y = 0;
                }

                if (pbi.display_fragments[fragIndex + pbi.HFragments + 1] != 0) {
                  x += mvect3.x = mvc.extract(opb);
                  y += mvect3.y = mvc.extract(opb);
                  lastInterMV.x = mvect3.x;
                  lastInterMV.y = mvect3.y;
                } else {
                  mvect3.x = 0;
                  mvect3.y = 0;
                }

                if (pbi.UVShiftY == 0) {
                  if (pbi.UVShiftX == 0) {
                    mvectU0.x = mvectV0.x = mvect0.x;
                    mvectU0.y = mvectV0.y = mvect0.y;
                    mvectU1.x = mvectV1.x = mvect1.x;
                    mvectU1.y = mvectV1.y = mvect1.y;
                    mvectU2.x = mvectV2.x = mvect2.x;
                    mvectU2.y = mvectV2.y = mvect2.y;
                    mvectU3.x = mvectV3.x = mvect3.x;
                    mvectU3.y = mvectV3.y = mvect3.y;
                  } else {
                    int ux0 = mvect0.x + mvect1.x;
                    ux0 = (ux0 >= 0) ? (ux0 + 1) / 2 : (ux0 - 1) / 2;
                    mvectU0.x = mvectV0.x = ux0;

                    int uy0 = mvect0.y + mvect1.y;
                    uy0 = (uy0 >= 0) ? (uy0 + 1) / 2 : (uy0 - 1) / 2;
                    mvectU0.y = mvectV0.y = uy0;

                    int ux2 = mvect2.x + mvect3.x;
                    ux2 = (ux2 >= 0) ? (ux2 + 1) / 2 : (ux2 - 1) / 2;
                    mvectU2.x = mvectV2.x = ux2;

                    int uy2 = mvect2.y + mvect3.y;
                    uy2 = (uy2 >= 0) ? (uy2 + 1) / 2 : (uy2 - 1) / 2;
                    mvectU2.y = mvectV2.y = uy2;
                  }
                } else {
                  x = (x >= 0) ? (x + 2) / 4 : (x - 2) / 4;
                  mvectU0.x = mvectV0.x = x;
                  y = (y >= 0) ? (y + 2) / 4 : (y - 2) / 4;
                  mvectU0.y = mvectV0.y = y;
                }
              } else if (codingMethod == CodingMode.CODE_INTER_LAST_MV) {
                mvect0.x =
                    mvect1.x =
                        mvect2.x =
                            mvect3.x =
                                mvectU0.x =
                                    mvectV0.x =
                                        mvectU1.x =
                                            mvectV1.x =
                                                mvectU2.x =
                                                    mvectV2.x =
                                                        mvectU3.x = mvectV3.x = lastInterMV.x;

                mvect0.y =
                    mvect1.y =
                        mvect2.y =
                            mvect3.y =
                                mvectU0.y =
                                    mvectV0.y =
                                        mvectU1.y =
                                            mvectV1.y =
                                                mvectU2.y =
                                                    mvectV2.y =
                                                        mvectU3.y = mvectV3.y = lastInterMV.y;
              } else if (codingMethod == CodingMode.CODE_INTER_PRIOR_LAST) {
                mvect0.x =
                    mvect1.x =
                        mvect2.x =
                            mvect3.x =
                                mvectU0.x =
                                    mvectV0.x =
                                        mvectU1.x =
                                            mvectV1.x =
                                                mvectU2.x =
                                                    mvectV2.x =
                                                        mvectU3.x = mvectV3.x = priorLastInterMV.x;

                mvect0.y =
                    mvect1.y =
                        mvect2.y =
                            mvect3.y =
                                mvectU0.y =
                                    mvectV0.y =
                                        mvectU1.y =
                                            mvectV1.y =
                                                mvectU2.y =
                                                    mvectV2.y =
                                                        mvectU3.y = mvectV3.y = priorLastInterMV.y;

                MotionVector tmpMVect = priorLastInterMV;
                priorLastInterMV = lastInterMV;
                lastInterMV = tmpMVect;
              } else {
                mvect0.x = 0;
                mvect0.y = 0;
              }
            }
          }
        }
        sb++;
      }
    }
  }

  private int extractToken(Buffer opb, HuffEntry currentRoot) {
    while (currentRoot.value < 0) {
      currentRoot = currentRoot.child[opb.readB(1)];
    }
    return currentRoot.value;
  }

  private void unpackAndExpandToken(
      short[] expandedBlock, byte[] coeffIndex, int fragIndex, int huffChoice) {
    int extraBits = 0;
    int token = extractToken(pbi.opb, pbi.HuffRoot_VP3x[huffChoice]);
    if (pbi.ExtraBitLengths_VP3x[token] > 0) {
      extraBits = (int) pbi.opb.readB(pbi.ExtraBitLengths_VP3x[token]);
    }
    if (token >= Huffman.DCT_SHORT_ZRL_TOKEN) {
      dctDecode.expandToken(expandedBlock, coeffIndex, fragIndex, token, extraBits);
      if (coeffIndex[fragIndex] >= Constants.BLOCK_SIZE) blocksToDecode--;
    } else {
      switch (token) {
        case Huffman.DCT_EOB_PAIR_TOKEN -> eobRun = 1;
        case Huffman.DCT_EOB_TRIPLE_TOKEN -> eobRun = 2;
        case Huffman.DCT_REPEAT_RUN_TOKEN -> eobRun = extraBits + 3;
        case Huffman.DCT_REPEAT_RUN2_TOKEN -> eobRun = extraBits + 7;
        case Huffman.DCT_REPEAT_RUN3_TOKEN -> eobRun = extraBits + 15;
        case Huffman.DCT_REPEAT_RUN4_TOKEN -> eobRun = extraBits - 1;
        case Huffman.DCT_EOB_TOKEN -> {}
        default -> {
          return;
        }
      }
      coeffIndex[fragIndex] = Constants.BLOCK_SIZE;
      blocksToDecode--;
    }
  }

  private void unPackVideo() {
    if (pbi.DecoderErrorCode != 0) return;

    MemUtils.set(fragCoeffs, (byte) 0, (byte) 0, pbi.UnitFragments);
    MemUtils.set(pbi.FragCoefEOB, (byte) 0, (byte) 0, pbi.UnitFragments);
    blocksToDecode = pbi.CodedBlockIndex;

    int dcHuffChoice1 = (int) (pbi.opb.readB(Huffman.DC_HUFF_CHOICE_BITS) + Huffman.DC_HUFF_OFFSET);
    int dcHuffChoice2 = (int) (pbi.opb.readB(Huffman.DC_HUFF_CHOICE_BITS) + Huffman.DC_HUFF_OFFSET);

    int cbl = 0;
    int cble = pbi.CodedBlockIndex;
    while (cbl < cble) {
      int fragIndex = pbi.CodedBlockList[cbl];
      pbi.FragCoefEOB[fragIndex] = fragCoeffs[fragIndex];
      int dcHuffChoice = (fragIndex < (int) pbi.YPlaneFragments) ? dcHuffChoice1 : dcHuffChoice2;

      if (eobRun != 0) {
        fragCoeffs[fragIndex] = Constants.BLOCK_SIZE;
        eobRun--;
        blocksToDecode--;
      } else {
        unpackAndExpandToken(pbi.QFragData[fragIndex], fragCoeffs, fragIndex, dcHuffChoice);
      }
      cbl++;
    }

    int acHuffIndex1 = (int) (pbi.opb.readB(Huffman.AC_HUFF_CHOICE_BITS) + Huffman.AC_HUFF_OFFSET);
    int acHuffIndex2 = (int) (pbi.opb.readB(Huffman.AC_HUFF_CHOICE_BITS) + Huffman.AC_HUFF_OFFSET);
    int encodedCoeffs = 1;

    while (encodedCoeffs < 64) {
      cbl = 0;
      cble = pbi.CodedBlockIndex;

      int acHuffChoice1;
      int acHuffChoice2;
      if (encodedCoeffs <= Huffman.AC_TABLE_2_THRESH) {
        acHuffChoice1 = acHuffIndex1;
        acHuffChoice2 = acHuffIndex2;
      } else if (encodedCoeffs <= Huffman.AC_TABLE_3_THRESH) {
        acHuffChoice1 = acHuffIndex1 + Huffman.AC_HUFF_CHOICES;
        acHuffChoice2 = acHuffIndex2 + Huffman.AC_HUFF_CHOICES;
      } else if (encodedCoeffs <= Huffman.AC_TABLE_4_THRESH) {
        acHuffChoice1 = acHuffIndex1 + (Huffman.AC_HUFF_CHOICES * 2);
        acHuffChoice2 = acHuffIndex2 + (Huffman.AC_HUFF_CHOICES * 2);
      } else {
        acHuffChoice1 = acHuffIndex1 + (Huffman.AC_HUFF_CHOICES * 3);
        acHuffChoice2 = acHuffIndex2 + (Huffman.AC_HUFF_CHOICES * 3);
      }

      while (cbl < cble) {
        int fragIndex = pbi.CodedBlockList[cbl];
        if (fragCoeffs[fragIndex] <= encodedCoeffs) {
          pbi.FragCoefEOB[fragIndex] = fragCoeffs[fragIndex];
          if (eobRun != 0) {
            fragCoeffs[fragIndex] = Constants.BLOCK_SIZE;
            eobRun--;
            blocksToDecode--;
          } else {
            int acHuffChoice =
                (fragIndex < (int) pbi.YPlaneFragments) ? acHuffChoice1 : acHuffChoice2;
            unpackAndExpandToken(pbi.QFragData[fragIndex], fragCoeffs, fragIndex, acHuffChoice);
          }
        }
        cbl++;
      }
      if (blocksToDecode == 0) break;
      encodedCoeffs++;
    }
  }

  public int loadAndDecode() {
    int loadFrameOK = loadFrame();
    if (loadFrameOK != 0) {
      if (pbi.DecoderErrorCode != 0) return 0;
      eobRun = 0;
      pbi.CodedBlocksThisFrame = pbi.CodedBlockIndex;
      decodeModes(pbi.YSBRows, pbi.YSBCols);
      decodeMVectors(pbi.YSBRows, pbi.YSBCols);
      decodeBlockLevelQi();
      unPackVideo();
      dctDecode.reconRefFrames(pbi);
      return 0;
    }
    return Result.BADPACKET;
  }
}
