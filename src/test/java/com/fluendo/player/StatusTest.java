package com.fluendo.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Button;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class StatusTest {

    private Status status;
    private Component dummyComponent;

    @BeforeEach
    void setUp() {
        dummyComponent = new Button("Dummy Parent");
        dummyComponent.setBounds(0, 0, 200, 50);
        status = new Status(dummyComponent);
        status.setBounds(0, 0, 200, 50);
        
        // Initialize 'r' inside Status by rendering/calling paint with a dummy Graphics object
        BufferedImage img = new BufferedImage(200, 50, BufferedImage.TYPE_INT_RGB);
        status.paint(img.getGraphics());
        status.update(img.getGraphics());
    }

    @Test
    @DisplayName("State and Buffer: Default states check")
    void testInitialStates() {
        assertDoesNotThrow(() -> status.setState(Status.STATE_PLAYING));
        assertDoesNotThrow(() -> status.setBufferPercent(true, 50));
        assertDoesNotThrow(() -> status.setBufferPercent(true, 50)); // Test duplicate assignment guard
    }

    @Test
    @DisplayName("Setters: Setting duration, start time, and time parameters safely")
    void testTimeAndDurationSetters() {
        assertDoesNotThrow(() -> {
            status.setStartTime(-5.0); // Test negative start time bounds
            status.setDuration(100.0);
            status.setTime(25.5);
            status.setTime(150.0); // Test time exceeding duration branch
            status.setByteDuration(1000L);
            status.setBytePosition(250L);
            status.setIgnoreBasetime(true);
        });
    }

    @Test
    @DisplayName("Display Options: Audio, Subtitles, and Live flags update")
    void testDisplayFlags() {
        assertDoesNotThrow(() -> {
            status.setHaveAudio(true);
            status.setShowSpeaker(true);
            status.setHaveSubtitles(true);
            status.setShowSubtitles(true);
            status.setSeekable(true);
            status.setLive(false);
            status.setMessage("Loading...");
        });
    }

    @Test
    @DisplayName("Mouse Events: Edge case interactions without loaded graphics/bounds")
    void testMouseInteractionsWithoutBounds() {
        MouseEvent dummyEvent = new MouseEvent(
                dummyComponent, 
                MouseEvent.MOUSE_PRESSED, 
                System.currentTimeMillis(), 
                0, 
                10, 10, 
                1, 
                false
        );

        assertDoesNotThrow(() -> status.mousePressed(dummyEvent));
        assertDoesNotThrow(() -> status.mouseReleased(dummyEvent));
        assertDoesNotThrow(() -> status.mouseDragged(dummyEvent));
        assertDoesNotThrow(() -> status.mouseMoved(dummyEvent));
        assertDoesNotThrow(() -> status.cancelMouseOperation());
        assertDoesNotThrow(() -> status.mouseClicked(dummyEvent));
        assertDoesNotThrow(() -> status.mouseEntered(dummyEvent));
        assertDoesNotThrow(() -> status.mouseExited(dummyEvent));
    }

    @Test
    @DisplayName("Mouse Actions: Clicking Play/Pause button bounds")
    void testButtonClickInteractions() {
        status.setState(Status.STATE_STOPPED);
        status.setSeekable(true);
        
        // Inside Button 1 coordinates (x=5, y=5 within bounds)
        MouseEvent pressEvent = new MouseEvent(
                dummyComponent, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 5, 5, 1, false);
        MouseEvent releaseEvent = new MouseEvent(
                dummyComponent, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 5, 5, 1, false);

        assertDoesNotThrow(() -> {
            status.mousePressed(pressEvent);
            status.mouseReleased(releaseEvent);
        });
    }

    @Test
    @DisplayName("Listeners: Adding, triggering, and removing status listeners successfully")
    void testStatusListeners() {
        AtomicInteger stateCalled = new AtomicInteger(-1);
        AtomicReference<Double> seekCalled = new AtomicReference<>(-1.0);
        AtomicBoolean audioCalled = new AtomicBoolean(false);
        AtomicBoolean subCalled = new AtomicBoolean(false);

        StatusListener dummyListener = new StatusListener() {
            @Override public void onState(int state) { stateCalled.set(state); }
            @Override public void onSeek(double position) { seekCalled.set(position); }
            @Override public void onAudio() { audioCalled.set(true); }
            @Override public void onSubtitles(int x, int y) { subCalled.set(true); }
        };

        status.addStatusListener(dummyListener);

        status.notifyNewState(Status.STATE_PLAYING);
        assertEquals(Status.STATE_PLAYING, stateCalled.get());

        status.notifySeek(0.75);
        assertEquals(0.75, seekCalled.get());

        status.notifyAudio();
        assertTrue(audioCalled.get());

        status.notifySubtitles(15, 25);
        assertTrue(subCalled.get());

        status.removeStatusListener(dummyListener);
    }
}
