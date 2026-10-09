package com.fluendo.jst;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EventTest {

  @Test
  void testNewEOS() {
    Event e = Event.newEOS();
    assertEquals(Event.Type.EOS, e.getType());
    assertEquals(-1, e.getSegmentPosition());
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
    assertEquals(7, e.getSeekFormat());
    assertEquals(12345L, e.getSeekPosition());

    String s = e.toString();
    assertTrue(s.contains("SEEK"));
    assertTrue(s.contains("format: 7"));
    assertTrue(s.contains("position: 12345"));
  }

  @Test
  void testNewSeekEdgeCases() {
    Event e = Event.newSeek(Integer.MAX_VALUE, Long.MIN_VALUE);

    assertEquals(Integer.MAX_VALUE, e.getSeekFormat());
    assertEquals(Long.MIN_VALUE, e.getSeekPosition());
  }

  @Test
  void testNewSegment() {
    Event e = Event.newSegment(true, 3, 100L, 200L, 150L);

    assertEquals(Event.Type.NEWSEGMENT, e.getType());
    assertTrue(e.isSegmentUpdate());
    assertEquals(3, e.getSegmentFormat());
    assertEquals(100L, e.getSegmentStart());
    assertEquals(200L, e.getSegmentStop());
    assertEquals(150L, e.getSegmentPosition());

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
    Event e = Event.newSegment(false, 1, 0L, 0L, -1L);

    assertFalse(e.isSegmentUpdate());
    assertEquals(1, e.getSegmentFormat());
    assertEquals(0L, e.getSegmentStart());
    assertEquals(0L, e.getSegmentStop());
    assertEquals(-1L, e.getSegmentPosition());

    assertTrue(e.toString().contains("non-update"));
  }

  @Test
  void testToStringDefaultCases() {
    assertEquals("[Event] type: FLUSH_START", Event.newFlushStart().toString());
    assertEquals("[Event] type: FLUSH_STOP", Event.newFlushStop().toString());
    assertEquals("[Event] type: EOS", Event.newEOS().toString());
  }

  @Test
  void testNewSegmentEdgeCases() {
    Event e =
      Event.newSegment(
        true,
        Integer.MAX_VALUE,
        Long.MIN_VALUE,
        Long.MAX_VALUE,
        Long.MIN_VALUE);

    assertTrue(e.isSegmentUpdate());
    assertEquals(Integer.MAX_VALUE, e.getSegmentFormat());
    assertEquals(Long.MIN_VALUE, e.getSegmentStart());
    assertEquals(Long.MAX_VALUE, e.getSegmentStop());
    assertEquals(Long.MIN_VALUE, e.getSegmentPosition());
  }
}
