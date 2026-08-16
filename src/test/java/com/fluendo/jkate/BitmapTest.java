package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BitmapTest {

  @Test
  @DisplayName("Constructors & Accessors: Values set and retrieved correctly")
  void testConstructorsAndGettersSetters() throws KateException {
    KateBitmapType bType = KateBitmapType.createBitmapType(1);
    byte[] pixelData = {0x01, 0x02, 0x03, 0x04};

    Bitmap bitmap = new Bitmap();
    bitmap.setWidth(64);
    bitmap.setHeight(32);
    bitmap.setBpp(8);
    bitmap.setType(bType);
    bitmap.setPalette(0);
    bitmap.setPixels(pixelData);
    bitmap.setSize(4);
    bitmap.setX_offset(5);
    bitmap.setY_offset(10);

    assertEquals(64, bitmap.getWidth());
    assertEquals(32, bitmap.getHeight());
    assertEquals(8, bitmap.getBpp());
    assertEquals(bType, bitmap.getType());
    assertEquals(0, bitmap.getPalette());
    assertArrayEquals(pixelData, bitmap.getPixels());
    assertEquals(4, bitmap.getSize());
    assertEquals(5, bitmap.getX_offset());
    assertEquals(10, bitmap.getY_offset());

    Bitmap paramBitmap = new Bitmap(64, 32, 8, bType, 0, pixelData, 4, 5, 10);
    assertEquals(bitmap, paramBitmap);
  }

  @Test
  @DisplayName("Equals and HashCode: Compare and hash matching bitmaps properly")
  void testEqualsAndHashCode() throws KateException {
    KateBitmapType t1 = KateBitmapType.createBitmapType(0);
    KateBitmapType t2 = KateBitmapType.createBitmapType(1);
    byte[] p1 = {0x00, 0x01};
    byte[] p2 = {0x02, 0x03};

    Bitmap b1 = new Bitmap(10, 20, 4, t1, 1, p1, 2, 0, 0);
    Bitmap b2 = new Bitmap(10, 20, 4, t1, 1, p1, 2, 0, 0);
    Bitmap b3 = new Bitmap(15, 20, 4, t1, 1, p1, 2, 0, 0);
    Bitmap b4 = new Bitmap(10, 20, 4, t2, 1, p1, 2, 0, 0);
    Bitmap b5 = new Bitmap(10, 20, 4, t1, 1, p2, 2, 0, 0);

    assertEquals(b1, b1);
    assertEquals(b1, b2);
    assertEquals(b1.hashCode(), b2.hashCode());
    assertNotEquals(b1, b3);
    assertNotEquals(b1, b4);
    assertNotEquals(b1, b5);
    assertNotEquals(b1, null);
    assertNotEquals(b1, "NotABitmap");
  }

  @Test
  @DisplayName("ToString: Generates descriptive string representation")
  void testToString() throws KateException {
    KateBitmapType t = KateBitmapType.createBitmapType(0);
    byte[] pixels = {0x00};
    Bitmap bitmap = new Bitmap(10, 10, 8, t, 0, pixels, 1, 0, 0);

    String str = bitmap.toString();
    assertNotNull(str);
    assertTrue(str.contains("width=10"));
    assertTrue(str.contains("height=10"));
    assertTrue(str.contains("bpp=8"));
  }
}
