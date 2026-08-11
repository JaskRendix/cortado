package com.fluendo.jst;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BufferTest {

    @Test
    void testCreateInitialState() {
        Buffer buf = Buffer.create();

        assertEquals(-1, buf.time_offset);
        assertEquals(-1, buf.timestamp);
        assertEquals(-1, buf.timestampEnd);
        assertEquals(0, buf.flags);
        assertNull(buf.object);
        assertNull(buf.caps);
    }

    @Test
    void testFlagSetAndClear() {
        Buffer buf = Buffer.create();

        buf.setFlag(Buffer.FLAG_DISCONT, true);
        assertTrue(buf.isFlagSet(Buffer.FLAG_DISCONT));

        buf.setFlag(Buffer.FLAG_DISCONT, false);
        assertFalse(buf.isFlagSet(Buffer.FLAG_DISCONT));
    }

    @Test
    void testEnsureSizeCreatesData() {
        Buffer buf = Buffer.create();

        buf.ensureSize(10);
        assertNotNull(buf.data);
        assertEquals(10, buf.data.length);
    }

    @Test
    void testEnsureSizeDoesNotShrink() {
        Buffer buf = Buffer.create();

        buf.ensureSize(10);
        buf.ensureSize(5);   // should NOT shrink

        assertEquals(10, buf.data.length);
    }

    @Test
    void testEnsureSizeExpands() {
        Buffer buf = Buffer.create();

        buf.ensureSize(10);
        buf.ensureSize(20);  // should expand

        assertEquals(20, buf.data.length);
    }

    @Test
    void testCopyData() {
        Buffer buf = Buffer.create();

        byte[] src = new byte[]{1, 2, 3, 4};
        buf.copyData(src, 1, 2);

        assertEquals(2, buf.length);
        assertEquals(0, buf.offset);
        assertArrayEquals(new byte[]{2, 3}, buf.data);
    }

    @Test
    void testFreePushesToPool() {
        Buffer buf = Buffer.create();
        buf.free();

        Buffer buf2 = Buffer.create();  // should reuse from pool

        assertSame(buf, buf2);
    }

    @Test
    void testFreeResetsFields() {
        Buffer buf = Buffer.create();
        buf.object = new java.lang.Object();
        buf.caps = new Caps("audio/raw");
        buf.flags = Buffer.FLAG_DELTA_UNIT;

        buf.free();

        assertNull(buf.object);
        assertNull(buf.caps);
        assertEquals(Buffer.FLAG_DELTA_UNIT, buf.flags);
    }

    @Test
    void testTimestampFields() {
        Buffer buf = Buffer.create();

        buf.timestamp = 12345L;
        buf.timestampEnd = 54321L;
        buf.time_offset = 999L;

        assertEquals(12345L, buf.timestamp);
        assertEquals(54321L, buf.timestampEnd);
        assertEquals(999L, buf.time_offset);
    }

    @Test
    void testDuplicateFlag() {
        Buffer buf = Buffer.create();
        buf.duplicate = true;

        assertTrue(buf.duplicate);

        buf.duplicate = false;
        assertFalse(buf.duplicate);
    }

    @Test
    void testCapsAssignment() {
        Buffer buf = Buffer.create();
        Caps caps = new Caps("video/raw");

        buf.caps = caps;

        assertEquals(caps, buf.caps);
    }

    @Test
    void testPoolReuseMultipleBuffers() {
        Buffer b1 = Buffer.create();
        Buffer b2 = Buffer.create();

        b1.free();
        b2.free();

        Buffer r1 = Buffer.create();
        Buffer r2 = Buffer.create();

        // pool is LIFO, so r1 == b2, r2 == b1
        assertSame(b2, r1);
        assertSame(b1, r2);
    }
}
