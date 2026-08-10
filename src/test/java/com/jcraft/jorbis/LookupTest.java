package com.jcraft.jorbis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LookupTest {

    @Test
    void coslookShouldHandleValidRange() {
        for (float a = 0f; a <= Math.PI; a += 0.01f) {
            float v = Lookup.coslook(a);
            assertFalse(Float.isNaN(v));
            assertFalse(Float.isInfinite(v));
            assertTrue(v >= -1f && v <= 1f);
        }
    }

    @Test
    void coslookShouldHandleZero() {
        float v = Lookup.coslook(0f);
        assertEquals(1f, v, 0.01f);
    }

    @Test
    void coslookShouldHandlePiMinusEpsilon() {
        float v = Lookup.coslook((float) Math.PI - 0.0001f);
        assertFalse(Float.isNaN(v));
        assertFalse(Float.isInfinite(v));
    }

    @Test
    void invsqlookShouldHandleValidRange() {
        for (float p = 0.5f; p < 1f; p += 0.01f) {
            float v = Lookup.invsqlook(p);
            assertFalse(Float.isNaN(v));
            assertFalse(Float.isInfinite(v));
        }
    }

    @Test
    void invsqlookShouldHandleLowerBound() {
        float v = Lookup.invsqlook(0.5f);
        assertFalse(Float.isNaN(v));
        assertFalse(Float.isInfinite(v));
    }

    @Test
    void invsqlookShouldHandleUpperBoundMinusEpsilon() {
        float v = Lookup.invsqlook(0.999f);
        assertFalse(Float.isNaN(v));
        assertFalse(Float.isInfinite(v));
    }

    @Test
    void invsq2explookShouldHandleValidRange() {
        for (int a = Lookup.INVSQ2EXP_LOOKUP_MIN; a <= Lookup.INVSQ2EXP_LOOKUP_MAX; a++) {
            float v = Lookup.invsq2explook(a);
            assertFalse(Float.isNaN(v));
            assertFalse(Float.isInfinite(v));
        }
    }

    @Test
    void fromdBlookShouldHandleZeroDb() {
        float v = Lookup.fromdBlook(0f);
        assertEquals(1f, v, 0.01f);
    }

    @Test
    void fromdBlookShouldHandleMinus140Db() {
        float v = Lookup.fromdBlook(-140f);
        assertTrue(v >= 0f);
        assertTrue(v <= 1f);
    }

    @Test
    void fromdBlookShouldClampAboveZero() {
        float v = Lookup.fromdBlook(10f);
        assertEquals(1f, v);
    }

    @Test
    void fromdBlookShouldClampBelowRange() {
        float v = Lookup.fromdBlook(-999f);
        assertEquals(0f, v);
    }

    @Test
    void fromdBlookShouldProduceFiniteValues() {
        for (float a = -140f; a <= 0f; a += 1f) {
            float v = Lookup.fromdBlook(a);
            assertFalse(Float.isNaN(v));
            assertFalse(Float.isInfinite(v));
        }
    }
}
