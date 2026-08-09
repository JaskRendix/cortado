package com.fluendo.jtiger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class BasicTextRendererTest {

    private BasicTextRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new BasicTextRenderer();
    }

    @Test
    void testRenderTextSuccessfully() {
        BufferedImage img = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        Rectangle region = new Rectangle(10, 10, 180, 80);
        Font font = new Font("SansSerif", Font.PLAIN, 12);

        assertDoesNotThrow(() -> renderer.renderText(g, region, font, "Basic Subtitle"));
        g.dispose();
    }

    @Test
    void testRenderTextWithNullOrEmptyInputs() {
        BufferedImage img = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        Rectangle region = new Rectangle(0, 0, 100, 50);
        Font font = new Font("SansSerif", Font.PLAIN, 12);

        assertDoesNotThrow(() -> renderer.renderText(null, region, font, "Text"));
        assertDoesNotThrow(() -> renderer.renderText(g, null, font, "Text"));
        assertDoesNotThrow(() -> renderer.renderText(g, region, null, "Text"));
        assertDoesNotThrow(() -> renderer.renderText(g, region, font, null));
        assertDoesNotThrow(() -> renderer.renderText(g, region, font, ""));

        g.dispose();
    }
}
