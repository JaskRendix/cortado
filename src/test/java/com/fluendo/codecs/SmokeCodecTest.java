package com.fluendo.codecs;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SmokeCodecTest {

  private SmokeCodec codec;

  @BeforeEach
  void setUp() {
    codec = new SmokeCodec();
  }

  @Test
  void testParseHeaderNullOrTooShort() {
    assertNull(codec.parseHeader(null, 0, 10));
    assertNull(codec.parseHeader(new byte[5], 0, 5));
  }

  @Test
  void testParseHeaderValidValues() {
    byte[] headerData = new byte[20];

    // Type = 1
    headerData[0] = 0x01;

    // Width = 320 (0x0140) at index 1, 2
    headerData[1] = 0x01;
    headerData[2] = 0x40;

    // Height = 240 (0x00F0) at index 3, 4
    headerData[3] = 0x00;
    headerData[4] = (byte) 0xF0;

    var header = codec.parseHeader(headerData, 0, headerData.length);

    assertNotNull(header);
    assertEquals(1, header.type());
    assertEquals(320, header.width());
    assertEquals(240, header.height());
  }

  @Test
  void testDecodeWithoutReferenceAndNotKeyframe() {
    byte[] data = new byte[20];
    data[13] = 0x00; // flags (not a keyframe)

    assertNull(codec.decode(data, 0, data.length));
  }

  @Test
  void testDecodeValidKeyframe() throws IOException {
    // Create a tiny valid JPEG image byte array to embed as payload
    byte[] jpegBytes;
    try (var baos = new ByteArrayOutputStream()) {
      BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
      ImageIO.write(img, "jpg", baos);
      jpegBytes = baos.toByteArray();
    }

    // Header size is 18 bytes (OFFS_PICT).
    // blocks = 0, so imgoff = 18. Total size = 18 + jpegBytes.length
    byte[] packet = new byte[18 + jpegBytes.length];

    // Set KEYFRAME flag (bit 0 -> value 1) at index 13
    packet[13] = (byte) SmokeCodec.KEYFRAME;

    // Set width = 16 (0x0010) at index 1
    packet[1] = 0x00;
    packet[2] = 0x10;
    // Set height = 16 (0x0010) at index 3
    packet[3] = 0x00;
    packet[4] = 0x10;

    // Copy JPEG bytes into payload starting at index 18
    System.arraycopy(jpegBytes, 0, packet, 18, jpegBytes.length);

    BufferedImage resultImage = codec.decode(packet, 0, packet.length);

    assertNotNull(resultImage, "Keyframe decode should successfully return an image");
    assertEquals(16, resultImage.getWidth());
    assertEquals(16, resultImage.getHeight());
    assertTrue(codec.getHeader().isKeyframe());
  }

  @Test
  void testDecodeTruncatedPayload() {
    byte[] packet = new byte[18]; // Header only, no image payload data
    packet[13] = (byte) SmokeCodec.KEYFRAME;

    // Should return null because length - imgoff results in negative / missing
    // payload
    assertNull(codec.decode(packet, 0, packet.length));
  }
}
