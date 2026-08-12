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

import java.util.Arrays;

public final class Filter {
    /* in-loop filter tables. one of these is used in dct_decode.c */
    private static final byte[] LOOP_FILTER_LIMIT_VALUES_V1 = {
        30, 25, 20, 20, 15, 15, 14, 14,
        13, 13, 12, 12, 11, 11, 10, 10,
        9,  9,  8,  8,  7,  7,  7,  7,
        6,  6,  6,  6,  5,  5,  5,  5,
        4,  4,  4,  4,  3,  3,  3,  3,
        2,  2,  2,  2,  2,  2,  2,  2,
        0,  0,  0,  0,  0,  0,  0,  0,
        0,  0,  0,  0,  0,  0,  0,  0
    };

    /* Loop filter bounding values */
    private final byte[] loopFilterLimits = new byte[Constants.Q_TABLE_SIZE];
    private final int[] filtBoundingValue = new int[512];

    private void setupBoundingValueArrayGeneric(int fLimit) {
        /* Set up the bounding value array. */
        Arrays.fill(filtBoundingValue, 0); // Replaced MemUtils.set(FiltBoundingValue, 0, 0, 512)
        for (int i = 0; i < fLimit; i++) {
            filtBoundingValue[256 - i - fLimit] = (-fLimit + i);
            filtBoundingValue[256 - i] = -i;
            filtBoundingValue[256 + i] = i;
            filtBoundingValue[256 + i + fLimit] = fLimit - i;
        }
    }

    /* copy in-loop filter limits from the bitstream header into our instance */
    public void copyFilterTables(Info ci) {
        System.arraycopy(ci.LoopFilterLimitValues, 0, loopFilterLimits, 0, Constants.Q_TABLE_SIZE);
    }

    /* initialize the filter limits from our static table */
    public void initFilterTables() {
        System.arraycopy(LOOP_FILTER_LIMIT_VALUES_V1, 0, loopFilterLimits, 0, Constants.Q_TABLE_SIZE);
    }

    public void setupLoopFilter(int frameQIndex) {
        /* nb: this was using the V2 values rather than V1
           we think is was a mistake; the results were not used */
        int fLimit = loopFilterLimits[frameQIndex];
        setupBoundingValueArrayGeneric(fLimit);
    }

    private static short clamp255(int val) {
        return (short) ((~(val >> 31)) & 255 & (val | ((255 - val) >> 31)));
    }

    private void filterHoriz(short[] pixelPtr, int idx, int lineLength, int[] boundingValuePtr) {
        for (int j = 0; j < 8; j++) {
            int filtVal = (pixelPtr[0 + idx]) -
                          (pixelPtr[1 + idx] * 3) +
                          (pixelPtr[2 + idx] * 3) -
                          (pixelPtr[3 + idx]);

            filtVal = boundingValuePtr[256 + ((filtVal + 4) >> 3)];

            pixelPtr[1 + idx] = clamp255(pixelPtr[1 + idx] + filtVal);
            pixelPtr[2 + idx] = clamp255(pixelPtr[2 + idx] - filtVal);

            idx += lineLength;
        }
    }

    private void filterVert(short[] pixelPtr, int idx, int lineLength, int[] boundingValuePtr) {
        /* the math was correct, but negative array indicies are forbidden
           by ANSI/C99 and will break optimization on several modern
           compilers */
        idx -= 2 * lineLength;

        for (int j = 0; j < 8; j++) {
            int filtVal = (pixelPtr[idx + 0]) -
                          (pixelPtr[idx + lineLength] * 3) +
                          (pixelPtr[idx + 2 * lineLength] * 3) -
                          (pixelPtr[idx + 3 * lineLength]);

            filtVal = boundingValuePtr[256 + ((filtVal + 4) >> 3)];

            pixelPtr[idx + lineLength] = clamp255(pixelPtr[idx + lineLength] + filtVal);
            pixelPtr[idx + 2 * lineLength] = clamp255(pixelPtr[idx + 2 * lineLength] - filtVal);

            idx++;
        }
    }

