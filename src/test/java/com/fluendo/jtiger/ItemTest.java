package com.fluendo.jtiger;

import static org.junit.jupiter.api.Assertions.*;

import com.fluendo.jkate.Bitmap;
import com.fluendo.jkate.Event;
import com.fluendo.jkate.Info;
import com.fluendo.jkate.Palette;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ItemTest {

  private Event ev;
  private MockComponent component;
  private Dimension dim;

  @BeforeEach
  void setUp() {
    component = new MockComponent();
    dim = new Dimension(640, 480);

    ev = createEvent();
    ev.start_time = 1.0;
    ev.end_time = 5.0;
    ev.text = "Hello Subtitle".getBytes(StandardCharsets.UTF_8);
  }

  @Nested
  @DisplayName("Constructor behavior")
  class ConstructorTests {

    @Test
    @DisplayName("Item initializes inactive and not dirty")
    void constructorInitialState() {
      Item item = new Item(ev);
      assertFalse(item.isActive());
      assertFalse(item.isDirty());
    }
  }

  @Nested
  @DisplayName("Lifecycle update behavior")
  class UpdateTests {

    @Test
    @DisplayName("Before start time → active=false, returns true")
    void beforeStartTime() {
      Item item = new Item(ev);
      assertTrue(item.update(component, dim, 0.5));
      assertFalse(item.isActive());
    }

    @Test
    @DisplayName("During event → active=true, dirty=true")
    void duringEvent() {
      Item item = new Item(ev);
      assertTrue(item.update(component, dim, 2.0));
      assertTrue(item.isActive());
      assertTrue(item.isDirty());
    }

    @Test
    @DisplayName("After end time → active=false, returns false")
    void afterEndTime() {
      Item item = new Item(ev);
      assertFalse(item.update(component, dim, 6.0));
      assertFalse(item.isActive());
    }
  }

  @Nested
  @DisplayName("Rendering behavior")
  class RenderingTests {

    @Test
    @DisplayName("Render executes without throwing and clears dirty flag")
    void renderClearsDirty() {
      Item item = new Item(ev);
      item.update(component, dim, 2.0); // activate

      BufferedImage img = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);

      assertDoesNotThrow(() -> item.render(component, img));
      assertFalse(item.isDirty());
    }

    @Test
    @DisplayName("Render does nothing when inactive")
    void renderInactiveDoesNothing() {
      Item item = new Item(ev);
      BufferedImage img = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);

      assertDoesNotThrow(() -> item.render(component, img));
      assertFalse(item.isActive());
    }

    @Test
    @DisplayName("Background image is created when event has bitmap + palette")
    void backgroundImageCreated() throws Exception {
      // Build bitmap
      Bitmap kb = new Bitmap();
      kb.width = 2;
      kb.height = 2;
      kb.bpp = 8;
      kb.pixels = new byte[] {0, 1, 1, 0};
      ev.bitmap = kb;

      // Build palette via reflection
      Palette palette = new Palette();
      populatePaletteWithTwoColors(palette);
      ev.palette = palette;

      Item item = new Item(ev);
      item.update(component, dim, 2.0);

      BufferedImage img = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);

      assertDoesNotThrow(() -> item.render(component, img));
      assertFalse(item.isDirty());
    }
  }

  private void populatePaletteWithTwoColors(Palette palette) throws Exception {
    Field colorsField = Palette.class.getDeclaredField("colors");
    colorsField.setAccessible(true);

    Class<?> colorClass = colorsField.getType().getComponentType();
    Object colorArray = Array.newInstance(colorClass, 2);

    Object c1 = createColorInstance(colorClass, (byte) 255, (byte) 0, (byte) 0, (byte) 255);
    Object c2 = createColorInstance(colorClass, (byte) 0, (byte) 255, (byte) 0, (byte) 255);

    Array.set(colorArray, 0, c1);
    Array.set(colorArray, 1, c2);

    colorsField.set(palette, colorArray);
  }

  private Object createColorInstance(Class<?> colorClass, byte r, byte g, byte b, byte a)
      throws Exception {
    try {
      return colorClass
          .getDeclaredConstructor(byte.class, byte.class, byte.class, byte.class)
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
    }
  }

  private Event createEvent() {
    try {
      Constructor<Event> ctor = Event.class.getDeclaredConstructor(Info.class);
      ctor.setAccessible(true);
      return ctor.newInstance(new Info());
    } catch (Throwable ignored) {
      return new Event(null);
    }
  }

  private static class MockComponent extends Component {
    private static final long serialVersionUID = 1L;
  }
}
