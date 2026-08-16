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

import com.jcraft.jogg.*;

public class Info {
  private static final int OV_EBADPACKET = -136;
  private static final int OV_ENOTAUDIO = -135;

  private static final byte[] VORBIS_HEADER_BYTES = "vorbis".getBytes();
  private static final int VI_TIMEB = 1;
  private static final int VI_FLOORB = 2;
  private static final int VI_RESB = 3;
  private static final int VI_MAPB = 1;
  private static final int VI_WINDOWB = 1;

  private int version;
  private int channels;
  private int rate;

  // The below bitrate declarations are *hints*.
  // Combinations of the three values carry the following implications:
  //
  // all three set to the same value:
  // implies a fixed rate bitstream
  // only nominal set:
  // implies a VBR stream that averages the nominal bitrate.  No hard
  // upper/lower limit
  // upper and or lower set:
  // implies a VBR bitstream that obeys the bitrate limits. nominal
  // may also be set to give a nominal rate.
  // none set:
  //  the coder does not care to speculate.

  private int bitrateUpper;
  private int bitrateNominal;
  private int bitrateLower;

  // Vorbis supports only short and long blocks, but allows the
  // encoder to choose the sizes

  private final int[] blocksizes = new int[2];

  // modes are the primary means of supporting on-the-fly different
  // blocksizes, different channel mappings (LR or mid-side),
  // different residue backends, etc.  Each mode consists of a
  // blocksize flag and a mapping (along with the mapping setup

  private int modes;
  private int maps;
  private int times;
  private int floors;
  private int residues;
  private int books;
  private int psys; // encode only

  private InfoMode[] modeParam = null;

  private int[] mapType = null;
  private Object[] mapParam = null;

  private int[] timeType = null;
  private Object[] timeParam = null;

  private int[] floorType = null;
  private Object[] floorParam = null;

  private int[] residueType = null;
  private Object[] residueParam = null;

  private StaticCodeBook[] bookParam = null;

  private final PsyInfo[] psyParam = new PsyInfo[64]; // encode only

  // for block long/sort tuning; encode only
  private int envelopesa;
  private float preechoThresh;
  private float preechoClamp;

  public int getVersion() {
    return version;
  }

  public int getChannels() {
    return channels;
  }

  public int getRate() {
    return rate;
  }

  public int getBitrateUpper() {
    return bitrateUpper;
  }

  public int getBitrateNominal() {
    return bitrateNominal;
  }

  public int getBitrateLower() {
    return bitrateLower;
  }

  public int[] getBlocksizes() {
    return blocksizes;
  }

  public int getModes() {
    return modes;
  }

  public int getMaps() {
    return maps;
  }

  public int getTimes() {
    return times;
  }

  public int getFloors() {
    return floors;
  }

  public int getResidues() {
    return residues;
  }

  public int getBooks() {
    return books;
  }

  public InfoMode[] getModeParam() {
    return modeParam;
  }

  public int[] getMapType() {
    return mapType;
  }

  public Object[] getMapParam() {
    return mapParam;
  }

  public int[] getTimeType() {
    return timeType;
  }

  public Object[] getTimeParam() {
    return timeParam;
  }

  public int[] getFloorType() {
    return floorType;
  }

  public Object[] getFloorParam() {
    return floorParam;
  }

  public int[] getResidueType() {
    return residueType;
  }

  public Object[] getResidueParam() {
    return residueParam;
  }

  public StaticCodeBook[] getBookParam() {
    return bookParam;
  }

  // used by synthesis, which has a full, alloced vi
  public void init() {
    rate = 0;
  }

  public void setChannels(int channels) {
    this.channels = channels;
  }

  public void setRate(int rate) {
    this.rate = rate;
  }

  public void setModes(int modes) {
    this.modes = modes;
  }

  public void setBooks(int books) {
    this.books = books;
  }

  public void setTimes(int times) {
    this.times = times;
  }

  public void setFloors(int floors) {
    this.floors = floors;
  }

  public void setResidues(int residues) {
    this.residues = residues;
  }

  public void setBlocksizes(int b0, int b1) {
    this.blocksizes[0] = b0;
    this.blocksizes[1] = b1;
  }

  public void setModeParam(InfoMode[] modeParam) {
    this.modeParam = modeParam;
  }

  public void setMapType(int[] mapType) {
    this.mapType = mapType;
  }

  public void setMapParam(Object[] mapParam) {
    this.mapParam = mapParam;
  }

  public void setTimeType(int[] timeType) {
    this.timeType = timeType;
  }

  public void setTimeParam(Object[] timeParam) {
    this.timeParam = timeParam;
  }

  public void setFloorType(int[] floorType) {
    this.floorType = floorType;
  }

  public void setFloorParam(Object[] floorParam) {
    this.floorParam = floorParam;
  }

  public void setResidueType(int[] residueType) {
    this.residueType = residueType;
  }

