package com.jcraft.jogg;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

class PacketTest {

  @Test
  void testDefaultConstruction() {
    Packet p = new Packet();
    assertNull(p.packet_base);
    assertEquals(0, p.packet);
    assertEquals(0, p.bytes);
    assertEquals(0, p.b_o_s);
    assertEquals(0, p.e_o_s);
    assertEquals(0L, p.granulepos);
    assertEquals(0L, p.packetno);
  }

  @Test
  void testAssignPacketBase() {
    Packet p = new Packet();
    byte[] data = {1, 2, 3, 4};
    p.packet_base = data;
    p.packet = 0;
    p.bytes = 4;
    assertArrayEquals(data, p.packet_base);
    assertEquals(4, p.bytes);
  }

  @Test
  void testPacketOffset() {
    Packet p = new Packet();
    byte[] data = {10, 20, 30, 40, 50};
    p.packet_base = data;
    p.packet = 2;
    p.bytes = 3;
    assertEquals(30, p.packet_base[p.packet]);
    assertEquals(40, p.packet_base[p.packet + 1]);
    assertEquals(50, p.packet_base[p.packet + 2]);
  }

  @Test
  void testZeroLengthPacket() {
    Packet p = new Packet();
    p.packet_base = new byte[] {};
    p.packet = 0;
    p.bytes = 0;
    assertEquals(0, p.bytes);
    assertEquals(0, p.packet);
    assertEquals(0, p.packet_base.length);
  }

  @Test
  void testNegativeOffsetIsAllowedByStruct() {
    Packet p = new Packet();
    p.packet_base = new byte[] {1, 2, 3};
    p.packet = -1;
    p.bytes = 2;
    assertEquals(-1, p.packet);
    assertEquals(2, p.bytes);
  }

  @Test
  void testNegativeBytesIsAllowedByStruct() {
    Packet p = new Packet();
    p.packet_base = new byte[] {1, 2, 3};
    p.packet = 0;
    p.bytes = -5;
    assertEquals(-5, p.bytes);
  }

  @Test
  void testLargeGranulepos() {
    Packet p = new Packet();
    p.granulepos = Long.MAX_VALUE;
    assertEquals(Long.MAX_VALUE, p.granulepos);
  }

  @Test
  void testLargePacketno() {
    Packet p = new Packet();
    p.packetno = Long.MAX_VALUE;
    assertEquals(Long.MAX_VALUE, p.packetno);
  }

  @Test
  void testBOSFlag() {
    Packet p = new Packet();
    p.b_o_s = 1;
    assertEquals(1, p.b_o_s);
  }

  @Test
  void testEOSFlag() {
    Packet p = new Packet();
    p.e_o_s = 1;
    assertEquals(1, p.e_o_s);
  }

  @Test
  void testMutatingPacketBaseDoesNotReplaceReference() {
    Packet p = new Packet();
    byte[] data = {5, 6, 7};
    p.packet_base = data;
    data[1] = 99;
    assertEquals(99, p.packet_base[1]);
  }

  @Test
  void testPacketBaseAliasingAcrossPackets() {
    byte[] shared = {9, 8, 7, 6};
    Packet p1 = new Packet();
    Packet p2 = new Packet();
    p1.packet_base = shared;
    p2.packet_base = shared;
    shared[2] = 42;
    assertEquals(42, p1.packet_base[2]);
    assertEquals(42, p2.packet_base[2]);
  }

  @Test
  void testPacketFieldsIndependentBetweenInstances() {
    Packet p1 = new Packet();
    Packet p2 = new Packet();
    p1.bytes = 10;
    p2.bytes = 20;
    assertEquals(10, p1.bytes);
    assertEquals(20, p2.bytes);
  }

  @Test
  void testPacketOffsetBeyondArrayIsNotChecked() {
    Packet p = new Packet();
    p.packet_base = new byte[] {1, 2, 3};
    p.packet = 10;
    p.bytes = 5;
    assertEquals(10, p.packet);
    assertEquals(5, p.bytes);
  }

  @Test
  void testPacketBaseNullAllowed() {
    Packet p = new Packet();
    p.packet_base = null;
    p.packet = 0;
    p.bytes = 0;
    assertNull(p.packet_base);
  }

  @Test
  void testPacketMetadataCombination() {
    Packet p = new Packet();
    p.packet_base = new byte[] {1, 2, 3, 4};
    p.packet = 1;
    p.bytes = 2;
    p.b_o_s = 1;
    p.e_o_s = 0;
    p.granulepos = 123456;
    p.packetno = 999;
    assertEquals(1, p.packet);
    assertEquals(2, p.bytes);
    assertEquals(1, p.b_o_s);
    assertEquals(0, p.e_o_s);
    assertEquals(123456, p.granulepos);
    assertEquals(999, p.packetno);
  }
}
