package com.fluendo.jst;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClockTest {

    /** Deterministic fake clock for testing */
    private static class TestClock extends Clock {
        private long fakeTime = 0;

        @Override
        protected long getInternalTime() {
            return fakeTime;
        }

        @Override
        protected WaitStatus waitFunc(ClockID id) {
            // deterministic: no waiting, just compute jitter
            long now = adjust(fakeTime);
            long jitter = now - id.getTime();
            if (jitter < 0) {
                return new WaitStatus().withStatus(WaitStatus.OK);
            } else if (jitter == 0) {
                return new WaitStatus().withStatus(WaitStatus.OK);
            } else {
                return new WaitStatus().withStatus(WaitStatus.LATE);
            }
        }

        @Override
        protected WaitStatus waitAsyncFunc(ClockID id) {
            return WaitStatus.newOK();
        }

        @Override
        protected void unscheduleFunc(ClockID id) {
            id.setStatus(WaitStatus.UNSCHEDULED);
        }

        void setFakeTime(long t) {
            fakeTime = t;
        }
    }

    @Test
    void testAdjustMonotonic() {
        TestClock clock = new TestClock();

        clock.setFakeTime(1000);
        long t1 = clock.getTime();

        clock.setFakeTime(500); // internal time goes backwards
        long t2 = clock.getTime();

        assertEquals(t1, t2); // monotonic: cannot go backwards
    }

    @Test
    void testAdjustOffset() {
        TestClock clock = new TestClock();

        clock.setAdjust(500);
        clock.setFakeTime(1000);

        long t = clock.getTime();
        assertEquals(1500, t);
    }

    @Test
    void testGetAdjust() {
        TestClock clock = new TestClock();
        clock.setAdjust(1234);
        assertEquals(1234, clock.getAdjust());
    }

    @Test
    void testNewSingleShotID() {
        TestClock clock = new TestClock();
        Clock.ClockID id = clock.newSingleShotID(5000);

        assertEquals(5000, id.getTime());
        assertEquals(0, id.getStatus()); // default
    }

    @Test
    void testNewPeriodicID() {
        TestClock clock = new TestClock();
        Clock.ClockID id = clock.newPeriodicID(7000, 100);

        assertEquals(7000, id.getTime());
        assertEquals(0, id.getStatus());
    }

    @Test
    void testClockIDWaitDelegatesToClock() {
        TestClock clock = new TestClock();
        clock.setFakeTime(1000);

        Clock.ClockID id = clock.newSingleShotID(500);

        WaitStatus ws = id.waitID();

        assertEquals(WaitStatus.LATE, ws.status());
    }

    @Test
    void testClockIDUnschedule() {
        TestClock clock = new TestClock();
        Clock.ClockID id = clock.newSingleShotID(1000);

        id.unschedule();

        assertEquals(WaitStatus.UNSCHEDULED, id.getStatus());
    }

    @Test
    void testWaitAsyncFunc() {
        TestClock clock = new TestClock();
        Clock.ClockID id = clock.newSingleShotID(0);

        WaitStatus ws = clock.waitAsyncFunc(id);

        assertEquals(WaitStatus.OK, ws.status());
    }
}
