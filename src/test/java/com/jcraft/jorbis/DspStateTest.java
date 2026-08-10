package com.jcraft.jorbis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DspStateTest {

    private Info makeValidInfo() {
        Info vi = new Info();
        vi.setChannels(2);
        vi.setRate(44100);

        vi.setBlocksizes(64, 128);
        vi.setModes(1);
        vi.setBooks(1);

        // Minimal mode
        InfoMode mode = new InfoMode();
        mode.setBlockflag(0);
        mode.setMapping(0);
        vi.setModeParam(new InfoMode[]{mode});

        // Proper InfoMapping0 instead of raw Mapping0
        InfoMapping0 map = new InfoMapping0();
        vi.setMapType(new int[]{0});
        vi.setMapParam(new Object[]{map});

        // Minimal codebook
        StaticCodeBook scb = new StaticCodeBook();
        scb.entries = 1;
        scb.dim = 1;
        scb.lengthlist = new int[]{1};
        scb.quantlist = new int[]{1};
        scb.maptype = 1;
        scb.q_sequencep = 0;
        scb.q_min = 0;
        scb.q_delta = 1;
        scb.q_quant = 1;
        vi.setBookParam(new StaticCodeBook[]{scb});

        return vi;
    }

    @Test
    void initShouldInitializePCMStorageAndTransforms() {
        Info vi = makeValidInfo();
        DspState ds = new DspState();

        assertEquals(0, ds.init(vi, false));

        assertNotNull(ds.pcm);
        assertEquals(2, ds.pcm.length);
        assertEquals(8192, ds.pcm[0].length);

        assertNotNull(ds.transform[0][0]);
        assertNotNull(ds.transform[1][0]);
    }

    @Test
    void windowShouldGenerateCorrectLength() {
        float[] w = DspState.window(0, 64, 32, 32);
        assertNotNull(w);
        assertEquals(64, w.length);
    }

    @Test
    void windowShouldReturnNullForInvalidType() {
        assertNull(DspState.window(99, 64, 32, 32));
    }

    @Test
    void synthesisInitShouldSetGranuleposAndSequence() {
        Info vi = makeValidInfo();
        DspState ds = new DspState();

        ds.synthesis_init(vi);

        assertEquals(-1, ds.granulepos);
        assertEquals(-1, ds.sequence);
    }

    @Test
    void synthesisPCMOutShouldReturnZeroWhenNoData() {
        Info vi = makeValidInfo();
        DspState ds = new DspState();
        ds.synthesis_init(vi);

        float[][][] pcm = new float[1][][];
        int[] index = new int[vi.getChannels()];

        assertEquals(0, ds.synthesis_pcmout(pcm, index));
    }

    @Test
    void synthesisReadShouldRejectReadsPastCenterW() {
        Info vi = makeValidInfo();
        DspState ds = new DspState();
        ds.synthesis_init(vi);

        assertEquals(-1, ds.synthesis_read(10));
    }

    @Test
    void synthesisBlockInShouldUpdateSequenceAndGranulepos() {
        Info vi = makeValidInfo();
        DspState ds = new DspState();
        ds.synthesis_init(vi);

        Block vb = new Block(ds);
        vb.W = 0;
        vb.sequence = 0;
        vb.granulepos = 123;
        vb.pcm = new float[][]{
                new float[64],
                new float[64]
        };

        ds.synthesis_blockin(vb);

        assertEquals(0, ds.sequence);
        assertEquals(123, ds.granulepos);
    }

    @Test
    void synthesisBlockInShouldResizePCMStorageIfNeeded() {
        Info vi = makeValidInfo();
        DspState ds = new DspState();
        ds.synthesis_init(vi);

        vi.getBlocksizes()[1] = 4096;

        Block vb = new Block(ds);
        vb.W = 1;
        vb.sequence = 0;
        vb.granulepos = 0;
        vb.pcm = new float[][]{
                new float[4096],
                new float[4096]
        };

        ds.synthesis_blockin(vb);

        assertTrue(ds.pcm_storage >= 4096);
    }

    @Test
    void synthesisBlockInShouldSetEofflagWhenVBHasEofflag() {
        Info vi = makeValidInfo();
        DspState ds = new DspState();
        ds.synthesis_init(vi);

        Block vb = new Block(ds);
        vb.W = 0;
        vb.sequence = 0;
        vb.eofflag = 1;
        vb.pcm = new float[][]{
                new float[64],
                new float[64]
        };

        ds.synthesis_blockin(vb);

        assertEquals(1, ds.eofflag);
    }

    @Test
    void clearShouldNotThrow() {
        Info vi = makeValidInfo();
        DspState ds = new DspState();
        ds.synthesis_init(vi);

        ds.clear();
    }
}
