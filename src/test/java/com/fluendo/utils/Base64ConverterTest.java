package com.fluendo.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Base64ConverterTest {

  @Test
  @DisplayName("Base64: Empty byte array encoding")
  void testEncodeEmptyArray() {
    byte[] input = new byte[0];
    String result = Base64Converter.encode(input);
    assertNotNull(result);
    assertEquals("", result);
  }

  @Test
  @DisplayName("Base64: Exact multiple of 3 bytes (No padding required)")
  void testEncodeMultipleOfThree() {
    byte[] input = "Man".getBytes(StandardCharsets.US_ASCII);
    String result = Base64Converter.encode(input);
    assertEquals("TWFu", result);
  }

  @Test
  @DisplayName("Base64: Exact multiple of 3 bytes check precise value")
  void testEncodeExactMatch() {
    byte[] input = "abc".getBytes(StandardCharsets.US_ASCII);
    assertEquals("YWJj", Base64Converter.encode(input));
  }

  @Test
  @DisplayName("Base64: Remainder of 2 bytes (Single '=' padding)")
  void testEncodeOnePaddingByte() {
    byte[] input = "Ma".getBytes(StandardCharsets.US_ASCII);
    String result = Base64Converter.encode(input);
    assertEquals("TWE=", result);
  }

  @Test
  @DisplayName("Base64: Remainder of 1 byte (Double '==' padding)")
  void testEncodeTwoPaddingBytes() {
    byte[] input = "M".getBytes(StandardCharsets.US_ASCII);
    String result = Base64Converter.encode(input);
    assertEquals("TQ==", result);
  }

  @Test
  @DisplayName("Base64: Binary/Raw bytes including zero-bytes and high bits")
  void testEncodeBinaryData() {
    byte[] input = new byte[] {0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0x80};
    String result = Base64Converter.encode(input);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    String expected = java.util.Base64.getEncoder().encodeToString(input);
    assertEquals(expected, result);
  }
}
