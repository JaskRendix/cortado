package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;

import com.jcraft.jogg.Buffer;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class Floor0Test {

  private final Floor0 floor = new Floor0();

  /** Reflection: get private byte[] buffer */
  private byte[] getBufferBytes(Buffer b) throws Exception {
    Field f = Buffer.class.getDeclaredField("buffer");
    f.setAccessible(true);
    return (byte[]) f.get(b);
  }

  /** Reflection: get private endbyte (actual bytes written) */
  private int getEndByte(Buffer b) throws Exception {
    Field f = Buffer.class.getDeclaredField("endbyte");
    f.setAccessible(true);
    return (int) f.get(b);
  }

  /** Flush bit accumulator manually */
  private void flushBits(Buffer b) throws Exception {
    Field fEndBit = Buffer.class.getDeclaredField("endbit");
    Field fEndByte = Buffer.class.getDeclaredField("endbyte");

    fEndBit.setAccessible(true);
    fEndByte.setAccessible(true);

    int endbit = (int) fEndBit.get(b);
    int endbyte = (int) fEndByte.get(b);

    if (endbit > 0) {
      endbyte++;
      fEndByte.set(b, endbyte);
    }
  }

  private DspState newDspState() {
    DspState vd = new DspState();
    vd.vi = new Info();
    vd.vi.setBlocksizes(512, 1024);
    vd.analysisp = 1;
    vd.fullbooks = new CodeBook[16];
    return vd;
  }

  private Block newBlock(DspState vd) {
    Block vb = new Block(vd);
    vb.opb.writeInit();
    return vb;
  }

  private Buffer newWriteBuffer() {
    Buffer b = new Buffer();
    b.writeInit();
    return b;
  }

  @Test
  void testUnpackRejectsZeroOrNegativeFields() {
    Buffer buf = newWriteBuffer();
    buf.write(0, 8);
    buf.write(0, 16);
    buf.write(0, 16);
    buf.write(0, 6);
    buf.write(0, 8);
    buf.write(0, 4);

    Info vi = new Info();
    vi.setBooks(10);

    assertNull(floor.unpack(vi, buf));
  }

  @Test
  void testPackAndUnpackRoundTrip() throws Exception {
    InfoFloor0 info = new InfoFloor0();
    info.order = 8;
    info.rate = 44100;
    info.barkmap = 32;
    info.ampbits = 6;
    info.ampdB = 40;
    info.numbooks = 2;
    info.books[0] = 3;
    info.books[1] = 7;

    Buffer buf = new Buffer();
    buf.writeInit();

    floor.pack(info, buf);
    flushBits(buf);

    byte[] raw = getBufferBytes(buf);
    int endbyte = getEndByte(buf);

    Buffer reader = new Buffer();
    reader.readInit(raw, 0, endbyte);

    Info vi = new Info();
    vi.setBooks(16);

    InfoFloor0 unpacked = (InfoFloor0) floor.unpack(vi, reader);
    assertNotNull(unpacked);
    assertEquals(info.order, unpacked.order);
    assertEquals(info.rate, unpacked.rate);
    assertEquals(info.barkmap, unpacked.barkmap);
    assertEquals(info.ampbits, unpacked.ampbits);
    assertEquals(info.ampdB, unpacked.ampdB);
    assertEquals(info.numbooks, unpacked.numbooks);
    assertEquals(info.books[0], unpacked.books[0]);
    assertEquals(info.books[1], unpacked.books[1]);
  }

  @Test
  void testLookCreatesLinearMapWithinBounds() {
    InfoFloor0 info = new InfoFloor0();
    info.order = 8;
    info.rate = 48000;
    info.barkmap = 32;

    DspState vd = newDspState();
    InfoMode mode = new InfoMode();
    mode.setBlockflag(1);

    LookFloor0 look = (LookFloor0) floor.look(vd, mode, info);
    assertNotNull(look);
    assertEquals(512, look.n);
    assertEquals(32, look.ln);

    for (int v : look.linearmap) {
      assertTrue(v >= 0 && v <= 32);
    }
  }

  @Test
  void testInverse1ReturnsNullWhenAmpRawZero() {
    DspState vd = newDspState();
    Block vb = newBlock(vd);

    vb.opb.write(0, 6);

    InfoFloor0 info = new InfoFloor0();
    info.ampbits = 6;
    info.numbooks = 1;

    LookFloor0 look = new LookFloor0();
    look.vi = info;
    look.m = 8;

    assertNull(floor.inverse1(vb, look, null));
  }

  @Test
  void testInverse1RejectsInvalidBookNumber() {
    DspState vd = newDspState();
    Block vb = newBlock(vd);

    vb.opb.write(10, 6);
    vb.opb.write(99, 4);

    InfoFloor0 info = new InfoFloor0();
    info.ampbits = 6;
    info.numbooks = 2;

    LookFloor0 look = new LookFloor0();
    look.vi = info;
    look.m = 8;

    assertNull(floor.inverse1(vb, look, null));
  }

  @Test
  void testInverse1FailsWhenCodebookDecodeFails() {
    DspState vd = newDspState();
    Block vb = newBlock(vd);

    vb.opb.write(10, 6);
    vb.opb.write(0, 4);

    InfoFloor0 info = new InfoFloor0();
    info.ampbits = 6;
    info.numbooks = 1;
    info.books[0] = 0;

    CodeBook failingBook = new CodeBook();
    failingBook.dim = 4;
    failingBook.entries = 0;

    vd.fullbooks[0] = failingBook;

    LookFloor0 look = new LookFloor0();
    look.vi = info;
    look.m = 8;

    assertNull(floor.inverse1(vb, look, null));
  }

  @Test
  void testInverse2WritesZeroCurveWhenMemoNull() {
    LookFloor0 look = new LookFloor0();
    look.n = 16;

    float[] out = new float[16];
    int result = floor.inverse2(null, look, null, out);

    assertEquals(0, result);
    for (float v : out) {
      assertEquals(0f, v);
    }
  }

  @Test
  void testInverse2ProducesCurveWhenMemoPresent() {
    LookFloor0 look = new LookFloor0();
    look.n = 8;
    look.ln = 4;
    look.m = 3;
    look.linearmap = new int[] {0, 1, 2, 3, 3, 2, 1, 0};

    InfoFloor0 info = new InfoFloor0();
    info.ampdB = 40;
    look.vi = info;

    float[] memo = new float[] {1f, 2f, 3f, 10f};
    float[] out = new float[8];

    int result = floor.inverse2(null, look, memo, out);

    assertEquals(1, result);
    for (float v : out) {
      assertTrue(v >= 0f);
    }
  }
}
