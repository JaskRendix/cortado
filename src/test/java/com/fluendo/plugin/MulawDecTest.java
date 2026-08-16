package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.fluendo.jst.Buffer;
import com.fluendo.jst.Event;
import com.fluendo.jst.Pad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MulawDecTest {

  private MulawDec mulawDec;

  @BeforeEach
  void setUp() {
    mulawDec = new MulawDec();
  }

  @Test
  @DisplayName("Metadata & TypeFind Verification")
  void testMetadataAndTypeFind() {
    assertEquals("mulawdec", mulawDec.getFactoryName());
    assertEquals("audio/x-mulaw", mulawDec.getMime());

    // MulawDec returns -1 for type finding
    byte[] dummyData = new byte[] {0x01, 0x02, 0x03, 0x04};
    assertEquals(-1, mulawDec.typeFind(dummyData, 0, 4));
  }

  @Test
  @DisplayName("Sink Pad Events: Handling flush start/stop, EOS, and general events safely")
  void testSinkPadEvents() {
    Pad sinkPad = mulawDec.getPad("sink");
    assertNotNull(sinkPad);

    Event flushStartEvent = Event.newFlushStart();
    Event flushStopEvent = Event.newFlushStop();
    Event eosEvent = Event.newEOS();

    assertDoesNotThrow(() -> sinkPad.pushEvent(flushStartEvent));
    assertDoesNotThrow(() -> sinkPad.pushEvent(flushStopEvent));
    assertDoesNotThrow(() -> sinkPad.pushEvent(eosEvent));
  }

  @Test
  @DisplayName("Source Pad Events: Pushing events through source pad safely")
  void testSourcePadEvents() {
    Pad srcPad = mulawDec.getPad("src");
    assertNotNull(srcPad);

    Event eosEvent = Event.newEOS();
    assertDoesNotThrow(() -> srcPad.pushEvent(eosEvent));
  }

  @Test
  @DisplayName("Chain Function & Buffer Handling without downstream peer")
  void testChainFunctionWithoutPeer() {
    Pad sinkPad = mulawDec.getPad("sink");
    assertNotNull(sinkPad);

    Buffer buffer = new Buffer();
    buffer.data = new byte[] {10, 20, 30, 40};
    buffer.offset = 0;
    buffer.length = 4;

    // Without a downstream peer element, push returns -1
    assertEquals(-1, sinkPad.push(buffer));
  }
}
