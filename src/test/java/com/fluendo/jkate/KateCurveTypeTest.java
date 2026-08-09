package com.fluendo.jkate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KateCurveTypeTest {

    @Test
    @DisplayName("Factory: Valid indices return correct curve type instances")
    void testCreateCurveTypeValid() throws KateException {
        KateCurveType noneType = KateCurveType.CreateCurveType(0);
        assertNotNull(noneType);
        assertEquals(KateCurveType.KATE_CURVE_NONE, noneType);
        assertEquals(KateCurveType.kate_curve_none, noneType);

        KateCurveType bsplineType = KateCurveType.CreateCurveType(5);
        assertNotNull(bsplineType);
        assertEquals(KateCurveType.KATE_CURVE_BSPLINE, bsplineType);
        assertEquals(KateCurveType.kate_curve_bspline, bsplineType);
    }

    @Test
    @DisplayName("Factory: Out of bounds negative index throws KateException")
    void testCreateCurveTypeNegativeIndex() {
        assertThrows(KateException.class, () -> {
            KateCurveType.CreateCurveType(-1);
        });
    }

    @Test
    @DisplayName("Factory: Out of bounds positive index throws KateException")
    void testCreateCurveTypeOutOfBoundsIndex() {
        assertThrows(KateException.class, () -> {
            KateCurveType.CreateCurveType(6);
        });
    }
}