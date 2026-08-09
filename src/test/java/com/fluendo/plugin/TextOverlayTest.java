package com.fluendo.plugin;

import com.fluendo.jst.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Button;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.ImageProducer;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class TextOverlayTest {

    private TextOverlay textOverlay;
    private Component dummyComponent;

    @BeforeEach
    void setUp() {
        textOverlay = new TextOverlay();
        // Use an anonymous subclass of Button to safely handle createImage(ImageProducer) for tests
        dummyComponent = new Button("Test Component") {
            @Override
            public Image createImage(ImageProducer producer) {
                // Return a renderable BufferedImage instead of an unsupported ToolkitImage
                BufferedImage bufferedImage = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
                return bufferedImage;
            }
        };
        dummyComponent.setBounds(0, 0, 320, 240);
        textOverlay.component = dummyComponent;
    }

    @Test
    @DisplayName("Factory Name: Returns expected factory name identifier")
    void testGetFactoryName() {
        assertEquals("textoverlay", textOverlay.getFactoryName());
    }

    @Test
    @DisplayName("Properties: Setting and getting valid text property")
    void testTextPropertyHandling() {
        assertNull(textOverlay.getProperty("text"), "Default text property should be null");

        boolean setSuccess = textOverlay.setProperty("text", "Hello Cortado");
        assertTrue(setSuccess, "Setting valid text property should return true");
        assertEquals("Hello Cortado", textOverlay.getProperty("text"));
    }

    @Test
    @DisplayName("Properties: Handling unknown properties safely")
    void testUnknownPropertyHandling() {
        assertFalse(textOverlay.setProperty("unknown_prop", "value"), "Setting unknown property should return false");
        assertNull(textOverlay.getProperty("unknown_prop"), "Getting unknown property should return null");
    }

    @Test
    @DisplayName("Overlay Processing: Handling direct Image buffer objects safely")
    void testOverlayWithDirectImageBuffer() {
        BufferedImage testImage = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
        Buffer buffer = new Buffer();
        buffer.object = testImage;

        textOverlay.setProperty("text", "Sample Video Overlay");

        assertDoesNotThrow(() -> textOverlay.overlay(buffer), "Overlay processing with Image buffer should not throw");
    }

    @Test
    @DisplayName("Overlay Processing: Handling ImageProducer buffer objects safely")
    void testOverlayWithImageProducerBuffer() {
        BufferedImage testImage = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
        Buffer buffer = new Buffer();
        buffer.object = testImage.getSource();

        textOverlay.setProperty("text", "Producer Overlay");

        assertDoesNotThrow(() -> textOverlay.overlay(buffer), "Overlay processing with ImageProducer should not throw");
    }

    @Test
    @DisplayName("Overlay Processing: Handling unknown buffer types gracefully without crashing")
    void testOverlayWithUnknownBufferType() {
        Buffer buffer = new Buffer();
        buffer.object = "Invalid Buffer Object Type";

        assertDoesNotThrow(() -> textOverlay.overlay(buffer), "Unknown buffer objects should be safely logged/skipped");
    }

    @Test
    @DisplayName("Font Resizing Branch: Small width dimensions falling back to minimum font limit")
    void testFontResizeMinimumLimit() {
        dummyComponent.setBounds(0, 0, 10, 10);
        BufferedImage testImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Buffer buffer = new Buffer();
        buffer.object = testImage;

        textOverlay.setProperty("text", "Tiny");

        assertDoesNotThrow(() -> textOverlay.overlay(buffer), "Small component dimensions should invoke the font minimum size guard branch safely");
    }
}
