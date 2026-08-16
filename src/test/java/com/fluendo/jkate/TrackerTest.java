package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Dimension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TrackerTest {

  private Tracker tracker;
  private Event event;

  @BeforeEach
  void setUp() {
    event = new Event();
    tracker = new Tracker(event);
  }

  @Test
  @DisplayName("Default Constructor: Initializes with safe defaults")
  void testDefaultConstructor() {
    Tracker emptyTracker = new Tracker();
    assertNull(emptyTracker.ev);
    assertNull(emptyTracker.getWindow());
    assertNull(emptyTracker.getFrame());
  }

  @Test
  @DisplayName("Update: Fails gracefully when event is null")
  void testUpdateNullEvent() {
    Tracker nullEventTracker = new Tracker(null);
    boolean result = nullEventTracker.update(0.0, new Dimension(800, 600), new Dimension(800, 600));
    assertFalse(result);
  }

  @Test
  @DisplayName("Update: Handles pixel metrics correctly without frame")
  void testUpdatePixelMetrics() {
    Region region = new Region();
    region.metric = KateSpaceMetric.KATE_METRIC_PIXELS;
    region.x = 10;
    region.y = 20;
    region.w = 100;
    region.h = 200;
    event.kr = region;

    boolean result = tracker.update(0.0, new Dimension(800, 600), null);
    assertTrue(result);
    assertTrue(tracker.has[Tracker.HAS_REGION]);
    assertEquals(10.0f, tracker.regionX);
    assertEquals(20.0f, tracker.regionY);
    assertEquals(100.0f, tracker.regionW);
    assertEquals(200.0f, tracker.regionH);
  }

  @Test
  @DisplayName("Update: Calculates percentage metrics relative to frame")
  void testUpdatePercentageMetrics() {
    Region region = new Region();
    region.metric = KateSpaceMetric.KATE_METRIC_PERCENTAGE;
    region.x = 10; // 10%
    region.y = 20; // 20%
    region.w = 50; // 50%
    region.h = 50; // 50%
    event.kr = region;

    Dimension frame = new Dimension(1000, 500);
    boolean result = tracker.update(0.0, new Dimension(1000, 500), frame);

    assertTrue(result);
    assertEquals(100.0f, tracker.regionX); // 10% of 1000
    assertEquals(100.0f, tracker.regionY); // 20% of 500
    assertEquals(500.0f, tracker.regionW); // 50% of 1000
    assertEquals(250.0f, tracker.regionH); // 50% of 500
  }

  @Test
  @DisplayName("Update: Returns false on invalid metric type")
  void testUpdateInvalidMetric() {
    Region region = new Region();
    region.metric = null;
    event.kr = region;

    boolean result = tracker.update(0.0, new Dimension(800, 600), new Dimension(800, 600));
    assertFalse(result);
  }

  @Test
  @DisplayName("ToString: Returns expected debug representation")
  void testToString() {
    String representation = tracker.toString();
    assertNotNull(representation);
    assertTrue(representation.contains("Tracker{"));
  }
}
