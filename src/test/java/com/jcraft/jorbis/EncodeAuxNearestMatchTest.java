package com.jcraft.jorbis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncodeAuxNearestMatchTest {

    @Test
    void defaultConstructorShouldInitializeToNulls() {
        EncodeAuxNearestMatch aux = new EncodeAuxNearestMatch();

        assertNull(aux.getPtr0());
        assertNull(aux.getPtr1());
        assertNull(aux.getP());
        assertNull(aux.getQ());
        assertEquals(0, aux.getAux());
        assertEquals(0, aux.getAlloc());
    }

    @Test
    void settersShouldStoreArraysCorrectly() {
        EncodeAuxNearestMatch aux = new EncodeAuxNearestMatch();

        int[] ptr0 = {1, 2, 3};
        int[] ptr1 = {4, 5};
        int[] p = {10};
        int[] q = {20, 30};

        aux.setPtr0(ptr0);
        aux.setPtr1(ptr1);
        aux.setP(p);
        aux.setQ(q);

        assertArrayEquals(ptr0, aux.getPtr0());
        assertArrayEquals(ptr1, aux.getPtr1());
        assertArrayEquals(p, aux.getP());
        assertArrayEquals(q, aux.getQ());
    }

    @Test
    void settersShouldHandleNullValues() {
        EncodeAuxNearestMatch aux = new EncodeAuxNearestMatch();

        aux.setPtr0(null);
        aux.setPtr1(null);
        aux.setP(null);
        aux.setQ(null);

        assertNull(aux.getPtr0());
        assertNull(aux.getPtr1());
        assertNull(aux.getP());
        assertNull(aux.getQ());
    }

    @Test
    void auxAndAllocShouldStoreValuesCorrectly() {
        EncodeAuxNearestMatch aux = new EncodeAuxNearestMatch();

        aux.setAux(42);
        aux.setAlloc(99);

        assertEquals(42, aux.getAux());
        assertEquals(99, aux.getAlloc());
    }

    @Test
    void shouldSupportEmptyArrays() {
        EncodeAuxNearestMatch aux = new EncodeAuxNearestMatch();

        aux.setPtr0(new int[0]);
        aux.setPtr1(new int[0]);
        aux.setP(new int[0]);
        aux.setQ(new int[0]);

        assertEquals(0, aux.getPtr0().length);
        assertEquals(0, aux.getPtr1().length);
        assertEquals(0, aux.getP().length);
        assertEquals(0, aux.getQ().length);
    }

    @Test
    void shouldSupportLargeArrays() {
        EncodeAuxNearestMatch aux = new EncodeAuxNearestMatch();

        int[] large = new int[10_000];
        for (int i = 0; i < large.length; i++) {
            large[i] = i;
        }

        aux.setPtr0(large);
        assertEquals(10_000, aux.getPtr0().length);
        assertEquals(9999, aux.getPtr0()[9999]);
    }

    @Test
    void shouldAllowNegativeValues() {
        EncodeAuxNearestMatch aux = new EncodeAuxNearestMatch();

        int[] p = {-1, -5, -10};
        aux.setP(p);

        assertArrayEquals(p, aux.getP());
    }

    @Test
    void gettersShouldReturnSameReferenceNotCopy() {
        EncodeAuxNearestMatch aux = new EncodeAuxNearestMatch();

        int[] ptr0 = {1, 2, 3};
        aux.setPtr0(ptr0);

        // Mutate original array
        ptr0[0] = 99;

        // Getter returns same reference
        assertEquals(99, aux.getPtr0()[0]);
    }

    @Test
    void shouldAllowZeroAuxAndAlloc() {
        EncodeAuxNearestMatch aux = new EncodeAuxNearestMatch();

        aux.setAux(0);
        aux.setAlloc(0);

        assertEquals(0, aux.getAux());
        assertEquals(0, aux.getAlloc());
    }

    @Test
    void shouldAllowNegativeAuxAndAlloc() {
        EncodeAuxNearestMatch aux = new EncodeAuxNearestMatch();

        aux.setAux(-10);
        aux.setAlloc(-20);

        assertEquals(-10, aux.getAux());
        assertEquals(-20, aux.getAlloc());
    }
}
