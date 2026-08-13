package com.fluendo.jheora;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CodingModeTest {

    @Test
    void allModesHaveCorrectValues() {
        assertEquals(0x0, CodingMode.CODE_INTER_NO_MV.getValue());
        assertEquals(0x1, CodingMode.CODE_INTRA.getValue());
        assertEquals(0x2, CodingMode.CODE_INTER_PLUS_MV.getValue());
        assertEquals(0x3, CodingMode.CODE_INTER_LAST_MV.getValue());
        assertEquals(0x4, CodingMode.CODE_INTER_PRIOR_LAST.getValue());
        assertEquals(0x5, CodingMode.CODE_USING_GOLDEN.getValue());
        assertEquals(0x6, CodingMode.CODE_GOLDEN_MV.getValue());
        assertEquals(0x7, CodingMode.CODE_INTER_FOURMV.getValue());
    }

    @Test
    void modesArrayContainsAllModesInCorrectOrder() {
        CodingMode[] m = CodingMode.MODES;

        assertEquals(8, m.length, "MODES should contain exactly 8 entries");

        assertSame(CodingMode.CODE_INTER_NO_MV, m[0]);
        assertSame(CodingMode.CODE_INTRA, m[1]);
        assertSame(CodingMode.CODE_INTER_PLUS_MV, m[2]);
        assertSame(CodingMode.CODE_INTER_LAST_MV, m[3]);
        assertSame(CodingMode.CODE_INTER_PRIOR_LAST, m[4]);
        assertSame(CodingMode.CODE_USING_GOLDEN, m[5]);
        assertSame(CodingMode.CODE_GOLDEN_MV, m[6]);
        assertSame(CodingMode.CODE_INTER_FOURMV, m[7]);
    }

    @Test
    void codingModeInstancesAreUniqueAndSingletonLike() {
        // Ensure no two modes share the same instance
        assertNotSame(CodingMode.CODE_INTER_NO_MV, CodingMode.CODE_INTRA);
        assertNotSame(CodingMode.CODE_INTRA, CodingMode.CODE_INTER_PLUS_MV);
        assertNotSame(CodingMode.CODE_INTER_LAST_MV, CodingMode.CODE_INTER_PRIOR_LAST);
        assertNotSame(CodingMode.CODE_USING_GOLDEN, CodingMode.CODE_GOLDEN_MV);
        assertNotSame(CodingMode.CODE_GOLDEN_MV, CodingMode.CODE_INTER_FOURMV);

        // Ensure MODES contains the exact same instances
        for (int i = 0; i < CodingMode.MODES.length; i++) {
            assertSame(CodingMode.MODES[i], CodingMode.MODES[i],
                    "Instance identity must remain stable");
        }
    }

    @Test
    void getValueIsPureAndDoesNotChange() {
        CodingMode mode = CodingMode.CODE_INTER_PLUS_MV;

        int v1 = mode.getValue();
        int v2 = mode.getValue();
        int v3 = mode.getValue();

        assertEquals(v1, v2);
        assertEquals(v2, v3);
        assertEquals(0x2, v1);
    }

    @Test
    void valuesAreWithinExpectedRange() {
        for (CodingMode mode : CodingMode.MODES) {
            int v = mode.getValue();
            assertTrue(v >= 0 && v <= 7,
                    "CodingMode value must be between 0 and 7, got " + v);
        }
    }

    @Test
    void noDuplicateValuesExist() {
        boolean[] seen = new boolean[8];

        for (CodingMode mode : CodingMode.MODES) {
            int v = mode.getValue();
            assertFalse(seen[v], "Duplicate CodingMode value detected: " + v);
            seen[v] = true;
        }
    }

    @Test
    void constructorIsPrivate() throws Exception {
        var ctor = CodingMode.class.getDeclaredConstructor(int.class);

        assertFalse(ctor.canAccess(null), "Constructor should be private");
    }
}
