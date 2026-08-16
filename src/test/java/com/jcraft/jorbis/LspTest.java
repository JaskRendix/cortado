package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LspTest {

  @Test
  void lspToCurveShouldHandleEvenOrderFilter() {
    int n = 16;
    int ln = 32;
    int m = 4; // even
    float[] curve = new float[n];
    int[] map = new int[n];
    float[] lsp = new float[m];

    // initialize curve and map
    for (int i = 0; i < n; i++) {
      curve[i] = 1.0f;
      map[i] = i % ln;
    }
    // simple LSPs
    lsp[0] = 0.1f;
    lsp[1] = 0.2f;
    lsp[2] = 0.3f;
    lsp[3] = 0.4f;

    Lsp.lspToCurve(curve, map, n, ln, lsp, m, 1.0f, 0.0f);

    for (float v : curve) {
      assertFalse(Float.isNaN(v));
      assertFalse(Float.isInfinite(v));
    }
  }

  @Test
  void lspToCurveShouldHandleOddOrderFilter() {
    int n = 16;
    int ln = 32;
    int m = 5; // odd
    float[] curve = new float[n];
    int[] map = new int[n];
    float[] lsp = new float[m];

    for (int i = 0; i < n; i++) {
      curve[i] = 1.0f;
      map[i] = i % ln;
    }
    for (int i = 0; i < m; i++) {
      lsp[i] = 0.1f * (i + 1);
    }

    Lsp.lspToCurve(curve, map, n, ln, lsp, m, 1.0f, 0.0f);

    for (float v : curve) {
      assertFalse(Float.isNaN(v));
      assertFalse(Float.isInfinite(v));
    }
  }

  @Test
  void lspToCurveShouldRespectAmpZero() {
    int n = 8;
    int ln = 16;
    int m = 4;
    float[] curve = new float[n];
    int[] map = new int[n];
    float[] lsp = new float[m];

    for (int i = 0; i < n; i++) {
      curve[i] = 2.0f;
      map[i] = i % ln;
    }
    for (int i = 0; i < m; i++) {
      lsp[i] = 0.2f * (i + 1);
    }

    // amp = 0 should produce fromdBlook( -ampoffset ) scaling only
    Lsp.lspToCurve(curve, map, n, ln, lsp, m, 0.0f, 0.0f);

    for (float v : curve) {
      assertFalse(Float.isNaN(v));
      assertFalse(Float.isInfinite(v));
    }
  }

  @Test
  void lspToCurveShouldHandleGroupedMapIndices() {
    int n = 12;
    int ln = 24;
    int m = 4;
    float[] curve = new float[n];
    int[] map = new int[n];
    float[] lsp = new float[m];

    // group some indices to same k
    for (int i = 0; i < n; i++) {
      curve[i] = 1.0f;
      map[i] = (i < 6) ? 3 : 10;
    }
    for (int i = 0; i < m; i++) {
      lsp[i] = 0.15f * (i + 1);
    }

    Lsp.lspToCurve(curve, map, n, ln, lsp, m, 1.0f, 0.5f);

    for (float v : curve) {
      assertFalse(Float.isNaN(v));
      assertFalse(Float.isInfinite(v));
    }
  }

  @Test
  void lspToCurveShouldHandleValidLspRange() {
    int n = 16;
    int ln = 32;
    int m = 4;
    float[] curve = new float[n];
    int[] map = new int[n];
    float[] lsp = new float[m];

    for (int i = 0; i < n; i++) {
      curve[i] = 1.0f;
      map[i] = i % ln;
    }

    // Valid LSP range: (0, π)
    lsp[0] = 0.1f;
    lsp[1] = 1.0f;
    lsp[2] = 2.0f;
    lsp[3] = 3.0f;

    Lsp.lspToCurve(curve, map, n, ln, lsp, m, 2.0f, 1.0f);

    for (float v : curve) {
      assertFalse(Float.isNaN(v));
      assertFalse(Float.isInfinite(v));
    }
  }

  @Test
  void lspToCurveShouldNotModifyCurveWhenNIsZero() {
    int n = 0;
    int ln = 32;
    int m = 4;
    float[] curve = new float[0];
    int[] map = new int[0];
    float[] lsp = new float[m];

    Lsp.lspToCurve(curve, map, n, ln, lsp, m, 1.0f, 0.0f);

    assertEquals(0, curve.length);
  }
}
