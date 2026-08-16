package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FontMappingTest {

  @Test
  @DisplayName("Constructors & Accessors: Default and parameterized constructors work as expected")
  void testConstructorsAndGettersSetters() {
    FontRange[] ranges = {new FontRange(32, 126, 0)};
    FontMapping mapping1 = new FontMapping();
    mapping1.setRanges(ranges);

    assertArrayEquals(ranges, mapping1.getRanges());

    FontMapping mapping2 = new FontMapping(ranges);
    assertEquals(mapping1, mapping2);
  }

  @Test
  @DisplayName("Equals and HashCode: Equal objects match and have same hash")
  void testEqualsAndHashCode() {
    FontRange[] ranges1 = {new FontRange(65, 90, 10)};
    FontRange[] ranges2 = {new FontRange(65, 90, 10)};
    FontRange[] ranges3 = {new FontRange(97, 122, 36)};

    FontMapping m1 = new FontMapping(ranges1);
    FontMapping m2 = new FontMapping(ranges2);
    FontMapping m3 = new FontMapping(ranges3);

    assertEquals(m1, m1);
    assertEquals(m1, m2);
    assertEquals(m1.hashCode(), m2.hashCode());
    assertNotEquals(m1, m3);
    assertNotEquals(m1, null);
    assertNotEquals(m1, "SomeString");
  }

  @Test
  @DisplayName("ToString: Returns a non-null formatted string")
  void testToString() {
    FontRange[] ranges = {new FontRange(32, 126, 5)};
    FontMapping mapping = new FontMapping(ranges);
    String str = mapping.toString();
    assertNotNull(str);
    assertTrue(str.contains("ranges="));
    assertTrue(str.contains("first_code_point=32"));
  }
}
