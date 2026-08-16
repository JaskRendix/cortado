package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import com.jcraft.jogg.Packet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StateTest {

  private State state;

  @BeforeEach
  void setUp() {
    state = new State();
  }

  @Test
  @DisplayName("Default Constructor: Initializes state fields securely")
  void testDefaultConstructor() {
    assertEquals(-1, state.granulepos);
    assertNull(state.decodeEventOut());
  }

  @Test
  @DisplayName("decodeInit: Fails gracefully on null Info")
  void testDecodeInitNullInfo() {
    int result = state.decodeInit(null);
    assertEquals(Result.KATE_E_INVALID_PARAMETER, result);
  }

  @Test
  @DisplayName("decodePacketin: Fails gracefully if uninitialized")
  void testDecodePacketinUninitialized() {
    Packet packet = new Packet();
    int result = state.decodePacketin(packet);
    assertEquals(Result.KATE_E_INVALID_PARAMETER, result);
  }

  @Test
  @DisplayName("Clear: Resets state parameters completely")
  void testClearState() {
    state.granulepos = 100L;
    state.clear();
    assertEquals(-1, state.granulepos);
    assertNull(state.decodeEventOut());
  }

  @Test
  @DisplayName("Granule Time/Duration: Safe defaults when decoder is null")
  void testGranuleWithoutDecoder() {
    assertEquals(0.0, state.granuleTime(100L));
    assertEquals(0.0, state.granuleDuration(100L));
  }
}
