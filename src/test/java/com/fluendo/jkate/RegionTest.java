package com.fluendo.jkate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegionTest {

    private Region region;

    @BeforeEach
    void setUp() {
        region = new Region();
    }

    @Test
    @DisplayName("Default Constructor: Initializes with expected default values")
    void testDefaultConstructor() {
        assertNull(region.metric);
        assertEquals(0, region.x);
        assertEquals(0, region.y);
        assertEquals(0, region.w);
        assertEquals(0, region.h);
        assertEquals(0, region.style);
        assertFalse(region.clip);
    }

    @Test
    @DisplayName("Parameterized Constructor: Correctly assigns all fields")
    void testParameterizedConstructor() {
        Region customRegion = new Region(null, 10, 20, 100, 200, 1, true);

        assertNull(customRegion.metric);
        assertEquals(10, customRegion.x);
        assertEquals(20, customRegion.y);
        assertEquals(100, customRegion.w);
        assertEquals(200, customRegion.h);
        assertEquals(1, customRegion.style);
        assertTrue(customRegion.clip);
    }

    @Test
    @DisplayName("Equals and HashCode: Compares region states accurately")
    void testEqualsAndHashCode() {
        Region r1 = new Region(null, 10, 20, 100, 200, 1, true);
        Region r2 = new Region(null, 10, 20, 100, 200, 1, true);
        Region r3 = new Region(null, 0, 0, 0, 0, 0, false);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
        assertNotEquals(null, r1);
        assertNotEquals(r1, "some string");
    }

    @Test
    @DisplayName("ToString: Returns readable debug representation")
    void testToString() {
        String representation = region.toString();
        assertNotNull(representation);
        assertTrue(representation.contains("Region{"));
    }
}
