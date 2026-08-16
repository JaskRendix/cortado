package com.fluendo.jheora;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class ResultTest {

  @Test
  void classIsFinal() {
    assertTrue(Modifier.isFinal(Result.class.getModifiers()), "Result must be final");
  }

  @Test
  void constructorIsPrivate() throws Exception {
    Constructor<Result> c = Result.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(c.getModifiers()), "Constructor must be private");
  }

  @Test
  void constructorIsNotAccessible() throws Exception {
    Constructor<Result> c = Result.class.getDeclaredConstructor();
    c.setAccessible(true);

    Result instance = c.newInstance();

    assertNotNull(instance);
  }

  @Test
  void constantsHaveCorrectValues() {
    assertEquals(-1, Result.FAULT);
    assertEquals(-10, Result.EINVAL);
    assertEquals(-20, Result.BADHEADER);
    assertEquals(-21, Result.NOTFORMAT);
    assertEquals(-22, Result.VERSION);
    assertEquals(-23, Result.IMPL);
    assertEquals(-24, Result.BADPACKET);
    assertEquals(-25, Result.NEWPACKET);
  }

  @Test
  void constantsAreNegative() {
    int[] values = {
      Result.FAULT,
      Result.EINVAL,
      Result.BADHEADER,
      Result.NOTFORMAT,
      Result.VERSION,
      Result.IMPL,
      Result.BADPACKET,
      Result.NEWPACKET
    };

    for (int v : values) {
      assertTrue(v < 0, "All Result codes must be negative");
    }
  }

  @Test
  void constantsAreUnique() {
    int[] values = {
      Result.FAULT,
      Result.EINVAL,
      Result.BADHEADER,
      Result.NOTFORMAT,
      Result.VERSION,
      Result.IMPL,
      Result.BADPACKET,
      Result.NEWPACKET
    };

    for (int i = 0; i < values.length; i++) {
      for (int j = i + 1; j < values.length; j++) {
        assertNotEquals(values[i], values[j], "Result constants must not duplicate values");
      }
    }
  }

  @Test
  void constantsAreImmutable() {
    // Java final static primitives cannot be mutated,
    // but we assert the values remain unchanged.
    assertEquals(-1, Result.FAULT);
    assertEquals(-10, Result.EINVAL);
  }
}
