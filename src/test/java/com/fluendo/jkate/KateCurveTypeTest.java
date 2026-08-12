package com.fluendo.jkate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KateCurveTypeTest {

    @Test
    @DisplayName("Factory: Valid indices return correct curve type instances")
    void testCreateCurveTypeValid() throws KateException {
        KateCurveType noneType = KateCurveType.createCurveType(0);
        assertNotNull(noneType);
        assertEquals(KateCurveType.KATE_CURVE_NONE, noneType);

        KateCurveType bsplineType = KateCurveType.createCurveType(5);
        assertNotNull(bsplineType);
        assertEquals(KateCurveType.KATE_CURVE_BSPLINE, bsplineType);
    }

    @Test
    @DisplayName("Factory: Out of bounds negative index throws KateException")
    void testCreateCurveTypeNegativeIndex() {
        assertThrows(KateException.class, () -> {
            KateCurveType.createCurveType(-1);
        });
    }

    @Test
    @DisplayName("Factory: Out of bounds positive index throws KateException")
    void testCreateCurveTypeOutOfBoundsIndex() {
        assertThrows(KateException.class, () -> {
            KateCurveType.createCurveType(6);
        });
    }
}
