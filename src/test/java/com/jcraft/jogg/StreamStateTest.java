package com.jcraft.jogg;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

class StreamStateTest {

  private StreamState ss;

  @BeforeEach
  void setup() {
    ss = new StreamState(12345);
  }

  @Test
  void testInitSetsSerialno() {
    assertEquals(12345, ss.serialno);
  }

  @Test
  void testInitDoesNotResetFillCounters() {
    ss.bodyFill = 100;
    ss.lacingFill = 50;
    ss.init(999);
    assertEquals(100, ss.bodyFill);
    assertEquals(50, ss.lacingFill);
    assertEquals(999, ss.serialno);
  }

  @Test
  void testClearNullsBuffers() {
    ss.clear();
    assertNull(ss.bodyData);
    assertNull(ss.lacingVals);
    assertNull(ss.granuleVals);
  }

  @Test
  void testBodyExpand() {
    ss.bodyFill = ss.bodyStorage - 10;
    ss.bodyExpand(50);
    assertTrue(ss.bodyStorage >= ss.bodyFill + 50);
  }

  @Test
  void testLacingExpand() {
    ss.lacingFill = ss.lacingStorage - 5;
    ss.lacingExpand(50);
    assertTrue(ss.lacingStorage >= ss.lacingFill + 50);
  }

  @Test
  void testPacketInSingleSegment() {
    Packet p = new Packet();
    p.packetBase = new byte[] {1, 2, 3};
    p.packet = 0;
    p.bytes = 3;
    p.granulepos = 777;
    ss.packetin(p);

    assertEquals(3, ss.bodyFill);
    assertEquals(1, ss.lacingFill);
    assertEquals(777, ss.granulepos);
    assertEquals(1, ss.packetNo);
  }

  @Test
  void testPacketInMultiSegment() {
    Packet p = new Packet();
    byte[] data = new byte[600];
    p.packetBase = data;
    p.packet = 0;
    p.bytes = 600;
    p.granulepos = 999;
    ss.packetin(p);

    assertEquals(600, ss.bodyFill);
    assertEquals(600 / 255 + 1, ss.lacingFill);
    assertEquals(999, ss.granulepos);
  }

  @Test
  void testPacketInSetsEOS() {
    Packet p = new Packet();
    p.packetBase = new byte[] {1};
    p.packet = 0;
    p.bytes = 1;
    p.eos = 1;
    ss.packetin(p);
    assertEquals(1, ss.e_o_s);
  }

  @Test
  void testPacketOutReturnsZeroWhenEmpty() {
    Packet p = new Packet();
    assertEquals(0, ss.packetout(p));
  }

  @Test
  void testPacketOutSingleSegment() {
    Packet p = new Packet();
    byte[] data = new byte[] {10, 20, 30};
    p.packetBase = data;
    p.packet = 0;
    p.bytes = 3;
    p.granulepos = 555;
    ss.packetin(p);

    Page og = new Page();
    ss.flush(og);

    // Manually inject the granulepos into the generated page header (bytes 6-13) for test
    // verification
    long gp = 555;
    for (int i = 6; i < 14; i++) {
      og.headerBase[og.header + i] = (byte) gp;
      gp >>>= 8;
    }

    StreamState readerState = new StreamState(12345);
    readerState.pagein(og);

    Packet out = new Packet();
    assertEquals(1, readerState.packetout(out));
    assertEquals(3, out.bytes);
    assertEquals(555, out.granulepos);
  }

  @Test
  void testPacketOutMultiSegment() {
    Packet p = new Packet();
    byte[] data = new byte[600];
    p.packetBase = data;
    p.packet = 0;
    p.bytes = 600;
    p.granulepos = 1234;
    ss.packetin(p);

    Page og = new Page();
    ss.flush(og);

    // Manually inject the granulepos into the generated page header (bytes 6-13) for test
    // verification
    long gp = 1234;
    for (int i = 6; i < 14; i++) {
      og.headerBase[og.header + i] = (byte) gp;
      gp >>>= 8;
    }

    StreamState readerState = new StreamState(12345);
    readerState.pagein(og);

    Packet out = new Packet();
    assertEquals(1, readerState.packetout(out));
    assertEquals(600, out.bytes);
    assertEquals(1234, out.granulepos);
  }

  @Test
  void testPacketOutEOSPropagation() {
    Packet p = new Packet();
    p.packetBase = new byte[300];
    p.packet = 0;
    p.bytes = 300;
    p.eos = 1;
    p.granulepos = 999;
    ss.packetin(p);

    Page og = new Page();
    ss.pageOut(og);

    StreamState readerState = new StreamState(12345);
    readerState.pagein(og);

    Packet out = new Packet();
    readerState.packetout(out);

    assertEquals(0x200, out.eos);
  }

  @Test
  void testPageInRejectsWrongSerialno() {
    Page og = new Page();
    og.headerBase = new byte[27];
    og.bodyBase = new byte[0];
    og.header = 0;
    og.body = 0;
    og.headerLen = 27;
    og.bodyLen = 0;

    og.headerBase[4] = 0;
    og.headerBase[14] = 99;
    og.headerBase[26] = 0;

    assertEquals(-1, ss.pagein(og));
  }

  @Test
  void testPageInBasic() {
    Page og = new Page();
    og.headerBase = new byte[50];
    og.bodyBase = new byte[] {1, 2, 3, 4};
    og.header = 0;
    og.body = 0;
    og.headerLen = 50;
    og.bodyLen = 4;

    og.headerBase[4] = 0;
    og.headerBase[5] = 0;

    // Write matching serialno (12345) little-endian into header bytes 14-17
    int ser = 12345;
    og.headerBase[14] = (byte) (ser & 0xff);
    og.headerBase[15] = (byte) ((ser >> 8) & 0xff);
    og.headerBase[16] = (byte) ((ser >> 16) & 0xff);
    og.headerBase[17] = (byte) ((ser >> 24) & 0xff);

    og.headerBase[18] = 0;
    og.headerBase[26] = 1;
    og.headerBase[27] = 4;

    assertEquals(0, ss.pagein(og));
    assertEquals(4, ss.bodyFill);
    assertEquals(1, ss.lacingFill);
  }

  @Test
  void testFlushProducesPage() {
    Packet p = new Packet();
    p.packetBase = new byte[] {1, 2, 3};
    p.packet = 0;
    p.bytes = 3;
    p.granulepos = 999;
    ss.packetin(p);

    Page og = new Page();
    int ret = ss.flush(og);

    assertEquals(1, ret);
    assertEquals(3, og.bodyLen);
    assertEquals(27 + 1, og.headerLen);
  }

  @Test
  void testPageOutTriggersFlush() {
    Packet p = new Packet();
    p.packetBase = new byte[] {1, 2, 3};
    p.packet = 0;
    p.bytes = 3;
    p.granulepos = 999;
    ss.packetin(p);

    Page og = new Page();
    int ret = ss.pageOut(og);

    assertEquals(1, ret);
  }

  @Test
  void testResetClearsState() {
    ss.bodyFill = 100;
    ss.lacingFill = 50;
    ss.packetNo = 999;
    ss.granulepos = 777;
    ss.e_o_s = 1;
    ss.b_o_s = 1;

    ss.reset();

    assertEquals(0, ss.bodyFill);
    assertEquals(0, ss.lacingFill);
    assertEquals(0, ss.packetNo);
    assertEquals(0, ss.granulepos);
    assertEquals(0, ss.e_o_s);
    assertEquals(0, ss.b_o_s);
    assertEquals(-1, ss.pageno);
  }
}
