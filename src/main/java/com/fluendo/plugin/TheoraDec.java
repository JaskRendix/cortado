/* Copyright (C) <2004> Wim Taymans <wim@fluendo.com>
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

import com.fluendo.jheora.*;
import com.fluendo.jst.*;
import com.fluendo.utils.*;
import com.jcraft.jogg.*;
import java.util.*;

public class TheoraDec extends Element implements OggPayload {
  private static final byte[] signature = {-128, 0x74, 0x68, 0x65, 0x6f, 0x72, 0x61};

  private Info ti;
  private Comment tc;
  private State ts;
  private Packet op;
  private int packet;
  private YUVBuffer yuv;
  private java.lang.Object last_yuv_obj;

  private long lastTs;
  private long lastnondupe = -1;
  private boolean needKeyframe;
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
  public int takeHeader(Packet op) {
    int ret;
    byte header;
    ret = ti.decodeHeader(tc, op);
    if (ret < 0) {
      return ret;
    }
    header = op.packetBase[op.packet];
    if (header == -128) {
      haveBOS = true;
    } else if (header == -126) {
      ts.decodeInit(ti);
      haveDecoder = true;
    }
    return haveDecoder ? 1 : 0;
  }

  @Override
  public boolean isHeader(Packet op) {
    return (op.packetBase[op.packet] & 0x80) == 0x80;
  }

  @Override
  public boolean isKeyFrame(Packet op) {
    return ts.isKeyframe(op);
  }

  @Override
  public boolean isDiscontinuous() {
    return false;
  }

  @Override
  public long getFirstTs(List<com.fluendo.jst.Buffer> packets) {
    int len = packets.size();
    int i;
    com.fluendo.jst.Buffer data = null;

    /* first find buffer with valid offset */
    for (i = 0; i < len; i++) {
      data = packets.get(i);

      if (data.time_offset != -1) break;
    }
    if (i == packets.size()) return -1;

    long time = granuleToTime(data.time_offset);

    data = packets.get(0);
    data.timestamp =
        time - ((i + 1) * (Clock.SECOND * ti.fps_denominator / ti.fps_numerator));

    return time;
  }

  @Override
  public long granuleToTime(long gp) {
    if (gp < 0 || !haveBOS) return -1;

    long iframe = gp >> ti.keyframe_granule_shift;
    long pframe = gp - (iframe << ti.keyframe_granule_shift);

    return (long)
        ((iframe + pframe) * ((double) ti.fps_denominator / ti.fps_numerator) * Clock.SECOND);
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
            default:
              result = srcPad.pushEvent(event);
              break;
          }
          return result;
        }

        @Override
        protected int chainFunc(com.fluendo.jst.Buffer buf) {
          int result;
          long timestamp;

          Debug.log(Debug.DEBUG, parent.getName() + " <<< " + buf);

          op.packetBase = buf.data;
          op.packet = buf.offset;
          op.bytes = buf.length;
          op.b_o_s = (packet == 0 ? 1 : 0);
          op.e_o_s = 0;
          op.packetNo = packet;
          timestamp = buf.timestamp;

          if (buf.isFlagSet(com.fluendo.jst.Buffer.FLAG_DISCONT)) {
            Debug.log(Debug.INFO, "theora: got discont");
            needKeyframe = true;
            lastTs = -1;
          }

          if (packet < 3) {
            if (takeHeader(op) < 0) {
              buf.free();
              Debug.log(Debug.ERROR, "does not contain Theora video data.");
              return ERROR;
            }
            if (packet == 2) {
              ts.decodeInit(ti);

              Debug.log(Debug.INFO, "theora dimension: " + ti.width + "x" + ti.height);
              if (ti.aspect_denominator == 0) {
                ti.aspect_numerator = 1;
                ti.aspect_denominator = 1;
              }
              Debug.log(Debug.INFO, "theora offset: " + ti.offset_x + "," + ti.offset_y);
              Debug.log(Debug.INFO, "theora frame: " + ti.frame_width + "," + ti.frame_height);
              Debug.log(
                  Debug.INFO,
                  "theora aspect: " + ti.aspect_numerator + "/" + ti.aspect_denominator);
              Debug.log(
                  Debug.INFO, "theora framerate: " + ti.fps_numerator + "/" + ti.fps_denominator);

              caps = new Caps("video/raw");
              caps.setFieldInt("width", ti.frame_width);
              caps.setFieldInt("height", ti.frame_height);
              caps.setFieldInt("aspect_x", ti.aspect_numerator);
              caps.setFieldInt("aspect_y", ti.aspect_denominator);
            }
            buf.free();
            packet++;

            return OK;
          } else {
            if (timestamp != -1) {
              lastTs = timestamp;
            } else if (lastTs != -1) {
              long add;

              add = (Clock.SECOND * ti.fps_denominator) / ti.fps_numerator;
              lastTs += add;
              timestamp = lastTs;
            }

            if (op.bytes > 0) {
              if ((op.packetBase[op.packet] & 0x80) == 0x80) {
                Debug.log(Debug.INFO, "ignoring header");
                return OK;
              }
              if (needKeyframe && ts.isKeyframe(op)) {
                needKeyframe = false;
              }
            } else {
              Debug.log(Debug.DEBUG, "duplicate frame");
            }

            if (!needKeyframe) {
              try {
                if (ts.decodePacketin(op) != 0) {
                  Debug.log(
                      Debug.ERROR,
                      "Bad Theora packet. Most likely not fatal, hoping for better luck next packet.");
                }
                if (op.bytes > 0) {
                  if (ts.decodeYUVout(yuv) != 0) {
                    buf.free();
                    postMessage(Message.newError(this, "Error getting the Theora picture"));
                    Debug.log(Debug.ERROR, "Error getting the picture.");
                    return ERROR;
                  }
                  buf.duplicate = false;
                  lastnondupe = timestamp;
                  last_yuv_obj =
                      yuv.getObject(ti.offset_x, ti.offset_y, ti.frame_width, ti.frame_height);
                } else {
                  if (timestamp - lastnondupe >= Clock.SECOND) {
                    buf.duplicate = false;
                    lastnondupe = timestamp;
                  } else {
                    buf.duplicate = true;
                  }
                }
                buf.object = last_yuv_obj;
                buf.caps = caps;
                buf.timestamp = timestamp;
                Debug.log(Debug.DEBUG, parent.getName() + " >>> " + buf);
                result = srcPad.push(buf);
              } catch (Exception e) {
                e.printStackTrace();
                postMessage(Message.newError(this, e.getMessage()));
                result = ERROR;
              }
            } else {
              result = OK;
              buf.free();
            }
          }
          packet++;

          return result;
        }

        @Override
        protected boolean activateFunc(int mode) {
          return true;
        }
      };

  public TheoraDec() {
    super();

    ti = new Info();
    tc = new Comment();
    ts = new State();
    yuv = new YUVBuffer();
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
        packet = 0;
        needKeyframe = true;
        break;
      default:
        break;
    }

    res = super.changeState(transition);

    switch (transition) {
      case PAUSE_STOP:
        ti.clear();
        tc.clear();
        ts.clear();
        break;
      default:
        break;
    }

    return res;
  }

  @Override
  public String getFactoryName() {
    return "theoradec";
  }

  @Override
  public String getMime() {
    return "video/x-theora";
  }

  @Override
  public String getMime(Packet op) {
    if (!isType(op)) return null;
    return "video/x-theora";
  }

  @Override
  public int typeFind(byte[] data, int offset, int length) {
    if (MemUtils.startsWith(data, offset, length, signature)) return 10;
    return -1;
  }
}
