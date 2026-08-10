package com.jcraft.jorbis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PsyLookTest {

    @Test
    void defaultConstructorShouldInitializeToNullsAndZero() {
        PsyLook pl = new PsyLook();

        assertEquals(0, pl.getN());
        assertNull(pl.getVi());
        assertNull(pl.getTonecurves());
        assertNull(pl.getPeakatt());
        assertNull(pl.getNoisecurves());
        assertNull(pl.getAth());
        assertNull(pl.getOctave());
    }

    @Test
    void settersShouldStoreValuesCorrectly() {
        PsyLook pl = new PsyLook();

        pl.setN(256);
        assertEquals(256, pl.getN());

        PsyInfo info = new PsyInfo();
        pl.setVi(info);
        assertSame(info, pl.getVi());

        float[][][] tone = new float[2][3][4];
        pl.setTonecurves(tone);
        assertSame(tone, pl.getTonecurves());

        float[][] peak = new float[3][5];
        pl.setPeakatt(peak);
        assertSame(peak, pl.getPeakatt());

        float[][][] noise = new float[1][2][3];
        pl.setNoisecurves(noise);
        assertSame(noise, pl.getNoisecurves());

        float[] ath = new float[]{1f, 2f, 3f};
        pl.setAth(ath);
        assertSame(ath, pl.getAth());

        int[] octave = new int[]{4, 5, 6};
        pl.setOctave(octave);
        assertSame(octave, pl.getOctave());
    }

    @Test
    void settersShouldHandleNullValues() {
        PsyLook pl = new PsyLook();

        pl.setTonecurves(null);
        pl.setPeakatt(null);
        pl.setNoisecurves(null);
        pl.setAth(null);
        pl.setOctave(null);
        pl.setVi(null);

        assertNull(pl.getTonecurves());
        assertNull(pl.getPeakatt());
        assertNull(pl.getNoisecurves());
        assertNull(pl.getAth());
        assertNull(pl.getOctave());
        assertNull(pl.getVi());
    }

    @Test
    void shouldSupportEmptyArrays() {
        PsyLook pl = new PsyLook();

        pl.setTonecurves(new float[0][][]);
        pl.setPeakatt(new float[0][]);
        pl.setNoisecurves(new float[0][][]);
        pl.setAth(new float[0]);
        pl.setOctave(new int[0]);

        assertEquals(0, pl.getTonecurves().length);
        assertEquals(0, pl.getPeakatt().length);
        assertEquals(0, pl.getNoisecurves().length);
        assertEquals(0, pl.getAth().length);
        assertEquals(0, pl.getOctave().length);
    }

    @Test
    void shouldSupportLargeArrays() {
        PsyLook pl = new PsyLook();

        float[][][] tone = new float[50][50][50];
        pl.setTonecurves(tone);
        assertEquals(50, pl.getTonecurves().length);

        float[][] peak = new float[100][200];
        pl.setPeakatt(peak);
        assertEquals(100, pl.getPeakatt().length);

        float[][][] noise = new float[20][30][40];
        pl.setNoisecurves(noise);
        assertEquals(20, pl.getNoisecurves().length);

        float[] ath = new float[1000];
        pl.setAth(ath);
        assertEquals(1000, pl.getAth().length);

        int[] octave = new int[500];
        pl.setOctave(octave);
        assertEquals(500, pl.getOctave().length);
    }

    @Test
    void shouldAllowSpecialFloatValuesInAth() {
        PsyLook pl = new PsyLook();

        float[] ath = {Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
        pl.setAth(ath);

        assertTrue(Float.isNaN(pl.getAth()[0]));
        assertEquals(Float.POSITIVE_INFINITY, pl.getAth()[1]);
        assertEquals(Float.NEGATIVE_INFINITY, pl.getAth()[2]);
    }

    @Test
    void gettersShouldReturnSameReferenceNotCopy() {
        PsyLook pl = new PsyLook();

        float[][][] tone = new float[1][1][1];
        tone[0][0][0] = 5f;

        pl.setTonecurves(tone);

        tone[0][0][0] = 99f;

        assertEquals(99f, pl.getTonecurves()[0][0][0]);
    }

    @Test
    void initShouldNotThrowEvenThoughEmpty() {
        PsyLook pl = new PsyLook();
        PsyInfo info = new PsyInfo();

        assertDoesNotThrow(() -> pl.init(info, 256, 44100));
    }
}
