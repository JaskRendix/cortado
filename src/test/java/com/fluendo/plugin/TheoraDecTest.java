package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.jcraft.jogg.Packet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TheoraDecTest {

  private TheoraDec theoraDec;

  @BeforeEach
  public void setUp() {
    theoraDec = new TheoraDec();
  }

  @Test
  public void testFactoryAndMime() {
    assertEquals("theoradec", theoraDec.getFactoryName());
    assertEquals("video/x-theora", theoraDec.getMime());
  }

  @Test
  public void testTypeFindValidSignature() {
    // Theora header signature: {-128, 0x74, 0x68, 0x65, 0x6f, 0x72, 0x61} ('\x80theora')
    byte[] validHeader = new byte[] {(byte) 0x80, 0x74, 0x68, 0x65, 0x6f, 0x72, 0x61, 0x00};
    int confidence = theoraDec.typeFind(validHeader, 0, validHeader.length);
    assertEquals(10, confidence, "Valid Theora signature should return confidence 10");
  }

  @Test
  public void testTypeFindInvalidSignature() {
    byte[] invalidHeader = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    int confidence = theoraDec.typeFind(invalidHeader, 0, invalidHeader.length);
    assertEquals(-1, confidence, "Invalid Theora signature should return -1");
  }

  @Test
  public void testTypeFindWithOffset() {
    byte[] dataWithOffset =
        new byte[] {0x00, 0x00, (byte) 0x80, 0x74, 0x68, 0x65, 0x6f, 0x72, 0x61};
    int confidence = theoraDec.typeFind(dataWithOffset, 2, 7);
    assertEquals(10, confidence, "Valid Theora signature starting at offset 2 should be detected");
  }

  @Test
  public void testIsHeaderCheck() {
    Packet packet = new Packet();
    packet.packet_base = new byte[] {(byte) 0x80}; // High bit set indicates header
    packet.packet = 0;
    packet.bytes = 1;

    assertTrue(
        theoraDec.isHeader(packet), "Packet with high bit set should be recognized as header");

    packet.packet_base = new byte[] {0x00}; // High bit clear indicates video frame
    assertFalse(
        theoraDec.isHeader(packet),
        "Packet with high bit clear should not be recognized as header");
  }

  @Test
  public void testIsDiscontinuous() {
    assertFalse(theoraDec.isDiscontinuous(), "TheoraDec reports continuous stream by default");
  }

  @Test
  public void testGranuleToTimeEdgeCases() {
    // Negative granule positions or missing BOS should safely evaluate to -1
    long timeNegative = theoraDec.granuleToTime(-1);
    assertEquals(-1, timeNegative, "Negative granule position should return -1");

    long timeNoBOS = theoraDec.granuleToTime(1000);
    assertEquals(-1, timeNoBOS, "Granule conversion without BOS set should return -1");
  }

  @Test
  public void testEdgeCaseTakeHeaderWithoutInitialization() {
    Packet packet = new Packet();
    packet.packet_base = new byte[] {(byte) 0x80, 0x74, 0x68, 0x65, 0x6f, 0x72, 0x61};
    packet.packet = 0;
    packet.bytes = packet.packet_base.length;

    // Taking header without internal info structure setup should return an error code safely
    int result = theoraDec.takeHeader(packet);
    assertTrue(
        result < 0,
        "Taking header on uninitialized structures should fail gracefully with negative error code");
  }
}
