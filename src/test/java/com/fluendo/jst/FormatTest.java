package com.fluendo.jst;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormatTest {

    @Test
    void testFormatConstants() {
        assertEquals(0, Format.UNKNOWN);
        assertEquals(1, Format.DEFAULT);
        assertEquals(2, Format.BYTES);
        assertEquals(3, Format.TIME);
        assertEquals(4, Format.BUFFERS);
        assertEquals(5, Format.PERCENT);
    }

    @Test
    void testPercentConstants() {
        assertEquals(1_000_000L, Format.PERCENT_MAX);
        assertEquals(10_000L, Format.PERCENT_SCALE);
    }

    @Test
    void testUtilityClassCannotBeInstantiated() throws Exception {
        var ctor = Format.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        Exception ex = assertThrows(Exception.class, () -> ctor.newInstance());
        assertTrue(ex.getCause() instanceof UnsupportedOperationException);
    }
}
