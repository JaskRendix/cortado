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
import java.io.InputStream;

public class VorbisFile {

  static final int CHUNKSIZE = 8500;
  static final int SEEK_SET = 0;
  static final int SEEK_CUR = 1;
  static final int SEEK_END = 2;
  static final int OV_FALSE = -1;
  static final int OV_EOF = -2;
  static final int OV_HOLE = -3;
  static final int OV_EREAD = -128;
  static final int OV_EFAULT = -129;
  static final int OV_EIMPL = -130;
  static final int OV_EINVAL = -131;
  static final int OV_ENOTVORBIS = -132;
  static final int OV_EBADHEADER = -133;
  static final int OV_EVERSION = -134;
  static final int OV_ENOTAUDIO = -135;
  static final int OV_EBADPACKET = -136;
  static final int OV_EBADLINK = -137;
  static final int OV_ENOSEEK = -138;

  InputStream datasource;
  boolean seekable = false;
  long offset;
  long end;
  SyncState oy = new SyncState();
  int links;
  long[] offsets;
  long[] dataoffsets;
  int[] serialnos;
  long[] pcmlengths;
  Info[] vi;
  Comment[] vc;
  long pcmOffset;
  boolean decodeReady = false;
  int currentSerialno;
  int currentLink;
  float bittrack;
  float samptrack;
  StreamState os = new StreamState();
  DspState vd = new DspState();
  Block vb = new Block(vd);

  public VorbisFile(String file) throws JOrbisException {
    super();
    InputStream is = null;
    try {
      is = new SeekableInputStream(file);
    } catch (Exception e) {
      throw new JOrbisException("VorbisFile: " + e.toString());
    }
    int ret = open(is, null, 0);
    if (ret == -1) {
      throw new JOrbisException("VorbisFile: open return -1");
    }
  }

  public VorbisFile(InputStream is, byte[] initial, int ibytes) throws JOrbisException {
    super();
    int ret = open(is, initial, ibytes);
    if (ret == -1) {
    }
  }

  private int getData() {
    int index = oy.buffer(CHUNKSIZE);
    byte[] buffer = oy.data;
    int bytes = 0;
    try {
      bytes = datasource.read(buffer, index, CHUNKSIZE);
    } catch (Exception e) {
      System.err.println(e);
      return OV_EREAD;
    }
    oy.wrote(bytes);
    if (bytes == -1) {
      bytes = 0;
    }
    return bytes;
  }

  private void seekHelper(long offst) {
    fseek(datasource, offst, SEEK_SET);
    this.offset = offst;
    oy.reset();
  }

  private int getNextPage(Page page, long boundary) {
    if (boundary > 0) {
      boundary += offset;
    }
    while (true) {
      int more;
      if (boundary > 0 && offset >= boundary) {
        return OV_FALSE;
      }
      more = oy.pageSeek(page);
      if (more < 0) {
        offset -= more;
      } else {
        if (more == 0) {
          if (boundary == 0) {
            return OV_FALSE;
          }
          int ret = getData();
          if (ret == 0) {
            return OV_EOF;
          }
          if (ret < 0) {
            return OV_EREAD;
          }
        } else {
          int ret = (int) offset;
          offset += more;
          return ret;
        }
      }
    }
  }

  private int getPrevPage(Page page) {
    long begin = offset;
    int ret;
    int offst = -1;
    while (offst == -1) {
      begin -= CHUNKSIZE;
      if (begin < 0) {
        begin = 0;
      }
      seekHelper(begin);
      while (offset < begin + CHUNKSIZE) {
        ret = getNextPage(page, begin + CHUNKSIZE - offset);
        if (ret == OV_EREAD) {
          return OV_EREAD;
        }
        if (ret < 0) {
          break;
        } else {
          offst = ret;
        }
      }
    }
    seekHelper(offst);
    ret = getNextPage(page, CHUNKSIZE);
    if (ret < 0) {
      return OV_EFAULT;
    }
    return offst;
  }

