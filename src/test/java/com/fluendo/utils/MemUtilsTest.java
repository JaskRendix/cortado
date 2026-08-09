package com.fluendo.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class MemUtilsTest {

    private final PrintStream originalSystemOut = System.out;
    private ByteArrayOutputStream testOut;

    @BeforeEach
    void setUp() {
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalSystemOut);
    }

    @Test
    @DisplayName("MemUtils: cmp comparison matching and ordering")
    void testCmp() {
        byte[] mem1 = { 0x01, 0x02, 0x03, 0x04 };
        byte[] mem2 = { 0x01, 0x02, 0x05, 0x04 };
        byte[] mem3 = { 0x01, 0x02, 0x01, 0x04 };

        // Identical up to length 2
        assertEquals(0, MemUtils.cmp(mem1, mem2, 2));
        // mem1[2] (3) < mem2[2] (5) -> returns -index (-2)
        assertEquals(-2, MemUtils.cmp(mem1, mem2, 3));
        // mem1[2] (3) > mem3[2] (1) -> returns index (2)
        assertEquals(2, MemUtils.cmp(mem1, mem3, 3));
        // Fully identical up to length 4
        assertEquals(0, MemUtils.cmp(mem1, mem1, 4));
    }

    @Test
    @DisplayName("MemUtils: set memory utility for bytes, shorts, ints, and objects")
    void testSetMethods() {
        // Byte array set
        byte[] bytes = new byte[5];
        MemUtils.set(bytes, 1, 0xFF, 3); // sets indices 1, 2, 3 to (byte) 255 (-1)
        assertEquals(0, bytes[0]);
        assertEquals((byte) 0xFF, bytes[1]);
        assertEquals((byte) 0xFF, bytes[2]);
        assertEquals((byte) 0xFF, bytes[3]);
        assertEquals(0, bytes[4]);

        // Short array set
        short[] shorts = new short[4];
        MemUtils.set(shorts, 0, 123, 2);
        assertEquals(123, shorts[0]);
        assertEquals(123, shorts[1]);
        assertEquals(0, shorts[2]);

        // Int array set
        int[] ints = new int[4];
        MemUtils.set(ints, 2, 999, 2);
        assertEquals(0, ints[0]);
        assertEquals(999, ints[2]);
        assertEquals(999, ints[3]);

        // Object array set
        Object[] objs = new Object[3];
        MemUtils.set(objs, 0, "Test", 2);
        assertEquals("Test", objs[0]);
        assertEquals("Test", objs[1]);
        assertNull(objs[2]);
    }

    @Test
    @DisplayName("MemUtils: startsWith pattern checking")
    void testStartsWith() {
        byte[] arr = { 0x48, 0x65, 0x6c, 0x6c, 0x6f }; // "Hello"
        byte[] patternMatch = { 0x48, 0x65, 0x6c };    // "Hel"
        byte[] patternMismatch = { 0x48, 0x65, 0x70 }; // "Hep"
        byte[] patternTooLong = { 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x21 };

        assertTrue(MemUtils.startsWith(arr, 0, arr.length, patternMatch));
        assertFalse(MemUtils.startsWith(arr, 0, arr.length, patternMismatch));
        assertFalse(MemUtils.startsWith(arr, 0, arr.length, patternTooLong));
        
        // Offset check
        assertTrue(MemUtils.startsWith(arr, 2, arr.length - 2, new byte[] { 0x6c, 0x6c })); // "ll"
    }

    @Test
    @DisplayName("MemUtils: dump memory buffer format output")
    void testDump() {
        byte[] mem = "ABC\n\u0001".getBytes();
        assertDoesNotThrow(() -> MemUtils.dump(mem, 0, mem.length));
        
        String output = testOut.toString();
        assertNotNull(output);
        assertTrue(output.contains("41 42 43")); // Hex for ABC
    }
}
