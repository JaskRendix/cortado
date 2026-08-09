package com.fluendo.jtiger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class FancyTextRendererTest {

    private FancyTextRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new FancyTextRenderer();
    }

    @Test
    void testRenderTextSuccessfully() {
        BufferedImage img = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        Rectangle region = new Rectangle(10, 10, 180, 80);
        Font font = new Font("SansSerif", Font.PLAIN, 14);

        assertDoesNotThrow(() -> renderer.renderText(g2d, region, font, "Test Subtitle Line"));
        g2d.dispose();
    }

    @Test
    void testRenderTextWithNullOrEmptyInputs() {
        BufferedImage img = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        Rectangle region = new Rectangle(0, 0, 100, 50);
        Font font = new Font("SansSerif", Font.PLAIN, 12);

        assertDoesNotThrow(() -> renderer.renderText(null, region, font, "Text"));
        assertDoesNotThrow(() -> renderer.renderText(g2d, null, font, "Text"));
        assertDoesNotThrow(() -> renderer.renderText(g2d, region, null, "Text"));
        assertDoesNotThrow(() -> renderer.renderText(g2d, region, font, null));
        assertDoesNotThrow(() -> renderer.renderText(g2d, region, font, ""));

        g2d.dispose();
    }
}
