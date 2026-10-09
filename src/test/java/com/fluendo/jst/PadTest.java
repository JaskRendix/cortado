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
  void testLinkSrcToSrcFails() {
    TestPad otherSrc = new TestPad(Pad.SRC, "otherSrc");

    assertFalse(src.link(otherSrc));
    assertNull(src.getPeer());
    assertNull(otherSrc.getPeer());
  }

  @Test
  void testLinkFailsWhenPeerAlreadyLinked() {
    TestPad src2 = new TestPad(Pad.SRC, "src2");

    assertTrue(src.link(sink));
    assertFalse(src2.link(sink));

    assertEquals(sink, src.getPeer());
    assertEquals(src, sink.getPeer());
    assertNull(src2.getPeer());
  }

  @Test
  void testPushReturnsNotLinkedWhenNoPeer() {
    Buffer buffer = new Buffer();

    assertEquals(Pad.NOT_LINKED, src.push(buffer));
  }

  private static class ChainPad extends TestPad {
    boolean called;

    ChainPad(int direction, String name) {
      super(direction, name);
    }

    @Override
    protected int chainFunc(Buffer buffer) {
      called = true;
      return Pad.OK;
    }
  }

  @Test
  void testPushCallsChainFunc() {
    TestPad source = new TestPad(Pad.SRC, "src");
    ChainPad target = new ChainPad(Pad.SINK, "sink");

    assertTrue(source.link(target));

    Buffer buffer = new Buffer();

    assertEquals(Pad.OK, source.push(buffer));
    assertTrue(target.called);
  }

  @Test
  void testPushReturnsWrongStateWhenFlushing() {
    TestPad source = new TestPad(Pad.SRC, "src");
    ChainPad target = new ChainPad(Pad.SINK, "sink");

    assertTrue(source.link(target));

    target.setFlushing(true);

    Buffer buffer = new Buffer();

    assertEquals(Pad.WRONG_STATE, source.push(buffer));
  }

  @Test
  void testSetCapsNull() {
    assertTrue(src.setCaps(null));
    assertNull(src.getCaps());
  }

  @Test
  void testAllFlowNames() {
    assertEquals("ok", Pad.getFlowName(Pad.OK));
    assertEquals("not-linked", Pad.getFlowName(Pad.NOT_LINKED));
    assertEquals("wrong-state", Pad.getFlowName(Pad.WRONG_STATE));
    assertEquals("unexpected", Pad.getFlowName(Pad.UNEXPECTED));
    assertEquals("not-negotiated", Pad.getFlowName(Pad.NOT_NEGOTIATED));
    assertEquals("error", Pad.getFlowName(Pad.ERROR));
    assertEquals("not-supported", Pad.getFlowName(Pad.NOT_SUPPORTED));
  }

  @Test
  void testToStringContainsName() {
    String value = src.toString();

    assertTrue(value.startsWith("Pad:"));
    assertTrue(value.contains("src"));
  }

  @Test
  void testUnlinkWithoutPeer() {
    assertDoesNotThrow(() -> src.unlink());
    assertNull(src.getPeer());
  }

  @Test
  void testActivateModeChangeFromNoneToPushClearsFlushing() {
    src.setFlushing(true);

    assertTrue(src.activate(Pad.MODE_PUSH));

    assertFalse(src.isFlushing());
  }
}