  int bisectForwardSerialno(long begin, long searched, long end, int currentno, int m) {
    long endsearched = end;
    long next = end;
    Page page = new Page();
    int ret;
    while (searched < endsearched) {
      long bisect;
      if (endsearched - searched < CHUNKSIZE) {
        bisect = searched;
      } else {
        bisect = (searched + endsearched) / 2;
      }
      seekHelper(bisect);
      ret = getNextPage(page, -1);
      if (ret == OV_EREAD) {
        return OV_EREAD;
      }
      if (ret < 0 || page.serialno() != currentno) {
        endsearched = bisect;
        if (ret >= 0) {
          next = ret;
        }
      } else {
        searched = ret + page.headerLen + page.bodyLen;
      }
    }
    seekHelper(next);
    ret = getNextPage(page, -1);
    if (ret == OV_EREAD) {
      return OV_EREAD;
    }
    if (searched >= end || ret == -1) {
      links = m + 1;
      offsets = new long[m + 2];
      offsets[m + 1] = searched;
    } else {
      ret = bisectForwardSerialno(next, offset, end, page.serialno(), m + 1);
      if (ret == OV_EREAD) {
        return OV_EREAD;
      }
    }
    offsets[m] = begin;
    return 0;
  }

  int fetchHeaders(Info vi, Comment vc, int[] serialno, Page ogPtr) {
    Page og = new Page();
    Packet op = new Packet();
    int ret;
    if (ogPtr == null) {
      ret = getNextPage(og, CHUNKSIZE);
      if (ret == OV_EREAD) {
        return OV_EREAD;
      }
      if (ret < 0) {
        return OV_ENOTVORBIS;
      }
      ogPtr = og;
    }
    if (serialno != null) {
      serialno[0] = ogPtr.serialno();
    }
    os.init(ogPtr.serialno());
    vi.init();
    vc.init();
    int i = 0;
    while (i < 3) {
      os.pagein(ogPtr);
      while (i < 3) {
        int result = os.packetout(op);
        if (result == 0) {
          break;
        }
        if (result == -1) {
          System.err.println("Corrupt header in logical bitstream.");
          vi.clear();
          vc.clear();
          os.clear();
          return -1;
        }
        if (vi.synthesisHeaderIn(vc, op) != 0) {
          System.err.println("Illegal header in logical bitstream.");
          vi.clear();
          vc.clear();
          os.clear();
          return -1;
        }
        i++;
      }
      if (i < 3) {
        if (getNextPage(ogPtr, 1) < 0) {
          System.err.println("Missing header in logical bitstream.");
          vi.clear();
          vc.clear();
          os.clear();
          return -1;
        }
      }
    }
    return 0;
  }

  void prefetchAllHeaders(Info firstI, Comment firstC, int dataoffset) {
    Page og = new Page();
    int ret;
    vi = new Info[links];
    vc = new Comment[links];
    dataoffsets = new long[links];
    pcmlengths = new long[links];
    serialnos = new int[links];
    for (int i = 0; i < links; i++) {
      if (firstI != null && firstC != null && i == 0) {
        vi[i] = firstI;
        vc[i] = firstC;
        dataoffsets[i] = dataoffset;
      } else {
        seekHelper(offsets[i]);
        if (fetchHeaders(vi[i], vc[i], null, null) == -1) {
          System.err.println("Error opening logical bitstream #" + (i + 1) + "\n");
          dataoffsets[i] = -1;
        } else {
          dataoffsets[i] = offset;
          os.clear();
        }
      }
      {
        long end = offsets[i + 1];
        seekHelper(end);
        while (true) {
          ret = getPrevPage(og);
          if (ret == -1) {
            System.err.println("Could not find last page of logical " + "bitstream #" + (i) + "\n");
            vi[i].clear();
            vc[i].clear();
            break;
          }
          if (og.granulepos() != -1) {
            serialnos[i] = og.serialno();
            pcmlengths[i] = og.granulepos();
            break;
          }
        }
      }
    }
  }

  int makeDecodeReady() {
    if (decodeReady) {
      System.exit(1);
    }
    vd.synthesis_init(vi[0]);
    vb.init(vd);
    decodeReady = true;
    return (0);
  }

  int openSeekable() {
    Info initialI = new Info();
    Comment initialC = new Comment();
    int serialno;
    long end;
    int ret;
    int dataoffset;
    Page og = new Page();
    int[] foo = new int[1];
    ret = fetchHeaders(initialI, initialC, foo, null);
    serialno = foo[0];
    dataoffset = (int) offset;
    os.clear();
    if (ret == -1) {
      return (-1);
    }
    seekable = true;
    fseek(datasource, 0, SEEK_END);
    offset = ftell(datasource);
    end = offset;
    end = getPrevPage(og);
    if (og.serialno() != serialno) {
      if (bisectForwardSerialno(0, 0, end + 1, serialno, 0) < 0) {
        clear();
        return OV_EREAD;
      }
    } else {
      if (bisectForwardSerialno(0, end, end + 1, serialno, 0) < 0) {
        clear();
        return OV_EREAD;
      }
    }
    prefetchAllHeaders(initialI, initialC, dataoffset);
    return (rawSeek(0));
  }

