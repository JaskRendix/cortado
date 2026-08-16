package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResultTest {

  @Test
  @DisplayName("Constants: Verifies correct error code mappings")
  void testErrorCodes() {
    assertEquals(-1, Result.KATE_E_NOT_FOUND);
    assertEquals(-2, Result.KATE_E_INVALID_PARAMETER);
    assertEquals(-3, Result.KATE_E_OUT_OF_MEMORY);
    assertEquals(-4, Result.KATE_E_BAD_GRANULE);
    assertEquals(-5, Result.KATE_E_INIT);
    assertEquals(-6, Result.KATE_E_BAD_PACKET);
    assertEquals(-7, Result.KATE_E_TEXT);
    assertEquals(-8, Result.KATE_E_LIMIT);
    assertEquals(-9, Result.KATE_E_VERSION);
    assertEquals(-10, Result.KATE_E_NOT_KATE);
    assertEquals(-11, Result.KATE_E_BAD_TAG);
  }

  @Test
  @DisplayName("Utility Class Constructor: Throws exception on instantiation reflection attempt")
  void testPrivateConstructor() throws Exception {
    Constructor<Result> constructor = Result.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));

    constructor.setAccessible(true);
    InvocationTargetException exception =
        assertThrows(
            InvocationTargetException.class,
            () -> {
              constructor.newInstance();
            });

    assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
  }
}
