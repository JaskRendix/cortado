package com.fluendo.jst;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BusSyncHandlerTest {

  /** Minimal concrete Object subclass for testing */
  private static class TestObj extends com.fluendo.jst.Object {
    TestObj() {
      super();
    }
  }

  /** A simple bus-like helper that uses a BusSyncHandler */
  private static class TestBus {
    private BusSyncHandler handler;

    void setSyncHandler(BusSyncHandler h) {
      this.handler = h;
    }

    int dispatch(Message msg) {
      if (handler == null) {
        return BusSyncHandler.PASS; // default behavior
      }
      return handler.handleSyncMessage(msg);
    }
  }

  @Test
  void testHandlerReceivesMessage() {
    TestBus bus = new TestBus();
    Message msg = Message.newError(new TestObj(), "hello");

    final boolean[] called = {false};
    final Message[] received = {null};

    bus.setSyncHandler(
        m -> {
          called[0] = true;
          received[0] = m;
          return BusSyncHandler.PASS;
        });

    int result = bus.dispatch(msg);

    assertTrue(called[0]);
    assertSame(msg, received[0]);
    assertEquals(BusSyncHandler.PASS, result);
  }

  @Test
  void testHandlerReturnsDrop() {
    TestBus bus = new TestBus();
    Message msg = Message.newWarning(new TestObj(), "ignore me");

    bus.setSyncHandler(m -> BusSyncHandler.DROP);

    int result = bus.dispatch(msg);

    assertEquals(BusSyncHandler.DROP, result);
  }

  @Test
  void testHandlerReturnsPass() {
    TestBus bus = new TestBus();
    Message msg = Message.newEOS(new TestObj());

    bus.setSyncHandler(m -> BusSyncHandler.PASS);

    int result = bus.dispatch(msg);

    assertEquals(BusSyncHandler.PASS, result);
  }

  @Test
  void testNoHandlerMeansPass() {
    TestBus bus = new TestBus();
    Message msg = Message.newEOS(new TestObj());

    int result = bus.dispatch(msg);

    assertEquals(BusSyncHandler.PASS, result);
  }

  @Test
  void testHandlerObservesMessageContent() {
    TestBus bus = new TestBus();
    final String[] lastText = {null};

    bus.setSyncHandler(
        m -> {
          lastText[0] = m.toString();
          return BusSyncHandler.PASS;
        });

    bus.dispatch(Message.newError(new TestObj(), "alpha"));
    assertTrue(lastText[0].contains("alpha"));

    bus.dispatch(Message.newError(new TestObj(), "beta"));
    assertTrue(lastText[0].contains("beta"));
  }
}