  public void setResidueParam(Object[] residueParam) {
    this.residueParam = residueParam;
  }

  public void setBookParam(StaticCodeBook[] bookParam) {
    this.bookParam = bookParam;
  }

  public void clear() {
    if (modeParam != null) {
      for (int i = 0; i < modes; i++) {
        modeParam[i] = null;
      }
      modeParam = null;
    }

    if (mapParam != null) {
      for (int i = 0; i < maps; i++) {
        FuncMapping.MAPPING_P[mapType[i]].freeInfo(mapParam[i]);
      }
      mapParam = null;
    }

    if (timeParam != null) {
      for (int i = 0; i < times; i++) {
        FuncTime.TIME_P[timeType[i]].freeInfo(timeParam[i]);
      }
      timeParam = null;
    }

    if (floorParam != null) {
      for (int i = 0; i < floors; i++) {
        FuncFloor.FLOOR_P[floorType[i]].freeInfo(floorParam[i]);
      }
      floorParam = null;
    }

    if (residueParam != null) {
      for (int i = 0; i < residues; i++) {
        FuncResidue.RESIDUE_P[residueType[i]].freeInfo(residueParam[i]);
      }
      residueParam = null;
    }

    if (bookParam != null) {
      for (int i = 0; i < books; i++) {
        if (bookParam[i] != null) {
          bookParam[i].clear();
          bookParam[i] = null;
        }
      }
      bookParam = null;
    }

    for (int i = 0; i < psys; i++) {
      if (psyParam[i] != null) {
        psyParam[i].free();
      }
    }
  }

  // Header packing/unpacking
  int unpackInfo(Buffer opb) {
    version = opb.read(32);
    if (version != 0) return -1;

    channels = opb.read(8);
    rate = opb.read(32);

    bitrateUpper = opb.read(32);
    bitrateNominal = opb.read(32);
    bitrateLower = opb.read(32);

    blocksizes[0] = 1 << opb.read(4);
    blocksizes[1] = 1 << opb.read(4);

    if ((rate < 1)
        || (channels < 1)
        || (blocksizes[0] < 8)
        || (blocksizes[1] < blocksizes[0])
        || (opb.read(1) != 1)) {
      clear();
      return -1;
    }
    return 0;
  }

  int unpackBooks(Buffer opb) {
    books = opb.read(8) + 1;

    if (bookParam == null || bookParam.length != books) bookParam = new StaticCodeBook[books];
    for (int i = 0; i < books; i++) {
      bookParam[i] = new StaticCodeBook();
      if (bookParam[i].unpack(opb) != 0) {
        clear();
        return -1;
      }
    }

    // time backend settings
    times = opb.read(6) + 1;
    if (timeType == null || timeType.length != times) timeType = new int[times];
    if (timeParam == null || timeParam.length != times) timeParam = new Object[times];
    for (int i = 0; i < times; i++) {
      timeType[i] = opb.read(16);
      if (timeType[i] < 0 || timeType[i] >= VI_TIMEB) {
        clear();
        return -1;
      }
      timeParam[i] = FuncTime.TIME_P[timeType[i]].unpack(this, opb);
      if (timeParam[i] == null) {
        clear();
        return -1;
      }
    }

    // floor backend settings
    floors = opb.read(6) + 1;
    if (floorType == null || floorType.length != floors) floorType = new int[floors];
    if (floorParam == null || floorParam.length != floors) floorParam = new Object[floors];

    for (int i = 0; i < floors; i++) {
      floorType[i] = opb.read(16);
      if (floorType[i] < 0 || floorType[i] >= VI_FLOORB) {
        clear();
        return -1;
      }

      floorParam[i] = FuncFloor.FLOOR_P[floorType[i]].unpack(this, opb);
      if (floorParam[i] == null) {
        clear();
        return -1;
      }
    }

    // residue backend settings
    residues = opb.read(6) + 1;

    if (residueType == null || residueType.length != residues) residueType = new int[residues];

    if (residueParam == null || residueParam.length != residues)
      residueParam = new Object[residues];

    for (int i = 0; i < residues; i++) {
      residueType[i] = opb.read(16);
      if (residueType[i] < 0 || residueType[i] >= VI_RESB) {
        clear();
        return -1;
      }
      residueParam[i] = FuncResidue.RESIDUE_P[residueType[i]].unpack(this, opb);
      if (residueParam[i] == null) {
        clear();
        return -1;
      }
    }

    // map backend settings
    maps = opb.read(6) + 1;
    if (mapType == null || mapType.length != maps) mapType = new int[maps];
    if (mapParam == null || mapParam.length != maps) mapParam = new Object[maps];
    for (int i = 0; i < maps; i++) {
      mapType[i] = opb.read(16);
      if (mapType[i] < 0 || mapType[i] >= VI_MAPB) {
        clear();
        return -1;
      }
      mapParam[i] = FuncMapping.MAPPING_P[mapType[i]].unpack(this, opb);
      if (mapParam[i] == null) {
        clear();
        return -1;
      }
    }

    // mode settings
    modes = opb.read(6) + 1;
    if (modeParam == null || modeParam.length != modes) modeParam = new InfoMode[modes];
    for (int i = 0; i < modes; i++) {
      modeParam[i] = new InfoMode();
      modeParam[i].setBlockflag(opb.read(1));
      modeParam[i].setWindowtype(opb.read(16));
      modeParam[i].setTransformtype(opb.read(16));
      modeParam[i].setMapping(opb.read(8));

      if ((modeParam[i].getWindowtype() >= VI_WINDOWB)
          || (modeParam[i].getTransformtype() >= VI_WINDOWB)
          || (modeParam[i].getMapping() >= maps)) {
        clear();
        return -1;
      }
    }

    if (opb.read(1) != 1) {
      clear();
      return -1;
    }

    return 0;
  }

