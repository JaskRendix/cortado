package com.fluendo.jheora;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MotionVectorTest {

    @Test
    void defaultConstructor_initializesToZero() {
        MotionVector mv = new MotionVector();
        assertEquals(0, mv.x);
        assertEquals(0, mv.y);
    }

    @Test
    void parameterConstructor_setsCoordinatesCorrectly() {
        MotionVector mv = new MotionVector(7, -4);
        assertEquals(7, mv.x);
        assertEquals(-4, mv.y);
    }

    @Test
    void nullConstant_initialValuesAreZero() {
        assertEquals(0, MotionVector.NULL.x);
        assertEquals(0, MotionVector.NULL.y);
    }

    @Test
    void nullConstant_canBeMutatedBecauseCoordinateIsMutable() {
        MotionVector mv = MotionVector.NULL;
        mv.x = 99;
        mv.y = -99;

        assertEquals(99, MotionVector.NULL.x);
        assertEquals(-99, MotionVector.NULL.y);
    }

    @Test
    void motionVector_allowsNegativeValues() {
        MotionVector mv = new MotionVector(-12, -33);
        assertEquals(-12, mv.x);
        assertEquals(-33, mv.y);
    }

    @Test
    void motionVector_allowsLargeValues() {
        MotionVector mv = new MotionVector(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertEquals(Integer.MAX_VALUE, mv.x);
        assertEquals(Integer.MIN_VALUE, mv.y);
    }

    @Test
    void motionVector_isInstanceOfCoordinate() {
        MotionVector mv = new MotionVector();
        assertTrue(mv instanceof Coordinate);
    }

    @Test
    void motionVector_equalityBasedOnCoordinates() {
        MotionVector mv1 = new MotionVector(3, 4);
        MotionVector mv2 = new MotionVector(3, 4);

        assertEquals(mv1.x, mv2.x);
        assertEquals(mv1.y, mv2.y);
    }

    @Test
    void motionVector_toStringIsNonNull() {
        MotionVector mv = new MotionVector(2, 5);
        assertNotNull(mv.toString());
    }
}