  int openNonseekable() {
    links = 1;
    vi = new Info[links];
    vi[0] = new Info();
    vc = new Comment[links];
    vc[0] = new Comment();
    int[] foo = new int[1];
    if (fetchHeaders(vi[0], vc[0], foo, null) == -1) {
      return (-1);
    }
    currentSerialno = foo[0];
    makeDecodeReady();
    return 0;
  }

  void decodeClear() {
    os.clear();
    vd.clear();
    vb.clear();
    decodeReady = false;
    bittrack = 0.f;
    samptrack = 0.f;
  }

  int processPacket(int readp) {
    Page og = new Page();
    while (true) {
      if (decodeReady) {
        Packet op = new Packet();
        int result = os.packetout(op);
        long granulepos;
        if (result > 0) {
          granulepos = op.granulepos;
          if (vb.synthesis(op) == 0) {
            {
              int oldsamples = vd.synthesis_pcmout(null, null);
              vd.synthesis_blockin(vb);
              samptrack += vd.synthesis_pcmout(null, null) - oldsamples;
              bittrack += op.bytes * 8;
            }
            if (granulepos != -1 && op.eos == 0) {
              int link = (seekable ? currentLink : 0);
              int samples;
              samples = vd.synthesis_pcmout(null, null);
              granulepos -= samples;
              for (int i = 0; i < link; i++) {
                granulepos += pcmlengths[i];
              }
              pcmOffset = granulepos;
            }
            return (1);
          }
        }
      }
      if (readp == 0) {
        return (0);
      }
      if (getNextPage(og, -1) < 0) {
        return (0);
      }
      bittrack += og.headerLen * 8;
      if (decodeReady) {
        if (currentSerialno != og.serialno()) {
          decodeClear();
        }
      }
      if (!decodeReady) {
        int i;
        if (seekable) {
          currentSerialno = og.serialno();
          for (i = 0; i < links; i++) {
            if (serialnos[i] == currentSerialno) {
              break;
            }
          }
          if (i == links) {
            return (-1);
          }
          currentLink = i;
          os.init(currentSerialno);
          os.reset();
        } else {
          int[] foo = new int[1];
          int ret = fetchHeaders(vi[0], vc[0], foo, og);
          currentSerialno = foo[0];
          if (ret != 0) {
            return ret;
          }
          currentLink++;
          i = 0;
        }
        makeDecodeReady();
      }
      os.pagein(og);
    }
  }

  int clear() {
    vb.clear();
    vd.clear();
    os.clear();
    if (vi != null && links != 0) {
      for (int i = 0; i < links; i++) {
        vi[i].clear();
        vc[i].clear();
      }
      vi = null;
      vc = null;
    }
    if (dataoffsets != null) {
      dataoffsets = null;
    }
    if (pcmlengths != null) {
      pcmlengths = null;
    }
    if (serialnos != null) {
      serialnos = null;
    }
    if (offsets != null) {
      offsets = null;
    }
    oy.clear();
    return (0);
  }

  static int fseek(InputStream fis, long off, int whence) {
    if (fis instanceof SeekableInputStream sis) {
      try {
        if (whence == SEEK_SET) {
          sis.seek(off);
        } else if (whence == SEEK_END) {
          sis.seek(sis.getLength() - off);
        } else {
          System.out.println("seek: " + whence + " is not supported");
        }
      } catch (Exception e) {
      }
      return 0;
    }
    try {
      if (whence == 0) {
        fis.reset();
      }
      fis.skip(off);
    } catch (Exception e) {
      return -1;
    }
    return 0;
  }

  static long ftell(InputStream fis) {
    try {
      if (fis instanceof SeekableInputStream sis) {
        return (sis.tell());
      }
    } catch (Exception e) {
    }
    return 0;
  }

  int open(InputStream is, byte[] initial, int ibytes) {
    return openCallbacks(is, initial, ibytes);
  }

  int openCallbacks(InputStream is, byte[] initial, int ibytes) {
    int ret;
    datasource = is;
    oy.init();
    if (initial != null) {
      int index = oy.buffer(ibytes);
      System.arraycopy(initial, 0, oy.data, index, ibytes);
      oy.wrote(ibytes);
    }
    if (is instanceof SeekableInputStream) {
      ret = openSeekable();
    } else {
      ret = openNonseekable();
    }
    if (ret != 0) {
      datasource = null;
      clear();
    }
    return (ret);
  }

