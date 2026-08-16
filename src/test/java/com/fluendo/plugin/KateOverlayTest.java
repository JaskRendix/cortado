package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.fluendo.jst.Buffer;
import com.fluendo.jst.Event;
import com.fluendo.jst.Pad;
import java.awt.Button;
import java.awt.Component;
import java.awt.image.MemoryImageSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KateOverlayTest {

  private KateOverlay kateOverlay;
  private Component dummyComponent;

  @BeforeEach
  void setUp() {
    kateOverlay = new KateOverlay();
    dummyComponent = new Button("Overlay Canvas");
    kateOverlay.setProperty("component", dummyComponent);
  }

  @Test
  @DisplayName("Metadata: Factory name verification")
  void testGetFactoryName() {
    assertEquals("kateoverlay", kateOverlay.getFactoryName());
  }

  @Test
  @DisplayName("Flush Events: Triggering flush events via Kate sink pad")
  void testKateSinkPadFlushEvents() {
    Pad kateSinkPad = kateOverlay.getPad("katesink");
    assertNotNull(kateSinkPad);

    Event flushStartEvent = Event.newFlushStart();
    assertDoesNotThrow(() -> kateSinkPad.sendEvent(flushStartEvent));

    Event flushStopEvent = Event.newFlushStop();
    assertDoesNotThrow(() -> kateSinkPad.sendEvent(flushStopEvent));
  }

  @Test
  @DisplayName("Chain Function: Adding Kate event through sink pad handling")
  void testKateSinkPadChainFunc() {
    Pad kateSinkPad = kateOverlay.getPad("katesink");
    assertNotNull(kateSinkPad);

    Buffer kateBuffer = new Buffer();
    // Instantiate using the existing constructor with null Info
    com.fluendo.jkate.Event kateEvent = new com.fluendo.jkate.Event(null);
    kateEvent.text = "Subtitles test".getBytes();
    kateBuffer.object = kateEvent;

    assertDoesNotThrow(() -> kateOverlay.addKateEvent(kateEvent));
  }

  @Test
  @DisplayName("Overlay & Producer: Processing buffer with ImageProducer payload")
  void testOverlayWithImageProducerBuffer() {
    Buffer buffer = new Buffer();
    int[] pixels = new int[20 * 20];
    buffer.object = new MemoryImageSource(20, 20, pixels, 0, 20);
    buffer.timestamp = 1000L;

    assertDoesNotThrow(() -> kateOverlay.overlay(buffer));
    assertNotNull(buffer.object);

    java.awt.image.ImageProducer producer = (java.awt.image.ImageProducer) buffer.object;
    assertDoesNotThrow(() -> producer.startProduction(new TestImageConsumer()));
  }

  @Test
  @DisplayName("Overlay & Producer: Duplicate buffer handling with clean/dirty states")
  void testOverlayDuplicateBufferScenarios() {
    Buffer buffer = new Buffer();
    int[] pixels = new int[20 * 20];
    buffer.object = new MemoryImageSource(20, 20, pixels, 0, 20);
    buffer.timestamp = 1500L;
    buffer.duplicate = true;

    assertDoesNotThrow(() -> kateOverlay.overlay(buffer));
    java.awt.image.ImageProducer producer = (java.awt.image.ImageProducer) buffer.object;
    assertDoesNotThrow(() -> producer.startProduction(new TestImageConsumer()));
  }

  @Test
  @DisplayName("Overlay: Handling unsupported/unknown buffer object payloads")
  void testOverlayWithUnknownBufferObject() {
    Buffer buffer = new Buffer();
    buffer.object = "Invalid Object Payload";
    buffer.timestamp = 2000L;

    assertDoesNotThrow(() -> kateOverlay.overlay(buffer));

    java.awt.image.ImageProducer producer = (java.awt.image.ImageProducer) buffer.object;
    assertDoesNotThrow(() -> producer.startProduction(new TestImageConsumer()));
  }

  private static class TestImageConsumer implements java.awt.image.ImageConsumer {
    @Override
    public void imageComplete(int status) {}

    @Override
    public void setColorModel(java.awt.image.ColorModel model) {}

    @Override
    public void setDimensions(int width, int height) {}

    @Override
    public void setHints(int hintflags) {}

    @Override
    public void setProperties(java.util.Hashtable<?, ?> props) {}

    @Override
    public void setPixels(
        int x,
        int y,
        int w,
        int h,
        java.awt.image.ColorModel model,
        byte[] pixels,
        int off,
        int scansize) {}

    @Override
    public void setPixels(
        int x,
        int y,
        int w,
        int h,
        java.awt.image.ColorModel model,
        int[] pixels,
        int off,
        int scansize) {}
  }
}
