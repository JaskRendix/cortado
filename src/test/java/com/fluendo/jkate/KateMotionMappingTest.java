package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KateMotionMappingTest {

  @Test
  @DisplayName("Factory: Valid indices return correct motion mapping instances")
  void testCreateMotionMappingValid() throws KateException {
    KateMotionMapping noneMapping = KateMotionMapping.createMotionMapping(0);
    assertNotNull(noneMapping);
    assertEquals(KateMotionMapping.KMM_NONE, noneMapping);

    KateMotionMapping bitmapSizeMapping = KateMotionMapping.createMotionMapping(5);
    assertNotNull(bitmapSizeMapping);
    assertEquals(KateMotionMapping.KMM_BITMAP_SIZE, bitmapSizeMapping);
  }

  @Test
  @DisplayName("Factory: Out of bounds negative index throws KateException")
  void testCreateMotionMappingNegativeIndex() {
    assertThrows(
        KateException.class,
        () -> {
          KateMotionMapping.createMotionMapping(-1);
        });
  }

  @Test
  @DisplayName("Factory: Out of bounds positive index throws KateException")
  void testCreateMotionMappingOutOfBoundsIndex() {
    assertThrows(
        KateException.class,
        () -> {
          KateMotionMapping.createMotionMapping(6);
        });
  }
}
