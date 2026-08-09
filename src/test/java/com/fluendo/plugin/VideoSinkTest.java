package com.fluendo.plugin;

import com.fluendo.jst.Buffer;
import com.fluendo.jst.Caps;
import com.fluendo.jst.Pad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Button;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.image.MemoryImageSource;

import static org.junit.jupiter.api.Assertions.*;

class VideoSinkTest {

    private VideoSink videoSink;
    private Component dummyComponent;

    // Test subclass to allow setting protected state fields for transition testing
    private static class TestableVideoSink extends VideoSink {
        public void setStates(int current, int pending) {
            this.currentState = current;
            this.pendingState = pending;
        }
    }

    @BeforeEach
    void setUp() {
        videoSink = new VideoSink();
        dummyComponent = new Button("Video Canvas");
        videoSink.setProperty("component", dummyComponent);
    }

    @Test
    @DisplayName("Metadata: Factory name verification")
    void testGetFactoryName() {
        assertEquals("videosink", videoSink.getFactoryName());
    }

    @Test
    @DisplayName("Properties: Setting and getting configuration properties")
    void testPropertyHandling() {
        assertEquals(dummyComponent, videoSink.getProperty("component"));

        assertTrue(videoSink.setProperty("keep-aspect", "false"));
        assertEquals("false", videoSink.getProperty("keep-aspect"));

        assertTrue(videoSink.setProperty("ignore-aspect", "true"));
        assertTrue(videoSink.setProperty("scale", "false"));

        Rectangle bounds = new Rectangle(0, 0, 640, 480);
        assertTrue(videoSink.setProperty("bounds", bounds));
        assertEquals(bounds, videoSink.getProperty("bounds"));

        assertNull(videoSink.getProperty("unknown"));
        assertFalse(videoSink.setProperty("unknown", "value"));
    }

    @Test
    @DisplayName("Caps Negotiation: Valid and invalid caps scenarios")
    void testSetCapsFunc() {
        Caps invalidCaps = new Caps("audio/x-raw");
        assertFalse(videoSink.setCapsFunc(invalidCaps));

        Caps validCaps = new Caps("video/raw");
        validCaps.setFieldInt("width", 320);
        validCaps.setFieldInt("height", 240);
        validCaps.setFieldInt("aspect_x", 4);
        validCaps.setFieldInt("aspect_y", 3);
        assertTrue(videoSink.setCapsFunc(validCaps));

        Caps negAspectCaps = new Caps("video/raw");
        negAspectCaps.setFieldInt("width", 320);
        negAspectCaps.setFieldInt("height", 240);
        negAspectCaps.setFieldInt("aspect_x", -1);
        negAspectCaps.setFieldInt("aspect_y", 1);
        assertTrue(videoSink.setCapsFunc(negAspectCaps));
    }

    @Test
    @DisplayName("Render & Preroll: Handling ImageProducer and Image buffer objects")
    void testRenderWithImageObjects() {
        Buffer buffer = new Buffer();
        int[] pixels = new int[10 * 10];
        buffer.object = new MemoryImageSource(10, 10, pixels, 0, 10);
        
        assertDoesNotThrow(() -> videoSink.render(buffer));
        assertDoesNotThrow(() -> videoSink.preroll(buffer));
    }

    @Test
    @DisplayName("Render: Handling unknown or unsupported buffer objects gracefully")
    void testRenderWithUnknownBufferObject() {
        Buffer buffer = new Buffer();
        buffer.object = "Unsupported Object Type";

        assertEquals(Pad.ERROR, videoSink.render(buffer));
    }

    @Test
    @DisplayName("State Transition: Automatic Frame instantiation when component is null on STOP->PAUSE")
    void testStateTransitionFallback() {
        TestableVideoSink uninitializedSink = new TestableVideoSink();
        // Set currentState to STOP and pendingState to PAUSE with a null component to trigger fallback
        uninitializedSink.setStates(VideoSink.STOP, VideoSink.PAUSE);

        assertDoesNotThrow(() -> uninitializedSink.changeState(VideoSink.STOP_PAUSE));
        assertNotNull(uninitializedSink.getProperty("component"));
    }
}
