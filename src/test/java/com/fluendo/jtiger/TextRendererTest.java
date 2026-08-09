package com.fluendo.jtiger;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class TextRendererTest {

    @Test
    void testTextRendererFunctionalImplementation() {
        // Verify that TextRenderer works as a functional interface with lambda expressions
        TextRenderer renderer = (g, region, font, text) -> {
            assertNotNull(g);
            assertNotNull(region);
            assertNotNull(font);
            assertEquals("Hello JTiger", text);
        };

        BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        Rectangle region = new Rectangle(0, 0, 100, 50);
        Font font = new Font("SansSerif", Font.PLAIN, 12);

        assertDoesNotThrow(() -> renderer.renderText(g, region, font, "Hello JTiger"));
        g.dispose();
    }

    @Test
    void testNullParametersHandling() {
        TextRenderer renderer = (g, region, font, text) -> {
            // Implementation can choose how to handle nulls, here we just ensure invocation succeeds
        };

        assertDoesNotThrow(() -> renderer.renderText(null, null, null, null));
    }
}
