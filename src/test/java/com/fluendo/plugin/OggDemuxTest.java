package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.fluendo.jst.*;
import java.lang.reflect.Field;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OggDemuxTest {

  private OggDemux oggDemux;

  @BeforeEach
  public void setUp() {
    oggDemux = new OggDemux();
  }

  @Test
  public void testFactoryAndMime() {
    assertEquals("oggdemux", oggDemux.getFactoryName());
    assertEquals("application/ogg", oggDemux.getMime());
  }

  @Test
  public void testTypeFindValidSignature() {
    byte[] validHeader = new byte[] {0x4f, 0x67, 0x67, 0x53, 0x00, 0x02, 0x00};
    int confidence = oggDemux.typeFind(validHeader, 0, validHeader.length);
    assertEquals(10, confidence, "Valid OggS signature should return confidence 10");
  }

  @Test
  public void testTypeFindInvalidSignature() {
    byte[] invalidHeader = new byte[] {0x00, 0x01, 0x02, 0x03, 0x04};
    int confidence = oggDemux.typeFind(invalidHeader, 0, invalidHeader.length);
    assertEquals(-1, confidence, "Invalid signature should return -1");
  }

  @Test
  public void testTypeFindWithOffset() {
    byte[] dataWithOffset = new byte[] {0x00, 0x00, 0x4f, 0x67, 0x67, 0x53};
    int confidence = oggDemux.typeFind(dataWithOffset, 2, 4);
    assertEquals(10, confidence, "Valid OggS signature starting at offset 2 should be detected");
  }

  @Test
  public void testFlushEventsExecution() {
    Pad sinkPad = oggDemux.getPad("sink");
    assertNotNull(sinkPad);

    assertDoesNotThrow(() -> {
      sinkPad.pushEvent(Event.newFlushStart());
      sinkPad.pushEvent(Event.newFlushStop());
    });
  }

  @Test
  public void testEosEventExecution() {
    Pad sinkPad = oggDemux.getPad("sink");
    assertNotNull(sinkPad);

    assertDoesNotThrow(
        () -> sinkPad.pushEvent(Event.newEOS()));
  }

  @Test
  public void testDemuxAudioResourceDetailed() throws Exception {
    InputStream stream = getClass().getResourceAsStream("/media/test-audio.ogg");
    assertNotNull(stream, "Test resource /media/test-audio.ogg must be present");

    byte[] data = stream.readAllBytes();
    stream.close();

    assertTrue(data.length > 0, "Ogg audio file should contain payload data");

    int confidence = oggDemux.typeFind(data, 0, Math.min(data.length, 32));
    assertEquals(10, confidence, "Real Ogg audio asset should match Ogg typefind signature");

    Pad sinkPad = oggDemux.getPad("sink");
    assertNotNull(sinkPad, "OggDemux must expose a sink pad");
  }

  @Test
  public void testOggDemuxPageProcessing() throws Exception {
    Pad sinkPad = oggDemux.getPad("sink");
    assertNotNull(sinkPad);

    byte[] mockOggPage =
        new byte[] {
          0x4f,
          0x67,
          0x67,
          0x53, // "OggS"
          0x00, // stream structure version
          0x02, // header_type (bos: beginning of stream)
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00, // granulepos
          (byte) 0xef,
          (byte) 0xbe,
          (byte) 0xad,
          (byte) 0xde, // serialno (0xdeadbeef)
          0x00,
          0x00,
          0x00,
          0x00, // page sequence number
          0x00,
          0x00,
          0x00,
          0x00, // checksum placeholder
          0x01, // page_segments
          0x05 // segment lengths (1 segment of 5 bytes)
        };

    Buffer buf = Buffer.create();
    buf.copyData(mockOggPage, 0, mockOggPage.length);

    assertNotNull(buf.data, "Buffer data should be properly allocated and copied");
    assertEquals(mockOggPage.length, buf.length, "Buffer length should match mock page size");
  }

  @Test
  public void testAllMediaResourcesExist() throws Exception {
    String[] mediaFiles = {
      "/media/test-audio.ogg",
      "/media/test-audio.oga",
      "/media/test-silence.ogg",
      "/media/test-silence.oga",
      "/media/test-video-audio.ogv",
      "/media/test-video-only.ogv",
      "/media/test-video-silent.ogv"
    };

    for (String filePath : mediaFiles) {
      InputStream stream = getClass().getResourceAsStream(filePath);
      assertNotNull(stream, "Test resource " + filePath + " must be present in classpath");

      byte[] data = stream.readAllBytes();
      stream.close();

      assertTrue(data.length > 100, "Media file " + filePath + " should contain data streams");
      assertEquals(
          10,
          oggDemux.typeFind(data, 0, Math.min(data.length, 32)),
          "Asset " + filePath + " must match Ogg signature");
    }
  }

  @Test
  void testConstructorState() throws Exception {
      Field oyField =
          OggDemux.class.getDeclaredField("oy");

      Field ogField =
          OggDemux.class.getDeclaredField("og");

      Field opField =
          OggDemux.class.getDeclaredField("op");

      Field chainField =
          OggDemux.class.getDeclaredField("chain");

      oyField.setAccessible(true);
      ogField.setAccessible(true);
      opField.setAccessible(true);
      chainField.setAccessible(true);

      assertNotNull(oyField.get(oggDemux));
      assertNotNull(ogField.get(oggDemux));
      assertNotNull(opField.get(oggDemux));

      assertNull(chainField.get(oggDemux));
  }

  @Test
  void testSinkPadExists() {
      assertNotNull(oggDemux.getPad("sink"));
  }

  @Test
  void testPayloadArrayInitialization() throws Exception {
      Field payloadsField =
          OggDemux.class.getDeclaredField("payloads");

      payloadsField.setAccessible(true);

      OggPayload[] payloads =
          (OggPayload[]) payloadsField.get(oggDemux);

      assertEquals(3, payloads.length);
  }
}
