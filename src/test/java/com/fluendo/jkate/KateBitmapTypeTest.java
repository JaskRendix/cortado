package com.fluendo.jkate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KateBitmapTypeTest {

    @Test
    @DisplayName("Factory: Valid indices return correct bitmap type instances")
    void testCreateBitmapTypeValid() throws KateException {
        KateBitmapType palettedType = KateBitmapType.CreateBitmapType(0);
        assertNotNull(palettedType);
        assertEquals(KateBitmapType.KATE_BITMAP_TYPE_PALETTED, palettedType);
        assertEquals(KateBitmapType.kate_bitmap_type_paletted, palettedType);

        KateBitmapType pngType = KateBitmapType.CreateBitmapType(1);
        assertNotNull(pngType);
        assertEquals(KateBitmapType.KATE_BITMAP_TYPE_PNG, pngType);
        assertEquals(KateBitmapType.kate_bitmap_type_png, pngType);
    }

    @Test
    @DisplayName("Factory: Out of bounds negative index throws KateException")
    void testCreateBitmapTypeNegativeIndex() {
        assertThrows(KateException.class, () -> {
            KateBitmapType.CreateBitmapType(-1);
        });
    }

    @Test
    @DisplayName("Factory: Out of bounds positive index throws KateException")
    void testCreateBitmapTypeOutOfBoundsIndex() {
        assertThrows(KateException.class, () -> {
            KateBitmapType.CreateBitmapType(2);
        });
    }
}