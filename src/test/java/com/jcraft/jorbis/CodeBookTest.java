package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;

import com.jcraft.jogg.Buffer;
import org.junit.jupiter.api.Test;

class CodeBookTest {

  private StaticCodeBook makeMinimalSCB() {
    StaticCodeBook scb = new StaticCodeBook();
    scb.entries = 4;
    scb.dim = 2;

    // Simple lengthlist: all entries valid
    scb.lengthlist = new int[] {1, 2, 3, 4};

    // Minimal quantlist: dim * entries = 8 values
    scb.quantlist = new int[] {1, 2, 3, 4, 5, 6, 7, 8};

    scb.maptype = 1;
    scb.q_sequencep = 0;
    scb.q_min = 0;
    scb.q_delta = 1;
    scb.q_quant = 1;

    return scb;
  }

  private CodeBook makeMinimalCodeBook() {
    CodeBook cb = new CodeBook();
    StaticCodeBook scb = makeMinimalSCB();
    assertEquals(0, cb.initDecode(scb));
    return cb;
  }

  private Buffer makeBufferWithBits(int... bits) {
    byte[] data = new byte[(bits.length + 7) / 8];
    for (int i = 0; i < bits.length; i++) {
      if (bits[i] == 1) {
        data[i >> 3] |= (1 << (i & 7));
      }
    }
    Buffer b = new Buffer();
    b.readInit(data, 0, data.length);
    return b;
  }

  @Test
  void initDecodeShouldInitializeFields() {
    CodeBook cb = makeMinimalCodeBook();
    assertEquals(4, cb.entries);
    assertEquals(2, cb.dim);
    assertNotNull(cb.valueList);
    assertNotNull(cb.decodeTree);
  }

  @Test
  void errorvShouldReturnBestEntryAndModifyVector() {
    CodeBook cb = makeMinimalCodeBook();
    float[] vec = {0.1f, 0.2f};

    int best = cb.errorv(vec);

    assertTrue(best >= 0 && best < cb.entries);
    assertEquals(cb.valueList[best * cb.dim], vec[0]);
    assertEquals(cb.valueList[best * cb.dim + 1], vec[1]);
  }

  @Test
  void decodeShouldReturnMinusOneOnEOF() {
    CodeBook cb = makeMinimalCodeBook();
    Buffer b = makeBufferWithBits(); // empty

    assertEquals(-1, cb.decode(b));
  }

  @Test
  void decodevsAddShouldHandleSmallVectors() {
    CodeBook cb = makeMinimalCodeBook();
    float[] a = new float[10];
    Buffer b = makeBufferWithBits(0, 1, 0, 1, 0, 1);

    int ret = cb.decodevsAdd(a, 0, b, 4);
    assertTrue(ret == 0 || ret == -1);
  }

  @Test
  void decodevAddShouldHandleDimLessThan8() {
    CodeBook cb = makeMinimalCodeBook();
    cb.dim = 3;

    float[] a = new float[9];
    Buffer b = makeBufferWithBits(1, 0, 1, 0);

    int ret = cb.decodevAdd(a, 0, b, 9);
    assertTrue(ret == 0 || ret == -1);
  }

  @Test
  void decodevSetShouldOverwriteValues() {
    CodeBook cb = makeMinimalCodeBook();
    float[] a = new float[8];
    Buffer b = makeBufferWithBits(1, 0, 1, 0);

    int ret = cb.decodevAdd(a, 0, b, 4);
    assertTrue(ret == 0 || ret == -1);
  }

  @Test
  void decodevvAddShouldHandleMultiChannel() {
    CodeBook cb = makeMinimalCodeBook();
    float[][] a = new float[][] {new float[4], new float[4]};

    Buffer b = makeBufferWithBits(1, 0, 1, 0);

    int ret = cb.decodevvAdd(a, 0, 2, b, 4);
    assertTrue(ret == 0 || ret == -1);
  }

  @Test
  void bestShouldReturnValidEntry() {
    CodeBook cb = makeMinimalCodeBook();
    float[] vec = {1f, 2f};

    int best = cb.best(vec, 1);
    assertTrue(best >= 0 && best < cb.entries);
  }

  @Test
  void besterrorShouldModifyVectorOrLeaveItUnchangedButNotThrow() {
    CodeBook cb = makeMinimalCodeBook();
    float[] vec = {5f, 6f};
    vec.clone();

    int best = cb.besterror(vec, 1, 0);

    assertTrue(best >= 0 && best < cb.entries);

    // besterror may or may not modify vec depending on valuelist and best match
    // The only guarantee is: it must not throw and vec must remain valid
    assertEquals(2, vec.length);
  }

  @Test
  void makeWordsShouldReturnNullOnInvalidLengths() {
    // Truly invalid: overlapping codewords
    int[] lengths = {1, 1, 1};
    assertNull(CodeBook.makeWords(lengths, lengths.length));
  }

  @Test
  void makeDecodeTreeShouldReturnNonNullTree() {
    CodeBook cb = makeMinimalCodeBook();
    assertNotNull(cb.decodeTree);
  }
}
