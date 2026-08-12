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

public final class iDCT {

    private static final int IDCT_ADJUST_BEFORE_SHIFT = 8;
    private static final int XC1S7 = 64277;
    private static final int XC2S6 = 60547;
    private static final int XC3S5 = 54491;
    private static final int XC4S4 = 46341;
    private static final int XC5S3 = 36410;
    private static final int XC6S2 = 25080;
    private static final int XC7S1 = 12785;

    private final int[] ip = new int[64];

    private void dequantSlow(short[] dequantCoeffs, short[] quantizedList, int[] dctBlock) {
        for (int i = 0; i < 64; i++) {
            dctBlock[Constants.dequant_index[i]] = quantizedList[i] * dequantCoeffs[i];
        }
    }

    public void IDctSlow(short[] inputData, short[] quantMatrix, short[] outputData) {
        short[] op = outputData;

        int a, b, c, d, ad, bd, cd, dd, e, f, g, h;
        int ed, gd, add, bdd, fd, hd;
        int t1, t2;

        dequantSlow(quantMatrix, inputData, ip);

        /* Inverse DCT on the rows now */
        for (int loop = 0, off = 0; loop < 8; loop++, off += 8) {
            /* Check for non-zero values */
            if ((ip[0 + off] | ip[1 + off] | ip[2 + off] | ip[3 + off] | 
                 ip[4 + off] | ip[5 + off] | ip[6 + off] | ip[7 + off]) != 0) {
                
                t1 = (XC1S7 * ip[1 + off]) >> 16;
                t2 = (XC7S1 * ip[7 + off]) >> 16;
                a = t1 + t2;

                t1 = (XC7S1 * ip[1 + off]) >> 16;
                t2 = (XC1S7 * ip[7 + off]) >> 16;
                b = t1 - t2;

                t1 = (XC3S5 * ip[3 + off]) >> 16;
                t2 = (XC5S3 * ip[5 + off]) >> 16;
                c = t1 + t2;

                t1 = (XC3S5 * ip[5 + off]) >> 16;
                t2 = (XC5S3 * ip[3 + off]) >> 16;
                d = t1 - t2;

                ad = (XC4S4 * (short) (a - c)) >> 16;
                bd = (XC4S4 * (short) (b - d)) >> 16;

                cd = a + c;
                dd = b + d;

                e = (XC4S4 * (short) (ip[0 + off] + ip[4 + off])) >> 16;
                f = (XC4S4 * (short) (ip[0 + off] - ip[4 + off])) >> 16;

                t1 = (XC2S6 * ip[2 + off]) >> 16;
                t2 = (XC6S2 * ip[6 + off]) >> 16;
                g = t1 + t2;

                t1 = (XC6S2 * ip[2 + off]) >> 16;
                t2 = (XC2S6 * ip[6 + off]) >> 16;
                h = t1 - t2;

                ed = e - g;
                gd = e + g;

                add = f + ad;
                bdd = bd - h;

                fd = f - ad;
                hd = bd + h;

                /* Final sequence of operations over-write original inputs. */
                ip[0 + off] = (short) (gd + cd);
                ip[7 + off] = (short) (gd - cd);

                ip[1 + off] = (short) (add + hd);
                ip[2 + off] = (short) (add - hd);

                ip[3 + off] = (short) (ed + dd);
                ip[4 + off] = (short) (ed - dd);

                ip[5 + off] = (short) (fd + bdd);
                ip[6 + off] = (short) (fd - bdd);
            }
        }

        for (int loop = 0, off = 0; loop < 8; loop++, off++) {
            /* Check for non-zero values (bitwise or faster than ||) */
            if ((ip[0 * 8 + off] | ip[1 * 8 + off] | ip[2 * 8 + off] | ip[3 * 8 + off] |
                 ip[4 * 8 + off] | ip[5 * 8 + off] | ip[6 * 8 + off] | ip[7 * 8 + off]) != 0) {
                
                t1 = (XC1S7 * ip[1 * 8 + off]) >> 16;
                t2 = (XC7S1 * ip[7 * 8 + off]) >> 16;
                a = t1 + t2;

                t1 = (XC7S1 * ip[1 * 8 + off]) >> 16;
                t2 = (XC1S7 * ip[7 * 8 + off]) >> 16;
                b = t1 - t2;

                t1 = (XC3S5 * ip[3 * 8 + off]) >> 16;
                t2 = (XC5S3 * ip[5 * 8 + off]) >> 16;
                c = t1 + t2;

                t1 = (XC3S5 * ip[5 * 8 + off]) >> 16;
                t2 = (XC5S3 * ip[3 * 8 + off]) >> 16;
                d = t1 - t2;

                ad = (XC4S4 * (short) (a - c)) >> 16;
                bd = (XC4S4 * (short) (b - d)) >> 16;

                cd = a + c;
                dd = b + d;

                e = (XC4S4 * (short) (ip[0 * 8 + off] + ip[4 * 8 + off])) >> 16;
                f = (XC4S4 * (short) (ip[0 * 8 + off] - ip[4 * 8 + off])) >> 16;

                t1 = (XC2S6 * ip[2 * 8 + off]) >> 16;
                t2 = (XC6S2 * ip[6 * 8 + off]) >> 16;
                g = t1 + t2;

                t1 = (XC6S2 * ip[2 * 8 + off]) >> 16;
                t2 = (XC2S6 * ip[6 * 8 + off]) >> 16;
                h = t1 - t2;

                ed = e - g;
                gd = e + g;

                add = f + ad;
                bdd = bd - h;

                fd = f - ad;
                hd = bd + h;

                gd += IDCT_ADJUST_BEFORE_SHIFT;
                add += IDCT_ADJUST_BEFORE_SHIFT;
                ed += IDCT_ADJUST_BEFORE_SHIFT;
                fd += IDCT_ADJUST_BEFORE_SHIFT;

                /* Final sequence of operations over-write original inputs. */
                op[0 * 8 + off] = (short) ((gd + cd) >> 4);
                op[7 * 8 + off] = (short) ((gd - cd) >> 4);

                op[1 * 8 + off] = (short) ((add + hd) >> 4);
                op[2 * 8 + off] = (short) ((add - hd) >> 4);

                op[3 * 8 + off] = (short) ((ed + dd) >> 4);
                op[4 * 8 + off] = (short) ((ed - dd) >> 4);

                op[5 * 8 + off] = (short) ((fd + bdd) >> 4);
                op[6 * 8 + off] = (short) ((fd - bdd) >> 4);
            } else {
                op[0 * 8 + off] = 0;
                op[7 * 8 + off] = 0;
                op[1 * 8 + off] = 0;
                op[2 * 8 + off] = 0;
                op[3 * 8 + off] = 0;
                op[4 * 8 + off] = 0;
                op[5 * 8 + off] = 0;
                op[6 * 8 + off] = 0;
            }
        }
    }

