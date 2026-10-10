/* Copyright (C) <2008> Maik Merten <maikmerten@googlemail.com>
 * Copyright (C) <2004> Wim Taymans <wim@fluendo.com> (HTTPSrc.java parts)
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
package com.fluendo.player;

import com.fluendo.utils.Base64Converter;
import com.fluendo.utils.Debug;
import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author maik
 */
public class DurationScanner {

  private long contentLength = -1;
  private long responseOffset;
  private final Map<Integer, StreamInfo> streamInfos = new HashMap<>();
  private final SyncState oy = new SyncState();
  private final Page og = new Page();
  private final Packet op = new Packet();
  private static final int HEAD_SCAN_BYTES = 64 * 1024;
  private static final int TAIL_SCAN_BYTES = 128 * 1024;
  private static final int BUFFER_SIZE = 1024;

  private enum StreamType {
      NOT_DETECTED,
      UNKNOWN,
      VORBIS,
      THEORA
  }

  public record TimingInfo(float startTime, float duration) {
    public TimingInfo() {
      this(-1, -1);
    }
  }

  public DurationScanner() {
    oy.init();
  }

  private InputStream openStream(URL url, String userId, String password, long offset)
      throws IOException {
    String userAgent = "Cortado";

    URLConnection uc = url.openConnection();
    uc.setRequestProperty("Connection", "Keep-Alive");

    String range;
    if (offset != 0 && contentLength != -1) {
      range = "bytes=" + offset + "-" + (contentLength - 1);
    } else if (offset != 0) {
      range = "bytes=" + offset + "-";
    } else {
      range = null;
    }
    if (range != null) {
      Debug.info("doing range: " + range);
      uc.setRequestProperty("Range", range);
    }

    uc.setRequestProperty("User-Agent", userAgent);
    if (userId != null && password != null) {
      String userPassword = userId + ":" + password;
      String encoding = Base64Converter.encode(userPassword.getBytes(StandardCharsets.UTF_8));
      uc.setRequestProperty("Authorization", "Basic " + encoding);
    }
    uc.setRequestProperty("Content-Type", "application/octet-stream");

    /* This will send the request. */
    InputStream inputStream = uc.getInputStream();

    String responseRange = uc.getHeaderField("Content-Range");
    if (responseRange == null) {
      Debug.info("Response contained no Content-Range field, assuming offset=0");
      responseOffset = 0;
    } else {
      try {
        MessageFormat format = new MessageFormat("bytes {0,number}-{1,number}");
        format.setLocale(Locale.US);
        Object[] parts = format.parse(responseRange);
        responseOffset = ((Number) parts[0]).longValue();
        if (responseOffset < 0) {
          responseOffset = 0;
        }
        Debug.debug("Stream successfully with offset " + responseOffset);
      } catch (Exception e) {
        Debug.info("Error parsing Content-Range header");
        responseOffset = 0;
      }
    }

    contentLength = uc.getHeaderFieldInt("Content-Length", -1) + responseOffset;

    return inputStream;
  }

  private void detectStreamType(Packet packet, StreamInfo streamInfo) {
    int ret;
    Class<?> decoderClass;

    if (streamInfo.decoder != null) {
      ret = streamInfo.decoder.takeHeader(packet);
      if (ret > 0) {
        streamInfo.ready = true;
      }
      return;
    }

    // try theora
    try {
      decoderClass = Class.forName("com.fluendo.plugin.TheoraDec");
      com.fluendo.plugin.OggPayload pl =
          (com.fluendo.plugin.OggPayload) decoderClass.getDeclaredConstructor().newInstance();
      ret = pl.takeHeader(packet);
      if (ret >= 0) {
        streamInfo.decoder = pl;
        streamInfo.type = StreamType.THEORA;
        return;
      }
    } catch (Throwable ignored) {
    }

    // try vorbis
    try {
      decoderClass = Class.forName("com.fluendo.plugin.VorbisDec");
      com.fluendo.plugin.OggPayload pl =
          (com.fluendo.plugin.OggPayload) decoderClass.getDeclaredConstructor().newInstance();
      ret = pl.takeHeader(packet);
      if (ret >= 0) {
        streamInfo.decoder = pl;
        streamInfo.type = StreamType.VORBIS;
        return;
      }
    } catch (Throwable ignored) {
    }

    streamInfo.type = StreamType.UNKNOWN;
  }

