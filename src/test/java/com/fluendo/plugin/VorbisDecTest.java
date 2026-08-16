package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.jcraft.jogg.Packet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VorbisDecTest {

  private VorbisDec vorbisDec;

  @BeforeEach
  public void setUp() {
    vorbisDec = new VorbisDec();
  }

  @Test
  public void testFactoryAndMime() {
    assertEquals("vorbisdec", vorbisDec.getFactoryName());
    assertEquals("audio/x-vorbis", vorbisDec.getMime());
  }

  @Test
  public void testTypeFindValidSignature() {
    // Vorbis header signature: 0x01, 0x76, 0x6f, 0x72, 0x62, 0x69, 0x73 ('\x01vorbis')
    byte[] validHeader = new byte[] {0x01, 0x76, 0x6f, 0x72, 0x62, 0x69, 0x73, 0x00};
    int confidence = vorbisDec.typeFind(validHeader, 0, validHeader.length);
    assertEquals(10, confidence, "Valid Vorbis signature should return confidence 10");
  }

  @Test
  public void testTypeFindInvalidSignature() {
    byte[] invalidHeader = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    int confidence = vorbisDec.typeFind(invalidHeader, 0, invalidHeader.length);
    assertEquals(-1, confidence, "Invalid Vorbis signature should return -1");
  }

  @Test
  public void testTypeFindWithOffset() {
    byte[] dataWithOffset = new byte[] {0x00, 0x00, 0x01, 0x76, 0x6f, 0x72, 0x62, 0x69, 0x73};
    int confidence = vorbisDec.typeFind(dataWithOffset, 2, 7);
    assertEquals(10, confidence, "Valid Vorbis signature starting at offset 2 should be detected");
  }

  @Test
  public void testIsHeaderCheck() {
    Packet packet = new Packet();
    packet.packet_base = new byte[] {0x01}; // Odd byte indicates header
    packet.packet = 0;
    packet.bytes = 1;

    assertTrue(
        vorbisDec.isHeader(packet), "Packet with odd header flag should be recognized as header");

    packet.packet_base = new byte[] {0x00}; // Even byte indicates audio data
    assertFalse(
        vorbisDec.isHeader(packet),
        "Packet with even header flag should not be recognized as header");
  }

  @Test
  public void testIsKeyFrameAndDiscontinuous() {
    Packet packet = new Packet();
    assertTrue(vorbisDec.isKeyFrame(packet), "Vorbis frames are always keyframes");
    assertFalse(vorbisDec.isDiscontinuous(), "VorbisDec reports continuous stream by default");
  }

  @Test
  public void testGranuleToTimeConversion() {
    // Since vi.rate is 0 initially before headers, granuleToTime handles negative or zero
    // gracefully
    long time = vorbisDec.granuleToTime(-1);
    assertEquals(-1, time, "Negative granule position should return -1");
  }

  @Test
  public void testEdgeCaseTakeHeaderWithoutInitialization() {
    Packet packet = new Packet();
    packet.packet_base = new byte[] {0x01, 0x76, 0x6f, 0x72, 0x62, 0x69, 0x73};
    packet.packet = 0;
    packet.bytes = packet.packet_base.length;

    // Taking header without internal info structure setup should return an error code safely
    // instead of crashing
    int result = vorbisDec.takeHeader(packet);
    assertTrue(
        result < 0,
        "Taking header on uninitialized structures should fail gracefully with negative error code");
  }
}
