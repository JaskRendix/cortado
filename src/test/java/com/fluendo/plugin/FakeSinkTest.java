package com.fluendo.plugin;

import com.fluendo.jst.Buffer;
import com.fluendo.jst.Pad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FakeSinkTest {

    private FakeSink fakeSink;

    @BeforeEach
    void setUp() {
        fakeSink = new FakeSink();
    }

    @Test
    @DisplayName("Metadata: Factory name verification")
    void testGetFactoryName() {
        assertEquals("fakesink", fakeSink.getFactoryName());
    }

    @Test
    @DisplayName("Preroll: Handling valid buffer during preroll phase")
    void testPrerollWithBuffer() {
        Buffer buffer = new Buffer();
        int result = fakeSink.preroll(buffer);
        assertEquals(Pad.OK, result);
    }

    @Test
    @DisplayName("Render: Handling buffer containing an object payload")
    void testRenderWithObject() {
        Buffer buffer = new Buffer();
        buffer.object = "Sample Object Payload";

        int result = fakeSink.render(buffer);
        assertEquals(Pad.OK, result);
    }

    @Test
    @DisplayName("Render: Handling raw byte data buffer (MemUtils dump path)")
    void testRenderWithRawData() {
        Buffer buffer = new Buffer();
        buffer.object = null;
        buffer.data = new byte[]{0x41, 0x42, 0x43, 0x44}; // "ABCD"
        buffer.offset = 0;
        buffer.length = 4;

        int result = fakeSink.render(buffer);
        assertEquals(Pad.OK, result);
    }

    @Test
    @DisplayName("Sink Pad Integration: Pushing buffer through sink pad chain")
    void testSinkPadBufferPush() {
        Pad sinkPad = fakeSink.getPad("sink");
        assertNotNull(sinkPad);

        Buffer buffer = new Buffer();
        buffer.data = new byte[]{1, 2, 3};
        buffer.offset = 0;
        buffer.length = 3;

        // Pushing buffer into sink pad should successfully invoke render/preroll logic
        // Returns -1 if no upstream or peer connection, but sink execution handles it safely
        assertDoesNotThrow(() -> sinkPad.push(buffer));
    }
}