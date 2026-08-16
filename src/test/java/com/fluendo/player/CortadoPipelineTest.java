package com.fluendo.player;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CortadoPipelineTest {

  private CortadoPipeline pipeline;

  @BeforeEach
  void setUp() {
    pipeline = new CortadoPipeline();
  }

  @Test
  @DisplayName("Edge Case: Null URL handling")
  void testNullUrl() {
    pipeline.setUrl(null);
    assertNull(pipeline.getUrl(), "URL should remain null when set to null");
  }

  @Test
  @DisplayName("Edge Case: Malformed or Empty URL string")
  void testEmptyUrl() {
    pipeline.setUrl("");
    assertEquals("", pipeline.getUrl(), "URL should accept empty string");
  }

  @Test
  @DisplayName("Edge Case: Invalid/Negative buffer configurations")
  void testInvalidBufferSizes() {
    pipeline.setBufferSize(-999);
    pipeline.setBufferLow(-10);
    pipeline.setBufferHigh(-50);

    assertEquals(-999, pipeline.getBufferSize());
    assertEquals(-10, pipeline.getBufferLow());
    assertEquals(-50, pipeline.getBufferHigh());
  }

  @Test
  @DisplayName("Edge Case: Extreme buffer configurations")
  void testExtremeBufferSizes() {
    pipeline.setBufferSize(Integer.MAX_VALUE);
    pipeline.setBufferLow(0);
    pipeline.setBufferHigh(100);

    assertEquals(Integer.MAX_VALUE, pipeline.getBufferSize());
    assertEquals(0, pipeline.getBufferLow());
    assertEquals(100, pipeline.getBufferHigh());
  }

  @Test
  @DisplayName("Edge Case: Document Base configuration")
  void testDocumentBase() throws MalformedURLException {
    URL testUrl = new URL("http://localhost:8080/");
    pipeline.setDocumentBase(testUrl);
    assertEquals(testUrl, pipeline.getDocumentBase());
  }

  @Test
  @DisplayName("Edge Case: Null Component resize protection")
  void testResizeWithNullDimension() {
    // Should not throw a NullPointerException even if internal component/videosink is null
    assertDoesNotThrow(() -> pipeline.resize(null));
  }

  @Test
  @DisplayName("Edge Case: Out-of-bounds Kate stream index query")
  void testKateStreamOutOfBounds() {
    // No streams added yet, queries should gracefully return empty or default values
    assertEquals(-1, pipeline.getEnabledKateIndex());
    assertEquals("", pipeline.getKateStreamCategory(-1));
    assertEquals("", pipeline.getKateStreamCategory(999));
    assertEquals("", pipeline.getKateStreamLanguage(-1));
    assertEquals("", pipeline.getKateStreamLanguage(999));
  }

  @Test
  @DisplayName("Edge Case: Toggling Audio and Video states")
  void testAudioVideoToggles() {
    pipeline.enableAudio(false);
    assertFalse(pipeline.isAudioEnabled());

    pipeline.enableAudio(true);
    assertTrue(pipeline.isAudioEnabled());

    pipeline.enableVideo(false);
    assertFalse(pipeline.isVideoEnabled());

    pipeline.enableVideo(true);
    assertTrue(pipeline.isVideoEnabled());
  }

  @Test
  @DisplayName("Edge Case: Component assignment")
  void testComponentAssignment() {
    assertNull(pipeline.getComponent());
    // Passing a basic lightweight component for mock validation
    Component mockComp = new Component() {};
    pipeline.setComponent(mockComp);
    assertEquals(mockComp, pipeline.getComponent());
  }
}
