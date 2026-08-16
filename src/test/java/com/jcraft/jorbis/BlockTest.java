package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jcraft.jogg.Buffer;
import com.jcraft.jogg.Packet;
import java.lang.reflect.Field;
import org.junit.jupiter.api.*;

public class BlockTest {

  private DspState vd;
  private Info info;

  @BeforeEach
  void setup() {
    vd = mock(DspState.class);
    info = mock(Info.class);

    vd.vi = info;
    vd.analysisp = 0;
    vd.modebits = 3;
    vd.mode = new Object[4];

    when(info.getModeParam()).thenReturn(new InfoMode[] {new InfoMode(), new InfoMode()});
    when(info.getBlocksizes()).thenReturn(new int[] {64, 128});
    when(info.getMapType()).thenReturn(new int[] {0});
  }

  private Packet makePacket(byte[] data, long granule, long packetno, int eos) {
    Packet p = new Packet();
    p.packet_base = data;
    p.packet = 0;
    p.bytes = data.length;
    p.granulepos = granule;
    p.packetno = packetno;
    p.e_o_s = eos;
    return p;
  }

  private void injectMockBuffer(Block block, Buffer mockBuffer) throws Exception {
    Field f = Block.class.getDeclaredField("opb");
    f.setAccessible(true);
    f.set(block, mockBuffer);
  }

  private void setMapping(int index, FuncMapping mapping) throws Exception {
    Field f = FuncMapping.class.getDeclaredField("MAPPING_P");
    f.setAccessible(true);
    FuncMapping[] arr = (FuncMapping[]) f.get(null);
    arr[index] = mapping;
  }

  @Test
  void testRejectsNonAudioPacket() throws Exception {
    Packet p = makePacket(new byte[] {0}, 0, 3, 0);

    Block block = new Block(vd);
    Buffer mockBuffer = mock(Buffer.class);
    injectMockBuffer(block, mockBuffer);

    doReturn(1).when(mockBuffer).read(1); // non-audio packet

    assertEquals(-1, block.synthesis(p));
  }

  @Test
  void testModeOutOfRange() throws Exception {
    Packet p = makePacket(new byte[] {0}, 0, 3, 0);

    Block block = new Block(vd);
    Buffer mockBuffer = mock(Buffer.class);
    injectMockBuffer(block, mockBuffer);

    doReturn(0).when(mockBuffer).read(1); // audio
    doReturn(-1).when(mockBuffer).read(vd.modebits); // modebits fail

    assertEquals(-1, block.synthesis(p));
  }

  @Test
  void testWindowBitsMissing() throws Exception {
    Packet p = makePacket(new byte[] {0}, 0, 3, 0);

    info.setModeParam(new InfoMode[] {new InfoMode(), new InfoMode()});
    info.getModeParam()[1].setBlockflag(1);

    Block block = new Block(vd);
    Buffer mockBuffer = mock(Buffer.class);
    injectMockBuffer(block, mockBuffer);

    doReturn(0).when(mockBuffer).read(1); // audio
    doReturn(1).when(mockBuffer).read(vd.modebits); // mode=1
    doReturn(-1).when(mockBuffer).read(1); // nW missing

    assertEquals(-1, block.synthesis(p));
  }

  @Test
  void testPCMAllocation() throws Exception {
    Packet p = makePacket(new byte[] {0}, 0, 3, 0);

    info.setChannels(2);
    info.setBlocksizes(64, 128);
    info.setModeParam(new InfoMode[] {new InfoMode()});
    info.getModeParam()[0].setBlockflag(1); // long window
    info.setMapType(new int[] {0});
    vd.mode = new Object[] {new Object()};

    when(info.getChannels()).thenReturn(2);

    Block block = new Block(vd);
    Buffer mockBuffer = mock(Buffer.class);
    injectMockBuffer(block, mockBuffer);

    doReturn(0).when(mockBuffer).read(1); // audio
    doReturn(0).when(mockBuffer).read(vd.modebits); // mode=0

    FuncMapping mockMap = mock(FuncMapping.class);
    when(mockMap.inverse(any(), any())).thenReturn(0);

    setMapping(0, mockMap);

    assertEquals(0, block.synthesis(p));
    assertEquals(128, block.pcmend);
    assertEquals(2, block.pcm.length);
    assertEquals(128, block.pcm[0].length);
    assertEquals(128, block.pcm[1].length);
  }

  @Test
  void testMappingInverseErrorPropagates() throws Exception {
    Packet p = makePacket(new byte[] {0}, 0, 3, 0);

    info.setChannels(1);
    info.setBlocksizes(64, 128);
    info.setModeParam(new InfoMode[] {new InfoMode()});
    info.getModeParam()[0].setBlockflag(1);
    info.setMapType(new int[] {0});
    vd.mode = new Object[] {new Object()};

    Block block = new Block(vd);
    Buffer mockBuffer = mock(Buffer.class);
    injectMockBuffer(block, mockBuffer);

    doReturn(0).when(mockBuffer).read(1); // audio
    doReturn(0).when(mockBuffer).read(vd.modebits); // mode=0

    FuncMapping mockMap = mock(FuncMapping.class);
    when(mockMap.inverse(any(), any())).thenReturn(-99);

    setMapping(0, mockMap);

    assertEquals(-99, block.synthesis(p));
  }
}
