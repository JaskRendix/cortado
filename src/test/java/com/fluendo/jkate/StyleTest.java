package com.fluendo.jkate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StyleTest {

    private Style style;

    @BeforeEach
    void setUp() {
        style = new Style();
    }

    @Test
    @DisplayName("Default Constructor: Initializes with safe defaults")
    void testDefaultConstructor() {
        assertEquals(0.0, style.halign);
        assertEquals(0.0, style.valign);
        assertNull(style.text_color);
        assertNull(style.background_color);
        assertNull(style.draw_color);
        assertNull(style.font_metric);
        assertEquals(0.0, style.font_width);
        assertEquals(0.0, style.font_height);
        assertNull(style.margin_metric);
        assertEquals(0.0, style.left_margin);
        assertEquals(0.0, style.top_margin);
        assertEquals(0.0, style.right_margin);
        assertEquals(0.0, style.bottom_margin);
        assertFalse(style.bold);
        assertFalse(style.italics);
        assertFalse(style.underline);
        assertFalse(style.strike);
        assertFalse(style.justify);
        assertNull(style.wrap_mode);
        assertNull(style.font);
    }

    @Test
    @DisplayName("Parameterized Constructor: Correctly assigns all fields")
    void testParameterizedConstructor() {
        Style customStyle = new Style(
                0.5, 1.0, null, null, null,
                null, 12.0, 14.0,
                null, 5.0, 5.0, 5.0, 5.0,
                true, true, false, false, true,
                null, "Sans"
        );

        assertEquals(0.5, customStyle.halign);
        assertEquals(1.0, customStyle.valign);
        assertEquals(12.0, customStyle.font_width);
        assertEquals(14.0, customStyle.font_height);
        assertTrue(customStyle.bold);
        assertTrue(customStyle.italics);
        assertTrue(customStyle.justify);
        assertEquals("Sans", customStyle.font);
    }

    @Test
    @DisplayName("Equals and HashCode: Evaluates content equivalence accurately")
    void testEqualsAndHashCode() {
        Style s1 = new Style();
        s1.font = "Arial";
        s1.bold = true;

        Style s2 = new Style();
        s2.font = "Arial";
        s2.bold = true;

        Style s3 = new Style();
        s3.font = "Times";

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
        assertNotEquals(s1, s3);
        assertNotEquals(null, s1);
        assertNotEquals(s1, "some string");
    }

    @Test
    @DisplayName("ToString: Returns readable debug representation")
    void testToString() {
        String representation = style.toString();
        assertNotNull(representation);
        assertTrue(representation.contains("Style{"));
    }
}
