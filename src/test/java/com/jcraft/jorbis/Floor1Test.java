package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;

import com.jcraft.jogg.Buffer;
import org.junit.jupiter.api.Test;

class Floor1Test {

  private final Floor1 floor = new Floor1();

  private DspState newDspState() {
    DspState vd = new DspState();
    vd.vi = new Info();
    vd.vi.setBlocksizes(512, 1024);
    vd.analysisp = 1;
    vd.fullbooks = new CodeBook[256];
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
  void testUnpackRejectsInvalidClassBookIndex() {
    Buffer buf = newWriteBuffer();

    buf.write(1, 5); // partitions = 1
    buf.write(0, 4); // class 0

    buf.write(0, 3); // class_dim = 1
    buf.write(1, 2); // class_subs = 1 → class_book required
    buf.write(99, 8); // invalid book index

    buf.write(1, 8); // class_subbook entry

    Info vi = new Info();
    vi.setBooks(16);

    buf.readInit(buf.buffer(), buf.bytes());
    assertNull(floor.unpack(vi, buf));
  }

  @Test
  void testUnpackRejectsInvalidClassSubbookIndex() {
    Buffer buf = newWriteBuffer();

    buf.write(1, 5); // partitions = 1
    buf.write(0, 4); // class 0

    buf.write(0, 3); // class_dim = 1
    buf.write(1, 2); // class_subs = 1
    buf.write(3, 8); // class_book valid

    buf.write(255, 8); // class_subbook = 254 → invalid

    Info vi = new Info();
    vi.setBooks(16);

    buf.readInit(buf.buffer(), buf.bytes());
    assertNull(floor.unpack(vi, buf));
  }

  @Test
  void testPackUnpackRoundTrip() {
    InfoFloor1 info = new InfoFloor1();
    info.partitions = 1;
    info.partitionclass[0] = 0;

    info.class_dim[0] = 2;
    info.class_subs[0] = 1;
    info.class_book[0] = 3;
    info.class_subbook[0][0] = 1;
    info.class_subbook[0][1] = 2;

    info.mult = 2;
    info.postlist[0] = 0;
    info.postlist[1] = 16;
    info.postlist[2] = 4;
    info.postlist[3] = 12;

    Buffer buf = newWriteBuffer();
    floor.pack(info, buf);

    buf.readInit(buf.buffer(), buf.bytes());

    Info vi = new Info();
    vi.setBooks(256);

    InfoFloor1 unpacked = (InfoFloor1) floor.unpack(vi, buf);
    assertNotNull(unpacked);

    assertEquals(info.partitions, unpacked.partitions);
    assertEquals(info.partitionclass[0], unpacked.partitionclass[0]);
    assertEquals(info.class_dim[0], unpacked.class_dim[0]);
    assertEquals(info.class_subs[0], unpacked.class_subs[0]);
    assertEquals(info.class_book[0], unpacked.class_book[0]);
    assertEquals(info.mult, unpacked.mult);
    assertEquals(info.postlist[2], unpacked.postlist[2]);
    assertEquals(info.postlist[3], unpacked.postlist[3]);
  }

  @Test
  void testLookComputesSortedIndicesCorrectly() {
    InfoFloor1 info = new InfoFloor1();
    info.partitions = 1;
    info.partitionclass[0] = 0;
    info.class_dim[0] = 2;
    info.class_subs[0] = 0;
    info.mult = 2;

    info.postlist[0] = 0;
    info.postlist[1] = 16;
    info.postlist[2] = 4;
    info.postlist[3] = 12;

    DspState vd = newDspState();
    InfoMode mode = new InfoMode();

    LookFloor1 look = (LookFloor1) floor.look(vd, mode, info);
    assertNotNull(look);

    int[] expected = new int[] {0, 4, 12, 16};
    for (int i = 0; i < look.posts; i++) {
      assertEquals(expected[i], look.sorted_index[i]);
    }
  }

  @Test
  void testInverse1DecodesSuccessfully() {
    DspState vd = newDspState();
    Block vb = newBlock(vd);

    // Flag bit = 1 -> floor data present
    vb.opb.write(1, 1);

    // First two fit values
    vb.opb.write(200, 8);
    vb.opb.write(200, 8);

    vb.opb.readInit(vb.opb.buffer(), vb.opb.bytes());

    InfoFloor1 info = new InfoFloor1();
    info.postlist[0] = 0;
    info.postlist[1] = 16;
    info.postlist[2] = 4;
    info.postlist[3] = 12;

    info.partitions = 1;
    info.partitionclass[0] = 0;
    info.class_dim[0] = 2;
    info.class_subs[0] = 0;
    info.class_subbook[0][0] = -1;

    LookFloor1 look = new LookFloor1();
    look.vi = info;
    look.posts = 4;
    look.quant_q = 256;

    look.loneighbor[0] = 0;
    look.hineighbor[0] = 1;
    look.loneighbor[1] = 0;
    look.hineighbor[1] = 2;

    Object result = floor.inverse1(vb, look, null);
    assertNotNull(result);
    assertInstanceOf(int[].class, result);
  }

  @Test
  void testInverse2ProducesCurveWhenMemoPresent() {
    DspState vd = newDspState();
    Block vb = newBlock(vd);
    vb.mode = 0;

    InfoFloor1 info = new InfoFloor1();
    info.mult = 2;
    info.postlist[0] = 0;
    info.postlist[1] = 16;
    info.postlist[2] = 4;
    info.postlist[3] = 12;

    LookFloor1 look = new LookFloor1();
    look.vi = info;
    look.posts = 4;
    look.forward_index[0] = 0;
    look.forward_index[1] = 1;
    look.forward_index[2] = 2;
    look.forward_index[3] = 3;

    int[] memo = new int[] {10, 20, 30, 40};
    float[] out = new float[256];

    // initialize to non-zero so renderLine has effect
    for (int i = 0; i < out.length; i++) out[i] = 1.0f;

    int result = floor.inverse2(vb, look, memo, out);
    assertEquals(1, result);

    assertTrue(out[0] != 1.0f);
  }
}
