package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KateSpaceMetricTest {

  @Test
  @DisplayName("Factory: Valid indices return correct space metric instances")
  void testCreateSpaceMetricValid() throws KateException {
    KateSpaceMetric pixels = KateSpaceMetric.createSpaceMetric(0);
    assertNotNull(pixels);
    assertEquals(KateSpaceMetric.KATE_METRIC_PIXELS, pixels);

    KateSpaceMetric percentage = KateSpaceMetric.createSpaceMetric(1);
    assertNotNull(percentage);
    assertEquals(KateSpaceMetric.KATE_METRIC_PERCENTAGE, percentage);

    KateSpaceMetric millionths = KateSpaceMetric.createSpaceMetric(2);
    assertNotNull(millionths);
    assertEquals(KateSpaceMetric.KATE_METRIC_MILLIONTHS, millionths);
  }

  @Test
  @DisplayName("Factory: Out of bounds negative index throws KateException")
  void testCreateSpaceMetricNegativeIndex() {
    assertThrows(
        KateException.class,
        () -> {
          KateSpaceMetric.createSpaceMetric(-1);
        });
  }

  @Test
  @DisplayName("Factory: Out of bounds positive index throws KateException")
  void testCreateSpaceMetricOutOfBoundsIndex() {
    assertThrows(
        KateException.class,
        () -> {
          KateSpaceMetric.createSpaceMetric(3);
        });
  }
}
