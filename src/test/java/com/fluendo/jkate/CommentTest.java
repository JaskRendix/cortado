package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommentTest {

  @Test
  @DisplayName("Constructors & Accessors: Values set and retrieved correctly")
  void testConstructorsAndGettersSetters() {
    String[] comments = {"ARTIST=Test Artist", "TITLE=Test Title"};
    Comment comment = new Comment("Xiph.Org libKate", comments);

    assertEquals("Xiph.Org libKate", comment.getVendor());
    assertArrayEquals(comments, comment.getUserComments());
  }

  @Test
  @DisplayName("Clear Method: Clears fields to null")
  void testClear() {
    String[] comments = {"GENRE=Classical"};
    Comment comment = new Comment("Vendor", comments);

    comment.clear();

    assertNull(comment.getVendor());
    assertNull(comment.getUserComments());
  }

  @Test
  @DisplayName("Equals and HashCode: Compare and hash matching comments properly")
  void testEqualsAndHashCode() {
    String[] c1 = {"TITLE=A"};
    String[] c2 = {"TITLE=B"};

    Comment comment1 = new Comment("VendorA", c1);
    Comment comment2 = new Comment("VendorA", c1);
    Comment comment3 = new Comment("VendorB", c1);
    Comment comment4 = new Comment("VendorA", c2);

    assertEquals(comment1, comment1);
    assertEquals(comment1, comment2);
    assertEquals(comment1.hashCode(), comment2.hashCode());
    assertNotEquals(comment1, comment3);
    assertNotEquals(comment1, comment4);
    assertNotEquals(comment1, null);
    assertNotEquals(comment1, "NotAComment");
  }

  @Test
  @DisplayName("ToString: Generates descriptive string representation")
  void testToString() {
    String[] comments = {"ALBUM=Live"};
    Comment comment = new Comment("VendorX", comments);

    String str = comment.toString();
    assertNotNull(str);
    assertTrue(str.contains("VendorX"));
    assertTrue(str.contains("ALBUM=Live"));
  }
}
