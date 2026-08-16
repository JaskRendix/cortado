package com.fluendo.jst;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SystemClockTest {

  @Test
  void testGetInternalTimeMonotonic() {
    SystemClock clock = new SystemClock();

    long t1 = clock.getInternalTime();
    long t2 = clock.getInternalTime();

    assertTrue(t2 >= t1);
  }

  @Test
  void testWaitAsyncFuncAlwaysOK() {
    SystemClock clock = new SystemClock();
    Clock.ClockID id = clock.newSingleShotID(clock.getInternalTime());

    WaitStatus ws = clock.waitAsyncFunc(id);

    assertEquals(WaitStatus.OK, ws.status());
  }

  @Test
  void testUnscheduleFuncSetsStatus() {
    SystemClock clock = new SystemClock();
    Clock.ClockID id = clock.newSingleShotID(clock.getInternalTime());

    clock.unscheduleFunc(id);

    assertEquals(WaitStatus.UNSCHEDULED, id.getStatus());
  }

  @Test
  void testWaitFuncLateStatus() {
    SystemClock clock = new SystemClock();

    long now = clock.getInternalTime();
    long past = now - 1000; // 1000us in the past

    Clock.ClockID id = clock.newSingleShotID(past);

    WaitStatus ws = clock.waitFunc(id);

    assertEquals(WaitStatus.LATE, ws.status());
    assertTrue(ws.jitter() > 0);
  }

  @Test
  void testWaitFuncUnscheduleBeforeWait() {
    SystemClock clock = new SystemClock();

    long future = clock.getInternalTime() + 1_000_000;
    Clock.ClockID id = clock.newSingleShotID(future);

    id.setStatus(WaitStatus.UNSCHEDULED);

    WaitStatus ws = clock.waitFunc(id);

    assertEquals(WaitStatus.UNSCHEDULED, ws.status());
  }

  @Test
  void testJitterCalculation() {
    SystemClock clock = new SystemClock();

    long now = clock.getInternalTime();
    long target = now + 500_000;

    Clock.ClockID id = clock.newSingleShotID(target);

    WaitStatus ws = clock.waitFunc(id);

    long jitter = ws.jitter();
    assertTrue(jitter <= -480_000 && jitter >= -520_000);
  }
}
