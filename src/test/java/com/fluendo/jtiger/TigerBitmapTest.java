package com.fluendo.jtiger;

import com.fluendo.jkate.Bitmap;
import com.fluendo.jkate.Palette;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class TigerBitmapTest {

    private MockComponent component;

    @BeforeEach
    void setUp() {
        component = new MockComponent();
    }

    @Nested
    @DisplayName("Constructor behavior")
    class ConstructorTests {

        @Test
        @DisplayName("Null component throws NullPointerException")
        void nullComponentThrows() {
            assertThrows(NullPointerException.class,
                    () -> new TigerBitmap(null, new Bitmap(), new Palette()));
        }

        @Test
        @DisplayName("Null bitmap falls back to transparent image")
        void nullBitmapUsesFallbackImage() {
            TigerBitmap tigerBitmap = new TigerBitmap(component, null, null);
            Image scaled = tigerBitmap.getScaled(10, 10);
            assertNotNull(scaled);
        }

        @Test
        @DisplayName("PNG bitmap (bpp=0) uses fallback image")
        void pngBitmapUnsupportedUsesFallback() {
            Bitmap kb = new Bitmap();
            kb.bpp = 0; // triggers PNG branch

            TigerBitmap tigerBitmap = new TigerBitmap(component, kb, new Palette());
            Image scaled = tigerBitmap.getScaled(10, 10);
            assertNotNull(scaled);
        }

        @Test
        @DisplayName("Paletted bitmap with null palette colors falls back")
        void palettedBitmapWithNullColorsFallsBack() throws Exception {
            Bitmap kb = new Bitmap();
            kb.bpp = 8;
            kb.width = 2;
            kb.height = 2;
            kb.pixels = new byte[]{0, 1, 1, 0};

            Palette kp = new Palette();
            // Explicitly set colors to null if field exists
            Field colorsField = Palette.class.getDeclaredField("colors");
            colorsField.setAccessible(true);
            colorsField.set(kp, null);

            TigerBitmap tigerBitmap = new TigerBitmap(component, kb, kp);
            Image scaled = tigerBitmap.getScaled(10, 10);
            assertNotNull(scaled);
        }
    }

    @Nested
    @DisplayName("Scaling behavior")
    class ScalingTests {

        @Test
        @DisplayName("Invalid dimensions throw IllegalArgumentException")
        void invalidScaleDimensionsThrow() {
            TigerBitmap tigerBitmap = new TigerBitmap(component, null, null);
            assertThrows(IllegalArgumentException.class, () -> tigerBitmap.getScaled(0, 10));
            assertThrows(IllegalArgumentException.class, () -> tigerBitmap.getScaled(10, -5));
        }

        @Test
        @DisplayName("Scaled image is cached for same dimensions")
        void scaledImageIsCached() {
            TigerBitmap tigerBitmap = new TigerBitmap(component, null, null);

            Image first = tigerBitmap.getScaled(20, 20);
            Image second = tigerBitmap.getScaled(20, 20);

            assertSame(first, second, "Scaled image should be cached for identical dimensions");
        }

        @Test
        @DisplayName("Scaled image is recreated for different dimensions")
        void scaledImageRecreatedForDifferentDimensions() {
            TigerBitmap tigerBitmap = new TigerBitmap(component, null, null);

            Image first = tigerBitmap.getScaled(20, 20);
            Image second = tigerBitmap.getScaled(30, 30);

            assertNotSame(first, second, "Scaled image should be recreated when dimensions change");
        }
    }

    @Nested
    @DisplayName("Paletted bitmap creation")
    class PalettedBitmapTests {

        @Test
        @DisplayName("Valid paletted bitmap creates non-null scaled image")
        void palettedBitmapCreation() throws Exception {
            Bitmap kb = new Bitmap();
            kb.bpp = 8;
            kb.width = 2;
            kb.height = 2;
            kb.pixels = new byte[]{0, 1, 1, 0};

            Palette kp = new Palette();
            populatePaletteWithTwoColors(kp);

            TigerBitmap tigerBitmap = new TigerBitmap(component, kb, kp);
            Image scaled = tigerBitmap.getScaled(20, 20);

            assertNotNull(scaled);
        }

        @Test
        @DisplayName("Bitmap with zero width or height falls back to transparent image")
        void zeroWidthOrHeightFallsBack() throws Exception {
            Bitmap kb = new Bitmap();
            kb.bpp = 8;
            kb.width = 0;
            kb.height = 2;
            kb.pixels = new byte[]{0, 1};

            Palette kp = new Palette();
            populatePaletteWithTwoColors(kp);

            TigerBitmap tigerBitmap = new TigerBitmap(component, kb, kp);
            Image scaled = tigerBitmap.getScaled(10, 10);

            assertNotNull(scaled);
        }

        @Test
        @DisplayName("Bitmap with pixel array shorter than width*height still does not crash")
        void shortPixelArrayDoesNotCrash() throws Exception {
            Bitmap kb = new Bitmap();
            kb.bpp = 8;
            kb.width = 4;
            kb.height = 4;
            kb.pixels = new byte[]{0, 1, 2}; // shorter than 16

            Palette kp = new Palette();
            populatePaletteWithTwoColors(kp);

            TigerBitmap tigerBitmap = new TigerBitmap(component, kb, kp);
            Image scaled = tigerBitmap.getScaled(10, 10);

            assertNotNull(scaled);
        }
    }

    @Nested
    @DisplayName("Fallback image behavior")
    class FallbackImageTests {

        @Test
        @DisplayName("Fallback image is at least 1x1 and scalable")
        void fallbackImageIsValid() {
            TigerBitmap tigerBitmap = new TigerBitmap(component, null, null);

            Image base = tigerBitmap.getScaled(1, 1);
            assertNotNull(base);

            // Try scaling to a larger size
            Image scaled = tigerBitmap.getScaled(32, 32);
            assertNotNull(scaled);

            // Width/height may be -1 until fully realized; we just ensure no exceptions
            int w = scaled.getWidth(new DummyObserver());
            int h = scaled.getHeight(new DummyObserver());
            assertTrue(w == -1 || w > 0);
            assertTrue(h == -1 || h > 0);
        }
    }

    private void populatePaletteWithTwoColors(Palette kp) throws Exception {
        Class<?> colorClass = resolveColorClass();
        assertNotNull(colorClass, "Could not resolve Palette color class");

        Object color1 = createColorInstance(colorClass, (byte) 255, (byte) 0, (byte) 0, (byte) 255);
        Object color2 = createColorInstance(colorClass, (byte) 0, (byte) 255, (byte) 0, (byte) 255);

        Object colorArray = Array.newInstance(colorClass, 2);
        Array.set(colorArray, 0, color1);
        Array.set(colorArray, 1, color2);

        Field colorsField = Palette.class.getDeclaredField("colors");
        colorsField.setAccessible(true);
        colorsField.set(kp, colorArray);
    }

    private Class<?> resolveColorClass() throws Exception {
        // Try inner classes first
        for (Class<?> declaredClass : Palette.class.getDeclaredClasses()) {
            String name = declaredClass.getSimpleName().toLowerCase();
            if (name.contains("color") || name.contains("entry")) {
                return declaredClass;
            }
        }

        // Fallback: infer from 'colors' field component type
        Field colorsField = Palette.class.getDeclaredField("colors");
        colorsField.setAccessible(true);
        Class<?> componentType = colorsField.getType().getComponentType();
        return componentType;
    }

    private Object createColorInstance(Class<?> colorClass, byte r, byte g, byte b, byte a) throws Exception {
        try {
            return colorClass.getDeclaredConstructor(byte.class, byte.class, byte.class, byte.class)
                    .newInstance(r, g, b, a);
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
            // Best-effort; palette structure may differ
        }
    }

    private static class MockComponent extends Component {
        // Minimal headless-safe component for AWT image creation
    }

    private static class DummyObserver implements ImageObserver {
        @Override
        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
            return false;
        }
    }
}