  public int streams() {
    return links;
  }

  public boolean seekable() {
    return seekable;
  }

  public int bitrate(int i) {
    if (i >= links) {
      return (-1);
    }
    if (!seekable && i != 0) {
      return (bitrate(0));
    }
    if (i < 0) {
      long bits = 0;
      for (int j = 0; j < links; j++) {
        bits += (offsets[j + 1] - dataoffsets[j]) * 8;
      }
      return ((int) Math.rint(bits / timeTotal(-1)));
    } else {
      if (seekable) {
        return ((int) Math.rint((offsets[i + 1] - dataoffsets[i]) * 8 / timeTotal(i)));
      } else {
        if (vi[i].getBitrateNominal() > 0) {
          return vi[i].getBitrateNominal();
        } else {
          if (vi[i].getBitrateUpper() > 0) {
            if (vi[i].getBitrateLower() > 0) {
              return (vi[i].getBitrateUpper() + vi[i].getBitrateLower()) / 2;
            } else {
              return vi[i].getBitrateUpper();
            }
          }
          return -1;
        }
      }
    }
  }

  public int bitrateInstant() {
    int link = (seekable ? currentLink : 0);
    if (samptrack == 0) {
      return (-1);
    }
    int ret = (int) (bittrack / samptrack * vi[link].getRate() + .5);
    bittrack = 0.f;
    samptrack = 0.f;
    return (ret);
  }

  public int serialnumber(int i) {
    if (i >= links) {
      return (-1);
    }
    if (!seekable && i >= 0) {
      return (serialnumber(-1));
    }
    if (i < 0) {
      return (currentSerialno);
    } else {
      return (serialnos[i]);
    }
  }

  public long rawTotal(int i) {
    if (!seekable || i >= links) {
      return (-1);
    }
    if (i < 0) {
      long acc = 0;
      for (int j = 0; j < links; j++) {
        acc += rawTotal(j);
      }
      return (acc);
    } else {
      return (offsets[i + 1] - offsets[i]);
    }
  }

  public long pcmTotal(int i) {
    if (!seekable || i >= links) {
      return (-1);
    }
    if (i < 0) {
      long acc = 0;
      for (int j = 0; j < links; j++) {
        acc += pcmTotal(j);
      }
      return (acc);
    } else {
      return (pcmlengths[i]);
    }
  }

  public float timeTotal(int i) {
    if (!seekable || i >= links) {
      return (-1);
    }
    if (i < 0) {
      float acc = 0;
      for (int j = 0; j < links; j++) {
        acc += timeTotal(j);
      }
      return (acc);
    } else {
      return ((float) (pcmlengths[i]) / vi[i].getRate());
    }
  }

  public int rawSeek(int pos) {
    if (!seekable) {
      return (-1);
    }
    if (pos < 0 || pos > offsets[links]) {
      pcmOffset = -1;
      decodeClear();
      return -1;
    }
    pcmOffset = -1;
    decodeClear();
    seekHelper(pos);
    switch (processPacket(1)) {
      case 0:
        pcmOffset = pcmTotal(-1);
        return (0);
      case -1:
        pcmOffset = -1;
        decodeClear();
        return -1;
      default:
        break;
    }
    while (true) {
      switch (processPacket(0)) {
        case 0:
          return (0);
        case -1:
          pcmOffset = -1;
          decodeClear();
          return -1;
        default:
          break;
      }
    }
  }

