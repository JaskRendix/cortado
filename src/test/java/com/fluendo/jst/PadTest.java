package com.fluendo.jst;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PadTest {

  private static class TestPad extends Pad {
    TestPad(int direction, String name) {
      super(direction, name);
    }

    @Override
    protected boolean activateFunc(int mode) {
      return true;
    }
  }

  private TestPad src;
  private TestPad sink;

  @BeforeEach
  void setup() {
    src = new TestPad(Pad.SRC, "src");
    sink = new TestPad(Pad.SINK, "sink");
  }

  @Test
  void testLinkSuccess() {
    assertTrue(src.link(sink));
    assertEquals(sink, src.getPeer());
    assertEquals(src, sink.getPeer());
  }

  @Test
  void testLinkWrongDirection() {
    TestPad wrong = new TestPad(Pad.SINK, "wrong");
    assertFalse(wrong.link(sink));
  }

  @Test
  void testLinkAlreadyLinked() {
    assertTrue(src.link(sink));
    TestPad otherSink = new TestPad(Pad.SINK, "other");
    assertFalse(src.link(otherSink));
  }

  @Test
  void testUnlink() {
    src.link(sink);
    src.unlink();
    assertNull(src.getPeer());
    assertNull(sink.getPeer());
  }

  @Test
  void testActivatePushMode() {
    assertTrue(src.activate(Pad.MODE_PUSH));
    assertFalse(src.isFlushing());
  }

  @Test
  void testActivateNoneMode() {
    src.activate(Pad.MODE_PUSH);
    assertTrue(src.activate(Pad.MODE_NONE));
    assertTrue(src.isFlushing());
  }

  @Test
  void testActivateIdempotent() {
    src.activate(Pad.MODE_PUSH);
    assertTrue(src.activate(Pad.MODE_PUSH));
  }

  @Test
  void testSetFlushing() {
    assertFalse(src.isFlushing());
    src.setFlushing(true);
    assertTrue(src.isFlushing());
  }

  @Test
  void testGetPeer() {
    assertNull(src.getPeer());
    src.link(sink);
    assertEquals(sink, src.getPeer());
  }

  @Test
  void testIsFlowFatal() {
    assertTrue(Pad.isFlowFatal(Pad.UNEXPECTED));
    assertTrue(Pad.isFlowFatal(Pad.NOT_NEGOTIATED));
    assertTrue(Pad.isFlowFatal(Pad.ERROR));
    assertFalse(Pad.isFlowFatal(Pad.OK));
  }

  @Test
  void testIsFlowSuccess() {
    assertTrue(Pad.isFlowSuccess(Pad.OK));
    assertFalse(Pad.isFlowSuccess(Pad.NOT_LINKED));
  }

  @Test
  void testGetFlowName() {
    assertEquals("ok", Pad.getFlowName(Pad.OK));
    assertEquals("not-linked", Pad.getFlowName(Pad.NOT_LINKED));
    assertEquals("error", Pad.getFlowName(Pad.ERROR));
    assertEquals("unknown", Pad.getFlowName(999));
  }

  @Test
  void testToString() {
    assertNotNull(src.toString());
    assertTrue(src.toString().contains("Pad:"));
  }
}
