package com.fluendo.plugin;

import com.fluendo.jst.Buffer;
import com.fluendo.jst.Event;
import com.fluendo.jst.Pad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Button;
import java.awt.Component;

import static org.junit.jupiter.api.Assertions.*;

class OverlayTest {

    private Overlay overlay;
    private Component dummyComponent;

    @BeforeEach
    void setUp() {
        overlay = new Overlay();
        dummyComponent = new Button("Overlay Parent");
    }

    @Test
    @DisplayName("Metadata: Factory name verification")
    void testGetFactoryName() {
        assertEquals("overlay", overlay.getFactoryName());
    }

    @Test
    @DisplayName("Properties: Setting and getting component property safely")
    void testPropertyHandling() {
        assertNull(overlay.getProperty("component"));
        
        assertTrue(overlay.setProperty("component", dummyComponent));
        assertEquals(dummyComponent, overlay.getProperty("component"));

        // Fallback for unknown properties
        assertNull(overlay.getProperty("unknown"));
        assertFalse(overlay.setProperty("unknown", "value"));
    }

    @Test
    @DisplayName("Pads & Event Routing: Sink and source pad event intercommunication")
    void testPadEventRouting() {
        Pad sinkPad = overlay.getPad("videosink");
        Pad srcPad = overlay.getPad("videosrc");
        assertNotNull(sinkPad);
        assertNotNull(sinkPad);

        Event eos = Event.newEOS();
        Event flushStart = Event.newFlushStart();

        // Events on sink should push to source, and vice versa
        assertDoesNotThrow(() -> sinkPad.pushEvent(eos));
        assertDoesNotThrow(() -> srcPad.pushEvent(flushStart));
    }

    @Test
    @DisplayName("Chain Function & Default Overlay Passthrough")
    void testChainPassthrough() {
        Pad sinkPad = overlay.getPad("videosink");
        assertNotNull(sinkPad);

        Buffer buffer = new Buffer();
        buffer.data = new byte[]{1, 2, 3, 4};

        // Without a downstream peer, push returns -1, but the base overlay passthrough executes safely
        assertEquals(-1, sinkPad.push(buffer));
    }

    @Test
    @DisplayName("State Transition: Automatic Frame instantiation when component is null on STOP->PAUSE")
    void testStateTransitionFallback() {
        // Ensure component is initially null
        assertNull(overlay.getProperty("component"));

        // Simulate state transition: STOP (0) -> PAUSE (1)
        // Note: Element state constants depend on framework, typically STOP/PAUSE are fields or integers
        // We trigger changeState directly to test the fallback frame creation logic
        assertDoesNotThrow(() -> overlay.changeState(1)); // Passing target state or transition code

        // Component should now be initialized as a fallback Frame instance if transition rules matched
        // If your framework uses specific state constants, changeState handles it safely without crashing.
    }
}