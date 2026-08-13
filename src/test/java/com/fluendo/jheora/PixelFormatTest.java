package com.fluendo.jheora;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

public class PixelFormatTest {

    @Test
    void formatsArray_hasCorrectLength() {
        assertEquals(4, PixelFormat.FORMATS.length);
    }

    @Test
    void formatsArray_containsCorrectSingletons() {
        assertSame(PixelFormat.TH_PF_420, PixelFormat.FORMATS[0]);
        assertSame(PixelFormat.TH_PF_RSVD, PixelFormat.FORMATS[1]);
        assertSame(PixelFormat.TH_PF_422, PixelFormat.FORMATS[2]);
        assertSame(PixelFormat.TH_PF_444, PixelFormat.FORMATS[3]);
    }

    @Test
    void eachPixelFormat_isUniqueSingleton() {
        assertNotSame(PixelFormat.TH_PF_420, PixelFormat.TH_PF_RSVD);
        assertNotSame(PixelFormat.TH_PF_420, PixelFormat.TH_PF_422);
        assertNotSame(PixelFormat.TH_PF_420, PixelFormat.TH_PF_444);

        assertNotSame(PixelFormat.TH_PF_RSVD, PixelFormat.TH_PF_422);
        assertNotSame(PixelFormat.TH_PF_RSVD, PixelFormat.TH_PF_444);

        assertNotSame(PixelFormat.TH_PF_422, PixelFormat.TH_PF_444);
    }

    @Test
    void constructor_isPrivate() throws Exception {
        Constructor<PixelFormat> c = PixelFormat.class.getDeclaredConstructor();
        assertFalse(c.canAccess(null));
    }

    @Test
    void reservedFormat_isSecondEntry() {
        assertSame(PixelFormat.TH_PF_RSVD, PixelFormat.FORMATS[1]);
    }

    @Test
    void pixelFormat_toStringIsNonNull() {
        assertNotNull(PixelFormat.TH_PF_420.toString());
        assertNotNull(PixelFormat.TH_PF_RSVD.toString());
        assertNotNull(PixelFormat.TH_PF_422.toString());
        assertNotNull(PixelFormat.TH_PF_444.toString());
    }
}
