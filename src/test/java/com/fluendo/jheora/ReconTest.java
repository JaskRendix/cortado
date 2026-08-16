package com.fluendo.jheora;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ReconTest {

  @Test
  void copyBlock_copies8x8WithStride() {
    short[] src = new short[64];
    short[] dest = new short[64];

    for (int i = 0; i < 64; i++) {
      src[i] = (short) (i + 1);
    }

    Recon.copyBlock(src, dest, 0, 8);

    assertArrayEquals(src, dest, "copyBlock must copy 8x8 block with given stride");
  }

  @Test
  void copyBlock_respectsIndexAndStride() {
    short[] src = new short[160];
    short[] dest = new short[160];

    // Fill a 8x8 block starting at index 16 with recognizable pattern
    int idx = 16;
    int stride = 16;
    int off = idx;
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        src[off + col] = (short) (row * 10 + col);
      }
      off += stride;
    }

    Recon.copyBlock(src, dest, idx, stride);

    off = idx;
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        assertEquals(
            src[off + col],
            dest[off + col],
            "copyBlock must copy from src to dest at non-zero index/stride");
      }
      off += stride;
    }
  }

  @Test
  void reconIntra_zeroChangeProducesMidGray() {
    short[] recon = new short[64];
    short[] change = new short[64];

    Recon.reconIntra(recon, 0, change, 8);

    for (short v : recon) {
      assertEquals(128, v, "zero change should reconstruct to 128 (mid-gray)");
    }
  }

  @Test
  void reconIntra_clampsBelowZero() {
    short[] recon = new short[64];
    short[] change = new short[64];

    // changePtr + 128 < 0 → should clamp to 0
    for (int i = 0; i < 64; i++) {
      change[i] = (short) -200; // -200 + 128 = -72
    }

    Recon.reconIntra(recon, 0, change, 8);

    for (short v : recon) {
      assertEquals(0, v, "values below 0 must be clamped to 0");
    }
  }

  @Test
  void reconIntra_clampsAbove255() {
    short[] recon = new short[64];
    short[] change = new short[64];

    // changePtr + 128 > 255 → should clamp to 255
    for (int i = 0; i < 64; i++) {
      change[i] = (short) 300; // 300 + 128 = 428
    }

    Recon.reconIntra(recon, 0, change, 8);

    for (short v : recon) {
      assertEquals(255, v, "values above 255 must be clamped to 255");
    }
  }

  @Test
  void reconInter_addsChangeToReferenceWithClamping() {
    short[] recon = new short[64];
    short[] ref = new short[64];
    short[] change = new short[64];

    for (int i = 0; i < 64; i++) {
      ref[i] = 100;
      change[i] = 50;
    }

    Recon.reconInter(recon, 0, ref, 0, change, 8);

    for (short v : recon) {
      assertEquals(150, v, "reconInter must add change to reference");
    }
  }

  @Test
  void reconInter_clampsOverflow() {
    short[] recon = new short[64];
    short[] ref = new short[64];
    short[] change = new short[64];

    for (int i = 0; i < 64; i++) {
      ref[i] = 250;
      change[i] = 50; // 300 → clamp to 255
    }

    Recon.reconInter(recon, 0, ref, 0, change, 8);

    for (short v : recon) {
      assertEquals(255, v, "reconInter must clamp values above 255");
    }
  }

  @Test
  void reconInterHalfPixel2_averagesTwoRefsThenAddsChange() {
    short[] recon = new short[64];
    short[] ref1 = new short[64];
    short[] ref2 = new short[64];
    short[] change = new short[64];

    for (int i = 0; i < 64; i++) {
      ref1[i] = 100;
      ref2[i] = 200;
      change[i] = 10;
    }

    // average = (100 + 200) / 2 = 150; +10 = 160
    Recon.reconInterHalfPixel2(recon, 0, ref1, 0, ref2, 0, change, 8);

    for (short v : recon) {
      assertEquals(160, v, "reconInterHalfPixel2 must average refs and add change");
    }
  }

  @Test
  void reconInterHalfPixel2_clampsUnderflowAndOverflow() {
    short[] recon = new short[64];
    short[] ref1 = new short[64];
    short[] ref2 = new short[64];
    short[] change = new short[64];

    // Underflow case: average small, negative change
    for (int i = 0; i < 32; i++) {
      ref1[i] = 0;
      ref2[i] = 0;
      change[i] = -100; // → negative, clamp to 0
    }

    // Overflow case: average large, positive change
    for (int i = 32; i < 64; i++) {
      ref1[i] = 255;
      ref2[i] = 255;
      change[i] = 100; // → >255, clamp to 255
    }

    Recon.reconInterHalfPixel2(recon, 0, ref1, 0, ref2, 0, change, 8);

    for (int i = 0; i < 32; i++) {
      assertEquals(0, recon[i], "first half must clamp underflow to 0");
    }
    for (int i = 32; i < 64; i++) {
      assertEquals(255, recon[i], "second half must clamp overflow to 255");
    }
  }

  @Test
  void methods_doNotModifyInputArraysUnexpectedly() {
    short[] src = new short[64];
    short[] dest = new short[64];
    short[] recon = new short[64];
    short[] ref = new short[64];
    short[] change = new short[64];

    for (int i = 0; i < 64; i++) {
      src[i] = (short) i;
      ref[i] = (short) (i * 2);
      change[i] = (short) (i * -1);
    }

    short[] srcBefore = src.clone();
    short[] refBefore = ref.clone();
    short[] changeBefore = change.clone();

    Recon.copyBlock(src, dest, 0, 8);
    Recon.reconIntra(recon, 0, change, 8);
    Recon.reconInter(recon, 0, ref, 0, change, 8);
    Recon.reconInterHalfPixel2(recon, 0, ref, 0, src, 0, change, 8);

    assertArrayEquals(srcBefore, src, "copyBlock must not modify src");
    assertArrayEquals(refBefore, ref, "reconInter must not modify ref");
    assertArrayEquals(changeBefore, change, "recon methods must not modify changePtr");
  }
}
