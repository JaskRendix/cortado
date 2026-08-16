package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EncodeAuxThreshMatchTest {

  @Test
  void defaultConstructorShouldInitializeToNullsAndZeros() {
    EncodeAuxThreshMatch aux = new EncodeAuxThreshMatch();

    assertNull(aux.getQuantthresh());
    assertNull(aux.getQuantmap());
    assertEquals(0, aux.getQuantvals());
    assertEquals(0, aux.getThreshvals());
  }

  @Test
  void settersShouldStoreArraysCorrectly() {
    EncodeAuxThreshMatch aux = new EncodeAuxThreshMatch();

    float[] qt = {0.1f, 0.5f, 1.0f};
    int[] qm = {1, 2, 3};

    aux.setQuantthresh(qt);
    aux.setQuantmap(qm);

    assertArrayEquals(qt, aux.getQuantthresh());
    assertArrayEquals(qm, aux.getQuantmap());
  }

  @Test
  void settersShouldHandleNullValues() {
    EncodeAuxThreshMatch aux = new EncodeAuxThreshMatch();

    aux.setQuantthresh(null);
    aux.setQuantmap(null);

    assertNull(aux.getQuantthresh());
    assertNull(aux.getQuantmap());
  }

  @Test
  void quantvalsAndThreshvalsShouldStoreValuesCorrectly() {
    EncodeAuxThreshMatch aux = new EncodeAuxThreshMatch();

    aux.setQuantvals(7);
    aux.setThreshvals(4);

    assertEquals(7, aux.getQuantvals());
    assertEquals(4, aux.getThreshvals());
  }

  @Test
  void shouldSupportEmptyArrays() {
    EncodeAuxThreshMatch aux = new EncodeAuxThreshMatch();

    aux.setQuantthresh(new float[0]);
    aux.setQuantmap(new int[0]);

    assertEquals(0, aux.getQuantthresh().length);
    assertEquals(0, aux.getQuantmap().length);
  }

  @Test
  void shouldSupportLargeArrays() {
    EncodeAuxThreshMatch aux = new EncodeAuxThreshMatch();

    float[] qt = new float[5000];
    int[] qm = new int[5000];

    for (int i = 0; i < 5000; i++) {
      qt[i] = i * 0.1f;
      qm[i] = i;
    }

    aux.setQuantthresh(qt);
    aux.setQuantmap(qm);

    assertEquals(5000, aux.getQuantthresh().length);
    assertEquals(5000, aux.getQuantmap().length);
    assertEquals(499.9f, aux.getQuantthresh()[4999]);
    assertEquals(4999, aux.getQuantmap()[4999]);
  }

  @Test
  void shouldAllowNegativeValuesInQuantmap() {
    EncodeAuxThreshMatch aux = new EncodeAuxThreshMatch();

    int[] qm = {-1, -5, -10};
    aux.setQuantmap(qm);

    assertArrayEquals(qm, aux.getQuantmap());
  }

  @Test
  void shouldAllowSpecialFloatValuesInQuantthresh() {
    EncodeAuxThreshMatch aux = new EncodeAuxThreshMatch();

    float[] qt = {Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
    aux.setQuantthresh(qt);

    assertTrue(Float.isNaN(aux.getQuantthresh()[0]));
    assertEquals(Float.POSITIVE_INFINITY, aux.getQuantthresh()[1]);
    assertEquals(Float.NEGATIVE_INFINITY, aux.getQuantthresh()[2]);
  }

  @Test
  void gettersShouldReturnSameReferenceNotCopy() {
    EncodeAuxThreshMatch aux = new EncodeAuxThreshMatch();

    float[] qt = {1f, 2f, 3f};
    aux.setQuantthresh(qt);

    qt[0] = 99f;

    assertEquals(99f, aux.getQuantthresh()[0]);
  }

  @Test
  void shouldAllowZeroQuantvalsAndThreshvals() {
    EncodeAuxThreshMatch aux = new EncodeAuxThreshMatch();

    aux.setQuantvals(0);
    aux.setThreshvals(0);

    assertEquals(0, aux.getQuantvals());
    assertEquals(0, aux.getThreshvals());
  }

  @Test
  void shouldAllowNegativeQuantvalsAndThreshvals() {
    EncodeAuxThreshMatch aux = new EncodeAuxThreshMatch();

    aux.setQuantvals(-3);
    aux.setThreshvals(-7);

    assertEquals(-3, aux.getQuantvals());
    assertEquals(-7, aux.getThreshvals());
  }
}
