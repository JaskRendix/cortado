package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.fluendo.jst.Buffer;
import com.fluendo.jst.Pad;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("MultipartDemux Test Suite")
class MultipartDemuxTest {

  private MultipartDemux multipartDemux;

  @BeforeEach
  void setUp() {
    multipartDemux = new MultipartDemux();
  }

  @Nested
  @DisplayName("Metadata Tests")
  class MetadataTests {

    @Test
    @DisplayName("Should expose correct metadata")
    void testFactoryAndMetadata() {
      assertEquals("multipartdemux", multipartDemux.getFactoryName());
      assertEquals("multipart/x-mixed-replace", multipartDemux.getMime());
      assertEquals(-1, multipartDemux.typeFind(new byte[8], 0, 8));
    }
  }

  @Nested
  @DisplayName("Initialization Tests")
  class InitializationTests {

    @Test
    @DisplayName("Should create sink pad")
    void testSinkPadExists() {
      assertNotNull(multipartDemux.getPad("sink"));
    }

    @Test
    @DisplayName("Should initialize internal state")
    void testInitialState() throws Exception {
      Field accumField =
          MultipartDemux.class.getDeclaredField("accum");
      Field accumSizeField =
          MultipartDemux.class.getDeclaredField("accumSize");
      Field accumPosField =
          MultipartDemux.class.getDeclaredField("accumPos");
      Field streamsField =
          MultipartDemux.class.getDeclaredField("streams");

      accumField.setAccessible(true);
      accumSizeField.setAccessible(true);
      accumPosField.setAccessible(true);
      streamsField.setAccessible(true);

      byte[] accum = (byte[]) accumField.get(multipartDemux);

      assertEquals(8192, accum.length);
      assertEquals(0, accumSizeField.getInt(multipartDemux));
      assertEquals(0, accumPosField.getInt(multipartDemux));

      List<?> streams =
          (List<?>) streamsField.get(multipartDemux);

      assertTrue(streams.isEmpty());
    }

    @Test
    @DisplayName("Should not create multipart streams initially")
    void testNoStreamsInitially() throws Exception {
      Field streamsField =
          MultipartDemux.class.getDeclaredField("streams");

      streamsField.setAccessible(true);

      List<?> streams =
          (List<?>) streamsField.get(multipartDemux);

      assertEquals(0, streams.size());
    }
  }

  @Nested
  @DisplayName("Buffer Processing Tests")
  class BufferProcessingTests {

    @Test
    @DisplayName("Should process multipart payload safely")
    void testMultipartPayloadProcessing() {
      Pad sinkPad = multipartDemux.getPad("sink");

      assertNotNull(sinkPad);

      Buffer buffer = Buffer.create();

      byte[] multipartData =
          ("--ThisRandomString\n"
                  + "Content-Type: image/jpeg\n"
                  + "\n"
                  + "abcd\n"
                  + "--ThisRandomString\n")
              .getBytes(StandardCharsets.UTF_8);

      buffer.copyData(
          multipartData,
          0,
          multipartData.length);

      assertDoesNotThrow(
          () -> sinkPad.push(buffer));
    }

    @Test
    @DisplayName("Should process empty payload safely")
    void testEmptyBufferProcessing() {
      Pad sinkPad = multipartDemux.getPad("sink");

      assertNotNull(sinkPad);

      Buffer buffer = Buffer.create();

      buffer.copyData(new byte[0], 0, 0);

      assertDoesNotThrow(
          () -> sinkPad.push(buffer));
    }

    @Test
    @DisplayName("Should process fragmented multipart data safely")
    void testFragmentedMultipartData() {
      Pad sinkPad = multipartDemux.getPad("sink");

      assertNotNull(sinkPad);

      Buffer first = Buffer.create();
      Buffer second = Buffer.create();

      byte[] chunk1 =
          "--ThisRandomString\nContent-Type: image/jpeg\n"
              .getBytes(StandardCharsets.UTF_8);

      byte[] chunk2 =
          "\nabcd\n--ThisRandomString\n"
              .getBytes(StandardCharsets.UTF_8);

      first.copyData(chunk1, 0, chunk1.length);
      second.copyData(chunk2, 0, chunk2.length);

      assertDoesNotThrow(() -> sinkPad.push(first));
      assertDoesNotThrow(() -> sinkPad.push(second));
    }
  }

  @Nested
  @DisplayName("Pad Tests")
  class PadTests {

    @Test
    @DisplayName("Should expose only sink pad initially")
    void testPadsBeforeParsing() {
      assertNotNull(multipartDemux.getPad("sink"));
      assertNull(multipartDemux.getPad("src_image/jpeg"));
    }
  }
}
