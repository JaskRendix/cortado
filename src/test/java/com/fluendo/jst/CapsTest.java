package com.fluendo.jst;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CapsTest {

  @Test
  void testMimeParsing() {
    Caps caps = new Caps("video/x-raw; width=640; height=480");

    assertEquals("video/x-raw", caps.getMime());
  }

  @Test
  void testFieldParsing() {
    Caps caps = new Caps("audio/x-raw; rate=44100; channels=2");

    assertEquals("44100", caps.getField("rate"));
    assertEquals("2", caps.getField("channels"));
  }

  @Test
  void testFieldParsingWhitespace() {
    Caps caps = new Caps("video/x-raw ;   width = 1920 ; height = 1080 ");

    assertEquals("1920", caps.getField("width"));
    assertEquals("1080", caps.getField("height"));
  }

  @Test
  void testSetField() {
    Caps caps = new Caps("video/x-raw");
    caps.setField("format", "RGB");

    assertEquals("RGB", caps.getField("format"));
  }

  @Test
  void testSetFieldInt() {
    Caps caps = new Caps("video/x-raw");
    caps.setFieldInt("width", 800);

    assertEquals(800, caps.getFieldInt("width", -1));
  }

  @Test
  void testGetFieldIntFromString() {
    Caps caps = new Caps("video/x-raw; width=1024");

    assertEquals(1024, caps.getFieldInt("width", -1));
  }

  @Test
  void testGetFieldIntInvalidString() {
    Caps caps = new Caps("video/x-raw; width=abc");

    assertEquals(-1, caps.getFieldInt("width", -1));
  }

  @Test
  void testGetFieldIntMissing() {
    Caps caps = new Caps("video/x-raw");

    assertEquals(999, caps.getFieldInt("missing", 999));
  }

  @Test
  void testGetFieldString() {
    Caps caps = new Caps("video/x-raw; format=YUV");

    assertEquals("YUV", caps.getFieldString("format", "default"));
  }

  @Test
  void testGetFieldStringMissing() {
    Caps caps = new Caps("video/x-raw");

    assertEquals("default", caps.getFieldString("missing", "default"));
  }

  @Test
  void testToStringContainsMime() {
    Caps caps = new Caps("audio/x-raw; rate=48000");

    String s = caps.toString();
    assertTrue(s.contains("audio/x-raw"));
  }

  @Test
  void testToStringContainsFields() {
    Caps caps = new Caps("video/x-raw; width=1280; height=720");

    String s = caps.toString();

    assertTrue(s.contains("\"width\": \"1280\""));
    assertTrue(s.contains("\"height\": \"720\""));
  }

  @Test
  void testMultipleFieldsParsing() {
    Caps caps = new Caps("video/x-raw; a=1; b=2; c=3");

    assertEquals("1", caps.getField("a"));
    assertEquals("2", caps.getField("b"));
    assertEquals("3", caps.getField("c"));
  }

  @Test
  void testEmptyFields() {
    Caps caps = new Caps("video/x-raw");

    assertNull(caps.getField("anything"));
  }

  @Test
  void testMimeOnlyNoCrash() {
    Caps caps = new Caps("application/octet-stream");

    assertEquals("application/octet-stream", caps.getMime());
    assertTrue(caps.fields.isEmpty());
  }
}
