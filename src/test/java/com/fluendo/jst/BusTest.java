package com.fluendo.jst;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class BusTest {

  /** Minimal concrete Object implementation for Message.src */
  private static class TestObject extends com.fluendo.jst.Object {
    TestObject(String name) {
      super(name);
    }
  }

  /** Simple handler counting received messages */
  private static class CountingHandler implements BusHandler {
    AtomicInteger count = new AtomicInteger();

    @Override
    public void handleMessage(Message message) {
      count.incrementAndGet();
    }
  }

  /** Sync handler that passes all messages */
  private static class SyncPassHandler implements BusSyncHandler {
    AtomicInteger syncCount = new AtomicInteger();

    @Override
    public int handleSyncMessage(Message message) {
      syncCount.incrementAndGet();
      return PASS;
    }
  }

  /** Sync handler that drops all messages */
  private static class SyncDropHandler implements BusSyncHandler {
    AtomicInteger syncCount = new AtomicInteger();

    @Override
    public int handleSyncMessage(Message message) {
      syncCount.incrementAndGet();
      return DROP;
    }
  }

  private Message newMsg(String text) {
    return Message.newError(new TestObject("src"), text);
  }

  @Test
  void testAddAndRemoveHandler() {
    Bus bus = new Bus();
    CountingHandler h = new CountingHandler();

    bus.addHandler(h);
    bus.removeHandler(h);

    assertTrue(true); // no exception
  }

  @Test
  void testPostWithoutSyncHandler() {
    Bus bus = new Bus();
    Message msg = newMsg("test");

    bus.post(msg);

    assertEquals(msg, bus.peek());
  }

  @Test
  void testPostWithSyncPassHandler() {
    Bus bus = new Bus();
    SyncPassHandler sync = new SyncPassHandler();
    bus.setSyncHandler(sync);

    Message msg = newMsg("sync-pass");

    bus.post(msg);

    assertEquals(1, sync.syncCount.get());
    assertEquals(msg, bus.peek());
  }

  @Test
  void testPostWithSyncDropHandler() {
    Bus bus = new Bus();
    SyncDropHandler sync = new SyncDropHandler();
    bus.setSyncHandler(sync);

    Message msg = newMsg("sync-drop");

    bus.post(msg);

    assertEquals(1, sync.syncCount.get());
    assertNull(bus.peek()); // dropped
  }

  @Test
  void testPeekPop() {
    Bus bus = new Bus();
    Message msg1 = newMsg("m1");
    Message msg2 = newMsg("m2");

    bus.post(msg1);
    bus.post(msg2);

    assertEquals(msg1, bus.peek());
    assertEquals(msg1, bus.pop());
    assertEquals(msg2, bus.peek());
  }

  @Test
  void testPollWithTimeout() {
    Bus bus = new Bus();

    // empty queue → poll waits → returns null
    Message result = bus.poll(10);
    assertNull(result);

    Message msg = newMsg("poll");
    bus.post(msg);

    assertEquals(msg, bus.poll(10));
  }

  @Test
  void testFlushingClearsQueue() {
    Bus bus = new Bus();
    Message msg = newMsg("flush");

    bus.post(msg);
    assertNotNull(bus.peek());

    bus.setFlushing(true);

    assertNull(bus.peek());
    assertNull(bus.pop());
  }

  @Test
  void testWaitAndDispatch() {
    Bus bus = new Bus();
    CountingHandler h = new CountingHandler();
    bus.addHandler(h);

    Message msg = newMsg("dispatch");
    bus.post(msg);

    bus.waitAndDispatch();

    assertEquals(1, h.count.get());
  }

  @Test
  void testMultipleHandlers() {
    Bus bus = new Bus();
    CountingHandler h1 = new CountingHandler();
    CountingHandler h2 = new CountingHandler();

    bus.addHandler(h1);
    bus.addHandler(h2);

    Message msg = newMsg("multi");
    bus.post(msg);

    bus.waitAndDispatch();

    assertEquals(1, h1.count.get());
    assertEquals(1, h2.count.get());
  }

  @Test
  void testFlushingPreventsPost() {
    Bus bus = new Bus();
    bus.setFlushing(true);

    Message msg = newMsg("ignored");
    bus.post(msg);

    assertNull(bus.peek());
  }
}
