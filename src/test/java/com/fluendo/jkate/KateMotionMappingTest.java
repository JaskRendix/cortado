package com.fluendo.jkate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KateMotionMappingTest {

    @Test
    @DisplayName("Factory: Valid indices return correct motion mapping instances")
    void testCreateMotionMappingValid() throws KateException {
        KateMotionMapping noneMapping = KateMotionMapping.CreateMotionMapping(0);
        assertNotNull(noneMapping);
        assertEquals(KateMotionMapping.KMM_NONE, noneMapping);
        assertEquals(KateMotionMapping.kmm_none, noneMapping);

        KateMotionMapping bitmapSizeMapping = KateMotionMapping.CreateMotionMapping(5);
        assertNotNull(bitmapSizeMapping);
        assertEquals(KateMotionMapping.KMM_BITMAP_SIZE, bitmapSizeMapping);
        assertEquals(KateMotionMapping.kmm_bitmap_size, bitmapSizeMapping);
    }

    @Test
    @DisplayName("Factory: Out of bounds negative index throws KateException")
    void testCreateMotionMappingNegativeIndex() {
        assertThrows(KateException.class, () -> {
            KateMotionMapping.CreateMotionMapping(-1);
        });
    }

    @Test
    @DisplayName("Factory: Out of bounds positive index throws KateException")
    void testCreateMotionMappingOutOfBoundsIndex() {
        assertThrows(KateException.class, () -> {
            KateMotionMapping.CreateMotionMapping(6);
        });
    }
}