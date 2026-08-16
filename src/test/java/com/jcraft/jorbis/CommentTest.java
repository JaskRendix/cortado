package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;

import com.jcraft.jogg.Buffer;
import com.jcraft.jogg.Packet;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CommentTest {

  @Test
  void initShouldResetFields() {
    Comment c = new Comment();
    c.add("test");
    c.vendor = "vendor".getBytes(StandardCharsets.UTF_8);

    c.init();

    assertNull(c.user_comments);
    assertNull(c.vendor);
    assertEquals(0, c.comments);
  }

  @Test
  void addShouldStoreCommentAndLength() {
    Comment c = new Comment();
    c.init();

    c.add("hello");

    assertEquals(1, c.comments);
    assertNotNull(c.user_comments[0]);
    assertEquals(5, c.comment_lengths[0]);
    assertEquals("hello", new String(c.user_comments[0], 0, 5, StandardCharsets.UTF_8));
  }

  @Test
  void addTagShouldStoreTagAndValue() {
    Comment c = new Comment();
    c.init();

    c.add_tag("ARTIST", "Bach");

    assertEquals(1, c.comments);
    assertEquals("ARTIST=Bach", new String(c.user_comments[0], 0, 11, StandardCharsets.UTF_8));
  }

  @Test
  void tagcompareShouldBeCaseInsensitive() {
    byte[] a = "ARTIST=".getBytes(StandardCharsets.UTF_8);
    byte[] b = "artist=".getBytes(StandardCharsets.UTF_8);

    assertTrue(Comment.tagcompare(a, b, 6));
  }

  @Test
  void queryShouldReturnValueForTag() {
    Comment c = new Comment();
    c.init();

    c.add_tag("TITLE", "Symphony");

    assertEquals("Symphony", c.query("TITLE"));
  }

  @Test
  void queryShouldReturnNullForMissingTag() {
    Comment c = new Comment();
    c.init();

    c.add_tag("TITLE", "Symphony");

    assertNull(c.query("ARTIST"));
  }

  @Test
  void queryShouldReturnNthOccurrence() {
    Comment c = new Comment();
    c.init();

    c.add_tag("GENRE", "Classical");
    c.add_tag("GENRE", "Baroque");

    assertEquals("Baroque", c.query("GENRE", 1));
  }

  @Test
  void unpackShouldFailOnNegativeVendorLength() {
    Buffer b = new Buffer();
    b.readinit(new byte[] {(byte) 0xFF}, 0, 1); // read(32) → -1

    Comment c = new Comment();
    assertEquals(-1, c.unpack(b));
    assertNull(c.user_comments);
    assertNull(c.vendor);
  }

  @Test
  void unpackShouldFailIfFinalBitIsNotOne() {
    Buffer b = new Buffer();
    b.writeinit();

    // vendor length = 3
    b.write(3, 32);
    b.write("abc".getBytes(StandardCharsets.UTF_8));

    // comments = 1
    b.write(1, 32);

    // comment length = 4
    b.write(4, 32);
    b.write("test".getBytes(StandardCharsets.UTF_8));

    // final bit = 0 (invalid)
    b.write(0, 1);

    byte[] data = b.buffer();
    Buffer read = new Buffer();
    read.readinit(data, 0, data.length);

    Comment c = new Comment();
    assertEquals(-1, c.unpack(read));
  }

  @Test
  void packShouldWriteValidVorbisCommentHeader() {
    Comment c = new Comment();
    c.init();
    c.add_tag("TITLE", "Symphony");

    Buffer b = new Buffer();
    b.writeinit();

    assertEquals(0, c.pack(b));

    byte[] out = b.buffer();
    assertTrue(out.length > 0);

    // First byte must be 0x03 (Vorbis comment header)
    assertEquals(0x03, out[0] & 0xFF);
  }

  @Test
  void headerOutShouldPopulatePacketFields() {
    Comment c = new Comment();
    c.init();
    c.add_tag("TITLE", "Symphony");

    Packet p = new Packet();
    assertEquals(0, c.header_out(p));

    assertNotNull(p.packet_base);
    assertTrue(p.bytes > 0);
    assertEquals(0, p.b_o_s);
    assertEquals(0, p.e_o_s);
    assertEquals(0, p.granulepos);
  }

  @Test
  void getVendorShouldReturnVendorString() {
    Comment c = new Comment();
    c.init();

    c.vendor = "VendorX\0".getBytes(StandardCharsets.UTF_8);

    assertEquals("VendorX", c.getVendor());
  }

  @Test
  void getCommentShouldReturnCommentString() {
    Comment c = new Comment();
    c.init();

    c.add("hello");

    assertEquals("hello", c.getComment(0));
  }

  @Test
  void getCommentShouldReturnNullForOutOfRange() {
    Comment c = new Comment();
    c.init();

    c.add("hello");

    assertNull(c.getComment(5));
  }

  @Test
  void clearShouldNullOutCommentsAndVendor() {
    Comment c = new Comment();
    c.init();

    c.add("hello");
    c.vendor = "VendorX".getBytes(StandardCharsets.UTF_8);

    c.clear();

    assertNull(c.user_comments);
    assertNull(c.vendor);
  }

  @Test
  void toStringShouldContainVendorAndComments() {
    Comment c = new Comment();
    c.init();

    c.vendor = "VendorX\0".getBytes(StandardCharsets.UTF_8);
    c.add("hello");

    String s = c.toString();

    assertTrue(s.contains("VendorX"));
    assertTrue(s.contains("hello"));
  }
}
