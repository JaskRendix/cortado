package com.fluendo.jst;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClockProviderTest {

    /** A simple fake clock for testing */
    private static class TestClock extends Clock {
        private long time = 0;

        @Override
        protected long getInternalTime() {
            return time;
        }

        @Override
        protected WaitStatus waitFunc(ClockID id) {
            return WaitStatus.newOK();
        }

        @Override
        protected WaitStatus waitAsyncFunc(ClockID id) {
            return WaitStatus.newOK();
        }

        @Override
        protected void unscheduleFunc(ClockID id) {
            id.setStatus(WaitStatus.UNSCHEDULED);
        }

        void setTime(long t) {
            time = t;
        }
    }

    /** A helper class that uses a ClockProvider */
    private static class ClockUser {
        private final ClockProvider provider;

        ClockUser(ClockProvider provider) {
            this.provider = provider;
        }

        long readTime() {
            return provider.provideClock().getTime();
        }
    }

    @Test
    void testLambdaClockProvider() {
        TestClock clock = new TestClock();
        clock.setTime(1234);

        ClockProvider provider = () -> clock;

        assertSame(clock, provider.provideClock());
        assertEquals(1234, provider.provideClock().getTime());
    }

    @Test
    void testConstructorReferenceProvider() {
        ClockProvider provider = TestClock::new;

        Clock c1 = provider.provideClock();
        Clock c2 = provider.provideClock();

        assertNotNull(c1);
        assertNotNull(c2);
        assertNotSame(c1, c2); // constructor reference creates new instances
    }

    @Test
    void testProviderUsedInsideAnotherClass() {
        TestClock clock = new TestClock();
        clock.setTime(5000);

        ClockUser user = new ClockUser(() -> clock);

        assertEquals(5000, user.readTime());
    }

    @Test
    void testProviderReturnsDifferentInstances() {
        ClockProvider provider = TestClock::new;

        Clock c1 = provider.provideClock();
        Clock c2 = provider.provideClock();

        assertNotSame(c1, c2);
    }

    @Test
    void testProviderReturnsSameInstance() {
        TestClock clock = new TestClock();
        ClockProvider provider = () -> clock;

        Clock c1 = provider.provideClock();
        Clock c2 = provider.provideClock();

        assertSame(c1, c2);
    }
}
