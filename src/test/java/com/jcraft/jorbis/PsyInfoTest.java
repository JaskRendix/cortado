package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PsyInfoTest {

  @Test
  void defaultConstructorShouldInitializeArraysAndScalars() {
    PsyInfo pi = new PsyInfo();

    assertEquals(0, pi.athp);
    assertEquals(0, pi.decayp);
    assertEquals(0, pi.smoothp);
    assertEquals(0, pi.noisefitp);
    assertEquals(0, pi.noisefitSubblock);
    assertEquals(0f, pi.noisefitThreshdB);
    assertEquals(0f, pi.athAtt);
    assertEquals(0, pi.tonemaskp);
    assertEquals(0, pi.peakattp);
    assertEquals(0, pi.noisemaskp);
    assertEquals(0f, pi.maxCurveDb);
    assertEquals(0f, pi.attackCoeff);
    assertEquals(0f, pi.decayCoeff);

    // All arrays must exist and have length 5
    assertEquals(5, pi.toneatt125Hz.length);
    assertEquals(5, pi.toneatt250Hz.length);
    assertEquals(5, pi.toneatt500Hz.length);
    assertEquals(5, pi.toneatt1000Hz.length);
    assertEquals(5, pi.toneatt2000Hz.length);
    assertEquals(5, pi.toneatt4000Hz.length);
    assertEquals(5, pi.toneatt8000Hz.length);

    assertEquals(5, pi.peakatt125Hz.length);
    assertEquals(5, pi.peakatt250Hz.length);
    assertEquals(5, pi.peakatt500Hz.length);
    assertEquals(5, pi.peakatt1000Hz.length);
    assertEquals(5, pi.peakatt2000Hz.length);
    assertEquals(5, pi.peakatt4000Hz.length);
    assertEquals(5, pi.peakatt8000Hz.length);

    assertEquals(5, pi.noiseatt125Hz.length);
    assertEquals(5, pi.noiseatt250Hz.length);
    assertEquals(5, pi.noiseatt500Hz.length);
    assertEquals(5, pi.noiseatt1000Hz.length);
    assertEquals(5, pi.noiseatt2000Hz.length);
    assertEquals(5, pi.noiseatt4000Hz.length);
    assertEquals(5, pi.noiseatt8000Hz.length);
  }

  @Test
  void fieldsShouldStoreScalarValuesCorrectly() {
    PsyInfo pi = new PsyInfo();

    pi.athp = 1;
    pi.decayp = 2;
    pi.smoothp = 3;
    pi.noisefitp = 4;
    pi.noisefitSubblock = 5;
    pi.noisefitThreshdB = 6.5f;
    pi.athAtt = 7.5f;
    pi.tonemaskp = 8;
    pi.peakattp = 9;
    pi.noisemaskp = 10;
    pi.maxCurveDb = 11.5f;
    pi.attackCoeff = 12.5f;
    pi.decayCoeff = 13.5f;

    assertEquals(1, pi.athp);
    assertEquals(2, pi.decayp);
    assertEquals(3, pi.smoothp);
    assertEquals(4, pi.noisefitp);
    assertEquals(5, pi.noisefitSubblock);
    assertEquals(6.5f, pi.noisefitThreshdB);
    assertEquals(7.5f, pi.athAtt);
    assertEquals(8, pi.tonemaskp);
    assertEquals(9, pi.peakattp);
    assertEquals(10, pi.noisemaskp);
    assertEquals(11.5f, pi.maxCurveDb);
    assertEquals(12.5f, pi.attackCoeff);
    assertEquals(13.5f, pi.decayCoeff);
  }

  @Test
  void arraysShouldStoreValuesCorrectly() {
    PsyInfo pi = new PsyInfo();

    float[] arr = {1f, 2f, 3f, 4f, 5f};

    System.arraycopy(arr, 0, pi.toneatt125Hz, 0, arr.length);
    assertArrayEquals(arr, pi.toneatt125Hz);

    System.arraycopy(arr, 0, pi.peakatt4000Hz, 0, arr.length);
    assertArrayEquals(arr, pi.peakatt4000Hz);

    System.arraycopy(arr, 0, pi.noiseatt8000Hz, 0, arr.length);
    assertArrayEquals(arr, pi.noiseatt8000Hz);
  }

  @Test
  void arraysShouldSupportSpecialFloatValues() {
    PsyInfo pi = new PsyInfo();

    float[] special = {Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, 0f, -1f};

    System.arraycopy(special, 0, pi.toneatt500Hz, 0, special.length);

    assertTrue(Float.isNaN(pi.toneatt500Hz[0]));
    assertEquals(Float.POSITIVE_INFINITY, pi.toneatt500Hz[1]);
    assertEquals(Float.NEGATIVE_INFINITY, pi.toneatt500Hz[2]);
    assertEquals(0f, pi.toneatt500Hz[3]);
    assertEquals(-1f, pi.toneatt500Hz[4]);
  }

  @Test
  void freeShouldNotThrow() {
    PsyInfo pi = new PsyInfo();
    assertDoesNotThrow(pi::free);
  }
}
