package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import com.jcraft.jogg.Buffer;
import com.jcraft.jogg.Packet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InfoTest {

  private Info info;

  @BeforeEach
  void setUp() {
    info = new Info();
  }

  @Test
  @DisplayName("Clear Method: Resets metadata state and probe count")
  void testClearState() {
    info.num_headers = 5;
    info.language = "en";
    info.clear();

    assertEquals(0, info.num_headers);
    assertNull(info.regions);
    assertNull(info.styles);
    assertNull(info.curves);
    assertNull(info.motions);
  }

  @Test
  @DisplayName("CheckEOP Edge Case: Non-zero padding bits returns bad packet")
  void testCheckEOPInvalidPadding() {
    Buffer buffer = new Buffer();
    byte[] data = {(byte) 0xFF}; // Non-zero bits remaining
    buffer.readinit(data, 0, 1);

    int result = Info.checkEOP(buffer);
    assertEquals(Result.KATE_E_BAD_PACKET, result);
  }

  @Test
  @DisplayName("DecodeHeader Edge Case: Non-BOS initial packet returns error")
  void testDecodeHeaderNonBosInitialPacket() {
    Comment comment = new Comment();
    Packet packet = new Packet();

    // Prepare mock buffer with valid header magic but b_o_s == 0
    byte[] packetData = new byte[] {(byte) 0x80, 'k', 'a', 't', 'e', 0, 0, 0, 0};
    packet.packet_base = packetData;
    packet.packet = 0;
    packet.bytes = packetData.length;
    packet.packetno = 0;
    packet.b_o_s = 0; // Not beginning of stream

    int result = info.decodeHeader(comment, packet);
    assertEquals(Result.KATE_E_BAD_PACKET, result);
  }

  @Test
  @DisplayName("DecodeHeader Edge Case: Invalid magic header returns not kate error")
  void testDecodeHeaderInvalidMagic() {
    Comment comment = new Comment();
    Packet packet = new Packet();

    byte[] packetData = new byte[] {(byte) 0x80, 'j', 'a', 'v', 'a', 0, 0, 0, 0};
    packet.packet_base = packetData;
    packet.packet = 0;
    packet.bytes = packetData.length;
    packet.packetno = 0;
    packet.b_o_s = 1;

    int result = info.decodeHeader(comment, packet);
    assertEquals(Result.KATE_E_NOT_KATE, result);
  }

  @Test
  @DisplayName("UnpackRegion Edge Case: Validates extraction and clipping flags")
  void testUnpackRegionBasic() throws KateException {
    Buffer buffer = new Buffer();
    // Construct mock bitstream data for region parsing
    // metric (8 bits), x, y, w, h, style (v32 variables)
    byte[] data =
        new byte[] {
          0, 10, 20, 30, 40, 1, 0, 0 // Minimal encoded content
        };
    buffer.readinit(data, 0, data.length);
    info.bitstream_version_major = 0;
    info.bitstream_version_minor = 2;

    Region region = info.unpackRegion(buffer);
    assertNotNull(region);
    assertFalse(region.clip);
  }
}
