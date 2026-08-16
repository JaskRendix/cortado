package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KateWrapModeTest {

  @Test
  @DisplayName("Factory: Valid indices return correct wrap mode instances")
  void testCreateWrapModeValid() throws KateException {
    KateWrapMode wordMode = KateWrapMode.createWrapMode(0);
    assertNotNull(wordMode);
    assertEquals(KateWrapMode.KATE_WRAP_WORD, wordMode);

    KateWrapMode noneMode = KateWrapMode.createWrapMode(1);
    assertNotNull(noneMode);
    assertEquals(KateWrapMode.KATE_WRAP_NONE, noneMode);
  }

  @Test
  @DisplayName("Factory: Out of bounds negative index throws KateException")
  void testCreateWrapModeNegativeIndex() {
    assertThrows(
        KateException.class,
        () -> {
          KateWrapMode.createWrapMode(-1);
        });
  }

  @Test
  @DisplayName("Factory: Out of bounds positive index throws KateException")
  void testCreateWrapModeOutOfBoundsIndex() {
    assertThrows(
        KateException.class,
        () -> {
          KateWrapMode.createWrapMode(2);
        });
  }
}
