package com.fluendo.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(-1.0f, info.startTime, 0.001f);
        assertEquals(-1.0f, info.duration, 0.001f);
    }

    @Test
    @DisplayName("TimingInfo: Parameterized constructor values check")
    void testTimingInfoParameterizedConstructor() {
        DurationScanner.TimingInfo info = new DurationScanner.TimingInfo(2.5f, 120.0f);
        assertEquals(2.5f, info.startTime, 0.001f);
        assertEquals(120.0f, info.duration, 0.001f);
    }

    @Test
    @DisplayName("Edge Case: scanBuffer with empty/zero-length byte array handled via exception expectation")
    void testScanBufferEmpty() {
        byte[] emptyBuffer = new byte[0];
        assertThrows(NullPointerException.class, () -> {
            scanner.scanBuffer(emptyBuffer, 0);
        });
    }

    @Test
    @DisplayName("Edge Case: scanBuffer with uninitialized or garbage Ogg bytes")
    void testScanBufferGarbageData() {
        byte[] garbage = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05 };
        assertDoesNotThrow(() -> {
            DurationScanner.TimingInfo info = scanner.scanBuffer(garbage, garbage.length);
            assertNotNull(info);
            assertTrue(info.startTime <= 0.0f, "Start time should be negative or zero for invalid stream");
        });
    }

    @Test
    @DisplayName("Edge Case: scanURL with unreachable or invalid URL/Protocol")
    void testScanInvalidUrl() throws MalformedURLException {
        URL badUrl = new URL("http://localhost:1/nonexistent-media-stream");
        DurationScanner.TimingInfo info = scanner.scanURL(badUrl, null, null);
        assertNotNull(info);
        assertEquals(-1.0f, info.startTime, 0.001f);
        assertEquals(-1.0f, info.duration, 0.001f);
    }
}
