package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KateBitmapTypeTest {

  @Test
  @DisplayName("Factory: Valid indices return correct bitmap type instances")
  void testCreateBitmapTypeValid() throws KateException {
    KateBitmapType palettedType = KateBitmapType.createBitmapType(0);
    assertNotNull(palettedType);
    assertEquals(KateBitmapType.KATE_BITMAP_TYPE_PALETTED, palettedType);

    KateBitmapType pngType = KateBitmapType.createBitmapType(1);
    assertNotNull(pngType);
    assertEquals(KateBitmapType.KATE_BITMAP_TYPE_PNG, pngType);
  }

  @Test
  @DisplayName("Factory: Out of bounds negative index throws KateException")
  void testCreateBitmapTypeNegativeIndex() {
    assertThrows(
        KateException.class,
        () -> {
          KateBitmapType.createBitmapType(-1);
        });
  }

  @Test
  @DisplayName("Factory: Out of bounds positive index throws KateException")
  void testCreateBitmapTypeOutOfBoundsIndex() {
    assertThrows(
        KateException.class,
        () -> {
          KateBitmapType.createBitmapType(2);
        });
  }
}
