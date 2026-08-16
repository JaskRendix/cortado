package com.fluendo.jheora;

import static org.junit.jupiter.api.Assertions.*;

import com.jcraft.jogg.Buffer;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class HuffmanTest {

  static class MockBuffer extends Buffer {
    private final int[] bits;
    private int pos = 0;

    MockBuffer(int... bits) {
      this.bits = bits;
    }

    @Override
    public int readB(int n) {
      if (pos >= bits.length) return -1;
      return bits[pos++];
    }
  }

  @Test
  void createHuffmanList_sortsByFrequencyAscending() {
    HuffEntry[] roots = new HuffEntry[1];
    short[] freq = new short[Huffman.MAX_ENTROPY_TOKENS];

    freq[0] = 10;
    freq[1] = 1;
    freq[2] = 5;

    int[] codes = new int[Huffman.MAX_ENTROPY_TOKENS];
    byte[] lengths = new byte[Huffman.MAX_ENTROPY_TOKENS];

    Huffman.buildHuffmanTree(roots, codes, lengths, 0, freq);

    HuffEntry root = roots[0];

    // Root must be internal
    assertEquals(-1, root.value);

    // Tree must be valid
    assertNotNull(root.child[0]);
    assertNotNull(root.child[1]);
  }

  @Test
  void createHuffmanList_zeroFrequenciesBecomeOne() {
    HuffEntry[] roots = new HuffEntry[1];
    short[] freq = new short[Huffman.MAX_ENTROPY_TOKENS]; // all zero → all become 1

    int[] codes = new int[Huffman.MAX_ENTROPY_TOKENS];
    byte[] lengths = new byte[Huffman.MAX_ENTROPY_TOKENS];

    Huffman.buildHuffmanTree(roots, codes, lengths, 0, freq);

    HuffEntry root = roots[0];

    // Root must be internal
    assertEquals(-1, root.value);

    // All leaves must have frequency >= 1
    validateTree(root);

    // Root frequency must be sum of all frequencies
    assertEquals(Huffman.MAX_ENTROPY_TOKENS, root.frequency);
  }

  private void validateTree(HuffEntry node) {
    if (node.child[0] == null && node.child[1] == null) {
      assertTrue(node.frequency >= 1);
      return;
    }
    assertNotNull(node.child[0]);
    assertNotNull(node.child[1]);
    validateTree(node.child[0]);
    validateTree(node.child[1]);
  }

  @Test
  void buildHuffmanTree_createsValidBinaryTree() {
    HuffEntry[] roots = new HuffEntry[1];
    short[] freq = new short[Huffman.MAX_ENTROPY_TOKENS];

    for (int i = 0; i < freq.length; i++) freq[i] = (short) (i + 1);

    int[] codes = new int[freq.length];
    byte[] lengths = new byte[freq.length];

    Huffman.buildHuffmanTree(roots, codes, lengths, 0, freq);

    HuffEntry root = roots[0];

    assertEquals(-1, root.value);
    assertNotNull(root.child[0]);
    assertNotNull(root.child[1]);
  }

  @Test
  void buildHuffmanTree_deletesNextPreviousPointers() {
    HuffEntry[] roots = new HuffEntry[1];
    short[] freq = new short[Huffman.MAX_ENTROPY_TOKENS];

    for (int i = 0; i < freq.length; i++) freq[i] = 1;

    int[] codes = new int[freq.length];
    byte[] lengths = new byte[freq.length];

    Huffman.buildHuffmanTree(roots, codes, lengths, 0, freq);

    HuffEntry root = roots[0];

    assertNull(root.child[0].next);
    assertNull(root.child[0].previous);
    assertNull(root.child[1].next);
    assertNull(root.child[1].previous);
  }

  @Test
  void createCodeArray_assignsCodesToLeaves() {
    HuffEntry root = new HuffEntry();
    root.value = -1;

    root.child[0] = new HuffEntry();
    root.child[0].value = 3;

    root.child[1] = new HuffEntry();
    root.child[1].value = 5;

    int[] codes = new int[Huffman.MAX_ENTROPY_TOKENS];
    byte[] lengths = new byte[Huffman.MAX_ENTROPY_TOKENS];

    short[] freq = new short[Huffman.MAX_ENTROPY_TOKENS];
    Arrays.fill(freq, (short) 1);

    Huffman.buildHuffmanTree(new HuffEntry[] {root}, codes, lengths, 0, freq);

    assertTrue(lengths[3] > 0);
    assertTrue(lengths[5] > 0);
  }

  @Test
  void readHuffmanTrees_readsAllTables() {
    HuffEntry[] roots = new HuffEntry[Huffman.NUM_HUFF_TABLES];

    // 80 leaf nodes
    int[] bits = new int[Huffman.NUM_HUFF_TABLES * 2];
    for (int i = 0; i < bits.length; i += 2) {
      bits[i] = 1; // leaf
      bits[i + 1] = 7; // value
    }

    MockBuffer buf = new MockBuffer(bits);

    int ret = Huffman.readHuffmanTrees(roots, buf);

    assertEquals(0, ret);

    for (HuffEntry e : roots) {
      assertEquals(7, e.value);
    }
  }

  @Test
  void readHuffmanTrees_propagatesBadHeader() {
    HuffEntry[] roots = new HuffEntry[Huffman.NUM_HUFF_TABLES];

    MockBuffer buf = new MockBuffer(-1); // immediate failure

    int ret = Huffman.readHuffmanTrees(roots, buf);

    assertEquals(Result.BADHEADER, ret);
  }

  @Test
  void clearHuffmanTrees_setsAllEntriesToNull() {
    HuffEntry[] roots = new HuffEntry[Huffman.NUM_HUFF_TABLES];

    for (int i = 0; i < roots.length; i++) {
      roots[i] = new HuffEntry();
    }

    Huffman.clearHuffmanTrees(roots);

    for (HuffEntry e : roots) {
      assertNull(e);
    }
  }
}