  public TimingInfo scanBuffer(byte[] buffer, int bytesRead) {
    long start = -1;
    long time = -1;

    int offset = oy.buffer(bytesRead);
    System.arraycopy(buffer, 0, oy.data, offset, bytesRead);
    oy.wrote(bytesRead);

    while (oy.pageOut(og) == 1) {
      int serialNumber = og.serialno();
      StreamInfo streamInfo = streamInfos.get(serialNumber);
      if (streamInfo == null) {
        streamInfo = new StreamInfo();
        streamInfo.streamState = new StreamState();
        streamInfo.streamState.init(og.serialno());
        streamInfos.put(serialNumber, streamInfo);
        Debug.info("DurationScanner: created StreamState for stream no. " + serialNumber);
      }

      streamInfo.streamState.pagein(og);

      while (streamInfo.streamState.packetout(op) == 1) {
          if (streamInfo.type == StreamType.NOT_DETECTED || !streamInfo.ready) {
              detectStreamType(op, streamInfo);
          } else if (streamInfo.type != StreamType.NOT_DETECTED
                  && streamInfo.type != StreamType.UNKNOWN
                  && streamInfo.ready
                  && streamInfo.startGranule < 0) {

              streamInfo.startGranule = og.granulepos();

              long thisStartTime =
                      streamInfo.decoder.granuleToTime(streamInfo.startGranule);

              if (start < 0 || thisStartTime < start) {
                  start = thisStartTime;
              }

              Debug.info(
                      "start granule for stream "
                              + og.serialno()
                              + ": "
                              + streamInfo.startGranule);
          }

          if (streamInfo.ready) {
              switch (streamInfo.type) {
                  case VORBIS, THEORA -> {
                      var payload = streamInfo.decoder;

                      long t =
                              payload.granuleToTime(og.granulepos())
                                      - payload.granuleToTime(streamInfo.startGranule);

                      if (t > time) {
                          time = t;
                      }
                  }
                  default -> {}
              }
          }
      }
    }

    return new TimingInfo(
        start / (float) com.fluendo.jst.Clock.SECOND, time / (float) com.fluendo.jst.Clock.SECOND);
  }

  public TimingInfo scanUrl(URL url, String user, String password) {
    try {
      float start = -1;
      float time = 0;
      long totalbytes = 0;

      byte[] buffer = new byte[BUFFER_SIZE];

      try (InputStream is = openStream(url, user, password, 0)) {
        int read = is.read(buffer);
        // read beginning of the stream
        while (totalbytes < HEAD_SCAN_BYTES && read > 0) {
          totalbytes += read;
          TimingInfo timingInfo = scanBuffer(buffer, read);
          if (timingInfo.duration() >= 0) {
            float t = timingInfo.duration();
            time = Math.max(t, time);
          }
          if (timingInfo.startTime() >= 0 && start < 0) {
            start = timingInfo.startTime();
          }
          read = is.read(buffer);
        }
      }

      try (InputStream is =
          openStream(url, user, password, Math.max(0, contentLength - TAIL_SCAN_BYTES))) {
        if (responseOffset == 0 && TAIL_SCAN_BYTES < contentLength) {
          Debug.warning(
              "DurationScanner: Couldn't complete duration scan due to failing range requests!");
          return new TimingInfo();
        }

        int read = is.read(buffer);
        // read tail until eos, also abort if way too many bytes have been read
        while (read > 0 && totalbytes < (HEAD_SCAN_BYTES + TAIL_SCAN_BYTES) * 2) {
          totalbytes += read;
          TimingInfo timingInfo = scanBuffer(buffer, read);
          if (timingInfo.duration() >= 0) {
            time = Math.max(timingInfo.duration(), time);
          }
          read = is.read(buffer);
        }
      }

      return new TimingInfo(start, time);
    } catch (IOException e) {
      Debug.error(e.toString());
      return new TimingInfo();
    }
  }

  private static class StreamInfo {
      com.fluendo.plugin.OggPayload decoder;
      StreamType type = StreamType.NOT_DETECTED;
      long startGranule = -1;
      StreamState streamState;
      boolean ready;
  }

  public static void main(String[] args) throws IOException {
    URL url = URI.create(args[0]).toURL();

    DurationScanner scanner = new DurationScanner();
    TimingInfo timingInfo = scanner.scanUrl(url, null, null);
    System.out.println(timingInfo.duration());
  }
}
