package com.fluendo.jkate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaletteTest {

    private Palette palette;

    @BeforeEach
    void setUp() {
        palette = new Palette();
    }

    @Test
    @DisplayName("Default Constructor: Initializes colors to an empty array")
    void testDefaultConstructor() {
        assertNotNull(palette.colors);
        assertEquals(0, palette.colors.length);
    }

    @Test
    @DisplayName("Constructor with Null Edge Case: Converts null input to an empty array")
    void testConstructorWithNullColors() {
        Palette nullPalette = new Palette(null);
        assertNotNull(nullPalette.colors);
        assertEquals(0, nullPalette.colors.length);
    }

    @Test
    @DisplayName("Parameterized Constructor: Properly wraps array")
    void testParameterizedConstructor() {
        Color[] testColors = new Color[] { new Color(), new Color() };
        Palette customPalette = new Palette(testColors);
        
        assertArrayEquals(testColors, customPalette.colors);
        assertEquals(2, customPalette.colors.length);
    }

    @Test
    @DisplayName("Equals and HashCode: Evaluates content equality accurately")
    void testEqualsAndHashCode() {
        Color[] colors1 = new Color[] { new Color() };
        Color[] colors2 = new Color[] { new Color() };

        Palette p1 = new Palette(colors1);
        Palette p2 = new Palette(colors2);
        Palette p3 = new Palette(new Color[0]);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
        assertNotEquals(null, p1);
        assertNotEquals(p1, "invalid type");
    }

    @Test
    @DisplayName("ToString: Returns expected formatted representation")
    void testToString() {
        String result = palette.toString();
        assertNotNull(result);
        assertTrue(result.contains("Palette{"));
    }
}
