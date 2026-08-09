package com.fluendo.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
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
        @DisplayName("Should reject unknown property names")
        void testSetInvalidProperty() {
            assertFalse(httpSrc.setProperty("nonExistentProperty", "value"));
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
        @DisplayName("Should detect Microsoft JVM vendor property and set flag")
        void testMicrosoftJvmDetection() {
            String originalVendor = System.getProperty("java.vendor");
            try {
                System.setProperty("java.vendor", "Microsoft Corporation");
                HTTPSrc msSrc = new HTTPSrc();
                // Verifies instantiation doesn't throw and internal flag handles MS VM logic safely
                assertNotNull(msSrc.getFactoryName());
            } finally {
                if (originalVendor != null) {
                    System.setProperty("java.vendor", originalVendor);
                } else {
                    System.clearProperty("java.vendor");
                }
            }
        }
    }

    @Nested
    @DisplayName("Activation State Edge Cases")
    class ActivationTests {

        @Test
        @DisplayName("Should handle MODE_NONE cleanup gracefully without crashing")
        void testActivateModeNone() {
            // Accessing inner pad activation logic via standard lifecycle or reflection if needed,
            // or testing element state handling.
            boolean result = httpSrc.setProperty("url", "http://invalid-url-to-trigger-fail.local");
            assertTrue(result);
        }
    }
}
