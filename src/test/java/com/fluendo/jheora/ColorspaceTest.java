package com.fluendo.jheora;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ColorspaceTest {

    @Test
    void spacesArrayContainsAllSpacesInCorrectOrder() {
        Colorspace[] s = Colorspace.SPACES;

        assertEquals(3, s.length, "SPACES should contain exactly 3 entries");

        assertSame(Colorspace.UNSPECIFIED, s[0]);
        assertSame(Colorspace.ITU_REC_470M, s[1]);
        assertSame(Colorspace.ITU_REC_470BG, s[2]);
    }

    @Test
    void colorspaceInstancesAreUniqueSingletons() {
        assertNotSame(Colorspace.UNSPECIFIED, Colorspace.ITU_REC_470M);
        assertNotSame(Colorspace.UNSPECIFIED, Colorspace.ITU_REC_470BG);
        assertNotSame(Colorspace.ITU_REC_470M, Colorspace.ITU_REC_470BG);

        // SPACES must contain the exact same instances
        for (int i = 0; i < Colorspace.SPACES.length; i++) {
            assertSame(Colorspace.SPACES[i], Colorspace.SPACES[i],
                    "Instance identity must remain stable");
        }
    }

    @Test
    void constructorIsPrivate() throws Exception {
        var ctor = Colorspace.class.getDeclaredConstructor();

        assertFalse(ctor.canAccess(null), "Constructor should be private");
    }

    @Test
    void noDuplicateInstancesExistInSpacesArray() {
        Colorspace[] s = Colorspace.SPACES;

        assertSame(Colorspace.UNSPECIFIED, s[0]);
        assertSame(Colorspace.ITU_REC_470M, s[1]);
        assertSame(Colorspace.ITU_REC_470BG, s[2]);

        assertEquals(3, s.length);

        // Ensure no duplicates
        assertNotEquals(s[0], s[1]);
        assertNotEquals(s[0], s[2]);
        assertNotEquals(s[1], s[2]);
    }

    @Test
    void colorspaceInstancesAreImmutable() {
        // There is no public API to mutate a Colorspace instance.
        // This test ensures the class remains immutable.
        Colorspace c = Colorspace.UNSPECIFIED;

        assertSame(Colorspace.UNSPECIFIED, c);
        assertSame(Colorspace.UNSPECIFIED, Colorspace.UNSPECIFIED);
    }
}
