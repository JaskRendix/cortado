package com.fluendo.jheora;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class HuffEntryTest {

  static class MockBuffer extends com.jcraft.jogg.Buffer {
    private final int[] bits;
    private int pos = 0;

    MockBuffer(int... bits) {
      this.bits = bits;
    }

    @Override
    public int readB(int n) {
      if (pos >= bits.length) {
        return -1; // simulate bitstream exhaustion
      }
      int v = bits[pos];
      pos++;
      return v;
    }
  }

  @Test
  void read_leafNodeAssignsValue() {
    // bit = 1 → leaf node
    // next 5 bits = value
    MockBuffer buf = new MockBuffer(1, 5);

    HuffEntry entry = new HuffEntry();
    int ret = entry.read(0, buf);

    assertEquals(0, ret);
    assertEquals(5, entry.value);
    assertNull(entry.child[0]);
    assertNull(entry.child[1]);
  }

  @Test
  void read_internalNodeCreatesChildren() {
    // bit = 0 → internal node
    // then two leaf nodes
    MockBuffer buf =
        new MockBuffer(
            0, // internal
            1,
            7, // left leaf
            1,
            9 // right leaf
            );

    HuffEntry entry = new HuffEntry();
    int ret = entry.read(0, buf);

    assertEquals(0, ret);
    assertEquals(-1, entry.value);
    assertNotNull(entry.child[0]);
    assertNotNull(entry.child[1]);
    assertEquals(7, entry.child[0].value);
    assertEquals(9, entry.child[1].value);
  }

  @Test
  void read_returnsBadHeaderOnBitstreamExhaustion() {
    MockBuffer buf = new MockBuffer(); // empty

    HuffEntry entry = new HuffEntry();
    int ret = entry.read(0, buf);

    assertEquals(Result.BADHEADER, ret);
  }

  @Test
  void read_returnsBadHeaderOnDepthOverflow() {
    // Force depth > 32 by chaining 33 internal nodes
    int[] bits = new int[40];
    for (int i = 0; i < 33; i++) bits[i] = 0; // internal nodes
    bits[33] = 1;
    bits[34] = 5; // leaf to terminate

    MockBuffer buf = new MockBuffer(bits);

    HuffEntry entry = new HuffEntry();
    int ret = entry.read(0, buf);

    assertEquals(Result.BADHEADER, ret);
  }

  @Test
  void read_propagatesChildError() {
    // internal node, but child read fails
    MockBuffer buf =
        new MockBuffer(
            0, // internal
            -1 // child read fails immediately
            );

    HuffEntry entry = new HuffEntry();
    int ret = entry.read(0, buf);

    assertEquals(Result.BADHEADER, ret);
  }

  @Test
  void copy_leafNodeCopiesValueOnly() {
    HuffEntry leaf = new HuffEntry();
    leaf.value = 7;

    HuffEntry copy = leaf.copy();

    assertEquals(7, copy.value);
    assertNull(copy.child[0]);
    assertNull(copy.child[1]);
  }

  @Test
  void copy_internalNodeDeepCopiesChildren() {
    HuffEntry root = new HuffEntry();
    root.value = -1;

    root.child[0] = new HuffEntry();
    root.child[0].value = 3;

    root.child[1] = new HuffEntry();
    root.child[1].value = 5;

    HuffEntry copy = root.copy();

    assertEquals(-1, copy.value);
    assertNotSame(root.child[0], copy.child[0]);
    assertNotSame(root.child[1], copy.child[1]);
    assertEquals(3, copy.child[0].value);
    assertEquals(5, copy.child[1].value);
  }

  @Test
  void copy_doesNotCopyPreviousOrNextLinks() {
    HuffEntry root = new HuffEntry();
    root.value = -1; // internal node

    // Internal nodes MUST have children
    root.child[0] = new HuffEntry();
    root.child[0].value = 3;

    root.child[1] = new HuffEntry();
    root.child[1].value = 5;

    root.previous = new HuffEntry();
    root.next = new HuffEntry();

    HuffEntry copy = root.copy();

    assertNull(copy.previous);
    assertNull(copy.next);

    // Children must be deep‑copied
    assertNotSame(root.child[0], copy.child[0]);
    assertNotSame(root.child[1], copy.child[1]);
  }

  @Test
  void childArrayAlwaysHasLengthTwo() {
    HuffEntry entry = new HuffEntry();
    assertEquals(2, entry.child.length);
  }

  @Test
  void frequencyDefaultsToZero() {
    HuffEntry entry = new HuffEntry();
    assertEquals(0, entry.frequency);
  }
}
