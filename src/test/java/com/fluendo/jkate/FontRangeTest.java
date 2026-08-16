package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FontRangeTest {

  @Test
  @DisplayName("Constructors & Accessors: Default and parameterized constructors work as expected")
  void testConstructorsAndGettersSetters() {
    FontRange range1 = new FontRange();
    range1.setFirstCodePoint(32);
    range1.setLastCodePoint(126);
    range1.setFirstBitmap(0);

    assertEquals(32, range1.getFirstCodePoint());
    assertEquals(126, range1.getLastCodePoint());
    assertEquals(0, range1.getFirstBitmap());

    FontRange range2 = new FontRange(32, 126, 0);
    assertEquals(range1, range2);
  }

  @Test
  @DisplayName("Equals and HashCode: Equal objects match and have same hash")
  void testEqualsAndHashCode() {
    FontRange r1 = new FontRange(65, 90, 10);
    FontRange r2 = new FontRange(65, 90, 10);
    FontRange r3 = new FontRange(97, 122, 36);

    assertEquals(r1, r1);
    assertEquals(r1, r2);
    assertEquals(r1.hashCode(), r2.hashCode());
    assertNotEquals(r1, r3);
    assertNotEquals(r1, null);
    assertNotEquals(r1, "SomeString");
  }

  @Test
  @DisplayName("ToString: Returns a non-null formatted string")
  void testToString() {
    FontRange range = new FontRange(32, 126, 5);
    String str = range.toString();
    assertNotNull(str);
    assertTrue(str.contains("first_code_point=32"));
    assertTrue(str.contains("last_code_point=126"));
    assertTrue(str.contains("first_bitmap=5"));
  }
}
