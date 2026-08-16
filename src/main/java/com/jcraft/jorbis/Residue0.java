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

import com.jcraft.jogg.Buffer;

class Residue0 extends FuncResidue {
  @Override
  void pack(Object vr, Buffer opb) {
    InfoResidue0 info = (InfoResidue0) vr;
    int acc = 0;
    opb.write(info.begin, 24);
    opb.write(info.end, 24);

    opb.write(
        info.grouping - 1, 24); /* residue vectors to group and code with a partitioned book */
    opb.write(info.partitions - 1, 6); /* possible partition choices */
    opb.write(info.groupbook, 8); /* group huffman book */

    for (int j = 0; j < info.partitions; j++) {
      if (ilog(info.secondstages[j]) > 3) {
        opb.write(info.secondstages[j], 3);
        opb.write(1, 1);
        opb.write(info.secondstages[j] >>> 3, 5);
      } else {
        opb.write(info.secondstages[j], 4); /* trailing zero */
      }
      acc += icount(info.secondstages[j]);
    }
    for (int j = 0; j < acc; j++) {
      opb.write(info.booklist[j], 8);
    }
  }

  @Override
  Object unpack(Info vi, Buffer opb) {
    int acc = 0;
    InfoResidue0 info = new InfoResidue0();

    info.begin = opb.read(24);
    info.end = opb.read(24);
    info.grouping = opb.read(24) + 1;
    info.partitions = opb.read(6) + 1;
    info.groupbook = opb.read(8);

    for (int j = 0; j < info.partitions; j++) {
      int cascade = opb.read(3);
      if (opb.read(1) != 0) {
        cascade |= (opb.read(5) << 3);
      }
      info.secondstages[j] = cascade;
      acc += icount(cascade);
    }

    for (int j = 0; j < acc; j++) {
      info.booklist[j] = opb.read(8);
    }

    if (info.groupbook >= vi.getBooks()) {
      freeInfo(info);
      return null;
    }

    for (int j = 0; j < acc; j++) {
      if (info.booklist[j] >= vi.getBooks()) {
        freeInfo(info);
        return null;
      }
    }
    return info;
  }

  @Override
  Object look(DspState vd, InfoMode vm, Object vr) {
    InfoResidue0 info = (InfoResidue0) vr;
    LookResidue0 look = new LookResidue0();
    int acc = 0;
    int dim;
    int maxstage = 0;
    look.info = info;
    look.map = vm.getMapping();

    look.parts = info.partitions;
    look.fullbooks = vd.fullbooks;
    look.phrasebook = vd.fullbooks[info.groupbook];

    dim = look.phrasebook.dim;

    look.partbooks = new int[look.parts][];

    for (int j = 0; j < look.parts; j++) {
      int stages = ilog(info.secondstages[j]);
      if (stages != 0) {
        if (stages > maxstage) maxstage = stages;
        look.partbooks[j] = new int[stages];
        for (int k = 0; k < stages; k++) {
          if ((info.secondstages[j] & (1 << k)) != 0) {
            look.partbooks[j][k] = info.booklist[acc++];
          }
        }
      }
    }

    look.partvals = (int) Math.rint(Math.pow(look.parts, dim));
    look.stages = maxstage;
    look.decodemap = new int[look.partvals][];
    for (int j = 0; j < look.partvals; j++) {
      int val = j;
      int mult = look.partvals / look.parts;
      look.decodemap[j] = new int[dim];

      for (int k = 0; k < dim; k++) {
        int deco = val / mult;
        val -= deco * mult;
        mult /= look.parts;
        look.decodemap[j][k] = deco;
      }
    }
    return look;
  }

  @Override
  void freeInfo(Object i) {}

  @Override
  void freeLook(Object i) {}

  @Override
  int forward(Block vb, Object vl, float[][] in, int ch) {
    System.err.println("Residue0.forward: not implemented");
    return 0;
  }

  private static int[][][] partword = new int[2][][];

  static synchronized int _01inverse(Block vb, Object vl, float[][] in, int ch, int decodepart) {
    int i, j, k, l, s;
    LookResidue0 look = (LookResidue0) vl;
    InfoResidue0 info = look.info;

    int samples_per_partition = info.grouping;
    int partitions_per_word = look.phrasebook.dim;
    int n = info.end - info.begin;

    int partvals = n / samples_per_partition;
    int partwords = (partvals + partitions_per_word - 1) / partitions_per_word;

    if (partword.length < ch) {
      partword = new int[ch][][];
      for (j = 0; j < ch; j++) {
        partword[j] = new int[partwords][];
      }
    } else {
      for (j = 0; j < ch; j++) {
        if (partword[j] == null || partword[j].length < partwords)
          partword[j] = new int[partwords][];
      }
    }

    for (s = 0; s < look.stages; s++) {
      for (i = 0, l = 0; i < partvals; l++) {
        if (s == 0) {
          for (j = 0; j < ch; j++) {
            int temp = look.phrasebook.decode(vb.opb);
            if (temp == -1) {
              return 0;
            }
            partword[j][l] = look.decodemap[temp];
            if (partword[j][l] == null) {
              return 0;
            }
          }
        }

        for (k = 0; k < partitions_per_word && i < partvals; k++, i++)
          for (j = 0; j < ch; j++) {
            int offset = info.begin + i * samples_per_partition;
            if ((info.secondstages[partword[j][l][k]] & (1 << s)) != 0) {
              CodeBook stagebook = look.fullbooks[look.partbooks[partword[j][l][k]][s]];
              if (stagebook != null) {
                if (decodepart == 0) {
                  if (stagebook.decodevs_add(in[j], offset, vb.opb, samples_per_partition) == -1) {
                    return 0;
                  }
                } else if (decodepart == 1) {
                  if (stagebook.decodev_add(in[j], offset, vb.opb, samples_per_partition) == -1) {
                    return 0;
                  }
                }
              }
            }
          }
      }
    }
    return 0;
  }

  @Override
  int inverse(Block vb, Object vl, float[][] in, int[] nonzero, int ch) {
    int used = 0;
    for (int i = 0; i < ch; i++) {
      if (nonzero[i] != 0) {
        in[used++] = in[i];
      }
    }
    if (used != 0) return _01inverse(vb, vl, in, used, 0);
    else return 0;
  }

  private static int ilog(int v) {
    int ret = 0;
    while (v != 0) {
      ret++;
      v >>>= 1;
    }
    return ret;
  }

  private static int icount(int v) {
    int ret = 0;
    while (v != 0) {
      ret += (v & 1);
      v >>>= 1;
    }
    return ret;
  }
}
