package com.fluendo.jkate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KateTextEncodingTest {

    @Test
    @DisplayName("Factory: Valid encoding index returns correct instance")
    void testCreateTextEncodingValid() throws KateException {
        KateTextEncoding encoding = KateTextEncoding.CreateTextEncoding(0);
        assertNotNull(encoding);
        assertEquals(KateTextEncoding.KATE_UTF8, encoding);
        assertEquals(KateTextEncoding.kate_utf8, encoding);
    }

    @Test
    @DisplayName("Factory: Out of bounds negative index throws KateException")
    void testCreateTextEncodingNegativeIndex() {
        assertThrows(KateException.class, () -> {
            KateTextEncoding.CreateTextEncoding(-1);
        });
    }

    @Test
    @DisplayName("Factory: Out of bounds positive index throws KateException")
    void testCreateTextEncodingOutOfBoundsIndex() {
        assertThrows(KateException.class, () -> {
            KateTextEncoding.CreateTextEncoding(99);
        });
    }
}
