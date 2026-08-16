/* JOrbis
* Copyright (C) 2000 ymnk, JCraft,Inc.
*
* Written by: 2000 ymnk<ymnk@jcaft.com>
*
* Many thanks to
*  Monty <monty@xiph.org> and
*  The XIPHOPHORUS Company http://www.xiph.org/ .
* JOrbis has been based on their awesome works, Vorbis codec.
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Library General Public License
* as published by the Free Software Foundation; either version 2 of
* the License, or (at your option) any later version.

* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Library General Public License for more details.
*
* You should have received a copy of the GNU Library General Public
* License along with this program; if not, write to the Free Software
* Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package com.jcraft.jorbis;

/*
function: LSP (also called LSF) conversion routines

The LSP generation code is taken (with minimal modification) from
"On the Computation of the LSP Frequencies" by Joseph Rothweiler
<rothwlr@altavista.net>, available at:

http://www2.xtdl.com/~rothwlr/lsfpaper/lsfpage.html
********************************************************************/

public class Lsp {

  public static final float M_PI = (float) 3.1415926539;

  public Lsp() {}

  public static void lspToCurve(
      float[] curve, int[] map, int n, int ln, float[] lsp, int m, float amp, float ampoffset) {
    float wDel = M_PI / ln;
    for (int i = 0; i < m; i++) {
      lsp[i] = Lookup.coslook(lsp[i]);
    }
    int m2 = (m / 2) * 2;

    int i = 0;
    while (i < n) {
      int k = map[i];
      float p = 0.7071067812f;
      float q = 0.7071067812f;
      float w = Lookup.coslook(wDel * k);

      for (int j = 0; j < m2; j += 2) {
        q *= lsp[j] - w;
        p *= lsp[j + 1] - w;
      }

      if ((m & 1) != 0) {
        /* odd order filter; slightly asymmetric */
        /* the last coefficient */
        q *= lsp[m - 1] - w;
        q *= q;
        p *= p * (1.f - w * w);
      } else {
        /* even order filter; still symmetric */
        q *= q * (1.f + w);
        p *= p * (1.f - w);
      }

      q = p + q;
      int hx = Float.floatToIntBits(q);
      int ix = 0x7fffffff & hx;
      int qExp = 0;

      if (ix >= 0x7f800000 || (ix == 0)) {
        // 0, inf, nan
      } else {
        if (ix < 0x00800000) { // subnormal
          q *= 3.3554432000e+07; // 0x4c000000
          hx = Float.floatToIntBits(q);
          ix = 0x7fffffff & hx;
          qExp = -25;
        }
        qExp += ((ix >>> 23) - 126);
        hx = (hx & 0x807fffff) | 0x3f000000;
        q = Float.intBitsToFloat(hx);
      }

      q = Lookup.fromdBlook(amp * Lookup.invsqlook(q) * Lookup.invsq2explook(qExp + m) - ampoffset);

      do {
        curve[i++] *= q;
      } while (i < n && map[i] == k);
    }
  }
}
