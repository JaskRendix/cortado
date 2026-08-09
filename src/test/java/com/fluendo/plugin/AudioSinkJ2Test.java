package com.fluendo.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AudioSinkJ2Test {

    private AudioSinkJ2 audioSinkJ2;

    @BeforeEach
    void setUp() {
        audioSinkJ2 = new AudioSinkJ2();
    }

    @Test
    @DisplayName("Factory Metadata: Verify Correct J2 Factory Name")
    void testFactoryName() {
        assertEquals("audiosinkj2", audioSinkJ2.getFactoryName(), "Factory name must match expected identifier");
    }

    @Test
    @DisplayName("Edge Case: RingBuffer Allocation and Verification")
    void testCreateRingBuffer() {
        AudioSink.RingBuffer ring = audioSinkJ2.createRingBuffer();
        assertNotNull(ring, "AudioSinkJ2 must successfully allocate a concrete RingBuffer");
    }

    @Test
    @DisplayName("Edge Case: Invalid Write Parameters Handling")
    void testInvalidWriteParameters() {
        byte[] dummyData = new byte[100];
        
        // Passing invalid bounds (e.g., negative offset or out-of-bounds length) should fail safely without crashing
        int written = audioSinkJ2.write(dummyData, -1, 50);
        assertEquals(50, written, "Write method should safely short-circuit and return length on invalid offsets");
    }
}