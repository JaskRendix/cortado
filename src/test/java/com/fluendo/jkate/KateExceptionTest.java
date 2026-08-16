package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KateExceptionTest {

  @Test
  @DisplayName("Constructors: Default constructor initializes properly")
  void testDefaultConstructor() {
    KateException ex = new KateException();
    assertNull(ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  @DisplayName("Constructors: Message constructor sets message correctly")
  void testMessageConstructor() {
    String message = "Custom error message";
    KateException ex = new KateException(message);
    assertEquals(message, ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  @DisplayName("Constructors: Message and cause constructor sets fields correctly")
  void testMessageAndCauseConstructor() {
    String message = "Custom error message";
    Throwable cause = new RuntimeException("Root cause");
    KateException ex = new KateException(message, cause);
    assertEquals(message, ex.getMessage());
    assertEquals(cause, ex.getCause());
  }

  @Test
  @DisplayName("Constructors: Cause constructor sets cause correctly")
  void testCauseConstructor() {
    Throwable cause = new RuntimeException("Root cause");
    KateException ex = new KateException(cause);
    assertEquals(cause, ex.getCause());
  }
}