  public int synthesisHeaderIn(Comment vc, Packet op) {
    Buffer opb = new Buffer();

    if (op != null) {
      opb.readInit(op.packetBase, op.packet, op.bytes);

      int packtype = opb.read(8);
      byte[] buffer = new byte[6];
      opb.read(buffer, 6);

      if (buffer[0] != 'v'
          || buffer[1] != 'o'
          || buffer[2] != 'r'
          || buffer[3] != 'b'
          || buffer[4] != 'i'
          || buffer[5] != 's') {
        return -1;
      }

      switch (packtype) {
        case 0x01:
          if (op.bos == 0) return -1;
          if (rate != 0) return -1;
          return unpackInfo(opb);
        case 0x03:
          if (rate == 0) return -1;
          return vc.unpack(opb);
        case 0x05:
          if (rate == 0 || vc.vendor == null) return -1;
          return unpackBooks(opb);
        default:
          break;
      }
    }
    return -1;
  }

  int packInfo(Buffer opb) {
    opb.write(0x01, 8);
    opb.write(VORBIS_HEADER_BYTES);

    opb.write(0x00, 32);
    opb.write(channels, 8);
    opb.write(rate, 32);

    opb.write(bitrateUpper, 32);
    opb.write(bitrateNominal, 32);
    opb.write(bitrateLower, 32);

    opb.write(ilog2(blocksizes[0]), 4);
    opb.write(ilog2(blocksizes[1]), 4);
    opb.write(1, 1);
    return 0;
  }

  int packBooks(Buffer opb) {
    opb.write(0x05, 8);
    opb.write(VORBIS_HEADER_BYTES);

    opb.write(books - 1, 8);
    for (int i = 0; i < books; i++) {
      if (bookParam[i].pack(opb) != 0) {
        return -1;
      }
    }

    opb.write(times - 1, 6);
    for (int i = 0; i < times; i++) {
      opb.write(timeType[i], 16);
      FuncTime.TIME_P[timeType[i]].pack(this.timeParam[i], opb);
    }

    opb.write(floors - 1, 6);
    for (int i = 0; i < floors; i++) {
      opb.write(floorType[i], 16);
      FuncFloor.FLOOR_P[floorType[i]].pack(floorParam[i], opb);
    }

    opb.write(residues - 1, 6);
    for (int i = 0; i < residues; i++) {
      opb.write(residueType[i], 16);
      FuncResidue.RESIDUE_P[residueType[i]].pack(residueParam[i], opb);
    }

    opb.write(maps - 1, 6);
    for (int i = 0; i < maps; i++) {
      opb.write(mapType[i], 16);
      FuncMapping.MAPPING_P[mapType[i]].pack(this, mapParam[i], opb);
    }

    opb.write(modes - 1, 6);
    for (int i = 0; i < modes; i++) {
      opb.write(modeParam[i].getBlockflag(), 1);
      opb.write(modeParam[i].getWindowtype(), 16);
      opb.write(modeParam[i].getTransformtype(), 16);
      opb.write(modeParam[i].getMapping(), 8);
    }
    opb.write(1, 1);
    return 0;
  }

  public int blocksize(Packet op) {
    Buffer opb = new Buffer();
    int mode;

    opb.readInit(op.packetBase, op.packet, op.bytes);

    if (opb.read(1) != 0) {
      return OV_ENOTAUDIO;
    }

    int modebits = 0;
    int v = modes;
    while (v > 1) {
      modebits++;
      v >>>= 1;
    }

    mode = opb.read(modebits);

    if (mode == -1) return OV_EBADPACKET;
    return blocksizes[modeParam[mode].getBlockflag()];
  }

  private static int ilog2(int v) {
    int ret = 0;
    while (v > 1) {
      ret++;
      v >>>= 1;
    }
    return ret;
  }

  @Override
  public String toString() {
    return "version:"
        + version
        + ", channels:"
        + channels
        + ", rate:"
        + rate
        + ", bitrate:"
        + bitrateUpper
        + ","
        + bitrateNominal
        + ","
        + bitrateLower;
  }
}
