package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KateTextDirectionalityTest {

  @Test
  @DisplayName("Factory: Valid indices return correct text directionality instances")
  void testCreateTextDirectionalityValid() throws KateException {
    KateTextDirectionality l2r = KateTextDirectionality.createTextDirectionality(0);
    assertNotNull(l2r);
    assertEquals(KateTextDirectionality.KATE_L2R_T2B, l2r);

    KateTextDirectionality t2bl2r = KateTextDirectionality.createTextDirectionality(3);
    assertNotNull(t2bl2r);
    assertEquals(KateTextDirectionality.KATE_T2B_L2R, t2bl2r);
  }

  @Test
  @DisplayName("Factory: Out of bounds negative index throws KateException")
  void testCreateTextDirectionalityNegativeIndex() {
    assertThrows(
        KateException.class,
        () -> {
          KateTextDirectionality.createTextDirectionality(-1);
        });
  }

  @Test
  @DisplayName("Factory: Out of bounds positive index throws KateException")
  void testCreateTextDirectionalityOutOfBoundsIndex() {
    assertThrows(
        KateException.class,
        () -> {
          KateTextDirectionality.createTextDirectionality(4);
        });
  }
}
