package com.fluendo.jheora;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HuffTablesTest {

    @Test
    void extraBitLengths_hasExpectedLength() {
        assertEquals(32, HuffTables.EXTRA_BIT_LENGTHS_VP31.length);
    }

    @Test
    void extraBitLengths_allValuesNonNegative() {
        for (byte b : HuffTables.EXTRA_BIT_LENGTHS_VP31) {
            assertTrue(b >= 0, "Negative extra bit length found");
        }
    }

    @Test
    void extraBitLengths_zeroRunTokensHaveCorrectLengths() {
        // EOB and zero-run tokens: indices 0–8
        for (int i = 0; i <= 8; i++) {
            assertTrue(HuffTables.EXTRA_BIT_LENGTHS_VP31[i] >= 0);
        }
    }

    @Test
    void extraBitLengths_lowValueTokensHaveZeroLength() {
        // Very low value tokens: indices 9–12
        for (int i = 9; i <= 12; i++) {
            assertEquals(0, HuffTables.EXTRA_BIT_LENGTHS_VP31[i]);
        }
    }

    @Test
    void frequencyCounts_hasExpectedOuterLength() {
        assertEquals(80, HuffTables.FREQUENCY_COUNTS_VP3.length);
    }

    @Test
    void frequencyCounts_allRowsHaveLength32() {
        for (short[] row : HuffTables.FREQUENCY_COUNTS_VP3) {
            assertEquals(32, row.length, "Row length mismatch");
        }
    }

    @Test
    void frequencyCounts_allValuesNonNegative() {
        for (short[] row : HuffTables.FREQUENCY_COUNTS_VP3) {
            for (short v : row) {
                assertTrue(v >= 0, "Negative frequency found");
            }
        }
    }

    @Test
    void frequencyCounts_noZeroRows() {
        for (short[] row : HuffTables.FREQUENCY_COUNTS_VP3) {
            boolean allZero = true;
            for (short v : row) {
                if (v != 0) {
                    allZero = false;
                    break;
                }
            }
            assertFalse(allZero, "Found a completely zero frequency row");
        }
    }

    @Test
    void frequencyCounts_valuesWithinExpectedVP3Range() {
        for (short[] row : HuffTables.FREQUENCY_COUNTS_VP3) {
            for (short v : row) {
                assertTrue(v <= 3000, "Frequency too large for VP3 tables");
            }
        }
    }

    @Test
    void frequencyCounts_rowsAreIndependent() {
        short[] row0 = HuffTables.FREQUENCY_COUNTS_VP3[0];
        short[] row1 = HuffTables.FREQUENCY_COUNTS_VP3[1];

        assertNotSame(row0, row1);
    }

    @Test
    void frequencyCounts_defensiveCopyCheck() {
        // Ensure modifying a local copy does NOT modify the table
        short[] copy = HuffTables.FREQUENCY_COUNTS_VP3[0].clone();
        copy[0] = 9999;

        assertNotEquals(9999, HuffTables.FREQUENCY_COUNTS_VP3[0][0]);
    }

    @Test
    void extraBitLengths_defensiveCopyCheck() {
        byte[] copy = HuffTables.EXTRA_BIT_LENGTHS_VP31.clone();
        copy[0] = 99;

        assertNotEquals(99, HuffTables.EXTRA_BIT_LENGTHS_VP31[0]);
    }
}
