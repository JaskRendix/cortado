package com.fluendo.jheora;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JTheoraExceptionTest {

    @Test
    void defaultConstructor_setsErrorCodeToZero() {
        JTheoraException ex = new JTheoraException();
        assertEquals(0, ex.getErrorCode());
    }

    @Test
    void defaultConstructor_hasNullMessage() {
        JTheoraException ex = new JTheoraException();
        assertNull(ex.getMessage());
    }

    @Test
    void parameterConstructor_setsMessageAndErrorCode() {
        JTheoraException ex = new JTheoraException("failure", 42);
        assertEquals("failure", ex.getMessage());
        assertEquals(42, ex.getErrorCode());
    }

    @Test
    void parameterConstructor_allowsNullMessage() {
        JTheoraException ex = new JTheoraException(null, 7);
        assertNull(ex.getMessage());
        assertEquals(7, ex.getErrorCode());
    }

    @Test
    void errorCode_canBeNegative() {
        JTheoraException ex = new JTheoraException("neg", -5);
        assertEquals(-5, ex.getErrorCode());
    }

    @Test
    void errorCode_canBeLarge() {
        JTheoraException ex = new JTheoraException("big", Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, ex.getErrorCode());
    }

    @Test
    void exception_isInstanceOfException() {
        JTheoraException ex = new JTheoraException();
        assertTrue(ex instanceof Exception);
    }

    @Test
    void serialVersionUID_isPresent() throws Exception {
        assertNotNull(
                JTheoraException.class.getDeclaredField("serialVersionUID")
        );
    }

    @Test
    void exceptionStackTrace_isGenerated() {
        JTheoraException ex = new JTheoraException("boom", 99);
        StackTraceElement[] trace = ex.getStackTrace();
        assertNotNull(trace);
        assertTrue(trace.length > 0);
    }

    @Test
    void toString_containsMessageAndClassName() {
        JTheoraException ex = new JTheoraException("hello", 3);
        String s = ex.toString();
        assertTrue(s.contains("JTheoraException"));
        assertTrue(s.contains("hello"));
    }
}
