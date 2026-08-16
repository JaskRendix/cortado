package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import com.jcraft.jogg.Buffer;
import java.lang.reflect.Field;
import org.junit.jupiter.api.*;

public class Mapping0Test {

  private Mapping0 mapping;

  @BeforeEach
  void setup() {
    mapping = new Mapping0();
  }

  /** Reflection: get private byte[] buffer */
  private byte[] getBufferBytes(Buffer b) throws Exception {
    Field f = Buffer.class.getDeclaredField("buffer");
    f.setAccessible(true);
    return (byte[]) f.get(b);
  }

  /** Reflection: get private endbyte (actual bytes written) */
  private int getEndByte(Buffer b) throws Exception {
    Field f = Buffer.class.getDeclaredField("endbyte");
    f.setAccessible(true);
    return (int) f.get(b);
  }

  /** Flush bit accumulator manually */
  private void flushBits(Buffer b) throws Exception {
    Field fEndBit = Buffer.class.getDeclaredField("endbit");
    Field fEndByte = Buffer.class.getDeclaredField("endbyte");
    Field fBuffer = Buffer.class.getDeclaredField("buffer");

    fEndBit.setAccessible(true);
    fEndByte.setAccessible(true);
    fBuffer.setAccessible(true);

    int endbit = (int) fEndBit.get(b);
    int endbyte = (int) fEndByte.get(b);

    if (endbit > 0) {
      endbyte++;
      fEndByte.set(b, endbyte);
    }
  }

  /** Replace static final arrays (FuncTime, FuncFloor, FuncResidue) */
  private void injectStaticArray(Class<?> clazz, String fieldName, Object[] newArray)
      throws Exception {
    Field f = clazz.getDeclaredField(fieldName);
    f.setAccessible(true);
    Object[] arr = (Object[]) f.get(null);
    System.arraycopy(newArray, 0, arr, 0, newArray.length);
  }

  @Test
  void testLookBuildsCorrectStructure() throws Exception {
    DspState vd = new DspState();
    Info vi = new Info();
    vd.vi = vi;

    vi.setChannels(2);
    vi.setTimes(1);
    vi.setFloors(1);
    vi.setResidues(1);

    vi.setTimeType(new int[] {0});
    vi.setFloorType(new int[] {0});
    vi.setResidueType(new int[] {0});

    vi.setTimeParam(new Object[] {new Object()});
    vi.setFloorParam(new Object[] {new Object()});
    vi.setResidueParam(new Object[] {new Object()});

    InfoMode mode = new InfoMode();
    mode.setWindowtype(0);

    InfoMapping0 info = new InfoMapping0();
    info.submaps = 1;
    info.timesubmap[0] = 0;
    info.floorsubmap[0] = 0;
    info.residuesubmap[0] = 0;
    info.chmuxlist[0] = 0;
    info.chmuxlist[1] = 0;

    FuncTime mockTime = mock(FuncTime.class);
    FuncFloor mockFloor = mock(FuncFloor.class);
    FuncResidue mockResidue = mock(FuncResidue.class);

    injectStaticArray(FuncTime.class, "TIME_P", new FuncTime[] {mockTime});
    injectStaticArray(FuncFloor.class, "FLOOR_P", new FuncFloor[] {mockFloor});
    injectStaticArray(FuncResidue.class, "RESIDUE_P", new FuncResidue[] {mockResidue});

    when(mockTime.look(any(), any(), any())).thenReturn("TIME_LOOK");
    when(mockFloor.look(any(), any(), any())).thenReturn("FLOOR_LOOK");
    when(mockResidue.look(any(), any(), any())).thenReturn("RES_LOOK");

    LookMapping0 look = (LookMapping0) mapping.look(vd, mode, info);

    assertEquals("TIME_LOOK", look.time_look[0]);
    assertEquals("FLOOR_LOOK", look.floor_look[0]);
    assertEquals("RES_LOOK", look.residue_look[0]);
    assertEquals(2, look.ch);
  }

