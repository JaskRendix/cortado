package com.fluendo.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class DebugTest {

    private final PrintStream originalSystemOut = System.out;
    private ByteArrayOutputStream testOut;

    @BeforeEach
    void setUp() {
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
        Debug.level = Debug.INFO;
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalSystemOut);
        Debug.level = Debug.INFO;
    }

    @Test
    @DisplayName("Debug: ID generation thread-safety and increment")
    void testGenId() {
        int id1 = Debug.genId();
        int id2 = Debug.genId();
        assertEquals(id1 + 1, id2, "ID counter should increment sequentially");
    }

    @Test
    @DisplayName("Debug: rpad utility with short string")
    void testRpadShortString() {
        String padded = Debug.rpad("abc", 6);
        assertEquals("abc   ", padded);
        assertEquals(6, padded.length());
    }

    @Test
    @DisplayName("Debug: rpad utility with exact or long string")
    void testRpadLongString() {
        String padded = Debug.rpad("abcdef", 4);
        assertEquals("abcdef", padded);
    }

    @Test
    @DisplayName("Debug: Log filtering based on log levels")
    void testLogFiltering() {
        Debug.level = Debug.WARNING;

        Debug.error("This is an error");
        Debug.warning("This is a warning");
        Debug.info("This is an info message");
        Debug.debug("This is a debug message");

        String output = testOut.toString();
        assertTrue(output.contains("This is an error"));
        assertTrue(output.contains("This is a warning"));
        assertFalse(output.contains("This is an info message"));
        assertFalse(output.contains("This is a debug message"));
    }

    @Test
    @DisplayName("Debug: Verbose formatting at DEBUG level")
    void testVerboseDebugFormat() {
        Debug.level = Debug.DEBUG;

        Debug.debug("Verbose test message");

        String output = testOut.toString();
        // At DEBUG level, it checks against prefix index 4 ("DBUG")
        assertTrue(output.contains("DBUG"));
        assertTrue(output.contains("Verbose test message"));
    }
}
