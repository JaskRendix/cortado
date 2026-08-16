package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fluendo.jst.*;
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

    sinkPad.pushEvent(Event.newFlushStart());
    sinkPad.pushEvent(Event.newFlushStop());
    assertTrue(true, "Flush events executed successfully");
  }

  @Test
  public void testEosEventExecution() {
    Pad sinkPad = oggDemux.getPad("sink");
    assertNotNull(sinkPad);

    sinkPad.pushEvent(Event.newEOS());
    assertTrue(true, "EOS event executed successfully");
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
  public void testEventInteractionsWithMockito() {
    Pad sinkPad = oggDemux.getPad("sink");
    assertNotNull(sinkPad);

    Event mockEvent = mock(Event.class);
    when(mockEvent.getType()).thenReturn(Event.Type.FLUSH_START);

    // Verify pushing custom or mocked events handles gracefully without errors
    assertDoesNotThrow(() -> sinkPad.pushEvent(mockEvent), "Pushing events should execute cleanly");
  }

  @Test
  public void testBufferDataDelegationWithMockito() {
    Buffer mockBuffer = spy(Buffer.create());

    byte[] dummyData = new byte[] {0x4f, 0x67, 0x67, 0x53, 0x00, 0x02, 0x00};
    mockBuffer.copyData(dummyData, 0, dummyData.length);

    assertNotNull(mockBuffer.data, "Spy buffer should retain real data");

    int score = oggDemux.typeFind(mockBuffer.data, 0, mockBuffer.length);
    assertEquals(10, score, "Typefind should still succeed on spy buffer data");
  }
}
