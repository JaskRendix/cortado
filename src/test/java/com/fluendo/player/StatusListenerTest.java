package com.fluendo.player;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StatusListenerTest {

  private static class RecordingStatusListener implements StatusListener {
    int state = -1;
    double position = -1;
    boolean audioReceived;
    int subtitleX = -1;
    int subtitleY = -1;

    @Override
    public void onState(int newState) {
      state = newState;
    }

    @Override
    public void onSeek(double position) {
      this.position = position;
    }

    @Override
    public void onAudio() {
      audioReceived = true;
    }

    @Override
    public void onSubtitles(int x, int y) {
      subtitleX = x;
      subtitleY = y;
    }
  }

  @Test
  @DisplayName("StatusListener implementations receive callback values")
  void testListenerReceivesCallbacks() {
    RecordingStatusListener listener = new RecordingStatusListener();

    listener.onState(Status.STATE_PLAYING);
    listener.onSeek(0.75);
    listener.onAudio();
    listener.onSubtitles(15, 30);

    assertEquals(Status.STATE_PLAYING, listener.state);
    assertEquals(0.75, listener.position, 0.001);
    assertTrue(listener.audioReceived);
    assertEquals(15, listener.subtitleX);
    assertEquals(30, listener.subtitleY);
  }
}
