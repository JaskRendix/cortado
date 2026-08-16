package com.fluendo.jst;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CapsListenerTest {

  /** A tiny helper class to simulate a component that notifies listeners */
  private static class CapsNotifier {
    private CapsListener listener;

    void setListener(CapsListener l) {
      this.listener = l;
    }

    void changeCaps(Caps caps) {
      if (listener != null) {
        listener.capsChanged(caps);
      }
    }
  }

  @Test
  void testListenerReceivesCaps() {
    CapsNotifier notifier = new CapsNotifier();
    Caps caps = new Caps("video/x-raw; width=640; height=480");

    final boolean[] called = {false};
    final Caps[] received = {null};

    notifier.setListener(
        c -> {
          called[0] = true;
          received[0] = c;
        });

    notifier.changeCaps(caps);

    assertTrue(called[0]);
    assertEquals(caps, received[0]);
  }

  @Test
  void testListenerObservesUpdatedFields() {
    CapsNotifier notifier = new CapsNotifier();
    Caps caps = new Caps("audio/x-raw; rate=44100");

    final int[] rate = {0};

    notifier.setListener(c -> rate[0] = c.getFieldInt("rate", -1));

    notifier.changeCaps(caps);

    assertEquals(44100, rate[0]);
  }

  @Test
  void testListenerNotCalledIfNotSet() {
    CapsNotifier notifier = new CapsNotifier();
    Caps caps = new Caps("video/x-raw");

    // No listener set
    notifier.changeCaps(caps);

    // Nothing to assert except that no exception occurs
    assertTrue(true);
  }

  @Test
  void testMultipleNotifications() {
    CapsNotifier notifier = new CapsNotifier();
    Caps caps1 = new Caps("video/x-raw; width=320");
    Caps caps2 = new Caps("video/x-raw; width=800");

    final int[] width = {0};

    notifier.setListener(c -> width[0] = c.getFieldInt("width", -1));

    notifier.changeCaps(caps1);
    assertEquals(320, width[0]);

    notifier.changeCaps(caps2);
    assertEquals(800, width[0]);
  }
}
