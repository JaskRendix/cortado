package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventTest {

  @Test
  @DisplayName("Constructor: Default constructor initializes safe default values")
  void testDefaultConstructor() {
    Event event = new Event();

    assertNull(event.ki);
    assertEquals(-1, event.id);
    assertNull(event.kr);
    assertNull(event.ks);
    assertNull(event.ks2);
    assertNull(event.motions);
    assertNull(event.palette);
    assertNull(event.bitmap);
    assertNull(event.text);
    assertNull(event.font_mapping);
    assertNull(event.language);
  }

  @Test
  @DisplayName("Constructor: Info-based constructor with null Info parameter")
  void testInfoConstructorWithNull() {
    Event event = new Event(null);

    assertNull(event.ki);
    assertEquals(-1, event.id);
    assertNull(event.text_encoding);
    assertNull(event.text_directionality);
    assertNull(event.markup_type);
    assertNull(event.language);
  }

  @Test
  @DisplayName(
      "Constructor: Info-based constructor with valid Info parameter maps properties correctly")
  void testInfoConstructorWithValidInfo() throws KateException {
    Info info = new Info();
    info.text_encoding = KateTextEncoding.createTextEncoding(0);

    Event event = new Event(info);

    assertEquals(info, event.ki);
    assertEquals(-1, event.id);
    assertEquals(KateTextEncoding.KATE_UTF8, event.text_encoding);
    assertNull(event.language);
    assertNull(event.kr);
    assertNull(event.ks);
    assertNull(event.text);
  }

  @Test
  @DisplayName("Fields: Direct field assignment and manipulation paths")
  void testFieldAssignments() {
    Event event = new Event();

    event.start = 100L;
    event.duration = 500L;
    event.backlink = 50L;
    event.start_time = 1.0;
    event.end_time = 5.0;
    event.id = 42;
    event.text = "Hello Kate".getBytes();

    assertEquals(100L, event.start);
    assertEquals(500L, event.duration);
    assertEquals(50L, event.backlink);
    assertEquals(1.0, event.start_time);
    assertEquals(5.0, event.end_time);
    assertEquals(42, event.id);
    assertNotNull(event.text);
    assertEquals("Hello Kate", new String(event.text));
  }
}
