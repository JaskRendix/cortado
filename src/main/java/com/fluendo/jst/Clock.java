/* Cortado - a video player java applet
 * Copyright (C) 2004 Fluendo S.L.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Street #330, Boston, MA 02111-1307, USA.
 */

package com.fluendo.jst;

public abstract class Clock {

  private long adjust;
  private long lastTime;

  public static final long USECOND = 1L;
  public static final long MSECOND = 1_000L * USECOND;
  public static final long SECOND = 1_000L * MSECOND;

  // ID types
  public static final int SINGLE = 0;
  public static final int PERIODIC = 0;

  public class ClockID {
    private final long time;
    private final long interval;
    private final int type;
    private int status;

    public ClockID(long time, long interval, int type) {
      this.time = time;
      this.interval = interval;
      this.type = type;
    }

    public long getTime() {
      return time;
    }

    public int getStatus() {
      return status;
    }

    public void setStatus(int status) {
      this.status = status;
    }

    public WaitStatus waitID() {
      WaitStatus res = waitFunc(this);

      if (type == PERIODIC) {
        // Note: time is a local copy or field depending on desired mutation;
      }

      return res;
    }

    public void unschedule() {
      unscheduleFunc(this);
    }
  }

  protected Clock() {
    this.adjust = 0L;
    this.lastTime = 0L;
  }

  protected synchronized long adjust(long internal) {
    long ret = internal + adjust;
    if (ret < lastTime) {
      ret = lastTime;
    } else {
      lastTime = ret;
    }
    return ret;
  }

  protected abstract long getInternalTime();

  protected abstract WaitStatus waitFunc(ClockID id);

  protected abstract WaitStatus waitAsyncFunc(ClockID id);

  protected abstract void unscheduleFunc(ClockID id);

  public synchronized long getTime() {
    long internal = getInternalTime();
    return adjust(internal);
  }

  public synchronized void setAdjust(long newAdjust) {
    this.adjust = newAdjust;
  }

  public synchronized long getAdjust() {
    return adjust;
  }

  public ClockID newSingleShotID(long time) {
    return new ClockID(time, 0L, SINGLE);
  }

  public ClockID newPeriodicID(long time, long interval) {
    return new ClockID(time, interval, PERIODIC);
  }
}
