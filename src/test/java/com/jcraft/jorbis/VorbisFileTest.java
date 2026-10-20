package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class VorbisFileTest {

  private VorbisFile vorbis;

  @BeforeEach
  void setup() throws Exception {
    try {
      vorbis = new VorbisFile(
          new ByteArrayInputStream(new byte[0]),
          null,
          0);
    } catch (Exception ignored) {
    }
  }

  @Test
  void seekableReturnsFalseForByteArrayInputStream() {
    assertFalse(vorbis.seekable());
  }

  @Test
  void streamsReturnsConfiguredLinks() {
    vorbis.links = 3;
    assertEquals(3, vorbis.streams());
  }

  @Test
  void serialnumberReturnsMinusOneForInvalidLink() {
    vorbis.links = 1;
    assertEquals(-1, vorbis.serialnumber(5));
  }

  @Test
  void rawTotalReturnsMinusOneWhenNotSeekable() {
    vorbis.seekable = false;
    assertEquals(-1, vorbis.rawTotal(0));
  }

  @Test
  void pcmTotalReturnsMinusOneWhenNotSeekable() {
    vorbis.seekable = false;
    assertEquals(-1, vorbis.pcmTotal(0));
  }

  @Test
  void timeTotalReturnsMinusOneWhenNotSeekable() {
    vorbis.seekable = false;
    assertEquals(-1f, vorbis.timeTotal(0));
  }

  @Test
  void bitrateReturnsMinusOneWhenLinkOutOfRange() {
    vorbis.links = 1;
    assertEquals(-1, vorbis.bitrate(5));
  }

  @Test
  void bitrateInstantReturnsMinusOneWhenNoSamplesProcessed() {
    vorbis.vi = new Info[]{new Info()};
    vorbis.samptrack = 0;
    assertEquals(-1, vorbis.bitrateInstant());
  }

  @Test
  void rawTellReturnsCurrentOffset() {
    vorbis.offset = 12345;
    assertEquals(12345, vorbis.rawTell());
  }

  @Test
  void pcmTellReturnsCurrentPcmOffset() {
    vorbis.pcmOffset = 9999L;
    assertEquals(9999L, vorbis.pcmTell());
  }

  @Test
  void getInfoReturnsNullWhenDecoderNotReady() {
    vorbis.seekable = false;
    vorbis.decodeReady = false;
    assertNull(vorbis.getInfo(0));
  }

  @Test
  void getCommentReturnsNullWhenDecoderNotReady() {
    vorbis.seekable = false;
    vorbis.decodeReady = false;
    assertNull(vorbis.getComment(0));
  }

  @Test
  void getInfoReturnsConfiguredInfo() {
    Info i = new Info();

    vorbis.seekable = false;
    vorbis.decodeReady = true;
    vorbis.vi = new Info[]{i};

    assertSame(i, vorbis.getInfo(0));
  }

  @Test
  void getCommentReturnsConfiguredComment() {
    Comment c = new Comment();

    vorbis.seekable = false;
    vorbis.decodeReady = true;
    vorbis.vc = new Comment[]{c};

    assertSame(c, vorbis.getComment(0));
  }

  @Test
  void clearRemovesArrays() {

    Info info = Mockito.mock(Info.class);
    Comment comment = Mockito.mock(Comment.class);

    vorbis.links = 1;
    vorbis.vi = new Info[]{info};
    vorbis.vc = new Comment[]{comment};

    vorbis.offsets = new long[]{1,2};
    vorbis.serialnos = new int[]{1};
    vorbis.pcmlengths = new long[]{1};
    vorbis.dataoffsets = new long[]{1};

    assertEquals(0, vorbis.clear());

    Mockito.verify(info).clear();
    Mockito.verify(comment).clear();

    assertNull(vorbis.vi);
    assertNull(vorbis.vc);
    assertNull(vorbis.offsets);
    assertNull(vorbis.serialnos);
    assertNull(vorbis.pcmlengths);
    assertNull(vorbis.dataoffsets);
  }

  @Test
  void decodeClearResetsFlagsAndCounters() {

    vorbis.decodeReady = true;
    vorbis.bittrack = 99;
    vorbis.samptrack = 44;

    vorbis.decodeClear();

    assertFalse(vorbis.decodeReady);
    assertEquals(0f, vorbis.bittrack);
    assertEquals(0f, vorbis.samptrack);
  }

  @Test
  void hostIsBigEndianAlwaysReturnsOne() {
    assertEquals(1, vorbis.hostIsBigEndian());
  }

  @Test
  void rawSeekFailsWhenNotSeekable() {
    vorbis.seekable = false;
    assertEquals(-1, vorbis.rawSeek(10));
  }

  @Test
  void pcmSeekFailsWhenNotSeekable() {
    vorbis.seekable = false;
    assertEquals(-1, vorbis.pcmSeek(10));
  }

  @Test
  void timeSeekFailsWhenNotSeekable() {
    vorbis.seekable = false;
    assertEquals(-1, vorbis.timeSeek(5f));
  }

  @Test
  void serialnumberReturnsCurrentSerialForNegativeIndex() {

    vorbis.seekable = true;
    vorbis.currentSerialno = 777;

    assertEquals(777, vorbis.serialnumber(-1));
  }

  @Test
  void getInfoArrayReturnsUnderlyingArray() {

    Info[] infos = {
        new Info(),
        new Info()
    };

    vorbis.vi = infos;

    assertSame(infos, vorbis.getInfo());
  }

  @Test
  void getCommentArrayReturnsUnderlyingArray() {

    Comment[] comments = {
        new Comment(),
        new Comment()
    };

    vorbis.vc = comments;

    assertSame(comments, vorbis.getComment());
  }

  @Test
  void fseekWithRegularInputStreamUsesResetAndSkip() {

    InputStream is = new ByteArrayInputStream(new byte[100]);

    assertEquals(
        0,
        VorbisFile.fseek(is, 20, 0)
    );
  }

  @Test
  void ftellWithNormalInputStreamReturnsZero() {

    InputStream is = new ByteArrayInputStream(new byte[10]);

    assertEquals(
        0,
        VorbisFile.ftell(is)
    );
  }

  private VorbisFile openAudio() throws Exception {
      InputStream is =
          getClass().getResourceAsStream("/media/test-audio.ogg");

      assertNotNull(is);

      return new VorbisFile(is, null, 0);
  }

  @Test
  void streamConstructorOpensAudioFile() throws Exception {
      VorbisFile vf = openAudio();

      assertNotNull(vf);
  }

  @Test
  void streamIsNotSeekable() throws Exception {
      VorbisFile vf = openAudio();

      assertFalse(vf.seekable());
  }

  @Test
  void getInfoReturnsNullForInvalidLink() {
      vorbis.seekable = true;
      vorbis.links = 1;
      vorbis.vi = new Info[]{new Info()};

      assertNull(vorbis.getInfo(5));
  }

  @Test
  void getCommentReturnsNullForInvalidLink() {
      vorbis.seekable = true;
      vorbis.links = 1;
      vorbis.vc = new Comment[]{new Comment()};

      assertNull(vorbis.getComment(5));
  }

  @Test
  void serialnumberReturnsConfiguredSerial() {
      vorbis.seekable = true;
      vorbis.links = 1;
      vorbis.serialnos = new int[]{1234};

      assertEquals(1234, vorbis.serialnumber(0));
  }

  @Test
  void bitrateInstantResetsCounters() {
      Info info = new Info();
      info.setRate(44100);

      vorbis.vi = new Info[]{info};
      vorbis.samptrack = 100;
      vorbis.bittrack = 8000;

      int value = vorbis.bitrateInstant();

      assertTrue(value > 0);
      assertEquals(0f, vorbis.samptrack);
      assertEquals(0f, vorbis.bittrack);
  }
}
