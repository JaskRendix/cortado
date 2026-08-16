/* JOrbis
* Copyright (C) 2000 ymnk, JCraft,Inc.
*
* Written by: 2000 ymnk<ymnk@jcaft.com>
*
* Many thanks to
*   Monty <monty@xiph.org> and
*   The XIPHOPHORUS Company http://www.xiph.org/ .
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

import com.jcraft.jogg.Buffer;

public final class CodeBook {

  public int dim; // Codebook dimensions (elements per vector)
  public int entries; // Codebook entries
  public StaticCodeBook c = new StaticCodeBook();

  public float[] valuelist; // List of dim*entries actual entry values
  public int[] codelist; // List of bitstream codewords for each entry
  public DecodeAux decode_tree;

  // Returns the number of bits
  public int encode(int a, Buffer b) {
    b.write(codelist[a], c.lengthlist[a]);
    return c.lengthlist[a];
  }

  // Floor0 LSP (single stage, non interleaved, nearest match)
  // Returns entry number and modifies a to the quantization value
  public int errorv(float[] a) {
    int best = best(a, 1);
    for (int k = 0; k < dim; k++) {
      a[k] = valuelist[best * dim + k];
    }
    return best;
  }

  // Returns the number of bits and modifies a to the quantization value
  public int encodev(int best, float[] a, Buffer b) {
    for (int k = 0; k < dim; k++) {
      a[k] = valuelist[best * dim + k];
    }
    return encode(best, b);
  }

  // Res0 (multistage, interleave, lattice)
  // Returns the number of bits and modifies a to the remainder value
  public int encodevs(float[] a, Buffer b, int step, int addmul) {
    int best = besterror(a, step, addmul);
    return encode(best, b);
  }

  private int[] t = new int[15]; // decodevs_add is synchronized for re-using t.

  public synchronized int decodevs_add(float[] a, int offset, Buffer b, int n) {
    int step = n / dim;
    int entry;
    int i, j, o;

    if (t.length < step) {
      t = new int[step];
    }

    for (i = 0; i < step; i++) {
      entry = decode(b);
      if (entry == -1) return -1;
      t[i] = entry * dim;
    }
    for (i = 0, o = 0; i < dim; i++, o += step) {
      for (j = 0; j < step; j++) {
        a[offset + o + j] += valuelist[t[j] + i];
      }
    }

    return 0;
  }

  public int decodev_add(float[] a, int offset, Buffer b, int n) {
    int i = 0;
    while (i < n) {
      int entry = decode(b);
      if (entry == -1) {
        return -1;
      }
      int codeVal = entry * dim;

      if (dim > 8) {
        for (int j = 0; j < dim; j++) {
          a[offset + i++] += valuelist[codeVal + j];
        }
      } else {
        int j = 0;
        switch (dim) {
          case 8:
            a[offset + i++] += valuelist[codeVal + (j++)];
            break;
          case 7:
            a[offset + i++] += valuelist[codeVal + (j++)];
            break;
          case 6:
            a[offset + i++] += valuelist[codeVal + (j++)];
            break;
          case 5:
            a[offset + i++] += valuelist[codeVal + (j++)];
            break;
          case 4:
            a[offset + i++] += valuelist[codeVal + (j++)];
            break;
          case 3:
            a[offset + i++] += valuelist[codeVal + (j++)];
            break;
          case 2:
            a[offset + i++] += valuelist[codeVal + (j++)];
            break;
          case 1:
            a[offset + i++] += valuelist[codeVal + (j++)];
            break;
          default:
            break;
        }
      }
    }
    return 0;
  }

  public int decodev_set(float[] a, int offset, Buffer b, int n) {
    int i, j, entry;
    int codeVal;

    for (i = 0; i < n; ) {
      entry = decode(b);
      if (entry == -1) return -1;
      codeVal = entry * dim;
      for (j = 0; j < dim; ) {
        a[offset + i++] = valuelist[codeVal + (j++)];
      }
    }
    return 0;
  }

  public int decodevv_add(float[][] a, int offset, int ch, Buffer b, int n) {
    int i, j, entry;
    int chptr = 0;

    for (i = offset / ch; i < (offset + n) / ch; ) {
      entry = decode(b);
      if (entry == -1) return -1;

      int codeVal = entry * dim;
      for (j = 0; j < dim; j++) {
        a[chptr++][i] += valuelist[codeVal + j];
        if (chptr == ch) {
          chptr = 0;
          i++;
        }
      }
    }
    return 0;
  }

  // Returns the entry number or -1 on eof
  public int decode(Buffer b) {
    int ptr = 0;
    DecodeAux tree = decode_tree;
    int lok = b.look(tree.tabn);

    if (lok >= 0) {
      ptr = tree.tab[lok];
      b.adv(tree.tabl[lok]);
      if (ptr <= 0) {
        return -ptr;
      }
    }
    do {
      switch (b.read1()) {
        case 0:
          ptr = tree.ptr0[ptr];
          break;
        case 1:
          ptr = tree.ptr1[ptr];
          break;
        case -1:
        default:
          return -1;
      }
    } while (ptr > 0);
    return -ptr;
  }

  // Returns the entry number or -1 on eof
  public int decodevs(float[] a, int index, Buffer b, int step, int addmul) {
    int entry = decode(b);
    if (entry == -1) return -1;
    switch (addmul) {
      case -1:
        for (int i = 0, o = 0; i < dim; i++, o += step) a[index + o] = valuelist[entry * dim + i];
        break;
      case 0:
        for (int i = 0, o = 0; i < dim; i++, o += step) a[index + o] += valuelist[entry * dim + i];
        break;
      case 1:
        for (int i = 0, o = 0; i < dim; i++, o += step) a[index + o] *= valuelist[entry * dim + i];
        break;
      default:
        System.err.println("CodeBook.decodevs: addmul=" + addmul);
    }
    return entry;
  }

  public int best(float[] a, int step) {
    EncodeAuxNearestMatch nt = c.nearest_tree;
    EncodeAuxThreshMatch tt = c.thresh_tree;
    int ptr = 0;

    if (tt != null) {
      int index = 0;
      int i = 0;
      for (int k = 0, o = step * (dim - 1); k < dim; k++, o -= step) {
        for (i = 0; i < tt.threshvals - 1; i++) {
          if (a[o] < tt.quantthresh[i]) {
            break;
          }
        }
        index = (index * tt.quantvals) + tt.quantmap[i];
      }
      if (c.lengthlist[index] > 0) {
        return index;
      }
    }
    if (nt != null) {
      while (true) {
        float sum = 0.0f;
        int p = nt.p[ptr];
        int q = nt.q[ptr];
        for (int k = 0, o = 0; k < dim; k++, o += step) {
          sum +=
              (valuelist[p + k] - valuelist[q + k])
                  * (a[o] - (valuelist[p + k] + valuelist[q + k]) * 0.5f);
        }
        if (sum > 0.0f) {
          ptr = -nt.ptr0[ptr];
        } else {
          ptr = -nt.ptr1[ptr];
        }
        if (ptr <= 0) break;
      }
      return -ptr;
    }

    {
      int besti = -1;
      float bestVal = 0.0f;
      int e = 0;
      for (int i = 0; i < entries; i++) {
        if (c.lengthlist[i] > 0) {
          float thisVal = dist(dim, valuelist, e, a, step);
          if (besti == -1 || thisVal < bestVal) {
            bestVal = thisVal;
            besti = i;
          }
        }
        e += dim;
      }
      return besti;
    }
  }

  // Returns the entry number and modifies a to the remainder value
  public int besterror(float[] a, int step, int addmul) {
    int bestVal = best(a, step);
    switch (addmul) {
      case 0:
        for (int i = 0, o = 0; i < dim; i++, o += step) a[o] -= valuelist[bestVal * dim + i];
        break;
      case 1:
        for (int i = 0, o = 0; i < dim; i++, o += step) {
          float val = valuelist[bestVal * dim + i];
          if (val == 0) {
            a[o] = 0;
          } else {
            a[o] /= val;
          }
        }
        break;
    }
    return bestVal;
  }

  public void clear() {}

  private static float dist(int el, float[] ref, int index, float[] b, int step) {
    float acc = 0.0f;
    for (int i = 0; i < el; i++) {
      float val = (ref[index + i] - b[i * step]);
      acc += val * val;
    }
    return acc;
  }

  public int init_decode(StaticCodeBook s) {
    c = s;
    entries = s.entries;
    dim = s.dim;
    valuelist = s.unquantize();

    decode_tree = make_decode_tree();
    if (decode_tree == null) {
      clear();
      return -1;
    }
    return 0;
  }

  public static int[] make_words(int[] l, int n) {
    int[] marker = new int[33];
    int[] r = new int[n];

    for (int i = 0; i < n; i++) {
      int length = l[i];
      if (length > 0) {
        int entry = marker[length];

        if (length < 32 && (entry >>> length) != 0) {
          return null;
        }
        r[i] = entry;

        for (int j = length; j > 0; j--) {
          if ((marker[j] & 1) != 0) {
            if (j == 1) marker[1]++;
            else marker[j] = marker[j - 1] << 1;
            break;
          }
          marker[j]++;
        }

        for (int j = length + 1; j < 33; j++) {
          if ((marker[j] >>> 1) == entry) {
            entry = marker[j];
            marker[j] = marker[j - 1] << 1;
          } else {
            break;
          }
        }
      }
    }

    for (int i = 0; i < n; i++) {
      int temp = 0;
      for (int j = 0; j < l[i]; j++) {
        temp <<= 1;
        temp |= (r[i] >>> j) & 1;
      }
      r[i] = temp;
    }

    return r;
  }

  public DecodeAux make_decode_tree() {
    int top = 0;
    DecodeAux tree = new DecodeAux();
    int[] ptr0 = tree.ptr0 = new int[entries * 2];
    int[] ptr1 = tree.ptr1 = new int[entries * 2];
    int[] generatedCodelist = make_words(c.lengthlist, c.entries);

    if (generatedCodelist == null) return null;
    tree.aux = entries * 2;

    for (int i = 0; i < entries; i++) {
      if (c.lengthlist[i] > 0) {
        int ptr = 0;
        int j;
        for (j = 0; j < c.lengthlist[i] - 1; j++) {
          int bit = (generatedCodelist[i] >>> j) & 1;
          if (bit == 0) {
            if (ptr0[ptr] == 0) {
              ptr0[ptr] = ++top;
            }
            ptr = ptr0[ptr];
          } else {
            if (ptr1[ptr] == 0) {
              ptr1[ptr] = ++top;
            }
            ptr = ptr1[ptr];
          }
        }

        if (((generatedCodelist[i] >>> j) & 1) == 0) {
          ptr0[ptr] = -i;
        } else {
          ptr1[ptr] = -i;
        }
      }
    }

    tree.tabn = ilog(entries) - 4;
    if (tree.tabn < 5) tree.tabn = 5;
    int n = 1 << tree.tabn;
    tree.tab = new int[n];
    tree.tabl = new int[n];
    for (int i = 0; i < n; i++) {
      int p = 0;
      int j = 0;
      for (j = 0; j < tree.tabn && (p > 0 || j == 0); j++) {
        if ((i & (1 << j)) != 0) {
          p = ptr1[p];
        } else {
          p = ptr0[p];
        }
      }
      tree.tab[i] = p; // -code
      tree.tabl[i] = j; // length
    }

    return tree;
  }

  private static int ilog(int v) {
    int ret = 0;
    while (v != 0) {
      ret++;
      v >>>= 1;
    }
    return ret;
  }
}

class DecodeAux {
  int[] tab;
  int[] tabl;
  int tabn;

  int[] ptr0;
  int[] ptr1;
  int aux; // Number of tree entries
}
