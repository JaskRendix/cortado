/* Copyright (C) <2008> ogg.k.ogg.k <ogg.k.ogg.k@googlemail.com>
 * based on code Copyright (C) <2004> Wim Taymans <wim@fluendo.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package com.fluendo.plugin;

import com.fluendo.jkate.*;
import com.fluendo.jst.*;
import com.fluendo.utils.*;
import com.jcraft.jogg.*;
import java.util.*;

/**
 * Katedec is a decoder element for the Kate stream format. See
 * http://wiki.xiph.org/index.php/OggKate for more information. Kate streams may be multiplexed in
 * Ogg. The Katedec element accepts Kate packets (presumably demultiplexed by an Ogg demuxer
 * element) on its sink, and generates Kate events on its source. Kate events are Kate specific
 * structures, which may then be interpreted by a renderer.
 */
public class KateDec extends Element implements OggPayload {

  /* Kate magic: 0x80 (BOS header) followed by "kate\0\0\0" */
  private static final byte[] signature = {-128, 0x6b, 0x61, 0x74, 0x65, 0x00, 0x00, 0x00};

  private Info ki;
  private Comment kc;
  private State k;
  private Packet op;
  private int packetNo;

  private long basetime = 0;
  private long lastTs;
  private boolean haveBOS = false;
  private boolean haveDecoder = false;

  /*
   * OggPayload interface
   */
  @Override
  public boolean isType(Packet op) {
    return typeFind(op.packetBase, op.packet, op.bytes) > 0;
  }

  @Override
  public boolean isKeyFrame(Packet op) {
    return true;
  }

  /** A discontinuous codec will not cause the pipeline to wait for data if starving */
  @Override
  public boolean isDiscontinuous() {
    return true;
  }

  @Override
  public int takeHeader(Packet op) {
    int ret = ki.decodeHeader(kc, op);
    if (ret >= 0) {
      haveBOS = true;
    }
    if (ret > 0) {
      k.decodeInit(ki);
      Debug.debug("Kate decoder ready");
      haveDecoder = true;
    }
    return ret;
  }

  @Override
  public boolean isHeader(Packet op) {
    return (op.packetBase[op.packet] & 0x80) == 0x80;
  }

  @Override
  public long getFirstTs(List<com.fluendo.jst.Buffer> packets) {
    int len = packets.size();
    int i;
    com.fluendo.jst.Buffer data = null;

    /* first find buffer with valid offset */
    for (i = 0; i < len; i++) {
      data = (com.fluendo.jst.Buffer) packets.get(i);

      if (data.time_offset != -1) break;
    }
    if (i == packets.size()) return -1;

    long time = granuleToTime(data.time_offset);

    data = (com.fluendo.jst.Buffer) packets.get(0);
    data.timestamp =
        time - (long) ((i + 1) * (Clock.SECOND * ki.gps_denominator / ki.gps_numerator));

    return time;
  }

  /** Converts a granule position to its time equivalent */
  public long granuleToTime(long gp) {
    long res;

    if (gp < 0 || !haveDecoder) return -1;

    res = (long) (k.granuleTime(gp) * Clock.SECOND);

    return res;
  }

  /** Converts a granule position to its duration equivalent */
  public long granuleToDuration(long gp) {
    long res;

    if (gp < 0 || !haveDecoder) return -1;

    res = (long) (k.granuleDuration(gp) * Clock.SECOND);

    return res;
  }

  private final Pad srcPad =
      new Pad(Pad.SRC, "src") {
        @Override
        protected boolean eventFunc(com.fluendo.jst.Event event) {
          return sinkPad.pushEvent(event);
        }
      };

