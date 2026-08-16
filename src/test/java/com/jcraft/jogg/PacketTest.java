package com.jcraft.jogg;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

class PacketTest {

  @Test
  void testDefaultConstruction() {
    Packet p = new Packet();
    assertNull(p.packetBase);
    assertEquals(0, p.packet);
    assertEquals(0, p.bytes);
    assertEquals(0, p.b_o_s);
    assertEquals(0, p.e_o_s);
    assertEquals(0L, p.granulepos);
    assertEquals(0L, p.packetNo);
  }

  @Test
  void testAssignPacketBase() {
    Packet p = new Packet();
    byte[] data = {1, 2, 3, 4};
    p.packetBase = data;
    p.packet = 0;
    p.bytes = 4;
    assertArrayEquals(data, p.packetBase);
    assertEquals(4, p.bytes);
  }

  @Test
  void testPacketOffset() {
    Packet p = new Packet();
    byte[] data = {10, 20, 30, 40, 50};
    p.packetBase = data;
    p.packet = 2;
    p.bytes = 3;
    assertEquals(30, p.packetBase[p.packet]);
    assertEquals(40, p.packetBase[p.packet + 1]);
    assertEquals(50, p.packetBase[p.packet + 2]);
  }

  @Test
  void testZeroLengthPacket() {
    Packet p = new Packet();
    p.packetBase = new byte[] {};
    p.packet = 0;
    p.bytes = 0;
    assertEquals(0, p.bytes);
    assertEquals(0, p.packet);
    assertEquals(0, p.packetBase.length);
  }

  @Test
  void testNegativeOffsetIsAllowedByStruct() {
    Packet p = new Packet();
    p.packetBase = new byte[] {1, 2, 3};
    p.packet = -1;
    p.bytes = 2;
    assertEquals(-1, p.packet);
    assertEquals(2, p.bytes);
  }

  @Test
  void testNegativeBytesIsAllowedByStruct() {
    Packet p = new Packet();
    p.packetBase = new byte[] {1, 2, 3};
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
    p.packetNo = Long.MAX_VALUE;
    assertEquals(Long.MAX_VALUE, p.packetNo);
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
    p.packetBase = data;
    data[1] = 99;
    assertEquals(99, p.packetBase[1]);
  }

  @Test
  void testPacketBaseAliasingAcrossPackets() {
    byte[] shared = {9, 8, 7, 6};
    Packet p1 = new Packet();
    Packet p2 = new Packet();
    p1.packetBase = shared;
    p2.packetBase = shared;
    shared[2] = 42;
    assertEquals(42, p1.packetBase[2]);
    assertEquals(42, p2.packetBase[2]);
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
    p.packetBase = new byte[] {1, 2, 3};
    p.packet = 10;
    p.bytes = 5;
    assertEquals(10, p.packet);
    assertEquals(5, p.bytes);
  }

  @Test
  void testPacketBaseNullAllowed() {
    Packet p = new Packet();
    p.packetBase = null;
    p.packet = 0;
    p.bytes = 0;
    assertNull(p.packetBase);
  }

  @Test
  void testPacketMetadataCombination() {
    Packet p = new Packet();
    p.packetBase = new byte[] {1, 2, 3, 4};
    p.packet = 1;
    p.bytes = 2;
    p.b_o_s = 1;
    p.e_o_s = 0;
    p.granulepos = 123456;
    p.packetNo = 999;
    assertEquals(1, p.packet);
    assertEquals(2, p.bytes);
    assertEquals(1, p.b_o_s);
    assertEquals(0, p.e_o_s);
    assertEquals(123456, p.granulepos);
    assertEquals(999, p.packetNo);
  }
}
