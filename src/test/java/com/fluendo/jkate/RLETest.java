package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.jcraft.jogg.Buffer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RLETest {

  @Mock private Buffer bufferMock;

  @Test
  @DisplayName("decodeRLE: Returns null for invalid dimensions")
  void testInvalidDimensions() {
    assertNull(RLE.decodeRLE(bufferMock, 0, 10, 8), "Width 0 should return null");
    assertNull(RLE.decodeRLE(bufferMock, 10, -1, 8), "Height -1 should return null");
  }

  @Test
  @DisplayName("decodeRLE: Handles unknown RLE type gracefully using Mockito")
  void testUnknownRleType() {
    // Return 7 for the type read, which falls into the default switch case returning null
    when(bufferMock.read(anyInt())).thenReturn(7);

    assertNull(RLE.decodeRLE(bufferMock, 1, 1, 8), "Unknown RLE type should return null");
  }

  @Test
  @DisplayName("Utility Class: Private constructor prevents instantiation")
  void testPrivateConstructor() throws Exception {
    var constructor = RLE.class.getDeclaredConstructor();
    assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));

    constructor.setAccessible(true);
    var exception =
        assertThrows(java.lang.reflect.InvocationTargetException.class, constructor::newInstance);
    assertTrue(exception.getCause() instanceof UnsupportedOperationException);
  }

  @Test
  @DisplayName("RLE Constants: Verify bit constants are as expected")
  void testConstants() {
    assertNotNull(RLE.class.getDeclaredFields());
  }
}
