package com.fluendo.jheora;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class VersionTest {

    @Test
    void classIsFinal() {
        assertTrue(Modifier.isFinal(Version.class.getModifiers()),
                "Version must be final");
    }

    @Test
    void constructorIsPrivate() throws Exception {
        Constructor<Version> c = Version.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(c.getModifiers()),
                "Constructor must be private");
    }

    @Test
    void constructorCanBeInvokedViaReflection() throws Exception {
        Constructor<Version> c = Version.class.getDeclaredConstructor();
        c.setAccessible(true);
        Version v = c.newInstance();
        assertNotNull(v, "Reflection should instantiate Version");
    }

    @Test
    void versionConstantsHaveCorrectValues() {
        assertEquals(3, Version.VERSION_MAJOR);
        assertEquals(2, Version.VERSION_MINOR);
        assertEquals(0, Version.VERSION_SUB);
    }

    @Test
    void versionStringIsCorrect() {
        assertEquals("Xiph.Org libTheora I 20040317 3 2 0",
                Version.getVersionString());
    }

    @Test
    void versionNumberIsEncodedCorrectly() {
        int expected = (3 << 16) + (2 << 8) + 0;
        assertEquals(expected, Version.getVersionNumber());
    }

    @Test
    void versionNumberIsStable() {
        int v1 = Version.getVersionNumber();
        int v2 = Version.getVersionNumber();
        assertEquals(v1, v2, "Version number must be stable across calls");
    }

    @Test
    void versionStringIsImmutable() {
        String s1 = Version.getVersionString();
        String s2 = Version.getVersionString();
        assertSame(s1, s2, "Vendor string must be constant and immutable");
    }

    @Test
    void versionConstantsAreNonNegative() {
        assertTrue(Version.VERSION_MAJOR >= 0);
        assertTrue(Version.VERSION_MINOR >= 0);
        assertTrue(Version.VERSION_SUB >= 0);
    }

    @Test
    void versionConstantsAreUnique() {
        assertNotEquals(Version.VERSION_MAJOR, Version.VERSION_MINOR);
        assertNotEquals(Version.VERSION_MINOR, Version.VERSION_SUB);
    }
}
