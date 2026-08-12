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

public final class Recon {
    private Recon() {
        // Prevent instantiation of utility class
    }

    private static short clamp255(int val) {
        return (short) ((~(val >> 31)) & 255 & (val | ((255 - val) >> 31)));
    }

    public static void copyBlock(short[] src, short[] dest, int idx, int srcStride) {
        int off = idx;

        for (int i = 0; i < 8; i++) {
            dest[off + 0] = src[off + 0];
            dest[off + 1] = src[off + 1];
            dest[off + 2] = src[off + 2];
            dest[off + 3] = src[off + 3];
            dest[off + 4] = src[off + 4];
            dest[off + 5] = src[off + 5];
            dest[off + 6] = src[off + 6];
            dest[off + 7] = src[off + 7];
            off += srcStride;
        }
    }

    public static void reconIntra(short[] reconPtr, int idx, short[] changePtr, int lineStep) {
        int roff = idx;
        int coff = 0;

        for (int i = 0; i < 8; i++) {
            /* Convert the data back to 8 bit unsigned */
            /* Saturate the output to unsigned 8 bit values */
            reconPtr[roff + 0] = clamp255(changePtr[coff++] + 128);
            reconPtr[roff + 1] = clamp255(changePtr[coff++] + 128);
            reconPtr[roff + 2] = clamp255(changePtr[coff++] + 128);
            reconPtr[roff + 3] = clamp255(changePtr[coff++] + 128);
            reconPtr[roff + 4] = clamp255(changePtr[coff++] + 128);
            reconPtr[roff + 5] = clamp255(changePtr[coff++] + 128);
            reconPtr[roff + 6] = clamp255(changePtr[coff++] + 128);
            reconPtr[roff + 7] = clamp255(changePtr[coff++] + 128);
            roff += lineStep;
        }
    }

    public static void reconInter(short[] reconPtr, int idx1, short[] refPtr, int idx2, short[] changePtr, int lineStep) {
        int coff = 0;
        int roff1 = idx1;
        int roff2 = idx2;

        for (int i = 0; i < 8; i++) {
            reconPtr[roff1 + 0] = clamp255(refPtr[roff2 + 0] + changePtr[coff++]);
            reconPtr[roff1 + 1] = clamp255(refPtr[roff2 + 1] + changePtr[coff++]);
            reconPtr[roff1 + 2] = clamp255(refPtr[roff2 + 2] + changePtr[coff++]);
            reconPtr[roff1 + 3] = clamp255(refPtr[roff2 + 3] + changePtr[coff++]);
            reconPtr[roff1 + 4] = clamp255(refPtr[roff2 + 4] + changePtr[coff++]);
            reconPtr[roff1 + 5] = clamp255(refPtr[roff2 + 5] + changePtr[coff++]);
            reconPtr[roff1 + 6] = clamp255(refPtr[roff2 + 6] + changePtr[coff++]);
            reconPtr[roff1 + 7] = clamp255(refPtr[roff2 + 7] + changePtr[coff++]);
            roff1 += lineStep;
            roff2 += lineStep;
        }
    }

    public static void reconInterHalfPixel2(short[] reconPtr, int idx1,
                                          short[] refPtr1, int idx2,
                                          short[] refPtr2, int idx3,
                                          short[] changePtr, int lineStep) {
        int coff = 0;
        int roff1 = idx1;
        int roff2 = idx2;
        int roff3 = idx3;

        for (int i = 0; i < 8; i++) {
            reconPtr[roff1 + 0] = clamp255(((refPtr1[roff2 + 0] + refPtr2[roff3 + 0]) >> 1) + changePtr[coff++]);
            reconPtr[roff1 + 1] = clamp255(((refPtr1[roff2 + 1] + refPtr2[roff3 + 1]) >> 1) + changePtr[coff++]);
            reconPtr[roff1 + 2] = clamp255(((refPtr1[roff2 + 2] + refPtr2[roff3 + 2]) >> 1) + changePtr[coff++]);
            reconPtr[roff1 + 3] = clamp255(((refPtr1[roff2 + 3] + refPtr2[roff3 + 3]) >> 1) + changePtr[coff++]);
            reconPtr[roff1 + 4] = clamp255(((refPtr1[roff2 + 4] + refPtr2[roff3 + 4]) >> 1) + changePtr[coff++]);
            reconPtr[roff1 + 5] = clamp255(((refPtr1[roff2 + 5] + refPtr2[roff3 + 5]) >> 1) + changePtr[coff++]);
            reconPtr[roff1 + 6] = clamp255(((refPtr1[roff2 + 6] + refPtr2[roff3 + 6]) >> 1) + changePtr[coff++]);
            reconPtr[roff1 + 7] = clamp255(((refPtr1[roff2 + 7] + refPtr2[roff3 + 7]) >> 1) + changePtr[coff++]);
            roff1 += lineStep;
            roff2 += lineStep;
            roff3 += lineStep;
        }
    }
}