    public void loopFilter(Playback pbi) {
        int fragsAcross = pbi.HFragments;
        int fromFragment;
        int fragsDown = pbi.VFragments;
        int lineFragments;
        int lineLength;

        /* Set the limit value for the loop filter based upon the current
           quantizer. */
        int qIndex = pbi.frameQIS[0];
        int fLimit = loopFilterLimits[qIndex];
        
        if (fLimit == 0) return;
        
        setupBoundingValueArrayGeneric(fLimit);

        for (int j = 0; j < 3; j++) {
            switch (j) {
                case 0 -> { /* y */
                    fromFragment = 0;
                    fragsAcross = pbi.HFragments;
                    fragsDown = pbi.VFragments;
                    lineLength = pbi.YStride;
                    lineFragments = pbi.HFragments;
                }
                case 1 -> { /* u */
                    fromFragment = pbi.YPlaneFragments;
                    fragsAcross = pbi.HFragments >> 1;
                    fragsDown = pbi.VFragments >> 1;
                    lineLength = pbi.UVStride;
                    lineFragments = pbi.HFragments / 2;
                }
                default -> { /* v */
                    fromFragment = pbi.YPlaneFragments + pbi.UVPlaneFragments;
                    fragsAcross = pbi.HFragments >> 1;
                    fragsDown = pbi.VFragments >> 1;
                    lineLength = pbi.UVStride;
                    lineFragments = pbi.HFragments / 2;
                }
            }

            int i = fromFragment;

            /**************************************************************
             First Row
            **************************************************************/
            /* first column conditions */
            /* only do 2 prediction if fragment coded and on non intra or if
               all fragments are intra */
            if (pbi.display_fragments[i] != 0) {
                /* Filter right hand border only if the block to the right is
                   not coded */
                if (pbi.display_fragments[i + 1] == 0) {
                    filterHoriz(pbi.LastFrameRecon,
                                pbi.recon_pixel_index_table[i] + 6,
                                lineLength, filtBoundingValue);
                }

                /* Bottom done if next row set */
                if (pbi.display_fragments[i + lineFragments] == 0) {
                    filterVert(pbi.LastFrameRecon,
                               pbi.recon_pixel_index_table[i + lineFragments],
                               lineLength, filtBoundingValue);
                }
            }
            i++;

            /***************************************************************/
            /* middle columns  */
            for (int n = 1; n < fragsAcross - 1; n++) {
                if (pbi.display_fragments[i] != 0) {
                    int index = pbi.recon_pixel_index_table[i];

                    /* Filter Left edge always */
                    filterHoriz(pbi.LastFrameRecon, index - 2,
                                lineLength, filtBoundingValue);

                    /* Filter right hand border only if the block to the right is
                       not coded */
                    if (pbi.display_fragments[i + 1] == 0) {
                        filterHoriz(pbi.LastFrameRecon,
                                    index + 6,
                                    lineLength, filtBoundingValue);
                    }

                    /* Bottom done if next row set */
                    if (pbi.display_fragments[i + lineFragments] == 0) {
                        filterVert(pbi.LastFrameRecon,
                                   pbi.recon_pixel_index_table[i + lineFragments],
                                   lineLength, filtBoundingValue);
                    }
                }
                i++;
            }

            /***************************************************************/
            /* Last Column */
            if (pbi.display_fragments[i] != 0) {
                /* Filter Left edge always */
                filterHoriz(pbi.LastFrameRecon,
                            pbi.recon_pixel_index_table[i] - 2,
                            lineLength, filtBoundingValue);

                /* Bottom done if next row set */
                if (pbi.display_fragments[i + lineFragments] == 0) {
                    filterVert(pbi.LastFrameRecon,
                               pbi.recon_pixel_index_table[i + lineFragments],
                               lineLength, filtBoundingValue);
                }
            }
            i++;

            /***************************************************************/
            /* Middle Rows */
            /***************************************************************/
            for (int m = 1; m < fragsDown - 1; m++) {

                /*****************************************************************/
                /* first column conditions */
                /* only do 2 prediction if fragment coded and on non intra or if
                   all fragments are intra */
                if (pbi.display_fragments[i] != 0) {
                    int index = pbi.recon_pixel_index_table[i];

                    /* TopRow is always done */
                    filterVert(pbi.LastFrameRecon, index,
                               lineLength, filtBoundingValue);

                    /* Filter right hand border only if the block to the right is
                       not coded */
                    if (pbi.display_fragments[i + 1] == 0) {
                        filterHoriz(pbi.LastFrameRecon, index + 6,
                                    lineLength, filtBoundingValue);
                    }

                    /* Bottom done if next row set */
                    if (pbi.display_fragments[i + lineFragments] == 0) {
                        filterVert(pbi.LastFrameRecon,
                                   pbi.recon_pixel_index_table[i + lineFragments],
                                   lineLength, filtBoundingValue);
                    }
                }
                i++;

                /*****************************************************************/
                /* middle columns  */
                for (int n = 1; n < fragsAcross - 1; n++, i++) {

                    if (pbi.display_fragments[i] != 0) {
                        int index = pbi.recon_pixel_index_table[i];
                        /* Filter Left edge always */
                        filterHoriz(pbi.LastFrameRecon, index - 2,
                                    lineLength, filtBoundingValue);

                        /* TopRow is always done */
                        filterVert(pbi.LastFrameRecon, index,
                                   lineLength, filtBoundingValue);

                        /* Filter right hand border only if the block to the right
                           is not coded */
                        if (pbi.display_fragments[i + 1] == 0) {
                            filterHoriz(pbi.LastFrameRecon, index + 6,
                                        lineLength, filtBoundingValue);
                        }

                        /* Bottom done if next row set */
                        if (pbi.display_fragments[i + lineFragments] == 0) {
                            filterVert(pbi.LastFrameRecon,
                                       pbi.recon_pixel_index_table[i + lineFragments],
                                       lineLength, filtBoundingValue);
                        }
                    }
                }

                /******************************************************************/
                /* Last Column */
                if (pbi.display_fragments[i] != 0) {
                    int index = pbi.recon_pixel_index_table[i];

                    /* Filter Left edge always*/
                    filterHoriz(pbi.LastFrameRecon, index - 2,
                                lineLength, filtBoundingValue);

                    /* TopRow is always done */
                    filterVert(pbi.LastFrameRecon, index,
                               lineLength, filtBoundingValue);

                    /* Bottom done if next row set */
                    if (pbi.display_fragments[i + lineFragments] == 0) {
                        filterVert(pbi.LastFrameRecon,
                                   pbi.recon_pixel_index_table[i + lineFragments],
                                   lineLength, filtBoundingValue);
                    }
                }
                i++;
            }

            /*******************************************************************/
            /* Last Row  */

            /* first column conditions */
            /* only do 2 prediction if fragment coded and on non intra or if
               all fragments are intra */
            if (pbi.display_fragments[i] != 0) {
                int index = pbi.recon_pixel_index_table[i];

                /* TopRow is always done */
                filterVert(pbi.LastFrameRecon, index,
                           lineLength, filtBoundingValue);

                /* Filter right hand border only if the block to the right is
                   not coded */
                if (pbi.display_fragments[i + 1] == 0) {
                    filterHoriz(pbi.LastFrameRecon, index + 6,
                                lineLength, filtBoundingValue);
                }
            }
            i++;

            /******************************************************************/
            /* middle columns  */
            for (int n = 1; n < fragsAcross - 1; n++, i++) {
                if (pbi.display_fragments[i] != 0) {
                    int index = pbi.recon_pixel_index_table[i];

                    /* Filter Left edge always */
                    filterHoriz(pbi.LastFrameRecon, index - 2,
                                lineLength, filtBoundingValue);

                    /* TopRow is always done */
                    filterVert(pbi.LastFrameRecon, index,
                               lineLength, filtBoundingValue);

                    /* Filter right hand border only if the block to the right is
                       not coded */
                    if (pbi.display_fragments[i + 1] == 0) {
                        filterHoriz(pbi.LastFrameRecon, index + 6,
                                    lineLength, filtBoundingValue);
                    }
                }
            }

            /******************************************************************/
            /* Last Column */
            if (pbi.display_fragments[i] != 0) {
                int index = pbi.recon_pixel_index_table[i];

                /* Filter Left edge always */
                filterHoriz(pbi.LastFrameRecon, index - 2,
                            lineLength, filtBoundingValue);

                /* TopRow is always done */
                filterVert(pbi.LastFrameRecon, index,
                           lineLength, filtBoundingValue);
            }
        }
    }
}
