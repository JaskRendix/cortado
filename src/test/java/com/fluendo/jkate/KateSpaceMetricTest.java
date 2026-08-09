package com.fluendo.jkate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KateSpaceMetricTest {

    @Test
    @DisplayName("Factory: Valid indices return correct space metric instances")
    void testCreateSpaceMetricValid() throws KateException {
        KateSpaceMetric pixels = KateSpaceMetric.CreateSpaceMetric(0);
        assertNotNull(pixels);
        assertEquals(KateSpaceMetric.KATE_METRIC_PIXELS, pixels);
        assertEquals(KateSpaceMetric.kate_metric_pixels, pixels);

        KateSpaceMetric percentage = KateSpaceMetric.CreateSpaceMetric(1);
        assertNotNull(percentage);
        assertEquals(KateSpaceMetric.KATE_METRIC_PERCENTAGE, percentage);
        assertEquals(KateSpaceMetric.kate_metric_percentage, percentage);

        KateSpaceMetric millionths = KateSpaceMetric.CreateSpaceMetric(2);
        assertNotNull(millionths);
        assertEquals(KateSpaceMetric.KATE_METRIC_MILLIONTHS, millionths);
        assertEquals(KateSpaceMetric.kate_metric_millionths, millionths);
    }

    @Test
    @DisplayName("Factory: Out of bounds negative index throws KateException")
    void testCreateSpaceMetricNegativeIndex() {
        assertThrows(KateException.class, () -> {
            KateSpaceMetric.CreateSpaceMetric(-1);
        });
    }

    @Test
    @DisplayName("Factory: Out of bounds positive index throws KateException")
    void testCreateSpaceMetricOutOfBoundsIndex() {
        assertThrows(KateException.class, () -> {
            KateSpaceMetric.CreateSpaceMetric(3);
        });
    }
}
