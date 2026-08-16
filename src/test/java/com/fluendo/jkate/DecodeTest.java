package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DecodeTest {

  private Info info;
  private Decode decode;

  @BeforeEach
  void setUp() {
    info = new Info();
    info.gps_numerator = 1000;
    info.gps_denominator = 1;
    info.granule_shift = 16;
    info.bitstream_version_major = 0;
    info.bitstream_version_minor = 1;
    decode = new Decode(info);
  }

  @Test
  @DisplayName("Granule Conversion: Valid granule time calculation")
  void testGranuleTimeValid() {
    long granule = 1000L << 16; // base = 1000, offset = 0
    double time = decode.granuleTime(granule);
    assertEquals(1.0, time, 0.0001);
  }

  @Test
  @DisplayName("Granule Conversion: Negative or zero denominator handled safely")
  void testGranuleTimeInvalidGranuleOrZeroNumerator() {
    assertEquals(-1.0, decode.granuleTime(-5L));

    info.gps_numerator = 0;
    assertEquals(-1.0, decode.granuleTime(100L));
  }

  @Test
  @DisplayName("Granule Duration: Valid duration calculation")
  void testGranuleDurationValid() {
    double duration = decode.granuleDuration(5000L);
    assertEquals(5.0, duration, 0.0001);
  }

  @Test
  @DisplayName("Granule Duration: Negative granule returns -1")
  void testGranuleDurationNegative() {
    assertEquals(-1.0, decode.granuleDuration(-1L));
  }

  @Test
  @DisplayName("Decode Text Packet: Null event handles gracefully")
  void testDecodeTextPacketNullEvent() {
    int result = decode.decodeTextPacket(null);
    assertEquals(Result.KATE_E_BAD_PACKET, result);
  }
}
