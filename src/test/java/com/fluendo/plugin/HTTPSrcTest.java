package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("HTTPSrc Test Suite")
class HTTPSrcTest {

  private HTTPSrc httpSrc;

  @BeforeEach
  void setUp() {
    httpSrc = new HTTPSrc();
  }

  @Nested
  @DisplayName("Property Setter Tests")
  class PropertyTests {

    @Test
    @DisplayName("Should successfully set valid properties")
    void testSetValidProperties() throws Exception {
      assertTrue(httpSrc.setProperty("url", "http://example.com/stream.ogg"));
      assertTrue(httpSrc.setProperty("documentBase", new URL("http://example.com")));
      assertTrue(httpSrc.setProperty("userId", "admin"));
      assertTrue(httpSrc.setProperty("password", "secret"));
      assertTrue(httpSrc.setProperty("userAgent", "CustomAgent"));
      assertTrue(httpSrc.setProperty("readSize", "8192"));
    }

    @Test
    @DisplayName("Should handle null values for optional properties safely")
    void testSetNullProperties() {
      assertTrue(httpSrc.setProperty("userId", null));
      assertTrue(httpSrc.setProperty("password", null));
    }

    @Test
    @DisplayName("Should allow null userAgent")
    void testSetNullUserAgent() {
      assertTrue(httpSrc.setProperty("userAgent", null));
    }

    @Test
    @DisplayName("Should allow null URL")
    void testSetNullUrl() {
      assertTrue(httpSrc.setProperty("url", null));
    }

    @Test
    @DisplayName("Should allow null documentBase")
    void testSetNullDocumentBase() {
      assertTrue(httpSrc.setProperty("documentBase", null));
    }

    @Test
    @DisplayName("Should reject unknown property names")
    void testSetInvalidProperty() {
      assertFalse(httpSrc.setProperty("nonExistentProperty", "value"));
    }

    @Test
    @DisplayName("Should throw NumberFormatException for invalid readSize")
    void testInvalidReadSize() {
      assertThrows(
          NumberFormatException.class,
          () -> httpSrc.setProperty("readSize", "abc"));
    }
  }

  @Nested
  @DisplayName("Factory and Initialization Tests")
  class InitializationTests {

    @Test
    @DisplayName("Should return correct factory name")
    void testGetFactoryName() {
      assertEquals("httpsrc", httpSrc.getFactoryName());
    }

    @Test
    @DisplayName("Should instantiate under Microsoft JVM vendor setting")
    void testMicrosoftJvmDetection() {
      String originalVendor = System.getProperty("java.vendor");

      try {
        System.setProperty("java.vendor", "Microsoft Corporation");

        HTTPSrc msSrc = new HTTPSrc();

        assertEquals("httpsrc", msSrc.getFactoryName());
      } finally {
        if (originalVendor != null) {
          System.setProperty("java.vendor", originalVendor);
        } else {
          System.clearProperty("java.vendor");
        }
      }
    }
  }
}
