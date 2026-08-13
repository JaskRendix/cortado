package com.fluendo.jheora;

import org.junit.jupiter.api.*;

import java.awt.image.ImageConsumer;
import java.awt.image.ColorModel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YUVBufferTest {

    private YUVBuffer buf;

    @BeforeEach
    void setup() {
        buf = new YUVBuffer();
        buf.yWidth = 4;
        buf.yHeight = 4;
        buf.yStride = 4;
        buf.uvWidth = 2;
        buf.uvHeight = 2;
        buf.uvStride = 2;

        buf.data = new short[4 * 4 + 2 * 2 * 2]; // Y + U + V
        buf.yOffset = 0;
        buf.uOffset = 16;
        buf.vOffset = 20;

        // Fill Y plane with mid‑gray
        for (int i = 0; i < 16; i++) buf.data[i] = 128;

        // Fill U/V planes with neutral chroma
        for (int i = 16; i < 24; i++) buf.data[i] = 128;
    }

    @Test
    void testNewPixelsFlag() {
        buf.newPixels();
        // Trigger conversion
        buf.startProduction(mock(ImageConsumer.class));
        // After conversion, flag must be false
        assertFalse(getPrivateNewPixels(buf));
    }

    @Test
    void testGetObjectReturnsSelfForFullFrame() {
        assertSame(buf, buf.getObject(0, 0, buf.yWidth, buf.yHeight));
    }

    @Test
    void testGetObjectReturnsFilteredSourceForCrop() {
        Object cropped = buf.getObject(1, 1, 2, 2);
        assertNotSame(buf, cropped);
        assertTrue(cropped instanceof java.awt.image.ImageProducer);
    }

    @Test
    void testStartProductionCallsConsumerCorrectly() {
        ImageConsumer ic = mock(ImageConsumer.class);

        buf.startProduction(ic);

        verify(ic).setColorModel(ColorModel.getRGBdefault());
        verify(ic).setHints(anyInt());
        verify(ic).setDimensions(buf.yWidth, buf.yHeight);
        verify(ic).setPixels(eq(0), eq(0), eq(buf.yWidth), eq(buf.yHeight),
                eq(ColorModel.getRGBdefault()), any(int[].class), eq(0), eq(buf.yWidth));
        verify(ic).imageComplete(ImageConsumer.STATICIMAGEDONE);
    }

    @Test
    void testZeroDimensions() {
        buf.yWidth = 0;
        buf.yHeight = 0;

        ImageConsumer ic = mock(ImageConsumer.class);
        buf.startProduction(ic);

        verify(ic).setDimensions(0, 0);
        verify(ic).imageComplete(ImageConsumer.STATICIMAGEDONE);
    }

    @Test
    void testOddDimensionsYUV420() {
        buf.yWidth = 5;
        buf.yHeight = 5;
        buf.uvWidth = 3;
        buf.uvHeight = 3;

        buf.data = new short[5 * 5 + 3 * 3 * 2];
        buf.newPixels();

        ImageConsumer ic = mock(ImageConsumer.class);
        buf.startProduction(ic);

        verify(ic).setDimensions(5, 5);
    }

    @Test
    void testInvalidOffsetsDoNotCrash() {
        buf.yOffset = 9999; // beyond array
        buf.uOffset = 9999;
        buf.vOffset = 9999;

        ImageConsumer ic = mock(ImageConsumer.class);

        assertDoesNotThrow(() -> buf.startProduction(ic));
    }

    @Test
    void testPrepareRgbDataIdempotent() {
        buf.newPixels();
        invokePrepareRgbData(buf);

        int[] first = getPrivatePixels(buf);

        // second call should NOT regenerate pixels
        invokePrepareRgbData(buf);
        int[] second = getPrivatePixels(buf);

        assertSame(first, second);
    }

    private boolean getPrivateNewPixels(YUVBuffer b) {
        try {
            var f = YUVBuffer.class.getDeclaredField("newPixels");
            f.setAccessible(true);
            return f.getBoolean(b);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int[] getPrivatePixels(YUVBuffer b) {
        try {
            var f = YUVBuffer.class.getDeclaredField("pixels");
            f.setAccessible(true);
            return (int[]) f.get(b);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokePrepareRgbData(YUVBuffer b) {
        try {
            var m = YUVBuffer.class.getDeclaredMethod("prepareRgbData", int.class, int.class, int.class, int.class);
            m.setAccessible(true);
            m.invoke(b, 0, 0, b.yWidth, b.yHeight);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
