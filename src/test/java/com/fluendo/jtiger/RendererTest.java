package com.fluendo.jtiger;

import static org.junit.jupiter.api.Assertions.*;

import com.fluendo.jkate.Event;
import com.fluendo.jkate.Info;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RendererTest {

  private Renderer renderer;
  private MockComponent mockComponent;

  @BeforeEach
  void setUp() {
    renderer = new Renderer();
    mockComponent = new MockComponent();
  }

  @Test
  void testInitialStateIsDirty() {
    assertTrue(renderer.isDirty());
  }

  @Test
  void testAddEventSetsDirty() {
    renderer.render(mockComponent, new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB));
    assertFalse(renderer.isDirty());

    renderer.add(createDummyEvent());
    assertTrue(renderer.isDirty());
  }

  @Test
  void testFlushClearsItemsAndSetsDirty() {
    renderer.add(createDummyEvent());
    renderer.flush();
    assertTrue(renderer.isDirty());

    // Empty renderer update should return 1 (nothing to draw)
    Dimension dim = new Dimension(100, 100);
    assertEquals(1, renderer.update(mockComponent, dim, 0.0));
  }

  @Test
  void testRenderWithNullParameters() {
    Image img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    Image resultNullComp = renderer.render(null, img);
    Image resultNullImg = renderer.render(mockComponent, null);

    assertEquals(img, resultNullComp);
    assertNull(resultNullImg);
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
        // Modern standard fallback using MethodHandles instead of sun.misc.Unsafe
        MethodHandles.Lookup lookup =
            MethodHandles.privateLookupIn(Event.class, MethodHandles.lookup());
        return (Event)
            lookup.unreflectConstructor(Event.class.getDeclaredConstructor()).invokeWithArguments();
      } catch (Throwable ignored) {
      }
      return null;
    }
  }

  // --- Headless Test Double ---
  private static class MockComponent extends Component {
    // Keeps component alive for AWT operations during testing without display server
  }
}
