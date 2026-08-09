package com.fluendo.jkate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MotionTest {

    private Motion motion;

    @BeforeEach
    void setUp() {
        motion = new Motion();
    }

    @Test
    @DisplayName("Default Constructor: Initializes with safe empty arrays and defaults")
    void testDefaultConstructor() {
        assertNotNull(motion.curves);
        assertEquals(0, motion.curves.length);
        assertNotNull(motion.durations);
        assertEquals(0, motion.durations.length);
        assertFalse(motion.periodic);
        assertNull(motion.x_mapping);
        assertNull(motion.y_mapping);
        assertNull(motion.semantics);
    }

    @Test
    @DisplayName("Setters with Null Edge Case: Converts null arrays to empty arrays safely via constructor")
    void testConstructorWithNullValues() {
        Motion nullMotion = new Motion(null, null, null, null, null, false);

        assertNotNull(nullMotion.curves);
        assertEquals(0, nullMotion.curves.length);
        assertNotNull(nullMotion.durations);
        assertEquals(0, nullMotion.durations.length);
    }

    @Test
    @DisplayName("Parameterized Constructor: Correctly assigns fields")
    void testParameterizedConstructor() {
        Curve[] testCurves = new Curve[] { new Curve() };
        double[] testDurations = new double[] { 1.5, 2.0 };
        
        Motion customMotion = new Motion(testCurves, testDurations, 
                null, null, null, true);

        assertArrayEquals(testCurves, customMotion.curves);
        assertArrayEquals(testDurations, customMotion.durations);
        assertTrue(customMotion.periodic);
    }

    @Test
    @DisplayName("Equals and HashCode: Compares motion states accurately")
    void testEqualsAndHashCode() {
        Curve[] curves1 = new Curve[0];
        double[] durations1 = new double[] { 1.0 };

        Motion m1 = new Motion(curves1, durations1, null, null, null, false);
        Motion m2 = new Motion(curves1, durations1, null, null, null, false);
        Motion m3 = new Motion(curves1, durations1, null, null, null, true);

        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
        assertNotEquals(m1, m3);
        assertNotEquals(null, m1);
        assertNotEquals(m1, "some string");
    }

    @Test
    @DisplayName("ToString: Returns readable debug representation")
    void testToString() {
        String representation = motion.toString();
        assertNotNull(representation);
        assertTrue(representation.contains("Motion{"));
    }
}
