package com.fluendo.jst;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WaitStatusTest {

    @Test
    void testDefaultConstructor() {
        WaitStatus ws = new WaitStatus();
        assertEquals(WaitStatus.ERROR, ws.status());
        assertEquals(0L, ws.jitter());
    }

    @Test
    void testNewOK() {
        WaitStatus ws = WaitStatus.newOK();
        assertEquals(WaitStatus.OK, ws.status());
        assertEquals(0L, ws.jitter());
    }

    @Test
    void testStatusConstants() {
        assertEquals(0, WaitStatus.OK);
        assertEquals(1, WaitStatus.LATE);
        assertEquals(2, WaitStatus.UNSCHEDULED);
        assertEquals(3, WaitStatus.BUSY);
        assertEquals(4, WaitStatus.BADTIME);
        assertEquals(5, WaitStatus.ERROR);
        assertEquals(6, WaitStatus.UNSUPPORTED);
    }

    @Test
    void testRecordStoresValues() {
        WaitStatus ws = new WaitStatus(WaitStatus.LATE, 1234L);

        assertEquals(WaitStatus.LATE, ws.status());
        assertEquals(1234L, ws.jitter());
    }

    @Test
    void testWithStatusCreatesNewInstance() {
        WaitStatus ws1 = new WaitStatus(WaitStatus.OK, 500L);
        WaitStatus ws2 = ws1.withStatus(WaitStatus.LATE);

        assertEquals(WaitStatus.OK, ws1.status());
        assertEquals(WaitStatus.LATE, ws2.status());

        assertEquals(500L, ws1.jitter());
        assertEquals(500L, ws2.jitter());

        assertNotSame(ws1, ws2);
    }

    @Test
    void testImmutability() {
        WaitStatus ws = new WaitStatus(WaitStatus.BUSY, 999L);

        WaitStatus ws2 = ws.withStatus(WaitStatus.UNSUPPORTED);

        assertEquals(WaitStatus.BUSY, ws.status());
        assertEquals(999L, ws.jitter());

        assertEquals(WaitStatus.UNSUPPORTED, ws2.status());
        assertEquals(999L, ws2.jitter());
    }
}
