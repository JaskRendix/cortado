package com.fluendo.player;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class StatusListenerTest {

    @Test
    @DisplayName("StatusListener Interface: Concrete implementation contract check")
    void testStatusListenerImplementation() {
        AtomicInteger stateResult = new AtomicInteger(-1);
        AtomicReference<Double> seekResult = new AtomicReference<>(-1.0);
        AtomicBoolean audioCalled = new AtomicBoolean(false);
        AtomicInteger subtitleX = new AtomicInteger(-1);
        AtomicInteger subtitleY = new AtomicInteger(-1);

        StatusListener listener = new StatusListener() {
            @Override
            public void onState(int newState) {
                stateResult.set(newState);
            }

            @Override
            public void onSeek(double position) {
                seekResult.set(position);
            }

            @Override
            public void onAudio() {
                audioCalled.set(true);
            }

            @Override
            public void onSubtitles(int x, int y) {
                subtitleX.set(x);
                subtitleY.set(y);
            }
        };

        // Trigger methods and assert state changes
        listener.onState(Status.STATE_PLAYING);
        assertEquals(Status.STATE_PLAYING, stateResult.get());

        listener.onSeek(0.75);
        assertEquals(0.75, seekResult.get(), 0.001);

        listener.onAudio();
        assertTrue(audioCalled.get());

        listener.onSubtitles(15, 30);
        assertEquals(15, subtitleX.get());
        assertEquals(30, subtitleY.get());
    }
}
