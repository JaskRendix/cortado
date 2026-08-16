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

package com.jcraft.jogg;

import java.util.Arrays;

public class StreamState {
  byte[] bodyData; /* bytes from packet bodies */
  int bodyStorage; /* storage elements allocated */
  int bodyFill; /* elements stored; fill mark */
  private int bodyReturned; /* elements of fill returned */

  int[] lacingVals; /* The values that will go to the segment table */
  long[] granuleVals; /* pcm_pos values for headers */
  int lacingStorage;
  int lacingFill;
  int lacingPacket;
  int lacingReturned;

  byte[] header = new byte[282]; /* working space for header encode */
  int headerFill;

  public int e_o_s; /* set when we have buffered the last packet in the logical bitstream */
  int b_o_s; /* set after we've written the initial page of a logical bitstream */
  int serialno;
  int pageno;
  long packetno; /* sequence number for decode */
  long granulepos;

  public StreamState() {
    init();
  }

  StreamState(int serialno) {
    this();
    init(serialno);
  }

  void init() {
    bodyStorage = 16 * 1024;
    bodyData = new byte[bodyStorage];
    lacingStorage = 1024;
    lacingVals = new int[lacingStorage];
    granuleVals = new long[lacingStorage];
  }

  public void init(int serialno) {
    if (bodyData == null) {
      init();
    } else {
      Arrays.fill(bodyData, (byte) 0);
      Arrays.fill(lacingVals, 0);
      Arrays.fill(granuleVals, 0L);
    }
    this.serialno = serialno;
  }

  public void clear() {
    bodyData = null;
    lacingVals = null;
    granuleVals = null;
  }

  void destroy() {
    clear();
  }

  void bodyExpand(int needed) {
    if (bodyStorage <= bodyFill + needed) {
      bodyStorage += (needed + 1024);
      byte[] foo = new byte[bodyStorage];
      System.arraycopy(bodyData, 0, foo, 0, bodyData.length);
      bodyData = foo;
    }
  }

  void lacingExpand(int needed) {
    if (lacingStorage <= lacingFill + needed) {
      lacingStorage += (needed + 32);
      int[] foo = new int[lacingStorage];
      System.arraycopy(lacingVals, 0, foo, 0, lacingVals.length);
      lacingVals = foo;

      long[] bar = new long[lacingStorage];
      System.arraycopy(granuleVals, 0, bar, 0, granuleVals.length);
      granuleVals = bar;
    }
  }

  /* Submit data to the internal buffer of the framing engine */
  public int packetin(Packet op) {
    int lacingVal = op.bytes / 255 + 1;

    if (bodyReturned != 0) {
      bodyFill -= bodyReturned;
      if (bodyFill != 0) {
        System.arraycopy(bodyData, bodyReturned, bodyData, 0, bodyFill);
      }
      bodyReturned = 0;
    }

    bodyExpand(op.bytes);
    lacingExpand(lacingVal);

    System.arraycopy(op.packet_base, op.packet, bodyData, bodyFill, op.bytes);
    bodyFill += op.bytes;

    int j;
    for (j = 0; j < lacingVal - 1; j++) {
      lacingVals[lacingFill + j] = 255;
      granuleVals[lacingFill + j] = granulepos;
    }
    lacingVals[lacingFill + j] = op.bytes % 255;
    granulepos = granuleVals[lacingFill + j] = op.granulepos;

    lacingVals[lacingFill] |= 0x100;
    lacingFill += lacingVal;
    packetno++;

    if (op.e_o_s != 0) {
      e_o_s = 1;
    }
    return 0;
  }

  public int packetout(Packet op) {
    int ptr = lacingReturned;

    if (lacingPacket <= ptr) {
      return 0;
    }

    if ((lacingVals[ptr] & 0x400) != 0) {
      lacingReturned++;
      packetno++;
      return -1;
    }

    int size = lacingVals[ptr] & 0xff;
    int bytes = size;

    op.packet_base = bodyData;
    op.packet = bodyReturned;
    op.e_o_s = lacingVals[ptr] & 0x200;
    op.b_o_s = lacingVals[ptr] & 0x100;

    while (size == 255) {
      int val = lacingVals[++ptr];
      size = val & 0xff;
      if ((val & 0x200) != 0) {
        op.e_o_s = 0x200;
      }
      bytes += size;
    }

    op.packetno = packetno;
    op.granulepos = granuleVals[ptr];
    op.bytes = bytes;

    bodyReturned += bytes;
    lacingReturned = ptr + 1;

    packetno++;
    return 1;
  }

