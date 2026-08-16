package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.fluendo.jst.Buffer;
import com.fluendo.jst.Event;
import com.fluendo.jst.Pad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SelectorTest {

  private Selector selector;

  @BeforeEach
  void setUp() {
    selector = new Selector();
  }

  @Test
  @DisplayName("Metadata: Factory name verification")
  void testGetFactoryName() {
    assertEquals("selector", selector.getFactoryName());
  }

  @Test
  @DisplayName("Pad Request: Creating sink pads dynamically and managing selection")
  void testRequestSinkPadAndProperties() {
    assertEquals(-1, selector.getProperty("selected"));

    Pad sinkPad0 = selector.requestSinkPad(null);
    Pad sinkPad1 = selector.requestSinkPad(null);
    assertNotNull(sinkPad0);
    assertNotNull(sinkPad1);

    assertTrue(selector.setProperty("selected", 0));
    assertEquals(0, selector.getProperty("selected"));

    assertTrue(selector.setProperty("selected", 1));
    assertEquals(1, selector.getProperty("selected"));

    assertTrue(selector.setProperty("selected", 99));
    assertEquals(-1, selector.getProperty("selected"));

    assertTrue(selector.setProperty("selected", -1));
    assertEquals(-1, selector.getProperty("selected"));
  }

  @Test
  @DisplayName("Chain Function & Event Routing: Unselected vs Selected pad behavior")
  void testChainAndEventRouting() {
    Pad sinkPad0 = selector.requestSinkPad(null);
    selector.requestSinkPad(null);

    Buffer buffer = new Buffer();
    buffer.data = new byte[] {1, 2, 3};

    // Unselected pad push returns -1
    assertEquals(-1, sinkPad0.push(buffer));

    // Select pad 0
    selector.setProperty("selected", 0);

    assertDoesNotThrow(() -> sinkPad0.pushEvent(Event.newEOS()));

    // Pushing when selected attempts to push to srcPad; without a downstream sink, it returns -1
    assertEquals(-1, sinkPad0.push(buffer));
  }

  @Test
  @DisplayName("Source Pad Events: Pushing broadcast events to all registered sinks")
  void testSourcePadBroadcastEvents() {
    selector.requestSinkPad(null);
    selector.requestSinkPad(null);

    Pad srcPad = selector.getPad("src");
    assertNotNull(srcPad);

    assertDoesNotThrow(() -> srcPad.pushEvent(Event.newFlushStart()));
    assertDoesNotThrow(() -> srcPad.pushEvent(Event.newFlushStop()));
    assertDoesNotThrow(() -> srcPad.pushEvent(Event.newEOS()));
  }

  @Test
  @DisplayName("Properties: Unknown property fallback behavior")
  void testUnknownPropertyHandling() {
    assertNull(selector.getProperty("non_existent"));
    assertFalse(selector.setProperty("non_existent", "value"));
  }
}