    /************************
        x  x  x  x  0  0  0  0
        x  x  x  0  0  0  0  0
        x  x  0  0  0  0  0  0
        x  0  0  0  0  0  0  0
        0  0  0  0  0  0  0  0
        0  0  0  0  0  0  0  0
        0  0  0  0  0  0  0  0
        0  0  0  0  0  0  0  0
    *************************/

    private void dequantSlow10(short[] dequantCoeffs, short[] quantizedList, int[] dctBlock) {
        Arrays.fill(dctBlock, 0, 32, 0);

        for (int i = 0; i < 10; i++) {
            dctBlock[Constants.dequant_index[i]] = quantizedList[i] * dequantCoeffs[i];
        }
    }

    public void IDct10(short[] inputData, short[] quantMatrix, short[] outputData) {
        short[] op = outputData;

        int a, b, c, d, ad, bd, cd, dd, e, f, g, h;
        int ed, gd, add, bdd, fd, hd;

        dequantSlow10(quantMatrix, inputData, ip);

        /* Inverse DCT on the rows now */
        for (int loop = 0, off = 0; loop < 4; loop++, off += 8) {
            /* Check for non-zero values */
            if ((ip[0 + off] | ip[1 + off] | ip[2 + off] | ip[3 + off]) != 0) {
                a = (XC1S7 * ip[1 + off]) >> 16;
                b = (XC7S1 * ip[1 + off]) >> 16;
                c = (XC3S5 * ip[3 + off]) >> 16;
                d = -((XC5S3 * ip[3 + off]) >> 16);

                ad = (XC4S4 * (short) (a - c)) >> 16;
                bd = (XC4S4 * (short) (b - d)) >> 16;

                cd = a + c;
                dd = b + d;

                e = (XC4S4 * ip[0 + off]) >> 16;
                f = e;
                g = (XC2S6 * ip[2 + off]) >> 16;
                h = (XC6S2 * ip[2 + off]) >> 16;

                ed = e - g;
                gd = e + g;

                add = f + ad;
                bdd = bd - h;

                fd = f - ad;
                hd = bd + h;

                /* Final sequence of operations over-write original inputs. */
                ip[0 + off] = (short) (gd + cd);
                ip[7 + off] = (short) (gd - cd);

                ip[1 + off] = (short) (add + hd);
                ip[2 + off] = (short) (add - hd);

                ip[3 + off] = (short) (ed + dd);
                ip[4 + off] = (short) (ed - dd);

                ip[5 + off] = (short) (fd + bdd);
                ip[6 + off] = (short) (fd - bdd);
            }
        }

        for (int loop = 0, off = 0; loop < 8; loop++, off++) {
            /* Check for non-zero values (bitwise or faster than ||) */
            if ((ip[0 * 8 + off] | ip[1 * 8 + off] | ip[2 * 8 + off] | ip[3 * 8 + off]) != 0) {
                a = (XC1S7 * ip[1 * 8 + off]) >> 16;
                b = (XC7S1 * ip[1 * 8 + off]) >> 16;
                c = (XC3S5 * ip[3 * 8 + off]) >> 16;
                d = -((XC5S3 * ip[3 * 8 + off]) >> 16);

                ad = (XC4S4 * (short) (a - c)) >> 16;
                bd = (XC4S4 * (short) (b - d)) >> 16;

                cd = a + c;
                dd = b + d;

                e = (XC4S4 * ip[0 * 8 + off]) >> 16;
                f = e;
                g = (XC2S6 * ip[2 * 8 + off]) >> 16;
                h = (XC6S2 * ip[2 * 8 + off]) >> 16;

                ed = e - g;
                gd = e + g;

                add = f + ad;
                bdd = bd - h;

                fd = f - ad;
                hd = bd + h;

                gd += IDCT_ADJUST_BEFORE_SHIFT;
                add += IDCT_ADJUST_BEFORE_SHIFT;
                ed += IDCT_ADJUST_BEFORE_SHIFT;
                fd += IDCT_ADJUST_BEFORE_SHIFT;

                /* Final sequence of operations over-write original inputs. */
                op[0 * 8 + off] = (short) ((gd + cd) >> 4);
                op[7 * 8 + off] = (short) ((gd - cd) >> 4);

                op[1 * 8 + off] = (short) ((add + hd) >> 4);
                op[2 * 8 + off] = (short) ((add - hd) >> 4);

                op[3 * 8 + off] = (short) ((ed + dd) >> 4);
                op[4 * 8 + off] = (short) ((ed - dd) >> 4);

                op[5 * 8 + off] = (short) ((fd + bdd) >> 4);
                op[6 * 8 + off] = (short) ((fd - bdd) >> 4);
            } else {
                op[0 * 8 + off] = 0;
                op[7 * 8 + off] = 0;
                op[1 * 8 + off] = 0;
                op[2 * 8 + off] = 0;
                op[3 * 8 + off] = 0;
                op[4 * 8 + off] = 0;
                op[5 * 8 + off] = 0;
                op[6 * 8 + off] = 0;
            }
        }
    }

    /***************************
        x   0   0   0   0   0   0   0
        0   0   0   0   0   0   0   0
        0   0   0   0   0   0   0   0
        0   0   0   0   0   0   0   0
        0   0   0   0   0   0   0   0
        0   0   0   0   0   0   0   0
        0   0   0   0   0   0   0   0
        0   0   0   0   0   0   0   0
    **************************/

    public void IDct1(short[] inputData, short[] quantMatrix, short[] outputData) {
        short outD = (short) ((inputData[0] * quantMatrix[0] + 15) >> 5);
        Arrays.fill(outputData, outD);
    }
}
