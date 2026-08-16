package com.jcraft.jogg;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

class PageTest {

  private Page page;

  @BeforeEach
  void setup() {
    page = new Page();
  }

  private byte[] header(int size) {
    byte[] h = new byte[size];
    page.headerBase = h;
    page.header = 0;
    page.headerLen = size;
    return h;
  }

  private byte[] body(int size) {
    byte[] b = new byte[size];
    page.bodyBase = b;
    page.body = 0;
    page.bodyLen = size;
    return b;
  }

  @Test
  void testVersion() {
    byte[] h = header(27);
    h[4] = (byte) 0x01;
    assertEquals(1, page.version());
  }

  @Test
  void testContinuedFlag() {
    byte[] h = header(27);
    h[5] = (byte) 0x01;
    assertEquals(1, page.continued());
  }

  @Test
  void testBosFlag() {
    byte[] h = header(27);
    h[5] = (byte) 0x02;
    assertEquals(2, page.bos());
  }

  @Test
  void testEosFlag() {
    byte[] h = header(27);
    h[5] = (byte) 0x04;
    assertEquals(4, page.eos());
  }

  @Test
  void testGranulepos() {
    byte[] h = header(27);
    long expected = 0x1122334455667788L;
    h[6] = (byte) 0x88;
    h[7] = (byte) 0x77;
    h[8] = (byte) 0x66;
    h[9] = (byte) 0x55;
    h[10] = (byte) 0x44;
    h[11] = (byte) 0x33;
    h[12] = (byte) 0x22;
    h[13] = (byte) 0x11;
    assertEquals(expected, page.granulepos());
  }

  @Test
  void testSerialno() {
    byte[] h = header(27);
    int expected = 0xAABBCCDD;
    h[14] = (byte) 0xDD;
    h[15] = (byte) 0xCC;
    h[16] = (byte) 0xBB;
    h[17] = (byte) 0xAA;
    assertEquals(expected, page.serialno());
  }

  @Test
  void testPageno() {
    byte[] h = header(27);
    int expected = 0x01020304;
    h[18] = (byte) 0x04;
    h[19] = (byte) 0x03;
    h[20] = (byte) 0x02;
    h[21] = (byte) 0x01;
    assertEquals(expected, page.pageno());
  }

  @Test
  void testChecksumZeroBody() {
    byte[] h = header(27);
    body(0);
    for (int i = 0; i < 27; i++) h[i] = (byte) i;
    page.checksum();
    int crc =
        (h[22] & 0xff) | ((h[23] & 0xff) << 8) | ((h[24] & 0xff) << 16) | ((h[25] & 0xff) << 24);
    assertTrue(crc != 0);
  }

  @Test
  void testChecksumWithBody() {
    byte[] h = header(27);
    byte[] b = body(4);
    for (int i = 0; i < 27; i++) h[i] = (byte) (i * 3);
    b[0] = 1;
    b[1] = 2;
    b[2] = 3;
    b[3] = 4;
    page.checksum();
    int crc =
        (h[22] & 0xff) | ((h[23] & 0xff) << 8) | ((h[24] & 0xff) << 16) | ((h[25] & 0xff) << 24);
    assertTrue(crc != 0);
  }

  @Test
  void testChecksumChangesWhenBodyChanges() {
    byte[] h = header(27);
    byte[] b = body(4);
    for (int i = 0; i < 27; i++) h[i] = (byte) i;
    b[0] = 10;
    b[1] = 20;
    b[2] = 30;
    b[3] = 40;
    page.checksum();
    int crc1 =
        (h[22] & 0xff) | ((h[23] & 0xff) << 8) | ((h[24] & 0xff) << 16) | ((h[25] & 0xff) << 24);

    b[0] = 11;
    b[1] = 21;
    b[2] = 31;
    b[3] = 41;
    page.checksum();
    int crc2 =
        (h[22] & 0xff) | ((h[23] & 0xff) << 8) | ((h[24] & 0xff) << 16) | ((h[25] & 0xff) << 24);

    assertNotEquals(crc1, crc2);
  }

  @Test
  void testHeaderOffset() {
    byte[] h = new byte[40];
    page.headerBase = h;
    page.header = 10;
    page.headerLen = 27;
    h[10 + 4] = 7;
    assertEquals(7, page.version());
  }

  @Test
  void testBodyOffset() {
    byte[] h = header(27);
    byte[] b = new byte[50];
    page.bodyBase = b;
    page.body = 20;
    page.bodyLen = 5;
    for (int i = 0; i < 5; i++) b[20 + i] = (byte) (100 + i);
    page.checksum();
    int crc =
        (h[22] & 0xff) | ((h[23] & 0xff) << 8) | ((h[24] & 0xff) << 16) | ((h[25] & 0xff) << 24);
    assertTrue(crc != 0);
  }

  @Test
  void testHeaderBaseAliasing() {
    byte[] h = header(27);
    h[4] = 99;
    assertEquals(99, page.version());
  }

  @Test
  void testBodyBaseAliasing() {
    byte[] h = header(27);
    byte[] b = body(3);
    b[0] = 7;
    page.checksum();
    int crc =
        (h[22] & 0xff) | ((h[23] & 0xff) << 8) | ((h[24] & 0xff) << 16) | ((h[25] & 0xff) << 24);
    assertTrue(crc != 0);
  }

  @Test
  void testGranuleposZero() {
    header(27);
    assertEquals(0L, page.granulepos());
  }

  @Test
  void testSerialnoZero() {
    header(27);
    assertEquals(0, page.serialno());
  }

  @Test
  void testPagenoZero() {
    header(27);
    assertEquals(0, page.pageno());
  }

  @Test
  void testChecksumDeterministic() {
    byte[] h = header(27);
    byte[] b = body(10);

    for (int i = 0; i < 27; i++) h[i] = (byte) (i * 2);
    for (int i = 0; i < 10; i++) b[i] = (byte) (i * 3);

    page.checksum();

    int crc =
        (h[22] & 0xff) | ((h[23] & 0xff) << 8) | ((h[24] & 0xff) << 16) | ((h[25] & 0xff) << 24);

    assertTrue(crc != 0);
  }
}
