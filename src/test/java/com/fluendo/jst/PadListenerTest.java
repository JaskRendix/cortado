package com.fluendo.jst;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class PadListenerTest {

  private static class TestPad extends Pad {
    TestPad(String name) {
      super(Pad.SINK, name);
    }
  }

  private static class TestElement extends Element {
    TestElement() {
      super("test-element");
    }

    @Override
    public String getFactoryName() {
      return "test-factory";
    }
  }

  @Test
  void testPadAddedCallback() {
    AtomicInteger added = new AtomicInteger();

    PadListener listener = pad -> added.incrementAndGet();

    TestElement element = new TestElement();
    element.addPadListener(listener);

    TestPad pad = new TestPad("sink");
    element.addPad(pad);

    assertEquals(1, added.get());
  }

  @Test
  void testPadRemovedCallback() {
    AtomicInteger removed = new AtomicInteger();

    PadListener listener =
        new PadListener() {
          @Override
          public void padAdded(Pad pad) {}

          @Override
          public void padRemoved(Pad pad) {
            removed.incrementAndGet();
          }
        };

    TestElement element = new TestElement();
    element.addPadListener(listener);

    TestPad pad = new TestPad("sink");
    element.addPad(pad);
    element.removePad(pad);

    assertEquals(1, removed.get());
  }

  @Test
  void testNoMorePadsCallback() {
    AtomicInteger noMore = new AtomicInteger();

    PadListener listener =
        new PadListener() {
          @Override
          public void padAdded(Pad pad) {}

          @Override
          public void noMorePads() {
            noMore.incrementAndGet();
          }
        };

    TestElement element = new TestElement();
    element.addPadListener(listener);

    element.noMorePads();

    assertEquals(1, noMore.get());
  }

  @Test
  void testMultipleListeners() {
    AtomicInteger added1 = new AtomicInteger();
    AtomicInteger added2 = new AtomicInteger();

    PadListener l1 = pad -> added1.incrementAndGet();
    PadListener l2 = pad -> added2.incrementAndGet();

    TestElement element = new TestElement();
    element.addPadListener(l1);
    element.addPadListener(l2);

    TestPad pad = new TestPad("sink");
    element.addPad(pad);

    assertEquals(1, added1.get());
    assertEquals(1, added2.get());
  }

  @Test
  void testListenerNotCalledAfterRemoval() {
    AtomicInteger added = new AtomicInteger();

    PadListener listener = pad -> added.incrementAndGet();

    TestElement element = new TestElement();
    element.addPadListener(listener);

    TestPad pad = new TestPad("sink");
    element.addPad(pad);

    element.removePadListener(listener);

    TestPad pad2 = new TestPad("sink2");
    element.addPad(pad2);

    assertEquals(1, added.get());
  }
}
