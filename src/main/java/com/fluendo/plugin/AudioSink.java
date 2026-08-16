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

public abstract class AudioSink extends Sink implements ClockProvider {
  protected RingBuffer ringBuffer = null;

  private class AudioClock extends SystemClock {
    private long lastTime = -1;
    private long diff = -1;
    private boolean started = false;

    public void setStarted(boolean s) {
      started = s;
      if (started) {
        diff = -1;
        lastTime = -1;
      }
    }

    @Override
    protected long getInternalTime() {
      long samples;
      long result;
      long timePos;
      long now;

      synchronized (ringBuffer) {
        if (ringBuffer == null || ringBuffer.rate == 0) return 0;

        samples = ringBuffer.samplesPlayed();
        timePos = samples * Clock.SECOND / ringBuffer.rate;

        if (started) {
          /* Interpolate as the position can jump a lot */
          now = System.currentTimeMillis() * Clock.MSECOND;
          if (diff == -1) {
            diff = now;
          }

          if (timePos != lastTime) {
            lastTime = timePos;
            diff = now - timePos;
          }
          result = now - diff;
        } else {
          result = timePos;
        }
      }

      return result;
    }
  }

  private final AudioClock audioClock = new AudioClock();

  @Override
  public Clock provideClock() {
    return audioClock;
  }

  // Removed the 'abstract' keyword here:
  protected class RingBuffer implements Runnable {
    protected byte[] buffer;
    private int state;
    private Thread thread;
    private long nextSample;
    private boolean flushing;
    private boolean autoStart;
    private boolean opened;

    protected static final int STOP = 0;
    protected static final int PAUSE = 1;
    protected static final int PLAY = 2;

    public int bps, sps;
    public byte[] emptySeg;
    public long playSeg;
    public int segTotal;
    public int segSize;
    public int rate, channels;

    @Override
    public void run() {
      boolean running = true;

      while (running) {
        synchronized (this) {
          if (state != PLAY) {
            while (state == PAUSE) {
              try {
                notifyAll();
                wait();
              } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
              }
            }
            if (state == STOP) {
              running = false;
              break;
            }
          }
        }

        int segNum = (int) (playSeg % segTotal);
        int index = segNum * segSize;
        int ret, toWrite;

        toWrite = segSize;
        while (toWrite > 0) {
          ret = write(buffer, index, segSize);
          if (ret == -1) break;

          toWrite -= ret;
        }

        clear(segNum);

        synchronized (this) {
          playSeg++;
          notifyAll();
        }
      }
    }

    public synchronized void setFlushing(boolean flushing) {
      this.flushing = flushing;
      clearAll();
      if (flushing) {
        pause();
      }
    }

    protected void startWriteThread() {
      thread = new Thread(this, "cortado-audiosink-ringbuffer");
      thread.start();
      try {
        wait();
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }
    }

    public synchronized boolean acquire(Caps caps) {
      boolean res;

      if (thread != null) return false;

      if (opened) return false;

      String mime = caps.getMime();
      if (!mime.equals("audio/raw")) return false;

      rate = caps.getFieldInt("rate", 44100);
      channels = caps.getFieldInt("channels", 1);
      bps = 2 * channels;

      if (!(res = open(this))) return res;

      opened = true;

      Debug.log(Debug.INFO, "audio: segSize: " + segSize);
      Debug.log(Debug.INFO, "audio: segTotal: " + segTotal);

      segTotal++;

      buffer = new byte[segSize * segTotal];
      sps = segSize / bps;

      state = PAUSE;
      nextSample = 0;
      playSeg = 0;

      startWriteThread();

      return res;
    }

    public synchronized boolean isAcquired() {
      return opened;
    }

    public boolean release() {
      stop();

      synchronized (this) {
        if (opened) {
          if (!close(this)) return false;
        }
        opened = false;
      }

      return true;
    }

    private synchronized boolean waitSegment() {
      if (flushing) return false;

      if (state != PLAY && autoStart) {
        play();
      }

      try {
        if (state != PLAY) {
          return false;
        }

        wait();
        if (flushing) return false;

        if (state != PLAY) return false;
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }

      return true;
    }

    public int commit(byte[] data, long sample, int offset, int len) {
      int idx;

      if (sample == -1) {
        sample = nextSample;
      }
      if (sample < 0) {
        return len;
      }
      if (nextSample != -1) {
        if (Math.abs(sample - nextSample) < (rate / 10)) sample = nextSample;
        else System.out.println("discont: found " + sample + " expected " + nextSample);
      }

      idx = offset;

      nextSample = sample + (len / bps);
      while (len > 0) {
        long writeSeg;
        int writeOff;
        int writeLen = 0;
        long diff = -1;

        writeSeg = sample / sps;
        writeOff = (int) ((sample % sps) * bps);

        while (true) {
          /* Get the currently playing segment */
          synchronized (this) {
            /* See how far away it is from the write segment */
            diff = writeSeg - playSeg;
          }

          /* Play segment too far ahead, we need to drop */
          if (diff < 0) {
            writeLen = Math.min(segSize, len);
            break;
          } else {
            /* Write segment is within writable range */
            if (diff < segTotal) break;

            /* Else wait for the segment to become writable */
            if (!waitSegment()) {
              return -1;
            }
          }
        }
        if (diff >= 0) {
          int writeSegRel;

          /* We can write now */
          writeSegRel = (int) (writeSeg % segTotal);
          writeLen = Math.min(segSize - writeOff, len);

          System.arraycopy(data, idx, buffer, writeSegRel * segSize + writeOff, writeLen);
        }

        len -= writeLen;
        idx += writeLen;
        sample += writeLen / bps;
      }

      return len;
    }

