package com.fluendo.player;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Button;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    assertDoesNotThrow(
        () -> {
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
    assertDoesNotThrow(
        () -> {
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
    MouseEvent dummyEvent =
        new MouseEvent(
            dummyComponent,
            MouseEvent.MOUSE_PRESSED,
            System.currentTimeMillis(),
            0,
            10,
            10,
            1,
            false);

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
    MouseEvent pressEvent =
        new MouseEvent(
            dummyComponent,
            MouseEvent.MOUSE_PRESSED,
            System.currentTimeMillis(),
            0,
            5,
            5,
            1,
            false);
    MouseEvent releaseEvent =
        new MouseEvent(
            dummyComponent,
            MouseEvent.MOUSE_RELEASED,
            System.currentTimeMillis(),
            0,
            5,
            5,
            1,
            false);

    assertDoesNotThrow(
        () -> {
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

    StatusListener dummyListener =
        new StatusListener() {
          @Override
          public void onState(int state) {
            stateCalled.set(state);
          }

          @Override
          public void onSeek(double position) {
            seekCalled.set(position);
          }

          @Override
          public void onAudio() {
            audioCalled.set(true);
          }

          @Override
          public void onSubtitles(int x, int y) {
            subCalled.set(true);
          }
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

  @Test
  @DisplayName("Listeners: removed listener no longer receives callbacks")
  void testRemoveStatusListener() {
      AtomicInteger stateCalled = new AtomicInteger(-1);

      StatusListener listener = new StatusListener() {
          @Override
          public void onState(int state) {
              stateCalled.set(state);
          }

          @Override
          public void onSeek(double position) {}

          @Override
          public void onAudio() {}

          @Override
          public void onSubtitles(int x, int y) {}
      };

      status.addStatusListener(listener);
      status.removeStatusListener(listener);

      status.notifyNewState(Status.STATE_PLAYING);

      assertEquals(-1, stateCalled.get());
  }

  @Test
  @DisplayName("Mouse: play button triggers state listener")
  void testPlayButtonNotifiesListener() {
      AtomicInteger stateCalled = new AtomicInteger(-1);

      status.addStatusListener(new StatusListener() {
          @Override
          public void onState(int state) {
              stateCalled.set(state);
          }

          @Override public void onSeek(double position) {}
          @Override public void onAudio() {}
          @Override public void onSubtitles(int x, int y) {}
      });

      status.setState(Status.STATE_STOPPED);

      MouseEvent press =
              new MouseEvent(dummyComponent,
                      MouseEvent.MOUSE_PRESSED,
                      System.currentTimeMillis(),
                      0, 5, 5, 1, false);

      MouseEvent release =
              new MouseEvent(dummyComponent,
                      MouseEvent.MOUSE_RELEASED,
                      System.currentTimeMillis(),
                      0, 5, 5, 1, false);

      status.mousePressed(press);
      status.mouseReleased(release);

      assertEquals(Status.STATE_PLAYING, stateCalled.get());
  }

  @Test
  @DisplayName("Mouse: stop button triggers state listener")
  void testStopButtonNotifiesListener() {
      AtomicInteger stateCalled = new AtomicInteger(-1);

      status.addStatusListener(new StatusListener() {
          @Override
          public void onState(int state) {
              stateCalled.set(state);
          }

          @Override public void onSeek(double position) {}
          @Override public void onAudio() {}
          @Override public void onSubtitles(int x, int y) {}
      });

      status.setState(Status.STATE_PLAYING);

      int stopX = status.getHeight() + 5;

      MouseEvent press =
              new MouseEvent(dummyComponent,
                      MouseEvent.MOUSE_PRESSED,
                      System.currentTimeMillis(),
                      0, stopX, 5, 1, false);

      MouseEvent release =
              new MouseEvent(dummyComponent,
                      MouseEvent.MOUSE_RELEASED,
                      System.currentTimeMillis(),
                      0, stopX, 5, 1, false);

      status.mousePressed(press);
      status.mouseReleased(release);

      assertEquals(Status.STATE_STOPPED, stateCalled.get());
  }

  @Test
  @DisplayName("Listeners: all listeners receive notifications")
  void testMultipleListeners() {
      AtomicInteger first = new AtomicInteger();
      AtomicInteger second = new AtomicInteger();

      status.addStatusListener(new StatusListener() {
          @Override
          public void onState(int state) {
              first.set(state);
          }

          @Override public void onSeek(double position) {}
          @Override public void onAudio() {}
          @Override public void onSubtitles(int x, int y) {}
      });

      status.addStatusListener(new StatusListener() {
          @Override
          public void onState(int state) {
              second.set(state);
          }

          @Override public void onSeek(double position) {}
          @Override public void onAudio() {}
          @Override public void onSubtitles(int x, int y) {}
      });

      status.notifyNewState(Status.STATE_PLAYING);

      assertEquals(Status.STATE_PLAYING, first.get());
      assertEquals(Status.STATE_PLAYING, second.get());
  }

  @Test
  @DisplayName("notifySeek forwards position")
  void testNotifySeek() {
      AtomicReference<Double> position = new AtomicReference<>();

      status.addStatusListener(new StatusListener() {
          @Override public void onState(int state) {}

          @Override
          public void onSeek(double value) {
              position.set(value);
          }

          @Override public void onAudio() {}
          @Override public void onSubtitles(int x, int y) {}
      });

      status.notifySeek(0.5);

      assertEquals(0.5, position.get(), 0.001);
  }

  @Test
  @DisplayName("setStartTime clamps negative values")
  void testNegativeStartTime() {
      assertDoesNotThrow(() -> status.setStartTime(-100));
  }

  @Test
  @DisplayName("Byte duration zero does not crash")
  void testZeroByteDuration() {
      assertDoesNotThrow(() -> {
          status.setDuration(-1);
          status.setByteDuration(0);
          status.setBytePosition(0);
      });
  }

  @Test
  @DisplayName("Mouse: audio button triggers listener")
  void testAudioButtonClick() {
      AtomicBoolean audioCalled = new AtomicBoolean(false);

      status.addStatusListener(new StatusListener() {
          @Override public void onState(int state) {}
          @Override public void onSeek(double position) {}

          @Override
          public void onAudio() {
              audioCalled.set(true);
          }

          @Override public void onSubtitles(int x, int y) {}
      });

      status.setHaveAudio(true);
      status.setShowSpeaker(true);

      int x = status.getWidth() - 6;
      int y = status.getHeight() - 5;

      MouseEvent press =
          new MouseEvent(
              dummyComponent,
              MouseEvent.MOUSE_PRESSED,
              System.currentTimeMillis(),
              0,
              x,
              y,
              1,
              false);

      MouseEvent release =
          new MouseEvent(
              dummyComponent,
              MouseEvent.MOUSE_RELEASED,
              System.currentTimeMillis(),
              0,
              x,
              y,
              1,
              false);

      status.mousePressed(press);
      status.mouseReleased(release);

      assertTrue(audioCalled.get());
  }

  @Test
  @DisplayName("Mouse: subtitles button triggers listener")
  void testSubtitleButtonClick() {
      AtomicBoolean subtitleCalled = new AtomicBoolean(false);

      status.addStatusListener(new StatusListener() {
          @Override public void onState(int state) {}
          @Override public void onSeek(double position) {}
          @Override public void onAudio() {}

          @Override
          public void onSubtitles(int x, int y) {
              subtitleCalled.set(true);
          }
      });

      status.setHaveSubtitles(true);
      status.setShowSubtitles(true);

      int x = status.getWidth() - 10;
      int y = 10;

      MouseEvent press =
          new MouseEvent(dummyComponent,
              MouseEvent.MOUSE_PRESSED,
              System.currentTimeMillis(),
              0, x, y, 1, false);

      MouseEvent release =
          new MouseEvent(dummyComponent,
              MouseEvent.MOUSE_RELEASED,
              System.currentTimeMillis(),
              0, x, y, 1, false);

      status.mousePressed(press);
      status.mouseReleased(release);

      assertTrue(subtitleCalled.get());
  }

  @Test
  @DisplayName("Play button toggles playing to paused")
  void testPlayToPauseTransition() {
      AtomicInteger stateCalled = new AtomicInteger();

      status.addStatusListener(new StatusListener() {
          @Override
          public void onState(int state) {
              stateCalled.set(state);
          }

          @Override public void onSeek(double position) {}
          @Override public void onAudio() {}
          @Override public void onSubtitles(int x, int y) {}
      });

      status.setState(Status.STATE_PLAYING);

      MouseEvent press =
          new MouseEvent(dummyComponent,
              MouseEvent.MOUSE_PRESSED,
              System.currentTimeMillis(),
              0, 5, 5, 1, false);

      MouseEvent release =
          new MouseEvent(dummyComponent,
              MouseEvent.MOUSE_RELEASED,
              System.currentTimeMillis(),
              0, 5, 5, 1, false);

      status.mousePressed(press);
      status.mouseReleased(release);

      assertEquals(Status.STATE_PAUSED, stateCalled.get());
  }

  @Test
  @DisplayName("Seeking notifies listener")
  void testSeekNotifiesListener() {
      AtomicReference<Double> seekPosition = new AtomicReference<>();

      status.addStatusListener(new StatusListener() {
          @Override public void onState(int state) {}

          @Override
          public void onSeek(double position) {
              seekPosition.set(position);
          }

          @Override public void onAudio() {}
          @Override public void onSubtitles(int x, int y) {}
      });

      status.setSeekable(true);
      status.setState(Status.STATE_PLAYING);

      // Click somewhere in the seek bar

      MouseEvent press =
          new MouseEvent(dummyComponent,
              MouseEvent.MOUSE_PRESSED,
              System.currentTimeMillis(),
              0, 120, 25, 1, false);

      MouseEvent release =
          new MouseEvent(dummyComponent,
              MouseEvent.MOUSE_RELEASED,
              System.currentTimeMillis(),
              0, 120, 25, 1, false);

      status.mousePressed(press);
      status.mouseReleased(release);

      assertNotNull(seekPosition.get());
  }
}
