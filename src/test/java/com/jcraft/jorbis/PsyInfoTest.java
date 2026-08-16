package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PsyInfoTest {

  @Test
  void defaultConstructorShouldInitializeArraysAndScalars() {
    PsyInfo pi = new PsyInfo();

    assertEquals(0, pi.getAthp());
    assertEquals(0, pi.getDecayp());
    assertEquals(0, pi.getSmoothp());
    assertEquals(0, pi.getNoisefitp());
    assertEquals(0, pi.getNoisefitSubblock());
    assertEquals(0f, pi.getNoisefitThreshdB());
    assertEquals(0f, pi.getAthAtt());
    assertEquals(0, pi.getTonemaskp());
    assertEquals(0, pi.getPeakattp());
    assertEquals(0, pi.getNoisemaskp());
    assertEquals(0f, pi.getMaxCurveDb());
    assertEquals(0f, pi.getAttackCoeff());
    assertEquals(0f, pi.getDecayCoeff());

    // All arrays must exist and have length 5
    assertEquals(5, pi.getToneatt125Hz().length);
    assertEquals(5, pi.getToneatt250Hz().length);
    assertEquals(5, pi.getToneatt500Hz().length);
    assertEquals(5, pi.getToneatt1000Hz().length);
    assertEquals(5, pi.getToneatt2000Hz().length);
    assertEquals(5, pi.getToneatt4000Hz().length);
    assertEquals(5, pi.getToneatt8000Hz().length);

    assertEquals(5, pi.getPeakatt125Hz().length);
    assertEquals(5, pi.getPeakatt250Hz().length);
    assertEquals(5, pi.getPeakatt500Hz().length);
    assertEquals(5, pi.getPeakatt1000Hz().length);
    assertEquals(5, pi.getPeakatt2000Hz().length);
    assertEquals(5, pi.getPeakatt4000Hz().length);
    assertEquals(5, pi.getPeakatt8000Hz().length);

    assertEquals(5, pi.getNoiseatt125Hz().length);
    assertEquals(5, pi.getNoiseatt250Hz().length);
    assertEquals(5, pi.getNoiseatt500Hz().length);
    assertEquals(5, pi.getNoiseatt1000Hz().length);
    assertEquals(5, pi.getNoiseatt2000Hz().length);
    assertEquals(5, pi.getNoiseatt4000Hz().length);
    assertEquals(5, pi.getNoiseatt8000Hz().length);
  }

  @Test
  void settersShouldStoreScalarValuesCorrectly() {
    PsyInfo pi = new PsyInfo();

    pi.setAthp(1);
    pi.setDecayp(2);
    pi.setSmoothp(3);
    pi.setNoisefitp(4);
    pi.setNoisefitSubblock(5);
    pi.setNoisefitThreshdB(6.5f);
    pi.setAthAtt(7.5f);
    pi.setTonemaskp(8);
    pi.setPeakattp(9);
    pi.setNoisemaskp(10);
    pi.setMaxCurveDb(11.5f);
    pi.setAttackCoeff(12.5f);
    pi.setDecayCoeff(13.5f);

    assertEquals(1, pi.getAthp());
    assertEquals(2, pi.getDecayp());
    assertEquals(3, pi.getSmoothp());
    assertEquals(4, pi.getNoisefitp());
    assertEquals(5, pi.getNoisefitSubblock());
    assertEquals(6.5f, pi.getNoisefitThreshdB());
    assertEquals(7.5f, pi.getAthAtt());
    assertEquals(8, pi.getTonemaskp());
    assertEquals(9, pi.getPeakattp());
    assertEquals(10, pi.getNoisemaskp());
    assertEquals(11.5f, pi.getMaxCurveDb());
    assertEquals(12.5f, pi.getAttackCoeff());
    assertEquals(13.5f, pi.getDecayCoeff());
  }

  @Test
  void settersShouldReplaceArraysCorrectly() {
    PsyInfo pi = new PsyInfo();

    float[] arr = {1f, 2f, 3f, 4f, 5f};

    pi.setToneatt125Hz(arr);
    assertSame(arr, pi.getToneatt125Hz());

    pi.setPeakatt4000Hz(arr);
    assertSame(arr, pi.getPeakatt4000Hz());

    pi.setNoiseatt8000Hz(arr);
    assertSame(arr, pi.getNoiseatt8000Hz());
  }

  @Test
  void settersShouldAcceptNullArrays() {
    PsyInfo pi = new PsyInfo();

    pi.setToneatt125Hz(null);
    pi.setPeakatt125Hz(null);
    pi.setNoiseatt125Hz(null);

    assertNull(pi.getToneatt125Hz());
    assertNull(pi.getPeakatt125Hz());
    assertNull(pi.getNoiseatt125Hz());
  }

  @Test
  void arraysShouldSupportSpecialFloatValues() {
    PsyInfo pi = new PsyInfo();

    float[] special = {Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, 0f, -1f};

    pi.setToneatt500Hz(special);

    assertTrue(Float.isNaN(pi.getToneatt500Hz()[0]));
    assertEquals(Float.POSITIVE_INFINITY, pi.getToneatt500Hz()[1]);
    assertEquals(Float.NEGATIVE_INFINITY, pi.getToneatt500Hz()[2]);
    assertEquals(0f, pi.getToneatt500Hz()[3]);
    assertEquals(-1f, pi.getToneatt500Hz()[4]);
  }

  @Test
  void deprecatedToneattMethodsShouldReturnSameArrays() {
    PsyInfo pi = new PsyInfo();

    float[] arr = {1f, 2f, 3f, 4f, 5f};
    pi.setToneatt1000Hz(arr);

    assertSame(arr, pi.toneatt_1000Hz());
  }

  @Test
  void deprecatedPeakattMethodsShouldReturnSameArrays() {
    PsyInfo pi = new PsyInfo();

    float[] arr = {9f, 8f, 7f, 6f, 5f};
    pi.setPeakatt2000Hz(arr);

    assertSame(arr, pi.peakatt_2000Hz());
  }

  @Test
  void deprecatedNoiseattMethodsShouldReturnSameArrays() {
    PsyInfo pi = new PsyInfo();

    float[] arr = {3f, 3f, 3f, 3f, 3f};
    pi.setNoiseatt4000Hz(arr);

    assertSame(arr, pi.noiseatt_4000Hz());
  }

  @Test
  void freeShouldNotThrow() {
    PsyInfo pi = new PsyInfo();
    assertDoesNotThrow(pi::free);
  }
}
