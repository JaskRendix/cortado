package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;

import com.jcraft.jogg.Buffer;
import com.jcraft.jogg.Packet;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

class InfoTest {

  private Info info;

  @BeforeEach
  void setup() {
    info = new Info();
  }

  private Packet makePacket(Buffer b) {
    Packet p = new Packet();
    byte[] raw = b.buffer(); // correct accessor
    p.packetBase = raw;
    p.packet = 0;
    p.bytes = raw.length;
    p.bos = 1;
    return p;
  }

  @Test
  void initResetsRateToZero() {
    info.setRate(44100);
    info.init();
    assertEquals(0, info.getRate());
  }

  @Test
  void setBlocksizesStoresValues() {
    info.setBlocksizes(256, 1024);
    assertArrayEquals(new int[] {256, 1024}, info.getBlocksizes());
  }

  @Test
  void settersStoreValuesCorrectly() {
    info.setChannels(2);
    info.setRate(48000);
    info.setModes(3);
    info.setBooks(5);

    assertEquals(2, info.getChannels());
    assertEquals(48000, info.getRate());
    assertEquals(3, info.getModes());
    assertEquals(5, info.getBooks());
  }

  @Test
  void unpackInfoFailsWhenVersionNonZero() {
    Buffer b = new Buffer();
    b.writeInit();
    b.write(123, 32);
    b.write(2, 8);
    b.write(44100, 32);
    b.write(0, 32);
    b.write(0, 32);
    b.write(0, 32);
    b.write(3, 4);
    b.write(4, 4);
    b.write(1, 1);

    assertEquals(-1, info.unpackInfo(b));
  }

  @Test
  void unpackInfoFailsWhenChannelsZero() {
    Buffer b = new Buffer();
    b.writeInit();
    b.write(0, 32);
    b.write(0, 8);
    b.write(44100, 32);
    b.write(0, 32);
    b.write(0, 32);
    b.write(0, 32);
    b.write(3, 4);
    b.write(4, 4);
    b.write(1, 1);

    assertEquals(-1, info.unpackInfo(b));
  }

  @Test
  void unpackInfoFailsWhenRateZero() {
    Buffer b = new Buffer();
    b.writeInit();
    b.write(0, 32);
    b.write(2, 8);
    b.write(0, 32);
    b.write(0, 32);
    b.write(0, 32);
    b.write(0, 32);
    b.write(3, 4);
    b.write(4, 4);
    b.write(1, 1);

    assertEquals(-1, info.unpackInfo(b));
  }

  @Test
  void unpackInfoFailsWhenBlocksizeTooSmall() {
    Buffer b = new Buffer();
    b.writeInit();
    b.write(0, 32);
    b.write(2, 8);
    b.write(44100, 32);
    b.write(0, 32);
    b.write(0, 32);
    b.write(0, 32);
    b.write(2, 4);
    b.write(4, 4);
    b.write(1, 1);

    assertEquals(-1, info.unpackInfo(b));
  }

  @Test
  void unpackInfoFailsWhenSecondBlocksizeSmallerThanFirst() {
    Buffer b = new Buffer();
    b.writeInit();
    b.write(0, 32);
    b.write(2, 8);
    b.write(44100, 32);
    b.write(0, 32);
    b.write(0, 32);
    b.write(0, 32);
    b.write(4, 4);
    b.write(2, 4);
    b.write(1, 1);

    assertEquals(-1, info.unpackInfo(b));
  }

  @Test
  void unpackInfoFailsWhenFramingBitNotOne() {
    Buffer b = new Buffer();
    b.writeInit();
    b.write(0, 32);
    b.write(2, 8);
    b.write(44100, 32);
    b.write(0, 32);
    b.write(0, 32);
    b.write(0, 32);
    b.write(4, 4);
    b.write(4, 4);
    b.write(0, 1);

    assertEquals(-1, info.unpackInfo(b));
  }

  @Test
  void synthesisHeaderInRejectsNonVorbisSignature() {
    Buffer b = new Buffer();
    b.writeInit();
    b.write(0x01, 8);
    b.write("xxxxx".getBytes());

    Packet p = makePacket(b);
    Comment vc = new Comment();

    assertEquals(-1, info.synthesisHeaderIn(vc, p));
  }

  @Test
  void synthesisHeaderInRejectsCodebookHeaderWhenVendorMissing() {
    Buffer b = new Buffer();
    b.writeInit();
    b.write(0x05, 8);
    b.write("vorbis".getBytes());

    Packet p = makePacket(b);
    Comment vc = new Comment();

    info.setRate(44100);

    assertEquals(-1, info.synthesisHeaderIn(vc, p));
  }

  @Test
  void blocksizeFailsWhenFirstBitIsOne() {
    Buffer b = new Buffer();
    b.writeInit();
    b.write(1, 1);

    Packet p = makePacket(b);

    assertEquals(-135, info.blocksize(p));
  }

  @Test
  void blocksizeFailsWhenModeIsInvalid() {
    info.setModes(4);
    info.setBlocksizes(256, 1024);

    InfoMode m0 = new InfoMode();
    m0.setBlockflag(0);
    InfoMode m1 = new InfoMode();
    m1.setBlockflag(1);
    InfoMode m2 = new InfoMode();
    m2.setBlockflag(0);
    InfoMode m3 = new InfoMode();
    m3.setBlockflag(1);

    info.setModeParam(new InfoMode[] {m0, m1, m2, m3});

    Buffer b = new Buffer();
    b.writeInit();
    b.write(0, 1); // audio packet
    b.write(15, 4); // lower 2 bits = 3 → valid mode

    Packet p = makePacket(b);

    // Mode 3 → blockflag = 1 → blocksize = 1024
    assertEquals(1024, info.blocksize(p));
  }

  @Test
  void blocksizeReturnsCorrectBlocksize() {
    info.setModes(2);
    info.setBlocksizes(256, 1024);

    InfoMode m0 = new InfoMode();
    m0.setBlockflag(0);
    InfoMode m1 = new InfoMode();
    m1.setBlockflag(1);

    info.setModeParam(new InfoMode[] {m0, m1});

    Buffer b = new Buffer();
    b.writeInit();
    b.write(0, 1);
    b.write(1, 1);

    Packet p = makePacket(b);

    assertEquals(1024, info.blocksize(p));
  }

  @Test
  void clearNullsModeParam() {
    info.setModes(2);
    info.setModeParam(new InfoMode[] {new InfoMode(), new InfoMode()});
    info.clear();
    assertNull(info.getModeParam());
  }

  @Test
  void clearNullsBookParamAndCallsClear() {
    StaticCodeBook book = Mockito.mock(StaticCodeBook.class);
    info.setBooks(1);
    info.setBookParam(new StaticCodeBook[] {book});

    info.clear();

    Mockito.verify(book).clear();
    assertNull(info.getBookParam());
  }
}