    public long samplesPlayed() {
      long delay, samples;
      long seg;

      /* Get the number of samples not yet played */
      delay = delay();

      seg = Math.max(0, playSeg - 1);

      samples = (seg * sps);

      if (samples >= delay) samples -= delay;
      else samples = 0;

      return samples;
    }

    public synchronized void clear(long segNum) {
      int index = ((int) (segNum % segTotal)) * segSize;
      System.arraycopy(emptySeg, 0, buffer, index, segSize);
    }

    public synchronized void clearAll() {
      for (int i = 0; i < segTotal; i++) {
        clear(i);
      }
    }

    public synchronized void setSample(long sample) {
      if (sample == -1) sample = 0;

      playSeg = sample / sps;
      nextSample = sample;

      clearAll();
    }

    public synchronized void setAutoStart(boolean start) {
      autoStart = start;
    }

    public boolean play() {
      synchronized (this) {
        if (flushing) return false;

        state = PLAY;
        audioClock.setStarted(true);
        notifyAll();
      }
      Debug.log(Debug.DEBUG, this + " playing");
      return true;
    }

    public boolean pause() {
      synchronized (this) {
        Debug.log(Debug.DEBUG, this + " pausing");
        state = PAUSE;
        audioClock.setStarted(false);
        notifyAll();
        if (thread != null) {
          try {
            Debug.log(Debug.DEBUG, this + " waiting for pause");
            wait();
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
          }
        }
      }
      Debug.log(Debug.DEBUG, this + " paused");
      return true;
    }

    public boolean stop() {
      synchronized (this) {
        Debug.log(Debug.DEBUG, this + " stopping");
        state = STOP;
        audioClock.setStarted(false);
        notifyAll();
      }
      if (thread != null) {
        try {
          Debug.log(Debug.DEBUG, this + " joining thread");
          thread.join();
          thread = null;
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
        }
      }
      Debug.log(Debug.DEBUG, this + " stopped");
      return true;
    }

    public synchronized int getState() {
      return state;
    }
  }

  /* Test whether the audio sink is likely to work. */
  public boolean test() {
    return true;
  }

  @Override
  protected WaitStatus doSync(long time) {
    return WaitStatus.newOK();
  }

  @Override
  protected boolean doEvent(Event event) {
    switch (event.getType()) {
      case FLUSH_START:
        ringBuffer.setFlushing(true);
        break;

      case FLUSH_STOP:
        ringBuffer.setFlushing(false);
        break;

      case NEWSEGMENT:
        break;

      case EOS:
        drain();
        break;

      case SEEK:
        break;
    }
    return true;
  }

  @Override
  protected int render(Buffer buf) {
    long sample;
    long time;

    if (buf.isFlagSet(com.fluendo.jst.Buffer.FLAG_DISCONT)) ringBuffer.nextSample = -1;

    time = buf.timestamp - segStart;
    if (time < 0) return Pad.OK;
    time += baseTime;

    sample = time * ringBuffer.rate / Clock.SECOND;

    ringBuffer.commit(buf.data, sample, buf.offset, buf.length);

    return Pad.OK;
  }

  @Override
  protected boolean setCapsFunc(Caps caps) {
    boolean res;

    ringBuffer.release();
    res = ringBuffer.acquire(caps);
    return res;
  }

  @Override
  protected int changeState(int transition) {
    int result;

    switch (transition) {
      case STOP_PAUSE:
        ringBuffer = createRingBuffer();
        ringBuffer.setFlushing(false);
        break;
      case PAUSE_PLAY:
        ringBuffer.setAutoStart(true);
        break;
      case PLAY_PAUSE:
        reset();
        ringBuffer.setAutoStart(false);
        ringBuffer.pause();
        break;
      case PAUSE_STOP:
        ringBuffer.setFlushing(true);
        break;
    }
    result = super.changeState(transition);

    switch (transition) {
      case PAUSE_STOP:
        ringBuffer.release();
        break;
    }

    return result;
  }

  /* Block until audio playback is finished */
  protected void drain() {
    if (ringBuffer.rate <= 0) {
      return;
    }

    if (!ringBuffer.isAcquired()) {
      return;
    }

    if (ringBuffer.getState() != PLAY) {
      ringBuffer.play();
    }

    if (ringBuffer.nextSample != -1) {
      long time = ringBuffer.nextSample * Clock.SECOND / ringBuffer.rate;
      Clock.ClockID id = audioClock.newSingleShotID(time);
      Debug.log(
          Debug.DEBUG,
          this + " waiting until t=" + ((double) time / Clock.SECOND) + "s for playback to finish");
      id.waitID();
      ringBuffer.nextSample = -1;
    }
  }

  protected abstract RingBuffer createRingBuffer();

  protected abstract boolean open(RingBuffer ring);

  protected abstract boolean close(RingBuffer ring);

  protected abstract int write(byte[] data, int offset, int length);

  protected abstract long delay();

  protected abstract void reset();
}
