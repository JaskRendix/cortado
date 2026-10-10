package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.fluendo.jst.Buffer;
import com.jcraft.jogg.Packet;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OggPayloadTest {

  private OggPayload payload;

  private static final class DummyPayload implements OggPayload {

    @Override
    public boolean isType(Packet op) {
      return false;
    }

    @Override
    public int takeHeader(Packet op) {
      return 0;
    }

    @Override
    public boolean isHeader(Packet op) {
      return false;
    }

    @Override
    public boolean isKeyFrame(Packet op) {
      return false;
    }

    @Override
    public long getFirstTs(List<Buffer> packets) {
      return 0L;
    }

    @Override
    public long granuleToTime(long gp) {
      return gp;
    }

    @Override
    public String getMime() {
      return "application/test";
    }

    @Override
    public String getMime(Packet op) {
      return getMime();
    }

    @Override
    public boolean isDiscontinuous() {
      return false;
    }
  }

  @BeforeEach
  void setUp() {
    payload = new DummyPayload();
  }

  @Test
  void testMimeConsistency() {
    assertNotNull(payload.getMime());
    assertFalse(payload.getMime().isEmpty());

    assertEquals(
        payload.getMime(),
        payload.getMime(new Packet()));
  }

  @Test
  void testGranuleToTimeIsDeterministic() {
    assertEquals(
        payload.granuleToTime(123),
        payload.granuleToTime(123));
  }

  @Test
  void testGetFirstTsHandlesEmptyList() {
    assertDoesNotThrow(
        () -> payload.getFirstTs(Collections.emptyList()));
  }

  @Test
  void testHeaderHandlingContract() {
    assertDoesNotThrow(
        () -> payload.takeHeader(new Packet()));

    assertDoesNotThrow(
        () -> payload.isHeader(new Packet()));
  }

  @Test
  void testKeyframeDetectionContract() {
    assertDoesNotThrow(
        () -> payload.isKeyFrame(new Packet()));
  }

  @Test
  void testDiscontinuityQueryContract() {
    assertDoesNotThrow(
        payload::isDiscontinuous);
  }
}
