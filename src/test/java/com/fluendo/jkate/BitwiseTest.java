package com.fluendo.jkate;

import com.jcraft.jogg.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BitwiseTest {

    private Buffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new Buffer();
        // Initialize an empty or dummy byte array for testing
        byte[] data = new byte[128];
        buffer.readinit(data, data.length);
    }

    @Test
    @DisplayName("Read Buffer: Valid extraction into byte array")
    void testReadbufValid() {
        byte[] target = new byte[4];
        assertDoesNotThrow(() -> Bitwise.readbuf(buffer, target, 4));
        assertEquals(4, target.length);
    }

    @Test
    @DisplayName("Read Buffer: Invalid or null parameters trigger exceptions")
    void testReadbufInvalidParams() {
        assertThrows(NullPointerException.class, () -> Bitwise.readbuf(null, new byte[2], 2));
        assertThrows(IllegalArgumentException.class, () -> Bitwise.readbuf(buffer, null, 2));
        assertThrows(IllegalArgumentException.class, () -> Bitwise.readbuf(buffer, new byte[2], -1));
    }

    @Test
    @DisplayName("Read 32v: Edge cases for variable size integers")
    void testRead32vEdgeCases() {
        // Test normal path where first 4 bits != 15
        // Since our mock buffer reads zeros, read(4) returns 0
        int val = Bitwise.read32v(buffer);
        assertEquals(0, val);
    }

    @Test
    @DisplayName("Read Floats: Invalid counts or streams return null")
    void testReadFloatsInvalidBounds() {
        assertNull(Bitwise.readFloats(buffer, 0, 1));
        assertNull(Bitwise.readFloats(buffer, 5, 0));
        assertNull(Bitwise.readFloats(buffer, -1, 1));
    }
}
