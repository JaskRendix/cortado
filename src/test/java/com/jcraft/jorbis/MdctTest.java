package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MdctTest {

  @Test
  void defaultConstructorShouldInitializeBuffers() {
    Mdct mdct = new Mdct();

    // Before init(), MDCT tables are not allocated
    assertNull(mdct.trig);
    assertNull(mdct.bitrev);
    assertEquals(0, mdct.n);
    assertEquals(0, mdct.log2n);
    assertEquals(0f, mdct.scale);
  }

  @Test
  void initShouldSetCorrectNAndLog2n() {
    Mdct mdct = new Mdct();
    mdct.init(1024);

    assertEquals(1024, mdct.n);
    assertEquals(10, mdct.log2n); // log2(1024) = 10
  }

  @Test
  void initShouldAllocateTrigAndBitrevArrays() {
    Mdct mdct = new Mdct();
    mdct.init(512);

    assertNotNull(mdct.trig);
    assertNotNull(mdct.bitrev);

    assertEquals(512 + 512 / 4, mdct.trig.length);
    assertEquals(512 / 4, mdct.bitrev.length);
  }

  @Test
  void initShouldComputeScaleCorrectly() {
    Mdct mdct = new Mdct();
    mdct.init(256);

    assertEquals(4f / 256f, mdct.scale);
  }

  @Test
  void initShouldNotThrowForSmallN() {
    Mdct mdct = new Mdct();

    assertDoesNotThrow(() -> mdct.init(64));
    assertEquals(64, mdct.n);
  }

  @Test
  void backwardShouldResizeBuffersIfTooSmall() {
    Mdct mdct = new Mdct();
    mdct.init(2048);

    float[] in = new float[2048];
    float[] out = new float[2048];

    // Force buffer resize
    mdct.backward(in, out);

    assertTrue(mdct.xBuffer.length >= 1024);
    assertTrue(mdct.wBuffer.length >= 1024);
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

    for (float t : mdct.trig) {
      assertTrue(t >= -1.0f && t <= 1.0f);
    }
  }

  @Test
  void bitrevShouldContainValidIndices() {
    Mdct mdct = new Mdct();
    mdct.init(512);

    int[] bitrev = mdct.bitrev;
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
  void fieldsShouldStoreValuesCorrectly() {
    Mdct mdct = new Mdct();

    mdct.n = 128;
    mdct.log2n = 7;
    mdct.scale = 0.5f;

    float[] trig = new float[] {1f, 2f, 3f};
    int[] bitrev = new int[] {4, 5, 6};

    mdct.trig = trig;
    mdct.bitrev = bitrev;

    assertEquals(128, mdct.n);
    assertEquals(7, mdct.log2n);
    assertEquals(0.5f, mdct.scale);
    assertSame(trig, mdct.trig);
    assertSame(bitrev, mdct.bitrev);
  }
}