  @Test
  void testPackSingleSubmap() throws Exception {
    Info vi = new Info();
    vi.setChannels(2);

    InfoMapping0 info = new InfoMapping0();
    info.submaps = 1;
    info.couplingSteps = 0;
    info.timesubmap[0] = 3;
    info.floorsubmap[0] = 4;
    info.residuesubmap[0] = 5;

    Buffer buffer = new Buffer();
    buffer.writeInit();

    mapping.pack(vi, info, buffer);
    flushBits(buffer);

    byte[] raw = getBufferBytes(buffer);
    int endbyte = getEndByte(buffer);

    Buffer reader = new Buffer();
    reader.readInit(raw, 0, endbyte);

    assertEquals(0, reader.read(1)); // no submaps flag
    assertEquals(0, reader.read(1)); // no coupling
    assertEquals(0, reader.read(2)); // reserved
    assertEquals(3, reader.read(8));
    assertEquals(4, reader.read(8));
    assertEquals(5, reader.read(8));
  }

  @Test
  void testUnpackRejectsReservedBits() throws Exception {
    Info vi = new Info();
    vi.setChannels(2);
    vi.setTimes(1);
    vi.setFloors(1);
    vi.setResidues(1);

    Buffer buffer = new Buffer();
    buffer.writeInit();

    buffer.write(0, 1); // submaps=1
    buffer.write(0, 1); // no coupling
    buffer.write(3, 2); // reserved bits nonzero
    flushBits(buffer);

    byte[] raw = getBufferBytes(buffer);
    int endbyte = getEndByte(buffer);

    Buffer reader = new Buffer();
    reader.readInit(raw, 0, endbyte);

    assertNull(mapping.unpack(vi, reader));
  }

  @Test
  void testInverseRunsWithMocks() throws Exception {
    DspState vd = new DspState();
    Info vi = new Info();
    vd.vi = vi;

    vi.setChannels(2);
    vi.setBlocksizes(64, 128);

    vd.window = new float[2][2][2][1][];
    vd.window[1][0][0][0] = new float[128];

    vd.transform = new Object[2][1];
    Mdct mockMdct = mock(Mdct.class);
    vd.transform[1][0] = mockMdct;

    Block vb = new Block(vd);
    vb.W = 1;
    vb.lW = 0;
    vb.nW = 0;
    vb.pcm = new float[2][128];

    LookMapping0 look = new LookMapping0();
    InfoMapping0 info = new InfoMapping0();
    info.submaps = 1;
    info.chmuxlist[0] = 0;
    info.chmuxlist[1] = 0;

    look.map = info;
    look.mode = new InfoMode();
    look.mode.setWindowtype(0);

    FuncFloor mockFloor = mock(FuncFloor.class);
    FuncResidue mockResidue = mock(FuncResidue.class);
    FuncTime mockTime = mock(FuncTime.class);

    injectStaticArray(FuncFloor.class, "FLOOR_P", new FuncFloor[] {mockFloor});
    injectStaticArray(FuncResidue.class, "RESIDUE_P", new FuncResidue[] {mockResidue});
    injectStaticArray(FuncTime.class, "TIME_P", new FuncTime[] {mockTime});

    look.floor_func = new FuncFloor[] {mockFloor};
    look.floor_look = new Object[] {"FLOOR_LOOK"};
    look.residue_func = new FuncResidue[] {mockResidue};
    look.residue_look = new Object[] {"RES_LOOK"};

    when(mockFloor.inverse1(any(), any(), any())).thenReturn(new Object());
    when(mockFloor.inverse2(any(), any(), any(), any())).thenReturn(0);
    when(mockResidue.inverse(any(), any(), any(), any(), anyInt())).thenReturn(0);

    assertEquals(0, mapping.inverse(vb, look));
  }
}
