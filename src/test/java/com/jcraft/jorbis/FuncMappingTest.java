package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jcraft.jogg.Buffer;
import java.lang.reflect.Field;
import org.junit.jupiter.api.*;

public class FuncMappingTest {

  private FuncMapping originalMapping;

  /** Replace MAPPING_P[0] with a mock */
  private void injectMockMapping(FuncMapping mock) throws Exception {
    Field f = FuncMapping.class.getDeclaredField("MAPPING_P");
    f.setAccessible(true);
    FuncMapping[] arr = (FuncMapping[]) f.get(null);
    originalMapping = arr[0];
    arr[0] = mock;
  }

  /** Restore original Mapping0 */
  private void restoreMapping() throws Exception {
    Field f = FuncMapping.class.getDeclaredField("MAPPING_P");
    f.setAccessible(true);
    FuncMapping[] arr = (FuncMapping[]) f.get(null);
    arr[0] = originalMapping;
  }

  @AfterEach
  void cleanup() throws Exception {
    if (originalMapping != null) restoreMapping();
  }

  @Test
  void testPackIsCalled() throws Exception {
    FuncMapping mockMap = mock(FuncMapping.class);
    injectMockMapping(mockMap);

    Info info = mock(Info.class);
    Buffer buffer = mock(Buffer.class);

    FuncMapping.MAPPING_P[0].pack(info, "imap", buffer);

    verify(mockMap).pack(info, "imap", buffer);
  }

  @Test
  void testUnpackReturnsValue() throws Exception {
    FuncMapping mockMap = mock(FuncMapping.class);
    injectMockMapping(mockMap);

    Info info = mock(Info.class);
    Buffer buffer = mock(Buffer.class);

    when(mockMap.unpack(info, buffer)).thenReturn("RESULT");

    Object out = FuncMapping.MAPPING_P[0].unpack(info, buffer);
    assertEquals("RESULT", out);
  }

  @Test
  void testLookIsCalled() throws Exception {
    FuncMapping mockMap = mock(FuncMapping.class);
    injectMockMapping(mockMap);

    DspState vd = mock(DspState.class);
    InfoMode vm = mock(InfoMode.class);

    FuncMapping.MAPPING_P[0].look(vd, vm, "m");

    verify(mockMap).look(vd, vm, "m");
  }

  @Test
  void testFreeInfoIsCalled() throws Exception {
    FuncMapping mockMap = mock(FuncMapping.class);
    injectMockMapping(mockMap);

    FuncMapping.MAPPING_P[0].freeInfo("X");

    verify(mockMap).freeInfo("X");
  }

  @Test
  void testFreeLookIsCalled() throws Exception {
    FuncMapping mockMap = mock(FuncMapping.class);
    injectMockMapping(mockMap);

    FuncMapping.MAPPING_P[0].freeLook("Y");

    verify(mockMap).freeLook("Y");
  }

  @Test
  void testInverseReturnsErrorCode() throws Exception {
    FuncMapping mockMap = mock(FuncMapping.class);
    injectMockMapping(mockMap);

    Block block = mock(Block.class);
    when(mockMap.inverse(block, "LM")).thenReturn(-77);

    int out = FuncMapping.MAPPING_P[0].inverse(block, "LM");
    assertEquals(-77, out);
  }

  @Test
  void testInverseHandlesNullBlock() throws Exception {
    FuncMapping mockMap = mock(FuncMapping.class);
    injectMockMapping(mockMap);

    when(mockMap.inverse(null, "LM")).thenReturn(-5);

    int out = FuncMapping.MAPPING_P[0].inverse(null, "LM");
    assertEquals(-5, out);
  }

  @Test
  void testUnpackHandlesNullBuffer() throws Exception {
    FuncMapping mockMap = mock(FuncMapping.class);
    injectMockMapping(mockMap);

    Info info = mock(Info.class);
    when(mockMap.unpack(info, null)).thenReturn(null);

    assertNull(FuncMapping.MAPPING_P[0].unpack(info, null));
  }

  @Test
  void testPackHandlesNullInfo() throws Exception {
    FuncMapping mockMap = mock(FuncMapping.class);
    injectMockMapping(mockMap);

    Buffer buffer = mock(Buffer.class);

    FuncMapping.MAPPING_P[0].pack(null, "imap", buffer);

    verify(mockMap).pack(null, "imap", buffer);
  }

  @Test
  void testLookHandlesNullMode() throws Exception {
    FuncMapping mockMap = mock(FuncMapping.class);
    injectMockMapping(mockMap);

    DspState vd = mock(DspState.class);

    FuncMapping.MAPPING_P[0].look(vd, null, "m");

    verify(mockMap).look(vd, null, "m");
  }

  @Test
  void testMappingTableIsStaticFinal() throws Exception {
    Field f = FuncMapping.class.getDeclaredField("MAPPING_P");
    assertTrue(java.lang.reflect.Modifier.isFinal(f.getModifiers()));
    assertTrue(java.lang.reflect.Modifier.isStatic(f.getModifiers()));
  }
}
