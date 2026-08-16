package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ColorTest {

  @Test
  @DisplayName("Constructors & Accessors: Values set and retrieved correctly")
  void testConstructorsAndGettersSetters() {
    Color color = new Color();
    color.setR((byte) 255);
    color.setG((byte) 128);
    color.setB((byte) 0);
    color.setA((byte) 64);

    assertEquals((byte) 255, color.getR());
    assertEquals((byte) 128, color.getG());
    assertEquals((byte) 0, color.getB());
    assertEquals((byte) 64, color.getA());

    Color paramColor = new Color((byte) 10, (byte) 20, (byte) 30, (byte) 40);
    assertEquals((byte) 10, paramColor.getR());
    assertEquals((byte) 20, paramColor.getG());
    assertEquals((byte) 30, paramColor.getB());
    assertEquals((byte) 40, paramColor.getA());
  }

  @Test
  @DisplayName("Equals and HashCode: Compare and hash matching colors properly")
  void testEqualsAndHashCode() {
    Color c1 = new Color((byte) 1, (byte) 2, (byte) 3, (byte) 4);
    Color c2 = new Color((byte) 1, (byte) 2, (byte) 3, (byte) 4);
    Color c3 = new Color((byte) 99, (byte) 2, (byte) 3, (byte) 4);

    assertEquals(c1, c1);
    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
    assertNotEquals(c1, c3);
    assertNotEquals(c1, null);
    assertNotEquals(c1, "NotAColor");
  }

  @Test
  @DisplayName("ToString: Generates descriptive string representation")
  void testToString() {
    Color color = new Color((byte) 10, (byte) 20, (byte) 30, (byte) 40);
    String str = color.toString();
    assertNotNull(str);
    assertTrue(str.contains("r=10"));
    assertTrue(str.contains("g=20"));
    assertTrue(str.contains("b=30"));
    assertTrue(str.contains("a=40"));
  }
}
