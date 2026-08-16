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

class Mapping0 extends FuncMapping {
  private static int seq = 0;

  @Override
  void freeInfo(Object imap) {}

  @Override
  void freeLook(Object imap) {}

  @Override
  Object look(DspState vd, InfoMode vm, Object m) {
    Info vi = vd.vi;
    LookMapping0 look = new LookMapping0();
    InfoMapping0 info = look.map = (InfoMapping0) m;
    look.mode = vm;

    look.time_look = new Object[info.submaps];
    look.floor_look = new Object[info.submaps];
    look.residue_look = new Object[info.submaps];

    look.time_func = new FuncTime[info.submaps];
    look.floor_func = new FuncFloor[info.submaps];
    look.residue_func = new FuncResidue[info.submaps];

    for (int i = 0; i < info.submaps; i++) {
      int timenum = info.timesubmap[i];
      int floornum = info.floorsubmap[i];
      int resnum = info.residuesubmap[i];

      look.time_func[i] = FuncTime.TIME_P[vi.getTimeType()[timenum]];
      look.time_look[i] = look.time_func[i].look(vd, vm, vi.getTimeParam()[timenum]);
      look.floor_func[i] = FuncFloor.FLOOR_P[vi.getFloorType()[floornum]];
      look.floor_look[i] = look.floor_func[i].look(vd, vm, vi.getFloorParam()[floornum]);
      look.residue_func[i] = FuncResidue.RESIDUE_P[vi.getResidueType()[resnum]];
      look.residue_look[i] = look.residue_func[i].look(vd, vm, vi.getResidueParam()[resnum]);
    }

    look.ch = vi.getChannels();
    return look;
  }

  @Override
  void pack(Info vi, Object imap, Buffer opb) {
    InfoMapping0 info = (InfoMapping0) imap;

    if (info.submaps > 1) {
      opb.write(1, 1);
      opb.write(info.submaps - 1, 4);
    } else {
      opb.write(0, 1);
    }

    if (info.couplingSteps > 0) {
      opb.write(1, 1);
      opb.write(info.couplingSteps - 1, 8);
      for (int i = 0; i < info.couplingSteps; i++) {
        opb.write(info.couplingMag[i], ilog2(vi.getChannels()));
        opb.write(info.couplingAng[i], ilog2(vi.getChannels()));
      }
    } else {
      opb.write(0, 1);
    }

    opb.write(0, 2); /* 2,3: reserved */

    if (info.submaps > 1) {
      for (int i = 0; i < vi.getChannels(); i++) opb.write(info.chmuxlist[i], 4);
    }
    for (int i = 0; i < info.submaps; i++) {
      opb.write(info.timesubmap[i], 8);
      opb.write(info.floorsubmap[i], 8);
      opb.write(info.residuesubmap[i], 8);
    }
  }

  @Override
  Object unpack(Info vi, Buffer opb) {
    InfoMapping0 info = new InfoMapping0();

    if (opb.read(1) != 0) {
      info.submaps = opb.read(4) + 1;
    } else {
      info.submaps = 1;
    }

    if (opb.read(1) != 0) {
      info.couplingSteps = opb.read(8) + 1;

      for (int i = 0; i < info.couplingSteps; i++) {
        int testM = info.couplingMag[i] = opb.read(ilog2(vi.getChannels()));
        int testA = info.couplingAng[i] = opb.read(ilog2(vi.getChannels()));

        if (testM < 0
            || testA < 0
            || testM == testA
            || testM >= vi.getChannels()
            || testA >= vi.getChannels()) {
          info.free();
          return null;
        }
      }
    }

    if (opb.read(2) > 0) {
      /* 2,3: reserved */
      info.free();
      return null;
    }

    if (info.submaps > 1) {
      for (int i = 0; i < vi.getChannels(); i++) {
        info.chmuxlist[i] = opb.read(4);
        if (info.chmuxlist[i] >= info.submaps) {
          info.free();
          return null;
        }
      }
    }

    for (int i = 0; i < info.submaps; i++) {
      info.timesubmap[i] = opb.read(8);
      if (info.timesubmap[i] >= vi.getTimes()) {
        info.free();
        return null;
      }
      info.floorsubmap[i] = opb.read(8);
      if (info.floorsubmap[i] >= vi.getFloors()) {
        info.free();
        return null;
      }
      info.residuesubmap[i] = opb.read(8);
      if (info.residuesubmap[i] >= vi.getResidues()) {
        info.free();
        return null;
      }
    }
    return info;
  }

  private float[][] pcmbundle = null;
  private int[] zerobundle = null;
  private int[] nonzero = null;
  private Object[] floormemo = null;

