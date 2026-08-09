package com.fluendo.jkate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KateMotionSemanticsTest {

    @Test
    @DisplayName("Factory: Valid indices return correct motion semantics instances")
    void testCreateMotionSemanticsValid() throws KateException {
        KateMotionSemantics timeSemantics = KateMotionSemantics.CreateMotionSemantics(0);
        assertNotNull(timeSemantics);
        assertEquals(KateMotionSemantics.KMS_TIME, timeSemantics);
        assertEquals(KateMotionSemantics.kms_time, timeSemantics);

        KateMotionSemantics drawWidthSemantics = KateMotionSemantics.CreateMotionSemantics(39);
        assertNotNull(drawWidthSemantics);
        assertEquals(KateMotionSemantics.KMS_DRAW_WIDTH, drawWidthSemantics);
        assertEquals(KateMotionSemantics.kms_draw_width, drawWidthSemantics);
    }

    @Test
    @DisplayName("Factory: Out of bounds negative index throws KateException")
    void testCreateMotionSemanticsNegativeIndex() {
        assertThrows(KateException.class, () -> {
            KateMotionSemantics.CreateMotionSemantics(-1);
        });
    }

    @Test
    @DisplayName("Factory: Out of bounds positive index throws KateException")
    void testCreateMotionSemanticsOutOfBoundsIndex() {
        assertThrows(KateException.class, () -> {
            KateMotionSemantics.CreateMotionSemantics(40);
        });
    }
}
