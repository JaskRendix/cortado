package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KateMarkupTypeTest {

  @Test
  @DisplayName("Factory: Valid indices return correct markup type instances")
  void testCreateMarkupTypeValid() throws KateException {
    KateMarkupType noneType = KateMarkupType.createMarkupType(0);
    assertNotNull(noneType);
    assertEquals(KateMarkupType.KATE_MARKUP_NONE, noneType);

    KateMarkupType simpleType = KateMarkupType.createMarkupType(1);
    assertNotNull(simpleType);
    assertEquals(KateMarkupType.KATE_MARKUP_SIMPLE, simpleType);
  }

  @Test
  @DisplayName("Factory: Out of bounds negative index throws KateException")
  void testCreateMarkupTypeNegativeIndex() {
    assertThrows(
        KateException.class,
        () -> {
          KateMarkupType.createMarkupType(-1);
        });
  }

  @Test
  @DisplayName("Factory: Out of bounds positive index throws KateException")
  void testCreateMarkupTypeOutOfBoundsIndex() {
    assertThrows(
        KateException.class,
        () -> {
          KateMarkupType.createMarkupType(2);
        });
  }
}
