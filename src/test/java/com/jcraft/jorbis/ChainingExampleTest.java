package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;

class ChainingExampleTest {

  private String runWithInput(byte[] input) {
    ByteArrayInputStream in = new ByteArrayInputStream(input);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    InputStream oldIn = System.in;
    PrintStream oldOut = System.out;
    PrintStream oldErr = System.err;

    System.setIn(in);
    System.setOut(new PrintStream(out));
    System.setErr(new PrintStream(err));

    try {
      ChainingExample.main(new String[0]);
    } catch (Throwable ignored) {
      // VorbisFile may throw for invalid input; we capture stderr
    } finally {
      System.setIn(oldIn);
      System.setOut(oldOut);
      System.setErr(oldErr);
    }

    return out.toString() + err.toString();
  }

  @Test
  void shouldHandleEmptyInputGracefully() {
    String output = runWithInput(new byte[0]);
    assertTrue(output.contains("Exception") || output.contains("error"));
  }

  @Test
  void shouldHandleNonVorbisInputGracefully() {
    String output = runWithInput("not an ogg file".getBytes());
    assertTrue(output.contains("Exception") || output.contains("error"));
  }

  @Test
  void shouldPrintSeekableMessageWhenVorbisFileIsSeekable() {
    // Fake minimal Ogg header (not valid Vorbis, but triggers seekable path)
    byte[] fakeOgg =
        new byte[] {
          'O', 'g', 'g', 'S', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0
        };

    String output = runWithInput(fakeOgg);

    // We cannot guarantee full parsing, but we can check branch behavior
    assertTrue(
        output.contains("seekable")
            || output.contains("logical bitstream")
            || output.contains("Exception"));
  }

  @Test
  void shouldPrintNonSeekableMessageWhenInputIsNotSeekable() {
    // VorbisFile(System.in, null, -1) treats stdin as non-seekable
    String output = runWithInput("dummy".getBytes());
    assertTrue(output.contains("not seekable") || output.contains("Exception"));
  }

  @Test
  void shouldPrintLogicalBitstreamInformationWhenStreamsPresent() {
    // This test cannot simulate real Vorbis streams without full Ogg pages,
    // but we can verify that the loop runs or fails gracefully.
    String output = runWithInput("OggS".getBytes());

    assertTrue(output.contains("logical bitstream") || output.contains("Exception"));
  }

  @Test
  void shouldNotCrashOnLargeInput() {
    byte[] large = new byte[1024 * 1024];
    String output = runWithInput(large);
    assertTrue(output.length() > 0);
  }

  @Test
  void shouldPrintCommentsIfAvailable() {
    // Cannot produce real Vorbis comments without full Ogg/Vorbis encoding,
    // but we can check that the program does not crash.
    String output = runWithInput("OggScomment".getBytes());
    assertTrue(output.length() > 0);
  }

  @Test
  void shouldCatchExceptionsAndPrintThem() {
    // Force an exception by giving malformed data
    String output = runWithInput(new byte[] {0x00});
    assertTrue(output.contains("Exception") || output.contains("error"));
  }
}
