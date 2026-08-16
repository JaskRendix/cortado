package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.fluendo.jst.Element;
import com.jcraft.jogg.Packet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("KateDec Test Suite")
class KateDecTest {

  private KateDec kateDec;

  @BeforeEach
  void setUp() {
    kateDec = new KateDec();
  }

  @Nested
  @DisplayName("Factory and Metadata Tests")
  class MetadataTests {

    @Test
    @DisplayName("Should return correct factory name and mime type")
    void testFactoryAndMime() {
      assertEquals("katedec", kateDec.getFactoryName());
      assertEquals("application/x-kate", kateDec.getMime());
      assertTrue(kateDec.isDiscontinuous());
      assertTrue(kateDec.isKeyFrame(new Packet()));
    }

    @Test
    @DisplayName("Should correctly identify Kate signature bytes")
    void testTypeFindSignature() {
      byte[] validPayload =
          new byte[] {(byte) -128, 0x6b, 0x61, 0x74, 0x65, 0x00, 0x00, 0x00, 0x01};
      byte[] invalidPayload = new byte[] {0x00, 0x01, 0x02, 0x03};

      assertEquals(10, kateDec.typeFind(validPayload, 0, validPayload.length));
      assertEquals(-1, kateDec.typeFind(invalidPayload, 0, invalidPayload.length));
    }

    @Test
    @DisplayName("Should check packet header correctly based on first byte")
    void testIsHeader() {
      Packet packet = new Packet();
      packet.packetBase = new byte[] {(byte) 0x80};
      packet.packet = 0;
      assertTrue(kateDec.isHeader(packet));

      packet.packetBase = new byte[] {0x00};
      assertFalse(kateDec.isHeader(packet));
    }
  }

  @Nested
  @DisplayName("Granule Conversion Edge Cases")
  class GranuleConversionTests {

    @Test
    @DisplayName("Should return -1 for granule conversions when decoder is not ready")
    void testGranuleConversionsWithoutDecoder() {
      assertEquals(-1, kateDec.granuleToTime(1000L));
      assertEquals(-1, kateDec.granuleToDuration(1000L));
    }

    @Test
    @DisplayName("Should return -1 for negative granule positions")
    void testNegativeGranulePositions() {
      assertEquals(-1, kateDec.granuleToTime(-1L));
      assertEquals(-1, kateDec.granuleToDuration(-50L));
    }
  }

  @Nested
  @DisplayName("Property Accessor Tests")
  class PropertyTests {

    @Test
    @DisplayName("Should return language and category properties or fallback safely")
    void testGetProperties() {
      // Before decoder initialization, info fields may be null
      assertNull(kateDec.getProperty("language"));
      assertNull(kateDec.getProperty("category"));

      Object unknown = kateDec.getProperty("nonExistentProperty");
      assertNull(unknown);
    }
  }

  @Nested
  @DisplayName("State Transition Tests")
  class StateTransitionTests {

    @Test
    @DisplayName("Should handle changeState transitions without errors")
    void testChangeState() {
      int resultPause = kateDec.changeState(Element.STOP_PAUSE);
      assertTrue(resultPause >= 0);

      int resultStop = kateDec.changeState(Element.PAUSE_STOP);
      assertTrue(resultStop >= 0);
    }
  }
}
