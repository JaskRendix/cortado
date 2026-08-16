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

import com.jcraft.jogg.*;
import java.util.Arrays;

public final class Block {

  /// Necessary stream state for linking to the framing abstraction
  public float[][] pcm = new float[0][]; // Pointer into local storage
  public final Buffer opb = new Buffer();

  public int lW;
  public int W;
  public int nW;
  public int pcmend;
  public int mode;

  public int eofflag;
  public long granulepos;
  public long sequence;
  public DspState vd; // For read-only access of configuration

  // Bit metrics for the frame
  public int glue_bits;
  public int time_bits;
  public int floor_bits;
  public int res_bits;

  public Block(DspState vd) {
    this.vd = vd;
    if (vd.analysisp != 0) {
      opb.writeInit();
    }
  }

  public void init(DspState vd) {
    this.vd = vd;
  }

  public int clear() {
    if (vd != null) {
      if (vd.analysisp != 0) {
        opb.writeInit();
      }
    }
    return 0;
  }

  public int synthesis(Packet op) {
    Info vi = vd.vi;

    // First things first. Make sure decode is ready
    opb.readInit(op.packetBase, op.packet, op.bytes);

    // Check the packet type
    if (opb.read(1) != 0) {
      // Oops. This is not an audio data packet
      return -1;
    }

    // Read our mode and pre/post window size
    int _mode = opb.read(vd.modebits);
    if (_mode == -1) return -1;

    mode = _mode;
    W = vi.getModeParam()[mode].getBlockflag();
    if (W != 0) {
      lW = opb.read(1);
      nW = opb.read(1);
      if (nW == -1) return -1;
    } else {
      lW = 0;
      nW = 0;
    }

    // More setup
    granulepos = op.granulepos;
    sequence = op.packetNo - 3; // First block is third packet
    eofflag = op.e_o_s;

    // Allocate pcm passback storage
    pcmend = vi.getBlocksizes()[W];
    if (pcm.length < vi.getChannels()) {
      pcm = new float[vi.getChannels()][];
    }
    for (int i = 0; i < vi.getChannels(); i++) {
      if (pcm[i] == null || pcm[i].length < pcmend) {
        pcm[i] = new float[pcmend];
      } else {
        Arrays.fill(pcm[i], 0, pcmend, 0.0f);
      }
    }

    // Unpack_header enforces range checking
    int type = vi.getMapType()[vi.getModeParam()[mode].getMapping()];
    return FuncMapping.MAPPING_P[type].inverse(this, vd.mode[mode]);
  }
}
