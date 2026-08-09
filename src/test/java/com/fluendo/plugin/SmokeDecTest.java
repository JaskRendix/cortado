package com.fluendo.plugin;

import com.fluendo.jst.Buffer;
import com.fluendo.jst.Event;
import com.fluendo.jst.Pad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Button;
import java.awt.Component;

import static org.junit.jupiter.api.Assertions.*;

class SmokeDecTest {

    private SmokeDec smokeDec;
    private Component dummyComponent;

    @BeforeEach
    void setUp() {
        smokeDec = new SmokeDec();
        dummyComponent = new Button("Smoke Parent");
        smokeDec.setProperty("component", dummyComponent);
    }

    @Test
    @DisplayName("Metadata: Factory name, mime type, and type finding verification")
    void testMetadataAndTypeFind() {
        assertEquals("smokedec", smokeDec.getFactoryName());
        assertEquals("video/x-smoke", smokeDec.getMime());

        // Test typeFind with valid pattern (offset + 1 == 0x73)
        byte[] validData = new byte[]{0x00, 0x73, 0x00, 0x00};
        assertEquals(10, smokeDec.typeFind(validData, 0, 4));

        // Test typeFind with invalid pattern
        byte[] invalidData = new byte[]{0x00, 0x00, 0x00, 0x00};
        assertEquals(-1, smokeDec.typeFind(invalidData, 0, 4));
    }

    @Test
    @DisplayName("Properties: Setting and getting component property safely")
    void testPropertyHandling() {
        assertEquals(dummyComponent, smokeDec.getProperty("component"));
        assertNull(smokeDec.getProperty("unknown"));
        assertFalse(smokeDec.setProperty("unknown", "value"));
    }

    @Test
    @DisplayName("Sink Pad Events: Handling flush and general events safely")
    void testSinkPadEvents() {
        Pad sinkPad = smokeDec.getPad("sink");
        assertNotNull(sinkPad);

        Event flushStartEvent = Event.newFlushStart();
        Event flushStopEvent = Event.newFlushStop();
        Event eosEvent = Event.newEOS();

        assertDoesNotThrow(() -> sinkPad.pushEvent(flushStartEvent));
        assertDoesNotThrow(() -> sinkPad.pushEvent(flushStopEvent));
        assertDoesNotThrow(() -> sinkPad.pushEvent(eosEvent));
    }

    @Test
    @DisplayName("Chain Function: Handling decode failure / null image edge case")
    void testChainFunctionDecodeFailure() {
        Pad sinkPad = smokeDec.getPad("sink");
        assertNotNull(sinkPad);

        Buffer buffer = new Buffer();
        buffer.data = new byte[]{1, 2, 3, 4};
        buffer.offset = 0;
        buffer.length = 4;

        // Expect -1 (error/drop code) when decoding invalid/dummy image bytes fails
        assertDoesNotThrow(() -> {
            int result = sinkPad.push(buffer);
            assertEquals(-1, result);
        });
    }

    @Test
    @DisplayName("Source Pad Events: Pushing events through source pad safely")
    void testSourcePadEvents() {
        Pad srcPad = smokeDec.getPad("src");
        assertNotNull(srcPad);

        Event eosEvent = Event.newEOS();
        assertDoesNotThrow(() -> srcPad.pushEvent(eosEvent));
    }
}
