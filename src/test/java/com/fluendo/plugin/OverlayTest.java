package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.fluendo.jst.Buffer;
import com.fluendo.jst.Element;
import com.fluendo.jst.Event;
import com.fluendo.jst.Pad;
import java.awt.Button;
import java.awt.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OverlayTest {

  private Overlay overlay;
  private Component dummyComponent;

  @BeforeEach
  void setUp() {
    overlay = new Overlay();
    dummyComponent = new Button("Overlay Parent");
  }

  @Test
  @DisplayName("Metadata: Factory name verification")
  void testGetFactoryName() {
    assertEquals("overlay", overlay.getFactoryName());
  }

  @Test
  @DisplayName("Properties: Setting and getting component property safely")
  void testPropertyHandling() {
    assertNull(overlay.getProperty("component"));

    assertTrue(overlay.setProperty("component", dummyComponent));
    assertEquals(dummyComponent, overlay.getProperty("component"));

    // Fallback for unknown properties
    assertNull(overlay.getProperty("unknown"));
    assertFalse(overlay.setProperty("unknown", "value"));
  }

  @Test
  @DisplayName("Pads & Event Routing: Sink and source pad event intercommunication")
  void testPadEventRouting() {
    Pad sinkPad = overlay.getPad("videosink");
    Pad srcPad = overlay.getPad("videosrc");
    assertNotNull(sinkPad);
    assertNotNull(srcPad);

    Event eos = Event.newEOS();
    Event flushStart = Event.newFlushStart();

    // Events on sink should push to source, and vice versa
    assertDoesNotThrow(() -> sinkPad.pushEvent(eos));
    assertDoesNotThrow(() -> srcPad.pushEvent(flushStart));
  }

  @Test
  @DisplayName("Chain Function & Default Overlay Passthrough")
  void testChainPassthrough() {
    Pad sinkPad = overlay.getPad("videosink");
    assertNotNull(sinkPad);

    Buffer buffer = new Buffer();
    buffer.data = new byte[] {1, 2, 3, 4};

    // Without a downstream peer, push returns -1, but the base overlay passthrough executes safely
    assertEquals(-1, sinkPad.push(buffer));
  }

  @Test
  @DisplayName("State transition should execute without throwing")
  void testStateTransitionFallback() {
      assertDoesNotThrow(
          () -> overlay.changeState(Element.STOP_PAUSE));
  }

  @Test
  void testPadsExist() {
      assertNotNull(overlay.getPad("videosink"));
      assertNotNull(overlay.getPad("videosrc"));
  }
}
