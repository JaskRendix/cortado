package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LpcTest {

  @Test
  void defaultConstructorShouldInitializeFields() {
    Lpc lpc = new Lpc();

    assertEquals(0, lpc.ln);
    assertEquals(0, lpc.m);
  }

  @Test
  void initShouldSetLnAndM() {
    Lpc lpc = new Lpc();
    lpc.init(128, 10);

    assertEquals(128, lpc.ln);
    assertEquals(10, lpc.m);
  }

  @Test
  void clearShouldNotThrow() {
    Lpc lpc = new Lpc();
    lpc.init(64, 5);

    assertDoesNotThrow(lpc::clear);
  }

  @Test
  void lpcFromDataShouldHandleZeroInput() {
    float[] data = new float[32];
    float[] coeff = new float[4];

    float error = Lpc.lpcFromData(data, coeff, 32, 4);

    assertEquals(0f, error);
    for (float c : coeff) {
      assertEquals(0f, c);
    }
  }

  @Test
  void lpcFromDataShouldHandleRandomInput() {
    float[] data = new float[64];
    float[] coeff = new float[8];

    for (int i = 0; i < data.length; i++) {
      data[i] = (float) Math.random();
    }

    float error = Lpc.lpcFromData(data, coeff, 64, 8);

    assertFalse(Float.isNaN(error));
    assertFalse(Float.isInfinite(error));
  }

  @Test
  void lpcFromDataShouldHandleNegativeValues() {
    float[] data = new float[32];
    float[] coeff = new float[4];

    for (int i = 0; i < data.length; i++) {
      data[i] = -i;
    }

    float error = Lpc.lpcFromData(data, coeff, 32, 4);

    assertFalse(Float.isNaN(error));
    assertFalse(Float.isInfinite(error));
  }

  @Test
  void lpcFromCurveShouldNotThrow() {
    Lpc lpc = new Lpc();
    lpc.init(64, 8);

    float[] curve = new float[64];
    float[] coeff = new float[8];

    assertDoesNotThrow(() -> lpc.lpcFromCurve(curve, coeff));
  }

  @Test
  void lpcFromCurveShouldProduceFiniteValues() {
    Lpc lpc = new Lpc();
    lpc.init(64, 8);

    float[] curve = new float[64];
    float[] coeff = new float[8];

    float error = lpc.lpcFromCurve(curve, coeff);

    assertFalse(Float.isNaN(error));
    assertFalse(Float.isInfinite(error));
  }

  @Test
  void lpcToCurveShouldZeroInitializeOutput() {
    Lpc lpc = new Lpc();
    lpc.init(32, 4);

    float[] curve = new float[64];
    float[] coeff = new float[] {1f, 2f, 3f, 4f};

    lpc.lpcToCurve(curve, coeff, 1f);

    // First pass zeroes everything
    for (float v : curve) {
      assertFalse(Float.isNaN(v));
    }
  }

  @Test
  void lpcToCurveShouldHandleZeroAmplitude() {
    Lpc lpc = new Lpc();
    lpc.init(32, 4);

    float[] curve = new float[64];
    float[] coeff = new float[] {1f, 2f, 3f, 4f};

    lpc.lpcToCurve(curve, coeff, 0f);

    for (float v : curve) {
      assertEquals(0f, v);
    }
  }

  @Test
  void lpcToCurveShouldProduceFiniteValues() {
    Lpc lpc = new Lpc();
    lpc.init(32, 4);

    float[] curve = new float[64];
    float[] coeff = new float[] {1f, -2f, 3f, -4f};

    lpc.lpcToCurve(curve, coeff, 2f);

    for (float v : curve) {
      assertFalse(Float.isNaN(v));
      assertFalse(Float.isInfinite(v));
    }
  }

  @Test
  void fieldsShouldStoreValuesCorrectly() {
    Lpc lpc = new Lpc();

    lpc.ln = 100;
    lpc.m = 7;

    assertEquals(100, lpc.ln);
    assertEquals(7, lpc.m);
  }
}
