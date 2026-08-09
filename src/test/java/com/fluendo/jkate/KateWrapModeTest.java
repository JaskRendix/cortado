package com.fluendo.jkate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KateWrapModeTest {

    @Test
    @DisplayName("Factory: Valid indices return correct wrap mode instances")
    void testCreateWrapModeValid() throws KateException {
        KateWrapMode wordMode = KateWrapMode.CreateWrapMode(0);
        assertNotNull(wordMode);
        assertEquals(KateWrapMode.KATE_WRAP_WORD, wordMode);
        assertEquals(KateWrapMode.kate_wrap_word, wordMode);

        KateWrapMode noneMode = KateWrapMode.CreateWrapMode(1);
        assertNotNull(noneMode);
        assertEquals(KateWrapMode.KATE_WRAP_NONE, noneMode);
        assertEquals(KateWrapMode.kate_wrap_none, noneMode);
    }

    @Test
    @DisplayName("Factory: Out of bounds negative index throws KateException")
    void testCreateWrapModeNegativeIndex() {
        assertThrows(KateException.class, () -> {
            KateWrapMode.CreateWrapMode(-1);
        });
    }

    @Test
    @DisplayName("Factory: Out of bounds positive index throws KateException")
    void testCreateWrapModeOutOfBoundsIndex() {
        assertThrows(KateException.class, () -> {
            KateWrapMode.CreateWrapMode(2);
        });
    }
}