package com.fluendo.codecs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Component;
import java.awt.MediaTracker;

import static org.junit.jupiter.api.Assertions.*;

class SmokeCodecTest {

    private SmokeCodec codec;
    private MockComponent mockComponent;
    private MediaTracker mockTracker;

    @BeforeEach
    void setUp() {
        mockComponent = new MockComponent();
        mockTracker = new MockMediaTracker(mockComponent);
        codec = new SmokeCodec(mockComponent, mockTracker);
    }

    @Test
    void testParseHeaderNullOrTooShort() {
        assertEquals(-1, codec.parseHeader(null, 0, 10));
        assertEquals(-1, codec.parseHeader(new byte[5], 0, 5));
    }

    @Test
    void testParseHeaderValidValues() {
        // Construct a mock header matching OFFS_PICT (18 bytes) minimum size
        byte[] headerData = new byte[20];
        
        // Type = 1
        headerData[0] = 0x01;
        
        // Width = 320 (0x0140) at index 1, 2
        headerData[1] = 0x01;
        headerData[2] = 0x40;
        
        // Height = 240 (0x00F0) at index 3, 4
        headerData[3] = 0x00;
        headerData[4] = (byte) 0xF0;

        int result = codec.parseHeader(headerData, 0, headerData.length);
        
        assertEquals(0, result);
        assertEquals(1, codec.type);
        assertEquals(320, codec.width);
        assertEquals(240, codec.height);
    }

    @Test
    void testDecodeWithoutReferenceAndNotKeyframe() {
        // Flags do not have KEYFRAME set (flags = 0)
        byte[] data = new byte[20];
        data[13] = 0x00; // flags

        // reference is null, and keyframe is false -> should return null immediately
        assertNull(codec.decode(data, 0, data.length));
    }

    // --- Lightweight Test Doubles for AWT Headless Environment ---
    
    private static class MockComponent extends Component {
        // Prevents headless exceptions during tests by overriding basic factory calls
    }

    private static class MockMediaTracker extends MediaTracker {
        public MockMediaTracker(Component comp) {
            super(comp);
        }

        @Override
        public void addImage(java.awt.Image image, int id) {}

        @Override
        public void waitForID(int id) {}

        @Override
        public void removeImage(java.awt.Image image, int id) {}
    }
}
