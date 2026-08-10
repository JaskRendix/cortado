package com.jcraft.jorbis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Residue2Test {

    private final Residue2 residue = new Residue2();

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
        vb.opb.writeinit();
        return vb;
    }

    @Test
    void testInverseWithAllNonzeroChannelsZeroReturnsZero() {
        DspState vd = newDspState();
        Block vb = newBlock(vd);

        InfoResidue0 info = new InfoResidue0();
        LookResidue0 look = new LookResidue0();
        look.info = info;

        float[][] in = new float[2][16];
        int[] nonzero = new int[]{0, 0};

        int result = residue.inverse(vb, look, in, nonzero, 2);
        assertEquals(0, result);
    }

    @Test
    void testInverseTerminatesOnInvalidPhrasebookDecode() {
        DspState vd = newDspState();
        Block vb = newBlock(vd);

        vb.opb.write(0, 1);
        vb.opb.readinit(vb.opb.buffer(), vb.opb.bytes());

        InfoResidue0 info = new InfoResidue0();
        info.begin = 0;
        info.end = 16;
        info.grouping = 4;
        info.partitions = 1;
        info.groupbook = 0;
        info.secondstages[0] = 0;
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
        look.decodemap = new int[][]{{0}};

        float[][] in = new float[2][16];
        int[] nonzero = new int[]{0, 1};

        int result = residue.inverse(vb, look, in, nonzero, 2);
        assertEquals(0, result);
    }

    @Test
    void testForwardCallsNotImplemented() {
        DspState vd = newDspState();
        Block vb = newBlock(vd);
        float[][] in = new float[2][16];

        int result = residue.forward(vb, null, in, 2);
        assertEquals(0, result);
    }
}
