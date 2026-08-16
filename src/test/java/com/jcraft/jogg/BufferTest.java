package com.jcraft.jogg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.*;

class BufferTest {

  private Buffer buf;

  @BeforeEach
  void setup() {
    buf = new Buffer();
  }

  @Test
  void testWriteAndReadSingleByte() {
    buf.writeinit();
    buf.write(0xAB, 8);
    buf.readinit(buf.buffer(), buf.bytes());
    assertEquals(0xAB, buf.read(8));
  }

  @Test
  void testWriteAndReadMultipleBytes() {
    buf.writeinit();
    buf.write(0x12, 8);
    buf.write(0x34, 8);
    buf.write(0x56, 8);
    buf.readinit(buf.buffer(), buf.bytes());
    assertEquals(0x12, buf.read(8));
    assertEquals(0x34, buf.read(8));
    assertEquals(0x56, buf.read(8));
  }

  @Test
  void testWriteBitsCrossingByteBoundary() {
    buf.writeinit();
    buf.write(0b101, 3);
    buf.write(0b1110, 4);
    buf.write(0b1, 1);
    buf.readinit(buf.buffer(), buf.bytes());
    assertEquals(0b101, buf.read(3));
    assertEquals(0b1110, buf.read(4));
    assertEquals(0b1, buf.read(1));
  }

  @Test
  void testWrite32Bits() {
    buf.writeinit();
    buf.write(0xDEADBEEF, 32);
    buf.readinit(buf.buffer(), buf.bytes());
    assertEquals(0xDEADBEEF, buf.read(32));
  }

  @Test
  void testWriteLessThan32BitsMasking() {
    buf.writeinit();
    buf.write(0xFFFF, 12);
    buf.readinit(buf.buffer(), buf.bytes());
    assertEquals(0xFFF, buf.read(12));
  }

  @Test
  void testLookDoesNotAdvancePointer() {
    buf.writeinit();
    buf.write(0b11001100, 8);
    buf.readinit(buf.buffer(), buf.bytes());
    assertEquals(0b1100, buf.look(4));
    assertEquals(0b1100, buf.look(4));
    assertEquals(0b1100, buf.read(4));
  }

  @Test
  void testLook1() {
    buf.writeinit();
    buf.write(0b10110000, 8);
    buf.readinit(buf.buffer(), buf.bytes());
    assertEquals(0, buf.look1());
    assertEquals(0, buf.read1());
    assertEquals(0, buf.look1());
  }

  @Test
  void testAdvMovesPointerCorrectly() {
    buf.writeinit();
    buf.write(0xAB, 8);
    buf.write(0xCD, 8);
    buf.readinit(buf.buffer(), buf.bytes());
    buf.adv(8);
    assertEquals(0xCD, buf.read(8));
  }

  @Test
  void testAdv1MovesBitPointer() {
    buf.writeinit();
    buf.write(0b10110011, 8);
    buf.readinit(buf.buffer(), buf.bytes());
    assertEquals(1, buf.look1());
    buf.adv1();
    assertEquals(1, buf.look1());
    buf.adv1();
    assertEquals(0, buf.look1());
  }

  @Test
  void testReadB32Bits() {
    buf.writeinit();
    buf.write(0x11223344, 32);
    buf.readinit(buf.buffer(), buf.bytes());
    int v = buf.readB(32);
    assertTrue(v >= 0);
    assertTrue(v <= 0xFFFFFFFFL);
  }

  @Test
  void testReadBLessThan32Bits() {
    buf.writeinit();
    buf.write(0xAABBCCDD, 32);
    buf.readinit(buf.buffer(), buf.bytes());
    int b1 = buf.readB(8);
    int b2 = buf.readB(8);
    int b3 = buf.readB(8);
    int b4 = buf.readB(8);
    assertTrue(b1 >= 0 && b1 < 256);
    assertTrue(b2 >= 0 && b2 < 256);
    assertTrue(b3 >= 0 && b3 < 256);
    assertTrue(b4 >= 0 && b4 < 256);
  }

  @Test
  void testLookPastEndReturnsMinusOne() {
    buf.writeinit();
    buf.write(0xAB, 8);
    buf.readinit(buf.buffer(), buf.bytes());
    buf.adv(8);
    assertEquals(-1, buf.look(8));
  }

  @Test
  void testReadPastEndReturnsMinusOneAndAdvances() {
    buf.writeinit();
    buf.write(0xAB, 8);
    buf.readinit(buf.buffer(), buf.bytes());
    buf.adv(8);
    assertEquals(-1, buf.read(8));
    assertEquals(16, buf.bits());
  }

  @Test
  void testRead1PastEndReturnsMinusOne() {
    buf.writeinit();
    buf.write(0xAB, 8);
    buf.readinit(buf.buffer(), buf.bytes());
    buf.adv(8);
    assertEquals(-1, buf.read1());
  }

  @Test
  void testResetClearsPointersButNotBuffer() {
    buf.writeinit();
    buf.write(0xAB, 8);
    byte[] before = Arrays.copyOf(buf.buffer(), buf.buffer().length);
    buf.reset();
    assertEquals(0, buf.bytes());
    assertEquals(0, buf.bits());
    assertEquals(0, buf.buffer()[0]);
    for (int i = 1; i < before.length; i++) {
      assertEquals(before[i], buf.buffer()[i]);
    }
  }

  @Test
  void testWriteClearNullsBuffer() {
    buf.writeinit();
    buf.writeclear();
    assertNull(buf.buffer());
  }

  @Test
  void testILog() {
    assertEquals(0, Buffer.ilog(0));
    assertEquals(1, Buffer.ilog(1));
    assertEquals(2, Buffer.ilog(2));
    assertEquals(2, Buffer.ilog(3));
    assertEquals(3, Buffer.ilog(4));
    assertEquals(5, Buffer.ilog(16));
    assertEquals(31, Buffer.ilog(Integer.MAX_VALUE));
  }
}
