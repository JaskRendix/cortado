package com.fluendo.jheora;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CoordinateTest {

    @Test
    void defaultConstructorInitializesToZeroZero() {
        Coordinate c = new Coordinate();
        assertEquals(0, c.x);
        assertEquals(0, c.y);
    }

    @Test
    void parameterizedConstructorSetsFieldsCorrectly() {
        Coordinate c = new Coordinate(5, -3);
        assertEquals(5, c.x);
        assertEquals(-3, c.y);
    }

    @Test
    void fieldsAreMutable() {
        Coordinate c = new Coordinate(1, 2);

        c.x = 10;
        c.y = 20;

        assertEquals(10, c.x);
        assertEquals(20, c.y);
    }

    @Test
    void supportsNegativeCoordinates() {
        Coordinate c = new Coordinate(-100, -200);

        assertEquals(-100, c.x);
        assertEquals(-200, c.y);
    }

    @Test
    void supportsLargeCoordinates() {
        Coordinate c = new Coordinate(Integer.MAX_VALUE, Integer.MIN_VALUE);

        assertEquals(Integer.MAX_VALUE, c.x);
        assertEquals(Integer.MIN_VALUE, c.y);
    }

    @Test
    void canSetCoordinatesToZeroAfterInitialization() {
        Coordinate c = new Coordinate(99, 88);

        c.x = 0;
        c.y = 0;

        assertEquals(0, c.x);
        assertEquals(0, c.y);
    }

    @Test
    void twoCoordinatesWithSameValuesAreEqualByFields() {
        Coordinate a = new Coordinate(7, 7);
        Coordinate b = new Coordinate(7, 7);

        assertEquals(a.x, b.x);
        assertEquals(a.y, b.y);
    }

    @Test
    void twoCoordinatesWithDifferentValuesAreNotEqualByFields() {
        Coordinate a = new Coordinate(7, 7);
        Coordinate b = new Coordinate(8, 7);

        assertNotEquals(a.x, b.x);
        assertEquals(a.y, b.y);
    }

    @Test
    void coordinateObjectIsIndependent() {
        Coordinate a = new Coordinate(1, 1);
        Coordinate b = new Coordinate(1, 1);

        b.x = 999;

        assertEquals(1, a.x);
        assertEquals(999, b.x);
    }
}