  public int pcmSeek(long pos) {
    int link = -1;
    long total = pcmTotal(-1);
    if (!seekable) {
      return (-1);
    }
    if (pos < 0 || pos > total) {
      pcmOffset = -1;
      decodeClear();
      return -1;
    }
    for (link = links - 1; link >= 0; link--) {
      total -= pcmlengths[link];
      if (pos >= total) {
        break;
      }
    }
    {
      long target = pos - total;
      long end = offsets[link + 1];
      long begin = offsets[link];
      int best = (int) begin;
      Page og = new Page();
      while (begin < end) {
        long bisect;
        int ret;
        if (end - begin < CHUNKSIZE) {
          bisect = begin;
        } else {
          bisect = (end + begin) / 2;
        }
        seekHelper(bisect);
        ret = getNextPage(og, end - bisect);
        if (ret == -1) {
          end = bisect;
        } else {
          long granulepos = og.granulepos();
          if (granulepos < target) {
            best = ret;
            begin = offset;
          } else {
            end = bisect;
          }
        }
      }
      if (rawSeek(best) != 0) {
        pcmOffset = -1;
        decodeClear();
        return -1;
      }
    }
    if (pcmOffset >= pos) {
      pcmOffset = -1;
      decodeClear();
      return -1;
    }
    if (pos > pcmTotal(-1)) {
      pcmOffset = -1;
      decodeClear();
      return -1;
    }
    while (pcmOffset < pos) {
      float[][] pcm;
      int target = (int) (pos - pcmOffset);
      float[][][] pcmContainer = new float[1][][];
      int[] index = new int[getInfo(-1).getChannels()];
      int samples = vd.synthesis_pcmout(pcmContainer, index);
      pcm = pcmContainer[0];
      if (samples > target) {
        samples = target;
      }
      vd.synthesis_read(samples);
      pcmOffset += samples;
      if (samples < target) {
        if (processPacket(1) == 0) {
          pcmOffset = pcmTotal(-1);
        }
      }
    }
    return 0;
  }

  int timeSeek(float seconds) {
    int link = -1;
    long pcmTotal = pcmTotal(-1);
    float timeTotal = timeTotal(-1);
    if (!seekable) {
      return (-1);
    }
    if (seconds < 0 || seconds > timeTotal) {
      pcmOffset = -1;
      decodeClear();
      return -1;
    }
    for (link = links - 1; link >= 0; link--) {
      pcmTotal -= pcmlengths[link];
      timeTotal -= timeTotal(link);
      if (seconds >= timeTotal) {
        break;
      }
    }
    {
      long target = (long) (pcmTotal + (seconds - timeTotal) * vi[link].getRate());
      return (pcmSeek(target));
    }
  }

  public long rawTell() {
    return (offset);
  }

  public long pcmTell() {
    return (pcmOffset);
  }

  public float timeTell() {
    int link = -1;
    long pcmTotal = 0;
    float timeTotal = 0.f;
    if (seekable) {
      pcmTotal = pcmTotal(-1);
      timeTotal = timeTotal(-1);
      for (link = links - 1; link >= 0; link--) {
        pcmTotal -= pcmlengths[link];
        timeTotal -= timeTotal(link);
        if (pcmOffset >= pcmTotal) {
          break;
        }
      }
    }
    return (timeTotal + (float) (pcmOffset - pcmTotal) / vi[link].getRate());
  }

  public Info getInfo(int link) {
    if (seekable) {
      if (link < 0) {
        if (decodeReady) {
          return vi[currentLink];
        } else {
          return null;
        }
      } else {
        if (link >= links) {
          return null;
        } else {
          return vi[link];
        }
      }
    } else {
      if (decodeReady) {
        return vi[0];
      } else {
        return null;
      }
    }
  }

  public Comment getComment(int link) {
    if (seekable) {
      if (link < 0) {
        if (decodeReady) {
          return vc[currentLink];
        } else {
          return null;
        }
      } else {
        if (link >= links) {
          return null;
        } else {
          return vc[link];
        }
      }
    } else {
      if (decodeReady) {
        return vc[0];
      } else {
        return null;
      }
    }
  }

  int hostIsBigEndian() {
    return 1;
  }

