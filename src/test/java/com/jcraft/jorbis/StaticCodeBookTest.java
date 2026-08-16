package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;

import com.jcraft.jogg.Buffer;
import org.junit.jupiter.api.Test;

class StaticCodeBookTest {

  private Buffer writeBuffer() {
    Buffer b = new Buffer();
    b.writeInit();
    return b;
  }

  private Buffer readBuffer(Buffer src) {
    Buffer r = new Buffer();
    r.readInit(src.buffer(), src.bytes());
    return r;
  }

  @Test
  void emptyConstructorShouldInitializeFields() {
    StaticCodeBook cb = new StaticCodeBook();
    assertEquals(0, cb.dim);
    assertEquals(0, cb.entries);
    assertNull(cb.lengthlist);
    assertEquals(0, cb.maptype);
    assertNull(cb.quantlist);
  }

  @Test
  void packUnpackMaptype0() {
    int[] lengths = {2, 2, 2, 2};
    StaticCodeBook cb = new StaticCodeBook(2, 4, lengths, 0, 0, 0, 0, 0, lengths, null, null);

    Buffer b = writeBuffer();
    assertEquals(0, cb.pack(b));

    Buffer r = readBuffer(b);
    StaticCodeBook out = new StaticCodeBook();
    assertEquals(0, out.unpack(r));

    assertEquals(2, out.dim);
    assertEquals(4, out.entries);
    assertArrayEquals(lengths, out.lengthlist);
    assertEquals(0, out.maptype);
    assertNull(out.quantlist);
  }

  @Test
  void packUnpackUnorderedWithUnused() {
    int[] lengths = {3, 0, 4, 0, 2};
    StaticCodeBook cb = new StaticCodeBook(2, 5, lengths, 0, 0, 0, 0, 0, lengths, null, null);

    Buffer b = writeBuffer();
    assertEquals(0, cb.pack(b));

    Buffer r = readBuffer(b);
    StaticCodeBook out = new StaticCodeBook();
    assertEquals(0, out.unpack(r));

    assertArrayEquals(lengths, out.lengthlist);
  }

  @Test
  void packUnpackOrdered() {
    int[] lengths = {2, 2, 3, 3, 3, 4};
    StaticCodeBook cb = new StaticCodeBook(2, 6, lengths, 0, 0, 0, 0, 0, lengths, null, null);

    Buffer b = writeBuffer();
    assertEquals(0, cb.pack(b));

    Buffer r = readBuffer(b);
    StaticCodeBook out = new StaticCodeBook();
    assertEquals(0, out.unpack(r));

    assertArrayEquals(lengths, out.lengthlist);
  }

  @Test
  void packUnpackMaptype1() {
    int[] lengths = {2, 2, 2, 2};
    int[] quantlist = {1, 2};
    StaticCodeBook cb = new StaticCodeBook(2, 4, lengths, 1, 10, 5, 4, 1, quantlist, null, null);

    Buffer b = writeBuffer();
    assertEquals(0, cb.pack(b));

    Buffer r = readBuffer(b);
    StaticCodeBook out = new StaticCodeBook();
    assertEquals(0, out.unpack(r));

    assertEquals(1, out.maptype);
    assertEquals(10, out.q_min);
    assertEquals(5, out.q_delta);
    assertEquals(4, out.q_quant);
    assertEquals(1, out.q_sequencep);
    assertNotNull(out.quantlist);
  }

  @Test
  void packUnpackMaptype2() {
    int[] lengths = {2, 2, 2};
    int[] quantlist = {1, 2, 3, 4, 5, 6};
    StaticCodeBook cb = new StaticCodeBook(2, 3, lengths, 2, 20, 7, 4, 0, quantlist, null, null);

    Buffer b = writeBuffer();
    assertEquals(0, cb.pack(b));

    Buffer r = readBuffer(b);
    StaticCodeBook out = new StaticCodeBook();
    assertEquals(0, out.unpack(r));

    assertEquals(2, out.maptype);
    assertArrayEquals(quantlist, out.quantlist);
  }

  @Test
  void unquantizeMaptype1() {
    int[] lengths = {2, 2, 2, 2};
    int[] quantlist = {1, 2};
    StaticCodeBook cb = new StaticCodeBook(2, 4, lengths, 1, 10, 5, 4, 1, quantlist, null, null);

    float[] vals = cb.unquantize();
    assertNotNull(vals);
    assertEquals(8, vals.length);

    for (float v : vals) {
      assertFalse(Float.isNaN(v));
      assertFalse(Float.isInfinite(v));
    }
  }

  @Test
  void unquantizeMaptype2() {
    int[] lengths = {2, 2, 2};
    int[] quantlist = {1, 2, 3, 4, 5, 6};
    StaticCodeBook cb = new StaticCodeBook(2, 3, lengths, 2, 20, 7, 4, 0, quantlist, null, null);

    float[] vals = cb.unquantize();
    assertNotNull(vals);
    assertEquals(6, vals.length);
  }

  @Test
  void float32PackUnpackShouldBehaveDeterministically() {
    float[] testVals = {0.1f, 1f, 2f, 10f, -5f};

    for (float v : testVals) {
      long packed1 = StaticCodeBook.float32_pack(v);
      long packed2 = StaticCodeBook.float32_pack(v);
      assertEquals(packed1, packed2);

      float unpacked1 = StaticCodeBook.float32_unpack((int) packed1);
      float unpacked2 = StaticCodeBook.float32_unpack((int) packed2);
      assertEquals(unpacked1, unpacked2);
    }
  }
}
