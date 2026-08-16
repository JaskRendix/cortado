package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;

import com.jcraft.jogg.Buffer;
import org.junit.jupiter.api.Test;

class Residue0Test {

  private final Residue0 residue = new Residue0();

  private DspState newDspState() {
    DspState vd = new DspState();
    vd.vi = new Info();
    vd.vi.setBlocksizes(512, 1024);
    vd.analysisp = 0;
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
  void testUnpackRejectsInvalidGroupBook() {
    Buffer buf = newWriteBuffer();

    buf.write(0, 24); // begin
    buf.write(100, 24); // end
    buf.write(3, 24); // grouping (grouping - 1 = 3 -> grouping = 4)
    buf.write(1, 6); // partitions (partitions - 1 = 1 -> partitions = 2)
    buf.write(99, 8); // groupbook index = 99 (invalid)

    // Partition 0 and 1 configuration
    buf.write(1, 3); // cascade = 1
    buf.write(0, 1); // extension bit = 0
    buf.write(1, 3); // cascade = 1
    buf.write(0, 1); // extension bit = 0

    buf.write(5, 8); // booklist entry for part 0
    buf.write(6, 8); // booklist entry for part 1

    Info vi = new Info();
    vi.setBooks(16); // Max books allowed is 16

    buf.readInit(buf.buffer(), buf.bytes());
    Object result = residue.unpack(vi, buf);
    assertNull(result);
  }

  @Test
  void testUnpackRejectsInvalidBooklistIndex() {
    Buffer buf = newWriteBuffer();

    buf.write(0, 24); // begin
    buf.write(100, 24); // end
    buf.write(3, 24); // grouping
    buf.write(1, 6); // partitions
    buf.write(2, 8); // groupbook valid (= 2)

    buf.write(1, 3); // cascade = 1
    buf.write(0, 1); // extension bit = 0
    buf.write(1, 3); // cascade = 1
    buf.write(0, 1); // extension bit = 0

    buf.write(5, 8); // booklist entry 0 valid
    buf.write(99, 8); // booklist entry 1 invalid (>= vi.books)

    Info vi = new Info();
    vi.setBooks(16);

    buf.readInit(buf.buffer(), buf.bytes());
    Object result = residue.unpack(vi, buf);
    assertNull(result);
  }

  @Test
  void testPackUnpackRoundTrip() {
    InfoResidue0 info = new InfoResidue0();
    info.begin = 10;
    info.end = 110;
    info.grouping = 8;
    info.partitions = 2;
    info.groupbook = 1;
    info.secondstages[0] = 1;
    info.secondstages[1] = 2;
    info.booklist[0] = 3;
    info.booklist[1] = 4;

    Buffer buf = newWriteBuffer();
    residue.pack(info, buf);

    buf.readInit(buf.buffer(), buf.bytes());

    Info vi = new Info();
    vi.setBooks(10);

    InfoResidue0 unpacked = (InfoResidue0) residue.unpack(vi, buf);
    assertNotNull(unpacked);
    assertEquals(info.begin, unpacked.begin);
    assertEquals(info.end, unpacked.end);
    assertEquals(info.grouping, unpacked.grouping);
    assertEquals(info.partitions, unpacked.partitions);
    assertEquals(info.groupbook, unpacked.groupbook);
    assertEquals(info.secondstages[0], unpacked.secondstages[0]);
    assertEquals(info.secondstages[1], unpacked.secondstages[1]);
    assertEquals(info.booklist[0], unpacked.booklist[0]);
    assertEquals(info.booklist[1], unpacked.booklist[1]);
  }

  @Test
  void testLookInitializesStructuresCorrectly() {
    DspState vd = newDspState();
    InfoMode mode = new InfoMode();
    mode.setMapping(0);

    InfoResidue0 info = new InfoResidue0();
    info.begin = 0;
    info.end = 64;
    info.grouping = 8;
    info.partitions = 2;
    info.groupbook = 0;
    info.secondstages[0] = 1;
    info.secondstages[1] = 2;
    info.booklist[0] = 1;
    info.booklist[1] = 2;

    CodeBook phrasebook = new CodeBook();
    phrasebook.dim = 2;
    vd.fullbooks[0] = phrasebook;

    Object lookObj = residue.look(vd, mode, info);
    assertNotNull(lookObj);
    assertInstanceOf(LookResidue0.class, lookObj);

    LookResidue0 look = (LookResidue0) lookObj;
    assertEquals(2, look.parts);
    assertEquals(2, look.stages);
    assertEquals(phrasebook, look.phrasebook);
    assertNotNull(look.decodemap);
  }

  @Test
  void testInverseWithZeroNonzeroChannelsReturnsZero() {
    DspState vd = newDspState();
    Block vb = newBlock(vd);

    InfoResidue0 info = new InfoResidue0();
    LookResidue0 look = new LookResidue0();
    look.info = info;

    float[][] in = new float[2][100];
    int[] nonzero = new int[] {0, 0};

    int result = residue.inverse(vb, look, in, nonzero, 2);
    assertEquals(0, result);
  }

  @Test
  void testInverseTerminatesOnInvalidPhrasebookDecode() {
    DspState vd = newDspState();
    Block vb = newBlock(vd);

    // Write data that forces a read/decode failure (-1)
    vb.opb.write(0, 1);
    vb.opb.readInit(vb.opb.buffer(), vb.opb.bytes());

    InfoResidue0 info = new InfoResidue0();
    info.begin = 0;
    info.end = 16;
    info.grouping = 4;
    info.partitions = 1;
    info.groupbook = 0;
    info.secondstages[0] = 0; // 0 stages bypasses stagebook decodes
    info.booklist[0] = 0;

    CodeBook phrasebook = new CodeBook();
    phrasebook.dim = 2;
    vd.fullbooks[0] = phrasebook;

    LookResidue0 look = new LookResidue0();
    look.info = info;
    look.phrasebook = phrasebook;
    look.stages = 0;
    look.parts = 1;
    look.fullbooks = vd.fullbooks;
    look.decodemap = new int[][] {{0}};

    float[][] in = new float[1][16];
    int[] nonzero = new int[] {1};

    int result = residue.inverse(vb, look, in, nonzero, 1);
    assertEquals(0, result);
  }
}