  @Override
  synchronized int inverse(Block vb, Object l) {
    DspState vd = vb.vd;
    Info vi = vd.vi;
    LookMapping0 look = (LookMapping0) l;
    InfoMapping0 info = look.map;
    InfoMode mode = look.mode;
    int n = vb.pcmend = vi.getBlocksizes()[vb.W];

    float[] window = vd.window[vb.W][vb.lW][vb.nW][mode.getWindowtype()];
    if (pcmbundle == null || pcmbundle.length < vi.getChannels()) {
      pcmbundle = new float[vi.getChannels()][];
      nonzero = new int[vi.getChannels()];
      zerobundle = new int[vi.getChannels()];
      floormemo = new Object[vi.getChannels()];
    }

    for (int i = 0; i < vi.getChannels(); i++) {
      float[] pcm = vb.pcm[i];
      int submap = info.chmuxlist[i];

      floormemo[i] = look.floor_func[submap].inverse1(vb, look.floor_look[submap], floormemo[i]);
      if (floormemo[i] != null) {
        nonzero[i] = 1;
      } else {
        nonzero[i] = 0;
      }
      for (int j = 0; j < n / 2; j++) {
        pcm[j] = 0;
      }
    }

    for (int i = 0; i < info.couplingSteps; i++) {
      if (nonzero[info.couplingMag[i]] != 0 || nonzero[info.couplingAng[i]] != 0) {
        nonzero[info.couplingMag[i]] = 1;
        nonzero[info.couplingAng[i]] = 1;
      }
    }

    for (int i = 0; i < info.submaps; i++) {
      int chInBundle = 0;
      for (int j = 0; j < vi.getChannels(); j++) {
        if (info.chmuxlist[j] == i) {
          if (nonzero[j] != 0) {
            zerobundle[chInBundle] = 1;
          } else {
            zerobundle[chInBundle] = 0;
          }
          pcmbundle[chInBundle++] = vb.pcm[j];
        }
      }

      look.residue_func[i].inverse(vb, look.residue_look[i], pcmbundle, zerobundle, chInBundle);
    }

    for (int i = info.couplingSteps - 1; i >= 0; i--) {
      float[] pcmM = vb.pcm[info.couplingMag[i]];
      float[] pcmA = vb.pcm[info.couplingAng[i]];

      for (int j = 0; j < n / 2; j++) {
        float mag = pcmM[j];
        float ang = pcmA[j];

        if (mag > 0) {
          if (ang > 0) {
            pcmM[j] = mag;
            pcmA[j] = mag - ang;
          } else {
            pcmA[j] = mag;
            pcmM[j] = mag + ang;
          }
        } else {
          if (ang > 0) {
            pcmM[j] = mag;
            pcmA[j] = mag + ang;
          } else {
            pcmA[j] = mag;
            pcmM[j] = mag - ang;
          }
        }
      }
    }

    for (int i = 0; i < vi.getChannels(); i++) {
      float[] pcm = vb.pcm[i];
      int submap = info.chmuxlist[i];
      look.floor_func[submap].inverse2(vb, look.floor_look[submap], floormemo[i], pcm);
    }

    for (int i = 0; i < vi.getChannels(); i++) {
      float[] pcm = vb.pcm[i];
      ((Mdct) vd.transform[vb.W][0]).backward(pcm, pcm);
    }

    for (int i = 0; i < vi.getChannels(); i++) {
      float[] pcm = vb.pcm[i];
      if (nonzero[i] != 0) {
        for (int j = 0; j < n; j++) {
          pcm[j] *= window[j];
        }
      } else {
        for (int j = 0; j < n; j++) {
          pcm[j] = 0.f;
        }
      }
    }

    return 0;
  }

  private static int ilog2(int v) {
    int ret = 0;
    if (v > 0) v--;
    while (v > 0) {
      ret++;
      v >>>= 1;
    }
    return ret;
  }
}

class InfoMapping0 {
  int submaps; // <= 16
  int[] chmuxlist = new int[256]; // up to 256 channels in a Vorbis stream

  int[] timesubmap = new int[16]; // [mux]
  int[] floorsubmap = new int[16]; // [mux] submap to floors
  int[] residuesubmap = new int[16]; // [mux] submap to residue
  int[] psysubmap = new int[16]; // [mux]; encode only

  int couplingSteps;
  int[] couplingMag = new int[256];
  int[] couplingAng = new int[256];

  void free() {
    chmuxlist = null;
    timesubmap = null;
    floorsubmap = null;
    residuesubmap = null;
    psysubmap = null;

    couplingMag = null;
    couplingAng = null;
  }
}

class LookMapping0 {
  InfoMode mode;
  InfoMapping0 map;
  Object[] time_look;
  Object[] floor_look;
  Object[] floor_state;
  Object[] residue_look;
  PsyLook[] psy_look;

  FuncTime[] time_func;
  FuncFloor[] floor_func;
  FuncResidue[] residue_func;

  int ch;
  float[][] decay;
  int lastframe;
}
