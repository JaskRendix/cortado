package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.fluendo.jst.Buffer;
import com.fluendo.jst.Element;
import java.awt.Button;
import java.awt.Component;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JPEGDec Test Suite")
class JPEGDecTest {

  private JPEGDec jpegDec;

  @BeforeEach
  void setUp() {
    jpegDec = new JPEGDec();
  }

  @Test
  @DisplayName("Should expose correct factory metadata")
  void testMetadataAndTypeFind() {
    assertEquals("jpegdec", jpegDec.getFactoryName());
    assertEquals("image/jpeg", jpegDec.getMime());

    byte[] dummyData = {
      (byte) 0xFF,
      (byte) 0xD8,
      (byte) 0xFF,
      (byte) 0xE0
    };

    assertEquals(-1, jpegDec.typeFind(dummyData, 0, dummyData.length));
  }

  @Test
  @DisplayName("Should set and retrieve component property")
  void testComponentProperty() {
    assertNull(jpegDec.getProperty("component"));

    Component component = new Button("Test Button");

    assertTrue(jpegDec.setProperty("component", component));
    assertSame(component, jpegDec.getProperty("component"));
  }

  @Test
  @DisplayName("Should replace component property")
  void testReplaceComponentProperty() {
    Component first = new Button("First");
    Component second = new Button("Second");

    assertTrue(jpegDec.setProperty("component", first));
    assertSame(first, jpegDec.getProperty("component"));

    assertTrue(jpegDec.setProperty("component", second));
    assertSame(second, jpegDec.getProperty("component"));
  }

  @Test
  @DisplayName("Should reject unknown property")
  void testUnknownProperty() {
    assertFalse(jpegDec.setProperty("unknown", "value"));
  }

  @Test
  @DisplayName("Should return null for unknown property")
  void testUnknownGetProperty() {
    assertNull(jpegDec.getProperty("unknown"));
  }

  @Test
  @DisplayName("Should throw NullPointerException for null component")
  void testNullComponentProperty() {
    assertThrows(
        NullPointerException.class,
        () -> jpegDec.setProperty("component", null));
  }

  @Test
  @DisplayName("Should reset dimensions on STOP_PAUSE transition")
  void testStopPauseResetsDimensions() throws Exception {
    Field widthField = JPEGDec.class.getDeclaredField("width");
    Field heightField = JPEGDec.class.getDeclaredField("height");

    widthField.setAccessible(true);
    heightField.setAccessible(true);

    widthField.setInt(jpegDec, 640);
    heightField.setInt(jpegDec, 480);

    jpegDec.changeState(Element.STOP_PAUSE);

    assertEquals(-1, widthField.getInt(jpegDec));
    assertEquals(-1, heightField.getInt(jpegDec));
  }

  @Test
  @DisplayName("Should leave dimensions unchanged for other transitions")
  void testNonStopPauseDoesNotResetDimensions() throws Exception {
    Field widthField = JPEGDec.class.getDeclaredField("width");
    Field heightField = JPEGDec.class.getDeclaredField("height");

    widthField.setAccessible(true);
    heightField.setAccessible(true);

    widthField.setInt(jpegDec, 640);
    heightField.setInt(jpegDec, 480);

    jpegDec.changeState(Element.PAUSE_PLAY);

    assertEquals(640, widthField.getInt(jpegDec));
    assertEquals(480, heightField.getInt(jpegDec));
  }

  @Test
  @DisplayName("Should handle invalid image buffer without throwing")
  void testChainWithInvalidBuffer() {
    Component component = new Button("Tracker Component");

    assertTrue(jpegDec.setProperty("component", component));

    Buffer buffer = new Buffer();
    buffer.data = new byte[] {1, 2, 3, 4};
    buffer.offset = 0;
    buffer.length = 4;

    assertDoesNotThrow(() -> jpegDec.getPad("sink").push(buffer));
  }

  @Test
  @DisplayName("Should expose sink and source pads")
  void testPadsExist() {
    assertNotNull(jpegDec.getPad("sink"));
    assertNotNull(jpegDec.getPad("src"));
  }
}
