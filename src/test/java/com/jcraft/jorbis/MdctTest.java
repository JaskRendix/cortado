package com.jcraft.jorbis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MdctTest {

    @Test
    void defaultConstructorShouldInitializeBuffers() {
        Mdct mdct = new Mdct();

        // Before init(), MDCT tables are not allocated
        assertNull(mdct.getTrig());
        assertNull(mdct.getBitrev());
        assertEquals(0, mdct.getN());
        assertEquals(0, mdct.getLog2n());
        assertEquals(0f, mdct.getScale());
    }

    @Test
    void initShouldSetCorrectNAndLog2n() {
        Mdct mdct = new Mdct();
        mdct.init(1024);

        assertEquals(1024, mdct.getN());
        assertEquals(10, mdct.getLog2n()); // log2(1024) = 10
    }

    @Test
    void initShouldAllocateTrigAndBitrevArrays() {
        Mdct mdct = new Mdct();
        mdct.init(512);

        assertNotNull(mdct.getTrig());
        assertNotNull(mdct.getBitrev());

        assertEquals(512 + 512 / 4, mdct.getTrig().length);
        assertEquals(512 / 4, mdct.getBitrev().length);
    }

    @Test
    void initShouldComputeScaleCorrectly() {
        Mdct mdct = new Mdct();
        mdct.init(256);

        assertEquals(4f / 256f, mdct.getScale());
    }

    @Test
    void initShouldNotThrowForSmallN() {
        Mdct mdct = new Mdct();

        assertDoesNotThrow(() -> mdct.init(64));
        assertEquals(64, mdct.getN());
    }

    @Test
    void backwardShouldResizeBuffersIfTooSmall() {
        Mdct mdct = new Mdct();
        mdct.init(2048);

        float[] in = new float[2048];
        float[] out = new float[2048];

        // Force buffer resize
        mdct.backward(in, out);

        assertTrue(mdct.getXBuffer().length >= 1024);
        assertTrue(mdct.getWBuffer().length >= 1024);
    }

    @Test
    void backwardShouldNotThrowOnZeroInput() {
        Mdct mdct = new Mdct();
        mdct.init(1024);

        float[] in = new float[1024];
        float[] out = new float[1024];

        assertDoesNotThrow(() -> mdct.backward(in, out));
    }

    @Test
    void backwardShouldNotThrowOnRandomInput() {
        Mdct mdct = new Mdct();
        mdct.init(512);

        float[] in = new float[512];
        float[] out = new float[512];

        for (int i = 0; i < in.length; i++) {
            in[i] = (float) Math.random();
        }

        assertDoesNotThrow(() -> mdct.backward(in, out));
    }

    @Test
    void backwardShouldProduceFiniteValues() {
        Mdct mdct = new Mdct();
        mdct.init(256);

        float[] in = new float[256];
        float[] out = new float[256];

        mdct.backward(in, out);

        for (float v : out) {
            assertFalse(Float.isNaN(v));
            assertFalse(Float.isInfinite(v));
        }
    }

    @Test
    void trigValuesShouldBeWithinMinusOneToOne() {
        Mdct mdct = new Mdct();
        mdct.init(1024);

        for (float t : mdct.getTrig()) {
            assertTrue(t >= -1.0f && t <= 1.0f);
        }
    }

    @Test
    void bitrevShouldContainValidIndices() {
        Mdct mdct = new Mdct();
        mdct.init(512);

        int[] bitrev = mdct.getBitrev();
        for (int v : bitrev) {
            assertTrue(v >= 0);
            assertTrue(v < 512 / 2);
        }
    }

    @Test
    void forwardShouldNotThrowEvenThoughUnimplemented() {
        Mdct mdct = new Mdct();
        mdct.init(256);

        float[] in = new float[256];
        float[] out = new float[256];

        assertDoesNotThrow(() -> mdct.forward(in, out));
    }

    @Test
    void clearShouldNotThrow() {
        Mdct mdct = new Mdct();
        mdct.init(128);

        assertDoesNotThrow(mdct::clear);
    }

    @Test
    void settersShouldStoreValuesCorrectly() {
        Mdct mdct = new Mdct();

        mdct.setN(128);
        mdct.setLog2n(7);
        mdct.setScale(0.5f);

        float[] trig = new float[]{1f, 2f, 3f};
        int[] bitrev = new int[]{4, 5, 6};

        mdct.setTrig(trig);
        mdct.setBitrev(bitrev);

        assertEquals(128, mdct.getN());
        assertEquals(7, mdct.getLog2n());
        assertEquals(0.5f, mdct.getScale());
        assertSame(trig, mdct.getTrig());
        assertSame(bitrev, mdct.getBitrev());
    }
}
