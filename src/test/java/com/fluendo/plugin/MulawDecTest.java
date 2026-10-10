package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.fluendo.jst.Buffer;
import com.fluendo.jst.Pad;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MulawDec Test Suite")
class MulawDecTest {

  private MulawDec mulawDec;

  @BeforeEach
  void setUp() {
    mulawDec = new MulawDec();
  }

  @Test
  @DisplayName("Should expose correct factory metadata")
  void testMetadataAndTypeFind() {
    assertEquals("mulawdec", mulawDec.getFactoryName());
    assertEquals("audio/x-mulaw", mulawDec.getMime());

    byte[] dummyData = {0x01, 0x02, 0x03, 0x04};

    assertEquals(
        -1,
        mulawDec.typeFind(dummyData, 0, dummyData.length));
  }

  @Test
  @DisplayName("Should create source and sink pads")
  void testPadsExist() {
    assertNotNull(mulawDec.getPad("src"));
    assertNotNull(mulawDec.getPad("sink"));
  }

  @Test
  @DisplayName("Should initialize default rate and channel count")
  void testDefaultRateAndChannels() throws Exception {
    Field rateField = MulawDec.class.getDeclaredField("rate");
    Field channelsField = MulawDec.class.getDeclaredField("channels");

    rateField.setAccessible(true);
    channelsField.setAccessible(true);

    assertEquals(8000, rateField.getInt(mulawDec));
    assertEquals(1, channelsField.getInt(mulawDec));
  }

  @Test
  @DisplayName("Should handle buffer push without downstream peer")
  void testChainFunctionWithoutPeer() {
    Pad sinkPad = mulawDec.getPad("sink");

    assertNotNull(sinkPad);

    Buffer buffer = new Buffer();
    buffer.data = new byte[] {10, 20, 30, 40};
    buffer.offset = 0;
    buffer.length = 4;

    assertEquals(
        Pad.NOT_LINKED,
        sinkPad.push(buffer));
  }
}