  private final Pad sinkPad =
      new Pad(Pad.SINK, "sink") {
        @Override
        protected boolean eventFunc(com.fluendo.jst.Event event) {
          boolean result;

          switch (event.getType()) {
            case FLUSH_START:
              result = srcPad.pushEvent(event);
              synchronized (streamLock) {
                Debug.log(Debug.DEBUG, "synced " + this);
              }
              break;
            case FLUSH_STOP:
              result = srcPad.pushEvent(event);
              break;
            case EOS:
              Debug.log(Debug.INFO, "got EOS " + this);
              result = srcPad.pushEvent(event);
              break;
            case NEWSEGMENT:
              basetime = event.parseNewsegmentStart();
              Debug.info("new segment: base time " + basetime);
              result = srcPad.pushEvent(event);
              break;
            default:
              result = srcPad.pushEvent(event);
              break;
          }
          return result;
        }

        /** receives Kate packets, and generates Kate events */
        @Override
        protected int chainFunc(com.fluendo.jst.Buffer buf) {
          int result;
          long timestamp;

          Debug.log(Debug.DEBUG, parent.getName() + " <<< " + buf);

          op.packetBase = buf.data;
          op.packet = buf.offset;
          op.bytes = buf.length;
          op.b_o_s = (packetNo == 0 ? 1 : 0);
          op.e_o_s = 0;
          op.packetNo = packetNo;
          timestamp = buf.timestamp;

          Debug.log(
              Debug.DEBUG,
              "Kate chainFunc with packetNo " + packetNo + ", haveDecoder " + haveDecoder);

          if (!haveDecoder) {
            result = takeHeader(op);
            if (result < 0) {
              buf.free();
              Debug.log(Debug.ERROR, "does not contain Kate data.");
              return ERROR;
            } else if (result > 0) {
              Debug.log(Debug.DEBUG, "Kate initialized for decoding");
              caps = new Caps("application/x-kate-event");
            }
            buf.free();
            packetNo++;
            return OK;
          } else {
            if ((op.packetBase[op.packet] & 0x80) == 0x80) {
              Debug.log(Debug.DEBUG, "ignoring header");
              buf.free();
              return OK;
            }

            if (timestamp != -1) {
              lastTs = timestamp;
            }

            try {
              result = k.decodePacketin(op);
              if (result < 0) {
                buf.free();
                Debug.log(Debug.ERROR, "Error Decoding Kate.");
                postMessage(Message.newError(this, "Error decoding Kate"));
                return ERROR;
              }
              com.fluendo.jkate.Event ev = k.decodeEventOut();
              if (ev != null) {
                buf.object = ev;
                buf.caps = caps;
                buf.timestamp = granuleToDuration(ev.start);
                buf.timestampEnd = buf.timestamp + granuleToDuration(ev.duration);
                Debug.log(Debug.DEBUG, parent.getName() + " >>> " + buf);
                Debug.debug(
                    "Got Kate text: "
                        + new String(ev.text)
                        + " from "
                        + buf.timestamp
                        + " to "
                        + buf.timestampEnd
                        + ", basetime "
                        + basetime);
                result = srcPad.push(buf);
                Debug.log(Debug.DEBUG, "push returned " + result);
              } else {
                Debug.debug("Got no event");
                buf.free();
                result = OK;
              }
            } catch (Exception e) {
              e.printStackTrace();
              postMessage(Message.newError(this, e.getMessage()));
              result = ERROR;
            }
          }
          packetNo++;

          return result;
        }

        @Override
        protected boolean activateFunc(int mode) {
          return true;
        }
      };

  public KateDec() {
    super();

    ki = new Info();
    kc = new Comment();
    k = new State();
    op = new Packet();

    addPad(srcPad);
    addPad(sinkPad);
  }

  @Override
  protected int changeState(int transition) {
    int res;

    switch (transition) {
      case STOP_PAUSE:
        lastTs = -1;
        packetNo = 0;
        break;
      default:
        break;
    }

    res = super.changeState(transition);

    switch (transition) {
      case PAUSE_STOP:
        ki.clear();
        kc.clear();
        k.clear();
        break;
      default:
        break;
    }

    return res;
  }

  @Override
  public java.lang.Object getProperty(String name) {
    if (Objects.equals(name, "language")) {
      return ki.language;
    } else if (Objects.equals(name, "category")) {
      return ki.category;
    } else {
      return super.getProperty(name);
    }
  }

  @Override
  public String getFactoryName() {
    return "katedec";
  }

  @Override
  public String getMime() {
    return "application/x-kate";
  }

  @Override
  public String getMime(Packet op) {
    Info ki = new Info();
    Comment kc = new Comment();
    if (!isType(op)) return null;
    int ret = ki.decodeHeader(kc, op);
    if (ret < 0) return null;
    String mime = "application/x-kate";
    if (ki.language != null && !ki.language.equals("")) mime += ";language=" + ki.language;
    if (ki.category != null && !ki.category.equals("")) mime += ";category=" + ki.category;
    return mime;
  }

  @Override
  public int typeFind(byte[] data, int offset, int length) {
    if (MemUtils.startsWith(data, offset, length, signature)) return 10;
    return -1;
  }
}
