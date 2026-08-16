package com.fluendo.jst;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BusHandlerTest {

  /** Minimal concrete Object subclass for testing */
  private static class TestObj extends com.fluendo.jst.Object {
    TestObj() {
      super();
    }
  }

  /** A simple bus-like helper that accepts a handler */
  private static class TestBus {
    private BusHandler handler;

    void setHandler(BusHandler h) {
      this.handler = h;
    }

    void post(Message msg) {
      if (handler != null) {
        handler.handleMessage(msg);
      }
    }
  }

  @Test
  void testHandlerReceivesMessage() {
    TestBus bus = new TestBus();
    Message msg = Message.newError(new TestObj(), "hello");

    final boolean[] called = {false};
    final Message[] received = {null};

    bus.setHandler(
        m -> {
          called[0] = true;
          received[0] = m;
        });

    bus.post(msg);

    assertTrue(called[0]);
    assertSame(msg, received[0]);
  }

  @Test
  void testHandlerNotCalledIfNotSet() {
    TestBus bus = new TestBus();
    Message msg = Message.newWarning(new TestObj(), "ignored");

    bus.post(msg);

    assertTrue(true); // no crash = success
  }

  @Test
  void testHandlerCalledMultipleTimes() {
    TestBus bus = new TestBus();
    final int[] count = {0};

    bus.setHandler(m -> count[0]++);

    bus.post(Message.newEOS(new TestObj()));
    bus.post(Message.newEOS(new TestObj()));
    bus.post(Message.newEOS(new TestObj()));

    assertEquals(3, count[0]);
  }

  @Test
  void testHandlerObservesMessageContent() {
    TestBus bus = new TestBus();
    final String[] lastText = {null};

    bus.setHandler(m -> lastText[0] = m.toString());

    bus.post(Message.newError(new TestObj(), "alpha"));
    assertTrue(lastText[0].contains("alpha"));

    bus.post(Message.newError(new TestObj(), "beta"));
    assertTrue(lastText[0].contains("beta"));
  }
}
