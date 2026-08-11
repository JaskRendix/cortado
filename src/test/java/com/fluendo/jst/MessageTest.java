package com.fluendo.jst;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    /** Minimal concrete Object implementation for Message.src */
    private static class TestObject extends com.fluendo.jst.Object {
        TestObject(String name) {
            super(name);
        }
    }

    private final TestObject src = new TestObject("src");

    @Test
    void testNewEOS() {
        Message m = Message.newEOS(src);

        assertEquals(Message.EOS, m.getType());
        assertEquals(src, m.getSrc());
        assertTrue(m.toString().contains("EOS"));
    }

    @Test
    void testNewError() {
        Message m = Message.newError(src, "fatal");

        assertEquals(Message.ERROR, m.getType());
        assertEquals("fatal", m.parseErrorString());
        assertTrue(m.toString().contains("fatal"));
    }

    @Test
    void testNewWarning() {
        Message m = Message.newWarning(src, "warn");

        assertEquals(Message.WARNING, m.getType());
        assertEquals("warn", m.parseErrorString());
    }

    @Test
    void testNewBuffering() {
        Message m = Message.newBuffering(src, true, 75);

        assertEquals(Message.BUFFERING, m.getType());
        assertTrue(m.parseBufferingBusy());
        assertEquals(75, m.parseBufferingPercent());
        assertTrue(m.toString().contains("percent:75"));
    }

    @Test
    void testNewStateChanged() {
        Message m = Message.newStateChanged(src, Element.STOP, Element.PLAY, Element.PAUSE);

        assertEquals(Message.STATE_CHANGED, m.getType());
        assertEquals(Element.STOP, m.parseStateChangedOld());
        assertEquals(Element.PLAY, m.parseStateChangedNext());
        assertEquals(Element.PAUSE, m.parseStateChangedPending());

        String s = m.toString();
        assertTrue(s.contains(Element.getStateName(Element.STOP)));
        assertTrue(s.contains(Element.getStateName(Element.PLAY)));
        assertTrue(s.contains(Element.getStateName(Element.PAUSE)));
    }

    @Test
    void testNewStateDirty() {
        Message m = Message.newStateDirty(src);

        assertEquals(Message.STATE_DIRTY, m.getType());
        assertTrue(m.toString().contains("STATE_DIRTY"));
    }

    @Test
    void testNewStreamStatus() {
        Message m = Message.newStreamStatus(src, true, Pad.OK, "sync");

        assertEquals(Message.STREAM_STATUS, m.getType());
        assertTrue(m.parseStreamStatusStart());
        assertEquals(Pad.OK, m.parseStreamStatusReason());
        assertEquals("sync", m.parseStreamStatusString());

        String s = m.toString();
        assertTrue(s.contains("start"));
        assertTrue(s.contains("ok"));
        assertTrue(s.contains("sync"));
    }

    @Test
    void testNewResource() {
        Message m = Message.newResource(src, "file.dat");

        assertEquals(Message.RESOURCE, m.getType());
        assertEquals("file.dat", m.parseResourceString());
    }

    @Test
    void testNewDuration() {
        Message m = Message.newDuration(src, Format.TIME, 123456L);

        assertEquals(Message.DURATION, m.getType());
        assertEquals(Format.TIME, m.parseDurationFormat());
        assertEquals(123456L, m.parseDurationValue());
    }

    @Test
    void testNewBytePosition() {
        Message m = Message.newBytePosition(src, 9999L);

        assertEquals(Message.BYTEPOSITION, m.getType());
        assertEquals(9999L, m.parseBytePosition());
    }

    @Test
    void testToStringDefault() {
        Message m = Message.newResource(src, "abc");
        String s = m.toString();
        assertTrue(s.contains(String.valueOf(Message.RESOURCE)));
        assertFalse(s.contains("abc"));
    }

    @Test
    void testSrcIsStored() {
        Message m = Message.newEOS(src);
        assertSame(src, m.getSrc());
    }

    @Test
    void testTypeIsStored() {
        Message m = Message.newEOS(src);
        assertEquals(Message.EOS, m.getType());
    }
}
