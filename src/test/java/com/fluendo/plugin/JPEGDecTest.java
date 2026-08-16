package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.fluendo.jst.Buffer;
import com.fluendo.jst.Event;
import com.fluendo.jst.Pad;
import java.awt.Button;
import java.awt.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JPEGDecTest {

  private JPEGDec jpegDec;

  @BeforeEach
  void setUp() {
    jpegDec = new JPEGDec();
  }

  @Test
  @DisplayName("Metadata & TypeFind Verification")
  void testMetadataAndTypeFind() {
    assertEquals("jpegdec", jpegDec.getFactoryName());
    assertEquals("image/jpeg", jpegDec.getMime());

    byte[] dummyData = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
    assertEquals(-1, jpegDec.typeFind(dummyData, 0, 4));
  }

  @Test
  @DisplayName("Properties: Setting and getting component and handling fallback properties")
  void testPropertyHandling() {
    assertNull(jpegDec.getProperty("component"));

    Component button = new Button("Test Button");
    assertTrue(jpegDec.setProperty("component", button));
    assertEquals(button, jpegDec.getProperty("component"));

    // Unknown property handling
    assertFalse(jpegDec.setProperty("unknown", "value"));
    assertNull(jpegDec.getProperty("unknown"));
  }

  @Test
  @DisplayName("Sink Pad Events: Handling flush start/stop, EOS, and general events safely")
  void testSinkPadEvents() {
    Pad sinkPad = jpegDec.getPad("sink");
    assertNotNull(sinkPad);

    Event flushStartEvent = Event.newFlushStart();
    Event flushStopEvent = Event.newFlushStop();
    Event eosEvent = Event.newEOS();

    assertDoesNotThrow(() -> sinkPad.pushEvent(flushStartEvent));
    assertDoesNotThrow(() -> sinkPad.pushEvent(flushStopEvent));
    assertDoesNotThrow(() -> sinkPad.pushEvent(eosEvent));
  }

  @Test
  @DisplayName("Source Pad Events: Pushing events through source pad safely")
  void testSourcePadEvents() {
    Pad srcPad = jpegDec.getPad("src");
    assertNotNull(srcPad);

    Event eosEvent = Event.newEOS();
    assertDoesNotThrow(() -> srcPad.pushEvent(eosEvent));
  }

  @Test
  @DisplayName("Chain Function: Handling malformed or empty image buffers gracefully")
  void testChainWithInvalidBuffer() {
    Pad sinkPad = jpegDec.getPad("sink");
    assertNotNull(sinkPad);

    // Set component first so mediaTracker is initialized to avoid NPE during image tracking
    Component button = new Button("Tracker Component");
    jpegDec.setProperty("component", button);

    Buffer buffer = new Buffer();
    buffer.data = new byte[] {1, 2, 3, 4}; // Invalid JPEG byte stream
    buffer.offset = 0;
    buffer.length = 4;

    // Even with invalid image data, it should handle failure gracefully and return OK or error
    // without crashing
    assertDoesNotThrow(() -> sinkPad.push(buffer));
  }

  @Test
  @DisplayName("State Transition: Resetting dimensions on state transition")
  void testStateTransition() {
    // Triggering state change transition safely using standard integer transition code
    assertDoesNotThrow(() -> jpegDec.changeState(1));
  }
}
