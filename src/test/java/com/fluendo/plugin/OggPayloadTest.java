package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.jcraft.jogg.Packet;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OggPayloadTest {

  private OggPayload payload;

  // Concrete dummy implementation of the OggPayload interface for testing contract behavior
  private static class DummyOggPayload implements OggPayload {
    @Override
    public boolean isType(Packet op) {
      return op != null && op.bytes > 0;
    }

    @Override
    public int takeHeader(Packet op) {
      if (op == null || op.bytes == 0) return -1;
      return op.bytes == 1 ? 1 : 0;
    }

    @Override
    public boolean isHeader(Packet op) {
      return op != null && op.b_o_s != 0;
    }

    @Override
    public boolean isKeyFrame(Packet op) {
      return op != null && op.packetNo == 0;
    }

    @Override
    public long getFirstTs(List<com.fluendo.jst.Buffer> packets) {
      if (packets == null || packets.isEmpty()) {
        return -1L;
      }
      return 1000L;
    }

    @Override
    public long granuleToTime(long gp) {
      return gp < 0 ? -1L : gp * 10L;
    }

    @Override
    public String getMime() {
      return "video/x-test";
    }

    @Override
    public String getMime(Packet op) {
      return op == null ? "application/octet-stream" : "video/x-test";
    }

    @Override
    public boolean isDiscontinuous() {
      return false;
    }
  }

  @BeforeEach
  void setUp() {
    payload = new DummyOggPayload();
  }

  @Test
  @DisplayName("Signature & Type Checking: Valid vs Invalid Packets")
  void testIsType() {
    Packet validPacket = new Packet();
    validPacket.bytes = 10;
    assertTrue(payload.isType(validPacket));

    Packet invalidPacket = new Packet();
    invalidPacket.bytes = 0;
    assertFalse(payload.isType(invalidPacket));
    assertFalse(payload.isType(null));
  }

  @Test
  @DisplayName("Header Handling: Error, OK, and Ready states")
  void testTakeHeader() {
    Packet errorPacket = new Packet();
    errorPacket.bytes = 0;
    assertEquals(-1, payload.takeHeader(errorPacket));
    assertEquals(-1, payload.takeHeader(null));

    Packet okPacket = new Packet();
    okPacket.bytes = 2;
    assertEquals(0, payload.takeHeader(okPacket));

    Packet readyPacket = new Packet();
    readyPacket.bytes = 1;
    assertEquals(1, payload.takeHeader(readyPacket));
  }

  @Test
  @DisplayName("Header & Keyframe Flag Checks")
  void testFlags() {
    Packet packet = new Packet();
    packet.b_o_s = 1;
    packet.packetNo = 0;

    assertTrue(payload.isHeader(packet));
    assertTrue(payload.isKeyFrame(packet));

    assertFalse(payload.isHeader(null));
    assertFalse(payload.isKeyFrame(null));
  }

  @Test
  @DisplayName("Timestamp Extraction: List buffer edges")
  void testGetFirstTs() {
    assertEquals(-1L, payload.getFirstTs(null));
    assertEquals(-1L, payload.getFirstTs(new ArrayList<>()));

    List<com.fluendo.jst.Buffer> packets = new ArrayList<>();
    packets.add(new com.fluendo.jst.Buffer());
    assertEquals(1000L, payload.getFirstTs(packets));
  }

  @Test
  @DisplayName("Granule Position to Time Conversion")
  void testGranuleToTime() {
    assertEquals(500L, payload.granuleToTime(50L));
    assertEquals(-1L, payload.granuleToTime(-5L));
  }

  @Test
  @DisplayName("Mime Type Resolution")
  void testMimeTypes() {
    assertEquals("video/x-test", payload.getMime());
    assertEquals("video/x-test", payload.getMime(new Packet()));
    assertEquals("application/octet-stream", payload.getMime(null));
  }

  @Test
  @DisplayName("Discontinuity Check")
  void testIsDiscontinuous() {
    assertFalse(payload.isDiscontinuous());
  }
}
