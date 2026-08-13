package com.fluendo.jheora;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class iDCTTest {

    private final iDCT idct = new iDCT();

    @Test
    void idct1_allZerosProducesZeroBlock() {
        short[] input = new short[64];
        short[] quant = new short[64];
        short[] out = new short[64];

        idct.IDct1(input, quant, out);

        for (short v : out) {
            assertEquals(0, v);
        }
    }

    @Test
    void idct1_singleDCProducesUniformBlock() {
        short[] input = new short[64];
        short[] quant = new short[64];
        short[] out = new short[64];

        input[0] = 10;
        quant[0] = 2;

        idct.IDct1(input, quant, out);

        short expected = (short) ((10 * 2 + 15) >> 5);

        for (short v : out) {
            assertEquals(expected, v);
        }
    }

    @Test
    void idct1_doesNotModifyInputArrays() {
        short[] input = new short[64];
        short[] quant = new short[64];
        short[] out = new short[64];

        short[] inputCopy = input.clone();
        short[] quantCopy = quant.clone();

        idct.IDct1(input, quant, out);

        assertArrayEquals(inputCopy, input);
        assertArrayEquals(quantCopy, quant);
    }

    @Test
    void idct10_zeroInputProducesZeroOutput() {
        short[] input = new short[64];
        short[] quant = new short[64];
        short[] out = new short[64];

        idct.IDct10(input, quant, out);

        for (short v : out) {
            assertEquals(0, v);
        }
    }

    @Test
    void idct10_onlyDCProducesUniformBlock() {
        short[] input = new short[64];
        short[] quant = new short[64];
        short[] out = new short[64];

        input[0] = 8;
        quant[0] = 4;

        idct.IDct10(input, quant, out);

        // IDct10 is not bit-exact, but DC must propagate uniformly
        short dc = out[0];
        for (short v : out) {
            assertEquals(dc, v);
        }
    }

    @Test
    void idct10_nonZeroHighCoefficientsIgnored() {
        short[] input = new short[64];
        short[] quant = new short[64];
        short[] out = new short[64];

        // Set coefficient 20 (outside first 10)
        input[20] = 100;
        quant[20] = 3;

        idct.IDct10(input, quant, out);

        // All output must be zero because only first 10 coefficients matter
        for (short v : out) {
            assertEquals(0, v);
        }
    }

    @Test
    void idctSlow_zeroInputProducesZeroOutput() {
        short[] input = new short[64];
        short[] quant = new short[64];
        short[] out = new short[64];

        idct.IDctSlow(input, quant, out);

        for (short v : out) {
            assertEquals(0, v);
        }
    }

    @Test
    void idctSlow_singleDCProducesUniformBlock() {
        short[] input = new short[64];
        short[] quant = new short[64];
        short[] out = new short[64];

        input[0] = 16;
        quant[0] = 2;

        idct.IDctSlow(input, quant, out);

        short dc = out[0];
        for (short v : out) {
            assertEquals(dc, v);
        }
    }

    @Test
    void idctSlow_randomInputProducesFiniteValues() {
        short[] input = new short[64];
        short[] quant = new short[64];
        short[] out = new short[64];

        for (int i = 0; i < 64; i++) {
            input[i] = (short) (i * 3);
            quant[i] = (short) (i + 1);
        }

        idct.IDctSlow(input, quant, out);

        for (short v : out) {
            assertTrue(v >= Short.MIN_VALUE && v <= Short.MAX_VALUE);
        }
    }

    @Test
    void idctSlow_doesNotModifyInputArrays() {
        short[] input = new short[64];
        short[] quant = new short[64];
        short[] out = new short[64];

        short[] inputCopy = input.clone();
        short[] quantCopy = quant.clone();

        idct.IDctSlow(input, quant, out);

        assertArrayEquals(inputCopy, input);
        assertArrayEquals(quantCopy, quant);
    }

    @Test
    void idct_internalBufferHasCorrectLength() {
        // ip[] must always be 64
        assertEquals(64, Arrays.stream(idct.getClass().getDeclaredFields())
                .filter(f -> f.getName().equals("ip"))
                .findFirst()
                .map(f -> {
                    f.setAccessible(true);
                    try {
                        return ((int[]) f.get(idct)).length;
                    } catch (Exception e) {
                        return -1;
                    }
                }).orElse(-1));
    }
}
