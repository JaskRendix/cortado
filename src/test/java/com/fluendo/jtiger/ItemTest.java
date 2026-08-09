package com.fluendo.jtiger;

import com.fluendo.jkate.Event;
import com.fluendo.jkate.Info;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    private Event dummyEvent;
    private MockComponent mockComponent;

    @BeforeEach
    void setUp() {
        mockComponent = new MockComponent();
        dummyEvent = createDummyEvent();
        if (dummyEvent != null) {
            dummyEvent.start_time = 1.0;
            dummyEvent.end_time = 5.0;
            dummyEvent.text = "Hello Subtitle".getBytes(StandardCharsets.UTF_8);
        }
    }

    @Test
    void testItemCreationAndInitialState() {
        Item item = new Item(dummyEvent);
        assertFalse(item.isActive());
        assertFalse(item.isDirty()); // Initialized to false per constructor logic
    }

    @Test
    void testItemLifecycleUpdate() {
        Item item = new Item(dummyEvent);
        Dimension dim = new Dimension(640, 480);

        // Before start time
        boolean activeBefore = item.update(mockComponent, dim, 0.5);
        assertTrue(activeBefore);
        assertFalse(item.isActive());

        // Within lifetime
        boolean activeDuring = item.update(mockComponent, dim, 2.0);
        assertTrue(activeDuring);
        assertTrue(item.isActive());
        assertTrue(item.isDirty());

        // After end time (should trigger destruction returning false)
        boolean activeAfter = item.update(mockComponent, dim, 6.0);
        assertFalse(activeAfter);
        assertFalse(item.isActive());
    }

    @Test
    void testItemRenderExecution() {
        Item item = new Item(dummyEvent);
        Dimension dim = new Dimension(640, 480);
        item.update(mockComponent, dim, 2.0); // Make active

        BufferedImage img = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
        assertDoesNotThrow(() -> item.render(mockComponent, img));
        assertFalse(item.isDirty());
    }

    private Event createDummyEvent() {
        try {
            Constructor<Event> constructor = Event.class.getDeclaredConstructor(Info.class);
            constructor.setAccessible(true);
            
            Info info = null;
            try {
                info = Info.class.getDeclaredConstructor().newInstance();
            } catch (Exception ignored) {
                if (Info.class.getDeclaredConstructors().length > 0) {
                    Constructor<?> c = Info.class.getDeclaredConstructors()[0];
                    c.setAccessible(true);
                    Object[] args = new Object[c.getParameterCount()];
                    info = (Info) c.newInstance(args);
                }
            }
            return constructor.newInstance(info);
        } catch (Exception e) {
            try {
                java.lang.invoke.MethodHandles.Lookup lookup = java.lang.invoke.MethodHandles.privateLookupIn(Event.class, java.lang.invoke.MethodHandles.lookup());
                return (Event) lookup.unreflectConstructor(Event.class.getDeclaredConstructor()).invokeWithArguments();
            } catch (Throwable ignored) {}
            return null;
        }
    }

    // --- Headless Test Double ---
    private static class MockComponent extends Component {
        // Keeps component alive for AWT operations during testing without display server
    }
}
