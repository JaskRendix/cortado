package com.fluendo.jheora;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  void clearSetsUserCommentsAndVendorToNull() {
    Comment c = new Comment();
    c.user_comments = new String[] {"test1", "test2"};
    c.vendor = "Xiph.Org";

    c.clear();

    assertNull(c.user_comments, "user_comments should be null after clear()");
    assertNull(c.vendor, "vendor should be null after clear()");
  }

  @Test
  void clearWorksWhenFieldsAreAlreadyNull() {
    Comment c = new Comment();

    // Both fields start null
    assertNull(c.user_comments);
    assertNull(c.vendor);

    // Should not throw
    c.clear();

    assertNull(c.user_comments);
    assertNull(c.vendor);
  }

  @Test
  void userCommentsCanBeAssignedAndRetrieved() {
    Comment c = new Comment();
    String[] comments = {"alpha", "beta", "gamma"};

    c.user_comments = comments;

    assertArrayEquals(comments, c.user_comments);
  }

  @Test
  void vendorCanBeAssignedAndRetrieved() {
    Comment c = new Comment();
    c.vendor = "Fluendo";

    assertEquals("Fluendo", c.vendor);
  }

  @Test
  void userCommentsArrayCanBeEmpty() {
    Comment c = new Comment();
    c.user_comments = new String[0];

    assertNotNull(c.user_comments);
    assertEquals(0, c.user_comments.length);
  }

  @Test
  void userCommentsCanContainNullEntries() {
    Comment c = new Comment();
    c.user_comments = new String[] {"valid", null, "also valid"};

    assertEquals("valid", c.user_comments[0]);
    assertNull(c.user_comments[1]);
    assertEquals("also valid", c.user_comments[2]);
  }

  @Test
  void clearDoesNotThrowWhenUserCommentsContainsNulls() {
    Comment c = new Comment();
    c.user_comments = new String[] {null, "x", null};
    c.vendor = "Vendor";

    c.clear();

    assertNull(c.user_comments);
    assertNull(c.vendor);
  }

  @Test
  void commentObjectIsMutable() {
    Comment c = new Comment();

    c.user_comments = new String[] {"first"};
    c.vendor = "Vendor1";

    assertEquals("first", c.user_comments[0]);
    assertEquals("Vendor1", c.vendor);

    c.user_comments = new String[] {"second", "third"};
    c.vendor = "Vendor2";

    assertEquals("second", c.user_comments[0]);
    assertEquals("Vendor2", c.vendor);
  }
}
