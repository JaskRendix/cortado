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

import com.fluendo.jst.*;
import com.fluendo.utils.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MultipartDemux extends Element {
  private static final String MIME = "multipart/x-mixed-replace";
  private static final String DEFAULT_BOUNDARY = "--ThisRandomString";

  private final List<MultipartStream> streams;

  private byte[] accum;
  private int accumSize;
  private int accumPos;
  private int dataEnd;

  private static final int STATE_FIND_BOUNDARY = 1;
  private static final int STATE_PARSE_HEADERS = 2;
  private static final int STATE_FIND_DATA_END = 3;

  private int state = STATE_FIND_BOUNDARY;
  private String boundaryString = DEFAULT_BOUNDARY;
  private byte[] boundary = boundaryString.getBytes(StandardCharsets.UTF_8);
  private int boundaryLen = boundary.length;

  private static final byte[] HEADER_END = "\n".getBytes(StandardCharsets.UTF_8);
  private static final int HEADER_END_LEN = HEADER_END.length;

  private static final String CONTENT_TYPE = "content-type: ";
  private static final int CONTENT_TYPE_LEN = CONTENT_TYPE.length();

  private MultipartStream currentStream = null;

  class MultipartStream extends Pad {
    private final String mimeType;

    public MultipartStream(String mime) {
      super(Pad.SRC, "src_" + mime);
      mimeType = mime;
      caps = new Caps(mime);
    }

    @Override
    protected boolean eventFunc(com.fluendo.jst.Event event) {
      return sinkpad.pushEvent(event);
    }
  }

  private final Pad sinkpad =
      new Pad(Pad.SINK, "sink") {

        @Override
        protected boolean setCapsFunc(Caps caps) {
          String mime = caps.getMime();
          String capsBoundary;

          if (!mime.equals(MIME)) {
            postMessage(Message.newError(this, "expected \"" + MIME + "\", got \"" + mime + "\""));
            return false;
          }

          capsBoundary = caps.getFieldString("boundary", DEFAULT_BOUNDARY);

          Debug.log(Debug.INFO, this + " boundary string: \"" + capsBoundary + "\"");

          boundaryString = capsBoundary + "\n";
          boundary = boundaryString.getBytes(StandardCharsets.UTF_8);
          boundaryLen = boundary.length;

          return true;
        }

        private MultipartStream findStream(String mime) {
          for (MultipartStream stream : streams) {
            if (stream.mimeType.equals(mime)) {
              return stream;
            }
          }
          return null;
        }

        private boolean forwardEvent(com.fluendo.jst.Event event) {
          for (MultipartStream stream : streams) {
            stream.pushEvent(event);
          }
          return true;
        }

        @Override
        protected boolean eventFunc(com.fluendo.jst.Event event) {
          switch (event.getType()) {
            case FLUSH_START -> {
              forwardEvent(event);
              synchronized (streamLock) {
                Debug.log(Debug.INFO, "synced " + this);
              }
            }
            case NEWSEGMENT, FLUSH_STOP, EOS -> {
              synchronized (streamLock) {
                forwardEvent(event);
              }
            }
            default -> forwardEvent(event);
          }
          return true;
        }

        /*
         * copy the buffer data into our buffer. If we need to enlarge the
         * buffer, we can flush out any skipped bytes
         */
        private void accumulateBuffer(com.fluendo.jst.Buffer buf) {
          int lastPos = accumSize + accumPos;

          /* make room */
          if (accum.length < lastPos + buf.length) {
            byte[] newAcum = new byte[accum.length + buf.length];
            System.arraycopy(accum, accumPos, newAcum, 0, accumSize);
            accum = newAcum;
            accumPos = 0;
            lastPos = accumSize;
          }
          System.arraycopy(buf.data, buf.offset, accum, lastPos, buf.length);
          accumSize += buf.length;
        }

        private void flushBytes(int bytes) {
          accumPos += bytes;
          accumSize -= bytes;
        }

        /*
         * find bytes of consecutive bytes in the buffer. This function returns
         * the position in the buffer where the bytes were found or -1 if the
         * bytes were not found.
         */
        private int findBytes(int startPos, byte[] bytes, int bytesLen) {
          int scanPos = startPos;
          int pos = 0;
          int size = accumSize;

          while (size > bytesLen) {
            if (accum[scanPos] == bytes[pos]) {
              pos++;
              if (pos == bytesLen) {
                return startPos;
              }
            } else {
              scanPos -= pos;
              size += pos;
              startPos = scanPos + 1;
              pos = 0;
            }
            scanPos++;
            size--;
          }
          return -1;
        }

        /*
         * find boundary bytes of consecutive bytes in the buffer. This function
         * returns true if the bytes where found with the accumPos position
         * pointing to the byte in the buffer.
         */
        private boolean findBoundary() {
          int pos = findBytes(accumPos, boundary, boundaryLen);
          if (pos != -1) {
            flushBytes(pos - accumPos);
          }
          return pos != -1;
        }

        /*
         * read the headers up to the first \n\n sequence. we store the
         * Content-Type: header in lastContentType
         */
        private boolean parseHeaders() {
          int headerStart = accumPos;
          int prevHdr;

          while (true) {
            prevHdr = headerStart;

            int pos = findBytes(headerStart, HEADER_END, HEADER_END_LEN);
            if (pos == -1) {
              return false;
            }

            if (pos == prevHdr) {
              /* all headers parsed */
              flushBytes(pos + 1 - accumPos);
              return true;
            }
            String header =
                new String(accum, headerStart, pos - headerStart, StandardCharsets.UTF_8);
            header = header.toLowerCase();

            if (header.startsWith(CONTENT_TYPE)) {
              String mime = header.substring(CONTENT_TYPE_LEN).trim();

              currentStream = findStream(mime);
              if (currentStream == null) {
                currentStream = new MultipartStream(mime);
                streams.add(currentStream);
                addPad(currentStream);
              }
            }

            /* go to next header */
            headerStart = pos + 1;
          }
        }

        private boolean findDataEnd() {
          int pos = findBytes(accumPos, boundary, boundaryLen);
          if (pos != -1) {
            dataEnd = pos - 1;
          }
          return pos != -1;
        }

        @Override
        protected int chainFunc(com.fluendo.jst.Buffer buf) {
          int flowRet = OK;

          accumulateBuffer(buf);
          buf.free();

          switch (state) {
            case STATE_FIND_BOUNDARY -> {
              if (!findBoundary()) {
                break;
              }
              /* skip boundary */
              flushBytes(boundary.length);
              state = STATE_PARSE_HEADERS;
              /* fallthrough */
            }
            case STATE_PARSE_HEADERS -> {
              if (!parseHeaders()) {
                break;
              }
              state = STATE_FIND_DATA_END;
              /* fallthrough */
            }
            case STATE_FIND_DATA_END -> {
              if (!findDataEnd()) {
                break;
              }

              com.fluendo.jst.Buffer data = com.fluendo.jst.Buffer.create();
              int dataSize = dataEnd - accumPos;

              data.copyData(accum, accumPos, dataSize);
              data.time_offset = -1;
              data.timestamp = -1;

              /* skip data */
              flushBytes(dataSize);

              /* and push */
              flowRet = currentStream.push(data);
              state = STATE_FIND_BOUNDARY;
              break;
            }
            default -> {
              flowRet = ERROR;
              break;
            }
          }
          return flowRet;
        }
      };

  @Override
  public String getFactoryName() {
    return "multipartdemux";
  }

  @Override
  public String getMime() {
    return MIME;
  }

  @Override
  public int typeFind(byte[] data, int offset, int length) {
    return -1;
  }

  public MultipartDemux() {
    super();

    accum = new byte[8192];
    accumSize = 0;
    accumPos = 0;

    streams = new ArrayList<>();

    addPad(sinkpad);
  }
}
