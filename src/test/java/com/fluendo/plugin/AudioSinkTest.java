package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.fluendo.jst.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AudioSinkTest {

  private TestAudioSink audioSink;

  private static class TestAudioSink extends AudioSink {
    private int writeCount = 0;
    private long mockDelay = 0;

    @Override
    public String getFactoryName() {
      return "testaudiosink";
    }

    @Override
    protected RingBuffer createRingBuffer() {
      return new RingBuffer();
    }

    @Override
    protected boolean open(RingBuffer ring) {
      ring.segSize = 1024;
      ring.segTotal = 4;
      ring.emptySeg = new byte[ring.segSize];
      return true;
    }

    @Override
    protected boolean close(RingBuffer ring) {
      return true;
    }

    @Override
    protected int write(byte[] data, int offset, int length) {
      writeCount++;
      return length;
    }

    @Override
    protected long delay() {
      return mockDelay;
    }

    @Override
    protected void reset() {
      mockDelay = 0;
    }
  }

  @BeforeEach
  void setUp() {
    audioSink = new TestAudioSink();
  }

  @Test
  @DisplayName("Edge Case: Zero-Rate Handling in Clock Provision when RingBuffer is Uninitialized")
  void testAudioClockWithNullRingBuffer() {
    Clock clock = audioSink.provideClock();
    assertNotNull(clock, "Clock provider should never return null");

    audioSink.changeState(Element.STOP_PAUSE);
    audioSink.ringBuffer.rate = 0;

    assertEquals(
        0, clock.getTime(), "Time should gracefully default to 0 when ringBuffer rate is 0");
  }

  @Test
  @DisplayName("Edge Case: Out-of-Bounds and Invalid Commit Samples")
  void testRingBufferCommitEdgeCases() {
    audioSink.changeState(Element.STOP_PAUSE);
    AudioSink.RingBuffer ring = audioSink.ringBuffer;

    Caps caps = new Caps("audio/raw");
    caps.setField("rate", 44100);
    caps.setField("channels", 2);

    assertTrue(
        ring.acquire(caps), "RingBuffer should successfully acquire valid audio capabilities");

    byte[] fakeData = new byte[4096];

    int resultNegativeSample = ring.commit(fakeData, -5, 0, 100);
    assertEquals(
        100,
        resultNegativeSample,
        "Commit with negative sample index should short-circuit and return length");

    ring.release();
  }

  @Test
  @DisplayName("Edge Case: State Machine Transitions and Reset Safety")
  void testStateTransitionsAndFlushing() {
    // Test valid standalone sink states (STOP -> PAUSE) which safely initialize the ring buffer
    audioSink.changeState(Element.STOP_PAUSE);
    assertNotNull(
        audioSink.ringBuffer, "RingBuffer should be instantiated after entering pause state");

    audioSink.reset();
    assertEquals(0, audioSink.delay(), "Delay should be reset cleanly to 0");

    audioSink.changeState(Element.PAUSE_STOP);
  }
}
