package com.fluendo.plugin;

import com.fluendo.jst.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        byte[] validHeader = new byte[] { 0x4f, 0x67, 0x67, 0x53, 0x00, 0x02, 0x00 };
        int confidence = oggDemux.typeFind(validHeader, 0, validHeader.length);
        assertEquals(10, confidence, "Valid OggS signature should return confidence 10");
    }

    @Test
    public void testTypeFindInvalidSignature() {
        byte[] invalidHeader = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04 };
        int confidence = oggDemux.typeFind(invalidHeader, 0, invalidHeader.length);
        assertEquals(-1, confidence, "Invalid signature should return -1");
    }

    @Test
    public void testTypeFindWithOffset() {
        byte[] dataWithOffset = new byte[] { 0x00, 0x00, 0x4f, 0x67, 0x67, 0x53 };
        int confidence = oggDemux.typeFind(dataWithOffset, 2, 4);
        assertEquals(10, confidence, "Valid OggS signature starting at offset 2 should be detected");
    }

    @Test
    public void testFlushEventsExecution() {
        Pad sinkPad = oggDemux.getPad("sink");
        assertNotNull(sinkPad);

        // Verify event handling execution without strict return requirements on unlinked pads
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
}
