package com.fluendo.jkate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KateMarkupTypeTest {

    @Test
    @DisplayName("Factory: Valid indices return correct markup type instances")
    void testCreateMarkupTypeValid() throws KateException {
        KateMarkupType noneType = KateMarkupType.CreateMarkupType(0);
        assertNotNull(noneType);
        assertEquals(KateMarkupType.KATE_MARKUP_NONE, noneType);
        assertEquals(KateMarkupType.kate_markup_none, noneType);

        KateMarkupType simpleType = KateMarkupType.CreateMarkupType(1);
        assertNotNull(simpleType);
        assertEquals(KateMarkupType.KATE_MARKUP_SIMPLE, simpleType);
        assertEquals(KateMarkupType.kate_markup_simple, simpleType);
    }

    @Test
    @DisplayName("Factory: Out of bounds negative index throws KateException")
    void testCreateMarkupTypeNegativeIndex() {
        assertThrows(KateException.class, () -> {
            KateMarkupType.CreateMarkupType(-1);
        });
    }

    @Test
    @DisplayName("Factory: Out of bounds positive index throws KateException")
    void testCreateMarkupTypeOutOfBoundsIndex() {
        assertThrows(KateException.class, () -> {
            KateMarkupType.CreateMarkupType(2);
        });
    }
}