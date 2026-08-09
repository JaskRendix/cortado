package com.fluendo.jtiger;

import com.fluendo.jkate.Bitmap;
import com.fluendo.jkate.Palette;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Component;
import java.awt.Image;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class TigerBitmapTest {

    private MockComponent mockComponent;

    @BeforeEach
    void setUp() {
        mockComponent = new MockComponent();
    }

    @Test
    void testNullComponentThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new TigerBitmap(null, new Bitmap(), new Palette()));
    }

    @Test
    void testNullBitmapFallsBackToTransparentImage() {
        TigerBitmap tigerBitmap = new TigerBitmap(mockComponent, null, null);
        assertNotNull(tigerBitmap.getScaled(10, 10));
    }

    @Test
    void testPngBitmapUnsupportedFallback() {
        Bitmap kb = new Bitmap();
        kb.bpp = 0; // Triggers PNG branch
        TigerBitmap tigerBitmap = new TigerBitmap(mockComponent, kb, new Palette());
        assertNotNull(tigerBitmap);
    }

    @Test
    void testPalettedBitmapCreation() throws Exception {
        Bitmap kb = new Bitmap();
        kb.bpp = 8;
        kb.width = 2;
        kb.height = 2;
        kb.pixels = new byte[]{0, 1, 1, 0};

        Palette kp = new Palette();
        
        // Use reflection to inspect and populate the palette's internal color structure safely
        Class<?> colorClass = null;
        for (Class<?> declaredClass : Palette.class.getDeclaredClasses()) {
            if (declaredClass.getSimpleName().toLowerCase().contains("color") || 
                declaredClass.getSimpleName().toLowerCase().contains("entry")) {
                colorClass = declaredClass;
                break;
            }
        }
        
        if (colorClass == null && Palette.class.getDeclaredFields().length > 0) {
            // If colors is just an array of a specific type or objects
            Field colorsField = Palette.class.getDeclaredField("colors");
            colorsField.setAccessible(true);
            Class<?> componentType = colorsField.getType().getComponentType();
            if (componentType != null) {
                colorClass = componentType;
            }
        }

        if (colorClass != null) {
            Object color1 = createColorInstance(colorClass, (byte)255, (byte)0, (byte)0, (byte)255);
            Object color2 = createColorInstance(colorClass, (byte)0, (byte)255, (byte)0, (byte)255);
            
            Object colorArray = java.lang.reflect.Array.newInstance(colorClass, 2);
            java.lang.reflect.Array.set(colorArray, 0, color1);
            java.lang.reflect.Array.set(colorArray, 1, color2);
            
            Field colorsField = Palette.class.getDeclaredField("colors");
            colorsField.setAccessible(true);
            colorsField.set(kp, colorArray);
        }

        TigerBitmap tigerBitmap = new TigerBitmap(mockComponent, kb, kp);
        assertNotNull(tigerBitmap);
        
        Image scaled = tigerBitmap.getScaled(20, 20);
        assertNotNull(scaled);
    }

    private Object createColorInstance(Class<?> colorClass, byte r, byte g, byte b, byte a) throws Exception {
        try {
            // Try constructor with r, g, b, a fields or similar
            return colorClass.getDeclaredConstructor(byte.class, byte.class, byte.class, byte.class).newInstance(r, g, b, a);
        } catch (NoSuchMethodException e) {
            Object instance = colorClass.getDeclaredConstructor().newInstance();
            setFieldIfExists(instance, "r", r);
            setFieldIfExists(instance, "g", g);
            setFieldIfExists(instance, "b", b);
            setFieldIfExists(instance, "a", a);
            return instance;
        }
    }

    private void setFieldIfExists(Object obj, String fieldName, byte value) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.setByte(obj, value);
        } catch (Exception ignored) {
        }
    }

    @Test
    void testInvalidScaleDimensions() {
        TigerBitmap tigerBitmap = new TigerBitmap(mockComponent, null, null);
        assertThrows(IllegalArgumentException.class, () -> tigerBitmap.getScaled(0, 10));
        assertThrows(IllegalArgumentException.class, () -> tigerBitmap.getScaled(10, -5));
    }

    // --- Headless Test Double ---
    private static class MockComponent extends Component {
        // Keeps component alive for AWT operations during testing without display server
    }
}
