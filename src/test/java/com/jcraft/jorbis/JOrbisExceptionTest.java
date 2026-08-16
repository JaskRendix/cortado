package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class JOrbisExceptionTest {

  @Test
  void defaultConstructorShouldHaveNullMessage() {
    JOrbisException ex = new JOrbisException();
    assertNull(ex.getMessage());
  }

  @Test
  void messageConstructorShouldPrefixMessage() {
    JOrbisException ex = new JOrbisException("failure");
    assertEquals("JOrbis: failure", ex.getMessage());
  }

  @Test
  void messageConstructorShouldHandleNullMessage() {
    JOrbisException ex = new JOrbisException(null);
    assertEquals("JOrbis: null", ex.getMessage());
  }

  @Test
  void exceptionShouldBeInstanceOfException() {
    JOrbisException ex = new JOrbisException("x");
    assertTrue(ex instanceof Exception);
  }

  @Test
  void exceptionShouldBeThrowable() {
    JOrbisException ex = new JOrbisException("x");
    assertTrue(ex instanceof Throwable);
  }

  @Test
  void exceptionShouldBeCatchable() {
    try {
      throw new JOrbisException("boom");
    } catch (JOrbisException ex) {
      assertEquals("JOrbis: boom", ex.getMessage());
    }
  }
}
