package com.fluendo.player;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DurationScannerTest {

  private DurationScanner scanner;

  @BeforeEach
  void setUp() {
    scanner = new DurationScanner();
  }

  @Test
  @DisplayName("TimingInfo: Default constructor values check")
  void testTimingInfoDefaultConstructor() {
    DurationScanner.TimingInfo info = new DurationScanner.TimingInfo();
    assertEquals(-1.0f, info.startTime(), 0.001f);
    assertEquals(-1.0f, info.duration(), 0.001f);
  }

  @Test
  @DisplayName("TimingInfo: Parameterized constructor/record accessor check")
  void testTimingInfoParameterizedConstructor() {
    DurationScanner.TimingInfo info = new DurationScanner.TimingInfo(2.5f, 120.0f);
    assertEquals(2.5f, info.startTime(), 0.001f);
    assertEquals(120.0f, info.duration(), 0.001f);
  }

  @Test
  @DisplayName(
      "Edge Case: scanBuffer with empty/zero-length byte array handled via exception expectation")
  void testScanBufferEmpty() {
    byte[] emptyBuffer = new byte[0];
    assertThrows(NullPointerException.class, () -> scanner.scanBuffer(emptyBuffer, 0));
  }

  @Test
  @DisplayName("Edge Case: scanBuffer with uninitialized or garbage Ogg bytes")
  void testScanBufferGarbageData() {
    byte[] garbage = new byte[] {0x01, 0x02, 0x03, 0x04, 0x05};
    assertDoesNotThrow(
        () -> {
          DurationScanner.TimingInfo info = scanner.scanBuffer(garbage, garbage.length);
          assertNotNull(info);
          assertTrue(
              info.startTime() <= 0.0f, "Start time should be negative or zero for invalid stream");
        });
  }

  @Test
  @DisplayName("Edge Case: scanUrl with unreachable or invalid URL/Protocol")
  void testScanInvalidUrl() throws Exception {
    URL badUrl = URI.create("http://localhost:1/nonexistent-media-stream").toURL();
    DurationScanner.TimingInfo info = scanner.scanUrl(badUrl, null, null);
    assertNotNull(info);
    assertEquals(-1.0f, info.startTime(), 0.001f);
    assertEquals(-1.0f, info.duration(), 0.001f);
  }

  @Test
  @DisplayName("TimingInfo: Records with same values are equal")
  void testTimingInfoEquality() {
      DurationScanner.TimingInfo first =
              new DurationScanner.TimingInfo(1.0f, 2.0f);

      DurationScanner.TimingInfo second =
              new DurationScanner.TimingInfo(1.0f, 2.0f);

      assertEquals(first, second);
      assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  @DisplayName("TimingInfo: toString contains values")
  void testTimingInfoToString() {
      DurationScanner.TimingInfo info =
              new DurationScanner.TimingInfo(3.5f, 99.0f);

      String text = info.toString();

      assertTrue(text.contains("3.5"));
      assertTrue(text.contains("99.0"));
  }

  @Test
  @DisplayName("scanBuffer: Null buffer throws NullPointerException")
  void testScanBufferNullBuffer() {
      assertThrows(
              NullPointerException.class,
              () -> scanner.scanBuffer(null, 0));
  }

  @Test
  @DisplayName("scanBuffer: Single byte input")
  void testScanBufferSingleByte() {
      byte[] buffer = new byte[] {0x00};

      assertDoesNotThrow(() -> {
          DurationScanner.TimingInfo info =
                  scanner.scanBuffer(buffer, 1);

          assertNotNull(info);
      });
  }

  @Test
  @DisplayName("scanBuffer: Repeated garbage input")
  void testScanBufferRepeatedGarbage() {
      byte[] garbage = new byte[] {
              0x01, 0x02, 0x03, 0x04
      };

      for (int i = 0; i < 100; i++) {
          assertDoesNotThrow(() ->
                  scanner.scanBuffer(garbage, garbage.length));
      }
  }

  @Test
  @DisplayName("scanBuffer: Large garbage block")
  void testScanBufferLargeGarbageBlock() {
      byte[] garbage = new byte[8192];

      for (int i = 0; i < garbage.length; i++) {
          garbage[i] = (byte) (i & 0xFF);
      }

      assertDoesNotThrow(() -> {
          DurationScanner.TimingInfo info =
                  scanner.scanBuffer(garbage, garbage.length);

          assertNotNull(info);
      });
  }

  @Test
  @DisplayName("scanUrl: Null URL")
  void testScanUrlNullUrl() {
      assertThrows(
              NullPointerException.class,
              () -> scanner.scanUrl(null, null, null));
  }

  @Test
  @DisplayName("scanUrl: Invalid URL with credentials")
  void testScanUrlWithCredentials() throws Exception {
      URL url =
              URI.create("http://localhost:1/test")
                      .toURL();

      DurationScanner.TimingInfo info =
              scanner.scanUrl(url, "user", "password");

      assertNotNull(info);
      assertEquals(-1.0f, info.startTime(), 0.001f);
      assertEquals(-1.0f, info.duration(), 0.001f);
  }

  @Test
  @DisplayName("scanUrl: Empty credentials")
  void testScanUrlWithEmptyCredentials() throws Exception {
      URL url =
              URI.create("http://localhost:1/test")
                      .toURL();

      DurationScanner.TimingInfo info =
              scanner.scanUrl(url, "", "");

      assertNotNull(info);
  }

  @Test
  @DisplayName("scanUrl: Multiple failed scans")
  void testMultipleFailedUrlScans() throws Exception {
      URL url =
              URI.create("http://localhost:1/test")
                      .toURL();

      for (int i = 0; i < 10; i++) {
          assertDoesNotThrow(() ->
                  scanner.scanUrl(url, null, null));
      }
  }

  @Test
  @DisplayName("scanBuffer: Multiple sequential calls preserve stability")
  void testMultipleSequentialBufferScans() {
      byte[] first = {0x01, 0x02};
      byte[] second = {0x03, 0x04};
      byte[] third = {0x05, 0x06};

      assertDoesNotThrow(() -> {
          scanner.scanBuffer(first, first.length);
          scanner.scanBuffer(second, second.length);
          scanner.scanBuffer(third, third.length);
      });
  }

  @Test
  @DisplayName("scanBuffer: bytesRead less than array length")
  void testScanBufferPartialRead() {
      byte[] buffer = new byte[128];

      DurationScanner.TimingInfo info =
              scanner.scanBuffer(buffer, 10);

      assertNotNull(info);
  }

  @Test
  @DisplayName("scanUrl: test-audio.ogg")
  void testScanAudioOgg() throws Exception {
      URL url = getClass().getResource("/media/test-audio.ogg");

      assertNotNull(url);

      DurationScanner.TimingInfo info =
              scanner.scanUrl(url, null, null);

      assertNotNull(info);
      assertTrue(info.duration() > 0.0f);
  }

  @Test
  @DisplayName("scanUrl: test-audio.oga")
  void testScanAudioOga() throws Exception {
      URL url = getClass().getResource("/media/test-audio.oga");

      assertNotNull(url);

      DurationScanner.TimingInfo info =
              scanner.scanUrl(url, null, null);

      assertNotNull(info);
      assertTrue(info.duration() > 0.0f);
  }

  @Test
  @DisplayName("scanUrl: test-silence.ogg")
  void testScanSilenceOgg() throws Exception {
      URL url = getClass().getResource("/media/test-silence.ogg");

      assertNotNull(url);

      DurationScanner.TimingInfo info =
              scanner.scanUrl(url, null, null);

      assertNotNull(info);
      assertTrue(info.duration() > 0.0f);
  }

  @Test
  @DisplayName("scanUrl: test-silence.oga")
  void testScanSilenceOga() throws Exception {
      URL url = getClass().getResource("/media/test-silence.oga");

      assertNotNull(url);

      DurationScanner.TimingInfo info =
              scanner.scanUrl(url, null, null);

      assertNotNull(info);
      assertTrue(info.duration() > 0.0f);
  }

  @Test
  void testScanVideoOnly() throws Exception {
      URL url =
              getClass().getResource("/media/test-video-only.ogv");

      assertNotNull(url);

      DurationScanner.TimingInfo info =
              scanner.scanUrl(url, null, null);

      assertNotNull(info);

      System.out.println(info);
  }

  @Test
  @DisplayName("scanUrl: test-video-silent.ogv")
  void testScanVideoSilent() throws Exception {
      URL url = getClass().getResource("/media/test-video-silent.ogv");

      assertNotNull(url);

      DurationScanner.TimingInfo info =
              scanner.scanUrl(url, null, null);

      assertNotNull(info);
      assertTrue(info.duration() > 0.0f);
  }

  @Test
  void testScanVideoAudio() throws Exception {
      URL url =
              getClass().getResource("/media/test-video-audio.ogv");

      assertNotNull(url);

      DurationScanner.TimingInfo info =
              scanner.scanUrl(url, null, null);

      assertNotNull(info);

      System.out.println(info);
  }

  @Test
  @DisplayName("All media test resources are available")
  void testMediaResourcesExist() {
      assertNotNull(getClass().getResource("/media/test-audio.ogg"));
      assertNotNull(getClass().getResource("/media/test-audio.oga"));
      assertNotNull(getClass().getResource("/media/test-silence.ogg"));
      assertNotNull(getClass().getResource("/media/test-silence.oga"));
      assertNotNull(getClass().getResource("/media/test-video-only.ogv"));
      assertNotNull(getClass().getResource("/media/test-video-silent.ogv"));
      assertNotNull(getClass().getResource("/media/test-video-audio.ogv"));
  }
}
