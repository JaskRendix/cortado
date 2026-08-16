package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KateBadPacketExceptionTest {

  @Test
  @DisplayName("Constructors: Default constructor sets default bad packet message")
  void testDefaultConstructor() {
    KateBadPacketException ex = new KateBadPacketException();
    assertEquals("Bad packet", ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  @DisplayName("Constructors: Message constructor sets custom message correctly")
  void testMessageConstructor() {
    String message = "Custom bad packet message";
    KateBadPacketException ex = new KateBadPacketException(message);
    assertEquals(message, ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  @DisplayName("Constructors: Message and cause constructor sets fields correctly")
  void testMessageAndCauseConstructor() {
    String message = "Custom bad packet message";
    Throwable cause = new RuntimeException("Root cause");
    KateBadPacketException ex = new KateBadPacketException(message, cause);
    assertEquals(message, ex.getMessage());
    assertEquals(cause, ex.getCause());
  }

  @Test
  @DisplayName("Constructors: Cause constructor sets cause correctly")
  void testCauseConstructor() {
    Throwable cause = new RuntimeException("Root cause");
    KateBadPacketException ex = new KateBadPacketException(cause);
    assertEquals(cause, ex.getCause());
  }
}