  int read(byte[] buffer, int length, int bigendianp, int word, int sgned, int[] bitstream) {
    int hostEndian = hostIsBigEndian();
    int index = 0;
    while (true) {
      if (decodeReady) {
        float[][] pcm;
        float[][][] pcmContainer = new float[1][][];
        int[] indexArr = new int[getInfo(-1).getChannels()];
        int samples = vd.synthesis_pcmout(pcmContainer, indexArr);
        pcm = pcmContainer[0];
        if (samples != 0) {
          int channels = getInfo(-1).getChannels();
          int bytespersample = word * channels;
          if (samples > length / bytespersample) {
            samples = length / bytespersample;
          }
          {
            int val;
            if (word == 1) {
              int off = (sgned != 0 ? 0 : 128);
              for (int j = 0; j < samples; j++) {
                for (int i = 0; i < channels; i++) {
                  val = (int) (pcm[i][indexArr[i] + j] * 128. + 0.5);
                  if (val > 127) {
                    val = 127;
                  } else if (val < -128) {
                    val = -128;
                  }
                  buffer[index++] = (byte) (val + off);
                }
              }
            } else {
              int off = (sgned != 0 ? 0 : 32768);
              if (hostEndian == bigendianp) {
                if (sgned != 0) {
                  for (int i = 0; i < channels; i++) {
                    int src = indexArr[i];
                    int dest = i;
                    for (int j = 0; j < samples; j++) {
                      val = (int) (pcm[i][src + j] * 32768. + 0.5);
                      if (val > 32767) {
                        val = 32767;
                      } else if (val < -32768) {
                        val = -32768;
                      }
                      buffer[dest] = (byte) (val >>> 8);
                      buffer[dest + 1] = (byte) (val);
                      dest += channels * 2;
                    }
                  }
                } else {
                  for (int i = 0; i < channels; i++) {
                    float[] src = pcm[i];
                    int dest = i;
                    for (int j = 0; j < samples; j++) {
                      val = (int) (src[j] * 32768. + 0.5);
                      if (val > 32767) {
                        val = 32767;
                      } else if (val < -32768) {
                        val = -32768;
                      }
                      buffer[dest] = (byte) ((val + off) >>> 8);
                      buffer[dest + 1] = (byte) (val + off);
                      dest += channels * 2;
                    }
                  }
                }
              } else if (bigendianp != 0) {
                for (int j = 0; j < samples; j++) {
                  for (int i = 0; i < channels; i++) {
                    val = (int) (pcm[i][j] * 32768. + 0.5);
                    if (val > 32767) {
                      val = 32767;
                    } else if (val < -32768) {
                      val = -32768;
                    }
                    val += off;
                    buffer[index++] = (byte) (val >>> 8);
                    buffer[index++] = (byte) val;
                  }
                }
              } else {
                for (int j = 0; j < samples; j++) {
                  for (int i = 0; i < channels; i++) {
                    val = (int) (pcm[i][j] * 32768. + 0.5);
                    if (val > 32767) {
                      val = 32767;
                    } else if (val < -32768) {
                      val = -32768;
                    }
                    val += off;
                    buffer[index++] = (byte) val;
                    buffer[index++] = (byte) (val >>> 8);
                  }
                }
              }
            }
          }
          vd.synthesis_read(samples);
          pcmOffset += samples;
          if (bitstream != null) {
            bitstream[0] = currentLink;
          }
          return (samples * bytespersample);
        }
      }
      switch (processPacket(1)) {
        case 0:
          return (0);
        case -1:
          return -1;
        default:
          break;
      }
    }
  }

  public Info[] getInfo() {
    return vi;
  }

  public Comment[] getComment() {
    return vc;
  }

  public static void main(String[] arg) {
    try {
      VorbisFile foo = new VorbisFile(arg[0]);
      int links = foo.streams();
      System.out.println("links=" + links);
      Comment[] comment = foo.getComment();
      Info[] info = foo.getInfo();
      for (int i = 0; i < links; i++) {
        System.out.println(info[i]);
        System.out.println(comment[i]);
      }
      System.out.println("raw_total: " + foo.rawTotal(-1));
      System.out.println("pcm_total: " + foo.pcmTotal(-1));
      System.out.println("time_total: " + foo.timeTotal(-1));
    } catch (Exception e) {
      System.err.println(e);
    }
  }

  class SeekableInputStream extends InputStream {

    java.io.RandomAccessFile raf = null;
    static final String MODE = "r";

    private SeekableInputStream() {}

    SeekableInputStream(String file) throws java.io.FileNotFoundException, java.io.IOException {
      raf = new java.io.RandomAccessFile(file, MODE);
    }

    public int read() throws java.io.IOException {
      return raf.read();
    }

    public int read(byte[] buf) throws java.io.IOException {
      return raf.read(buf);
    }

    public int read(byte[] buf, int s, int len) throws java.io.IOException {
      return raf.read(buf, s, len);
    }

    public long skip(long n) throws java.io.IOException {
      return (long) (raf.skipBytes((int) n));
    }

    public long getLength() throws java.io.IOException {
      return raf.length();
    }

    public long tell() throws java.io.IOException {
      return raf.getFilePointer();
    }

    public int available() throws java.io.IOException {
      return (raf.length() == raf.getFilePointer()) ? 0 : 1;
    }

    public void close() throws java.io.IOException {
      raf.close();
    }

    public synchronized void mark(int m) {}

    public synchronized void reset() throws java.io.IOException {}

    public boolean markSupported() {
      return false;
    }

    public void seek(long pos) throws java.io.IOException {
      raf.seek(pos);
    }
  }
}
