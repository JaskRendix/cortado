package com.fluendo.jst;

import static org.junit.jupiter.api.Assertions.*;

import com.fluendo.jst.testplugins.TestElementA;
import com.fluendo.jst.testplugins.TestElementB;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ElementFactoryTest {

  @BeforeAll
  static void injectTestPlugins() throws Exception {
    // Replace the static plugin list with our test plugins
    Field f = ElementFactory.class.getDeclaredField("elements");
    f.setAccessible(true);

    List<? super Element> list = (List<? super Element>) f.get(null);

    list.clear();
    list.add(new TestElementA());
    list.add(new TestElementB());
  }

  @Test
  void testMakeByMime() {
    Element e = ElementFactory.makeByMime("test/a", "instanceA");

    assertNotNull(e);
    assertEquals("instanceA", e.getName());
    assertEquals("test/a", e.getMime());
  }

  @Test
  void testMakeByMimeNotFound() {
    Element e = ElementFactory.makeByMime("unknown/mime", "x");
    assertNull(e);
  }

  @Test
  void testMakeByName() {
    Element e = ElementFactory.makeByName("TestElementB", "instanceB");

    assertNotNull(e);
    assertEquals("instanceB", e.getName());
    assertEquals("test/b", e.getMime());
  }

  @Test
  void testMakeByNameNotFound() {
    Element e = ElementFactory.makeByName("NoSuchPlugin", "x");
    assertNull(e);
  }

  @Test
  void testTypeFindMime() {
    byte[] data = {'B'};
    String mime = ElementFactory.typeFindMime(data, 0, 1);

    assertEquals("test/b", mime);
  }

  @Test
  void testTypeFindMimeNoMatch() {
    byte[] data = {'X'};
    String mime = ElementFactory.typeFindMime(data, 0, 1);

    assertNull(mime);
  }

  @Test
  void testMakeTypeFind() {
    byte[] data = {'A'};
    Element e = ElementFactory.makeTypeFind(data, 0, 1, "chosen");

    assertNotNull(e);
    assertEquals("chosen", e.getName());
    assertEquals("test/a", e.getMime());
  }

  @Test
  void testMakeTypeFindNoMatch() {
    byte[] data = {'Z'};
    Element e = ElementFactory.makeTypeFind(data, 0, 1, "x");

    assertNull(e);
  }

  @Test
  void testDupCreatesNewInstance() throws Exception {
    Element original = new TestElementA();

    // Access private dup() via reflection
    var m = ElementFactory.class.getDeclaredMethod("dup", Element.class, String.class);
    m.setAccessible(true);

    Element copy = (Element) m.invoke(null, original, "copyA");

    assertNotNull(copy);
    assertNotSame(original, copy);
    assertEquals("copyA", copy.getName());
    assertEquals(original.getMime(), copy.getMime());
  }
}
