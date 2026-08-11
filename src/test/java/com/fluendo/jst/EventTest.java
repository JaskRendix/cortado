package com.fluendo.jst;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    void testNewEOS() {
        Event e = Event.newEOS();
        assertEquals(Event.Type.EOS, e.getType());
        assertEquals(-1, e.parseNewsegmentPosition());
        assertEquals("[Event] type: EOS", e.toString());
    }

    @Test
    void testNewFlushStart() {
        Event e = Event.newFlushStart();
        assertEquals(Event.Type.FLUSH_START, e.getType());
        assertEquals("[Event] type: FLUSH_START", e.toString());
    }

    @Test
    void testNewFlushStop() {
        Event e = Event.newFlushStop();
        assertEquals(Event.Type.FLUSH_STOP, e.getType());
        assertEquals("[Event] type: FLUSH_STOP", e.toString());
    }

    @Test
    void testNewSeek() {
        Event e = Event.newSeek(7, 12345L);

        assertEquals(Event.Type.SEEK, e.getType());
        assertEquals(7, e.parseSeekFormat());
        assertEquals(12345L, e.parseSeekPosition());

        String s = e.toString();
        assertTrue(s.contains("SEEK"));
        assertTrue(s.contains("format: 7"));
        assertTrue(s.contains("position: 12345"));
    }

    @Test
    void testNewSeekEdgeCases() {
        Event e = Event.newSeek(Integer.MAX_VALUE, Long.MIN_VALUE);

        assertEquals(Integer.MAX_VALUE, e.parseSeekFormat());
        assertEquals(Long.MIN_VALUE, e.parseSeekPosition());
    }

    @Test
    void testNewSegment() {
        Event e = Event.newNewsegment(true, 3, 100L, 200L, 150L);

        assertEquals(Event.Type.NEWSEGMENT, e.getType());
        assertTrue(e.parseNewsegmentUpdate());
        assertEquals(3, e.parseNewsegmentFormat());
        assertEquals(100L, e.parseNewsegmentStart());
        assertEquals(200L, e.parseNewsegmentStop());
        assertEquals(150L, e.parseNewsegmentPosition());

        String s = e.toString();
        assertTrue(s.contains("NEWSEGMENT"));
        assertTrue(s.contains("update"));
        assertTrue(s.contains("format: 3"));
        assertTrue(s.contains("start: 100"));
        assertTrue(s.contains("stop: 200"));
        assertTrue(s.contains("position: 150"));
    }

    @Test
    void testNewSegmentNonUpdate() {
        Event e = Event.newNewsegment(false, 1, 0L, 0L, -1L);

        assertFalse(e.parseNewsegmentUpdate());
        assertEquals(1, e.parseNewsegmentFormat());
        assertEquals(0L, e.parseNewsegmentStart());
        assertEquals(0L, e.parseNewsegmentStop());
        assertEquals(-1L, e.parseNewsegmentPosition());

        assertTrue(e.toString().contains("non-update"));
    }

    @Test
    void testTypeIsImmutable() {
        Event e = Event.newEOS();
        assertEquals(Event.Type.EOS, e.getType());
        // no setter exists — compile‑time immutability
    }

    @Test
    void testToStringDefaultCases() {
        assertEquals("[Event] type: FLUSH_START", Event.newFlushStart().toString());
        assertEquals("[Event] type: FLUSH_STOP", Event.newFlushStop().toString());
        assertEquals("[Event] type: EOS", Event.newEOS().toString());
    }
}