  public int pagein(Page og) {
    byte[] headerBase = og.header_base;
    int header = og.header;
    byte[] bodyBase = og.body_base;
    int body = og.body;
    int bodysize = og.body_len;
    int segptr = 0;

    int version = og.version();
    int continued = og.continued();
    int bos = og.bos();
    int eos = og.eos();
    long granulepos = og.granulepos();
    int _serialno = og.serialno();
    int _pageno = og.pageno();
    int segments = headerBase[header + 26] & 0xff;

    int lr = lacingReturned;
    int br = bodyReturned;

    if (br != 0) {
      bodyFill -= br;
      if (bodyFill != 0) {
        System.arraycopy(bodyData, br, bodyData, 0, bodyFill);
      }
      bodyReturned = 0;
    }

    if (lr != 0) {
      if ((lacingFill - lr) != 0) {
        System.arraycopy(lacingVals, lr, lacingVals, 0, lacingFill - lr);
        System.arraycopy(granuleVals, lr, granuleVals, 0, lacingFill - lr);
      }
      lacingFill -= lr;
      lacingPacket -= lr;
      lacingReturned = 0;
    }

    if (_serialno != serialno || version > 0) {
      return -1;
    }

    lacingExpand(segments + 1);

    if (_pageno != pageno) {
      for (int i = lacingPacket; i < lacingFill; i++) {
        bodyFill -= lacingVals[i] & 0xff;
      }
      lacingFill = lacingPacket;

      if (pageno != -1) {
        lacingVals[lacingFill++] = 0x400;
        lacingPacket++;
      }
    }

    if (continued != 0) {
      if (lacingFill < 1 || lacingVals[lacingFill - 1] == 0x400) {
        bos = 0;
        for (; segptr < segments; segptr++) {
          int val = headerBase[header + 27 + segptr] & 0xff;
          body += val;
          bodysize -= val;
          if (val < 255) {
            segptr++;
            break;
          }
        }
      }
    }

    if (bodysize != 0) {
      bodyExpand(bodysize);
      System.arraycopy(bodyBase, body, bodyData, bodyFill, bodysize);
      bodyFill += bodysize;
    }

    int saved = -1;
    while (segptr < segments) {
      int val = headerBase[header + 27 + segptr] & 0xff;
      lacingVals[lacingFill] = val;
      granuleVals[lacingFill] = -1;

      if (bos != 0) {
        lacingVals[lacingFill] |= 0x100;
        bos = 0;
      }

      if (val < 255) {
        saved = lacingFill;
      }

      lacingFill++;
      segptr++;

      if (val < 255) {
        lacingPacket = lacingFill;
      }
    }

    if (saved != -1) {
      granuleVals[saved] = granulepos;
    }

    if (eos != 0) {
      e_o_s = 1;
      if (lacingFill > 0) {
        lacingVals[lacingFill - 1] |= 0x200;
      }
    }

    pageno = _pageno + 1;
    return 0;
  }

  public int flush(Page og) {
    int vals = 0;
    int maxvals = (lacingFill > 255) ? 255 : lacingFill;
    int bytes = 0;
    int acc = 0;
    long granulePos = granuleVals[0];

    if (maxvals == 0) {
      return 0;
    }

    if (b_o_s == 0) {
      granulePos = 0;
      for (vals = 0; vals < maxvals; vals++) {
        if ((lacingVals[vals] & 0x0ff) < 255) {
          vals++;
          break;
        }
      }
    } else {
      for (vals = 0; vals < maxvals; vals++) {
        if (acc > 4096) break;
        acc += (lacingVals[vals] & 0x0ff);
        granulePos = granuleVals[vals];
      }
    }

    System.arraycopy("OggS".getBytes(), 0, header, 0, 4);

    header[4] = 0x00;

    header[5] = 0x00;
    if ((lacingVals[0] & 0x100) == 0) header[5] |= 0x01;
    if (b_o_s == 0) header[5] |= 0x02;
    if (e_o_s != 0 && lacingFill == vals) header[5] |= 0x04;
    b_o_s = 1;

    for (int i = 6; i < 14; i++) {
      header[i] = (byte) granulePos;
      granulePos >>>= 8;
    }

    int _serialno = serialno;
    for (int i = 14; i < 18; i++) {
      header[i] = (byte) _serialno;
      _serialno >>>= 8;
    }

    if (pageno == -1) pageno = 0;
    int _pageno = pageno++;
    for (int i = 18; i < 22; i++) {
      header[i] = (byte) _pageno;
      _pageno >>>= 8;
    }

    header[22] = 0;
    header[23] = 0;
    header[24] = 0;
    header[25] = 0;

    header[26] = (byte) vals;
    for (int i = 0; i < vals; i++) {
      header[i + 27] = (byte) lacingVals[i];
      bytes += (header[i + 27] & 0xff);
    }

    og.header_base = header;
    og.header = 0;
    og.header_len = headerFill = vals + 27;
    og.body_base = bodyData;
    og.body = bodyReturned;
    og.body_len = bytes;

    lacingFill -= vals;
    System.arraycopy(lacingVals, vals, lacingVals, 0, lacingFill);
    System.arraycopy(granuleVals, vals, granuleVals, 0, lacingFill);
    bodyReturned += bytes;

    og.checksum();
    return 1;
  }

  public int pageout(Page og) {
    if ((e_o_s != 0 && lacingFill != 0)
        || (bodyFill - bodyReturned > 4096)
        || (lacingFill >= 255)
        || (lacingFill != 0 && b_o_s == 0)) {
      return flush(og);
    }
    return 0;
  }

  public int eof() {
    return e_o_s;
  }

  public int reset() {
    bodyFill = 0;
    bodyReturned = 0;

    lacingFill = 0;
    lacingPacket = 0;
    lacingReturned = 0;

    headerFill = 0;

    e_o_s = 0;
    b_o_s = 0;
    pageno = -1;
    packetno = 0;
    granulepos = 0;
    return 0;
  }
}
