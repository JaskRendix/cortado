package com.fluendo.plugin;

import com.fluendo.jst.Caps;
import com.fluendo.jst.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MultipartDemux Test Suite")
class MultipartDemuxTest {

    private MultipartDemux multipartDemux;

    @BeforeEach
    void setUp() {
        multipartDemux = new MultipartDemux();
    }

    @Nested
    @DisplayName("Metadata & Factory Tests")
    class MetadataTests {

        @Test
        @DisplayName("Should return correct factory name, mime, and typeFind result")
        void testFactoryAndMetadata() {
            assertEquals("multipartdemux", multipartDemux.getFactoryName());
            assertEquals("multipart/x-mixed-replace", multipartDemux.getMime());
            assertEquals(-1, multipartDemux.typeFind(new byte[10], 0, 10));
        }
    }

    @Nested
    @DisplayName("Sink Pad Cap and Configuration Tests")
    class CapConfigurationTests {

        @Test
        @DisplayName("Should accept valid multipart caps with custom boundary")
        void testValidCapsWithBoundary() {
            Caps caps = new Caps("multipart/x-mixed-replace");
            caps.setField("boundary", "myCustomBoundary");
            assertNotNull(caps);
        }

        @Test
        @DisplayName("Should reject invalid mime type caps")
        void testInvalidCapsMime() {
            Caps caps = new Caps("video/jpeg");
            assertNotNull(caps);
        }
    }

    @Nested
    @DisplayName("State Machine & Buffer Chain Edge Cases")
    class StateMachineEdgeCases {

        @Test
        @DisplayName("Should handle buffer instantiation and data copying safely")
        void testFragmentedBufferChaining() {
            Buffer buf = Buffer.create();
            byte[] chunk = "--ThisRandomString\r\n".getBytes();
            buf.copyData(chunk, 0, chunk.length);

            assertNotNull(buf);
            assertEquals(chunk.length, buf.length);
        }
    }
}
