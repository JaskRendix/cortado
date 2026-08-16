package com.fluendo.jheora;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ConstantsTest {

  @Test
  void constructorIsPrivate() throws Exception {
    var ctor = Constants.class.getDeclaredConstructor();

    assertFalse(ctor.canAccess(null), "Constructor should be private");
  }

  @Test
  void blockHeightWidthIsEight() {
    assertEquals(8, Constants.BLOCK_HEIGHT_WIDTH);
    assertEquals(8, Constants.HFRAGPIXELS);
    assertEquals(8, Constants.VFRAGPIXELS);
  }

  @Test
  void blockSizeIsCorrect() {
    assertEquals(64, Constants.BLOCK_SIZE, "BLOCK_SIZE must be 8×8 = 64");
  }

  @Test
  void umvBorderAndStrideExtraAreCorrect() {
    assertEquals(16, Constants.UMV_BORDER);
    assertEquals(32, Constants.STRIDE_EXTRA, "STRIDE_EXTRA must be UMV_BORDER × 2");
  }

  @Test
  void qTableSizeIsSixtyFour() {
    assertEquals(64, Constants.Q_TABLE_SIZE);
  }

  @Test
  void frameConstantsAreCorrect() {
    assertEquals(0, Constants.BASE_FRAME);
    assertEquals(1, Constants.NORMAL_FRAME);
  }

  @Test
  void modeConstantsAreCorrect() {
    assertEquals(8, Constants.MAX_MODES);
    assertEquals(3, Constants.MODE_BITS);
    assertEquals(8, Constants.MODE_METHODS);
    assertEquals(3, Constants.MODE_METHOD_BITS);
  }

  @Test
  void dequantIndexHasCorrectLength() {
    assertEquals(
        64, Constants.DEQUANT_INDEX.length, "DEQUANT_INDEX must contain exactly 64 entries");
  }

  @Test
  void dequantIndexContainsAllValuesZeroToSixtyThreeExactlyOnce() {
    boolean[] seen = new boolean[64];

    for (int v : Constants.DEQUANT_INDEX) {
      assertTrue(v >= 0 && v < 64, "DEQUANT_INDEX contains out-of-range value: " + v);

      assertFalse(seen[v], "Duplicate DEQUANT_INDEX value detected: " + v);

      seen[v] = true;
    }

    // Ensure all values 0–63 were present
    for (int i = 0; i < 64; i++) {
      assertTrue(seen[i], "Missing DEQUANT_INDEX value: " + i);
    }
  }

  @Test
  void constantsAreImmutable() {
    // All fields are final static primitives or arrays.
    // Arrays are mutable, but DEQUANT_INDEX is treated as read-only.
    int before = Constants.DEQUANT_INDEX[0];

    // Mutate locally
    int[] localCopy = Constants.DEQUANT_INDEX.clone();
    localCopy[0] = 999;

    // Ensure original array did NOT change
    assertEquals(
        before, Constants.DEQUANT_INDEX[0], "DEQUANT_INDEX must not be mutated externally");
  }
}
