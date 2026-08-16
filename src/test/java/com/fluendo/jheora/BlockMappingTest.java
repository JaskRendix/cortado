package com.fluendo.jheora;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class BlockMappingTest {

  private static final int[] MB_ORDER_MAP = {0, 2, 3, 1};
  private static final int[][] BLOCK_ORDER_MAP_1 = {
    {0, 1, 3, 2},
    {0, 2, 3, 1},
    {0, 2, 3, 1},
    {3, 2, 0, 1}
  };

  /**
   * Reference implementation of the mapping logic, mirroring BlockMapping#createMapping and the
   * constructor, so we can assert that the public API returns consistent values.
   */
  private int[][][] buildExpectedBlockMap(
      int ySuperBlocks, int uvSuperBlocks, int hFrags, int vFrags, int shiftx, int shifty) {

    int totalSBs = ySuperBlocks + uvSuperBlocks * 2;
    int[][][] map = new int[totalSBs][4][4];

    // Initialize to -1
    for (int sb = 0; sb < totalSBs; sb++) {
      for (int mb = 0; mb < 4; mb++) {
        for (int b = 0; b < 4; b++) {
          map[sb][mb][b] = -1;
        }
      }
    }

    class Mapper {
      void createMapping(int firstSB, int firstFrag, int hFragsLocal, int vFragsLocal) {
        int i = 0;
        int j = 0;
        int xpos;
        int ypos;
        int mb;
        int b;

        int sb = firstSB;
        int fragIndex = firstFrag;

        int sBRows = (vFragsLocal >> 2) + ((vFragsLocal & 0x3) != 0 ? 1 : 0);
        int sBCols = (hFragsLocal >> 2) + ((hFragsLocal & 0x3) != 0 ? 1 : 0);

        for (int sBRow = 0; sBRow < sBRows; sBRow++) {
          for (int sBCol = 0; sBCol < sBCols; sBCol++) {
            ypos = sBRow << 2;

            for (i = 0; (i < 4) && (ypos < vFragsLocal); i++, ypos++) {
              xpos = sBCol << 2;

              for (j = 0; (j < 4) && (xpos < hFragsLocal); j++, xpos++) {
                mb = (i & 2) + ((j & 2) >> 1);
                b = ((i & 1) << 1) + (j & 1);

                map[sb][mb][b] = fragIndex++;
              }

              fragIndex += hFragsLocal - j;
            }

            sb++;
            fragIndex -= i * hFragsLocal - j;
          }

          fragIndex += 3 * hFragsLocal;
        }
      }
    }

    Mapper mapper = new Mapper();

    // Y plane
    mapper.createMapping(0, 0, hFrags, vFrags);

    // U plane
    mapper.createMapping(ySuperBlocks, hFrags * vFrags, hFrags >> shiftx, vFrags >> shifty);

    // V plane
    mapper.createMapping(
        ySuperBlocks + uvSuperBlocks,
        hFrags * vFrags + (hFrags >> shiftx) * (vFrags >> shifty),
        hFrags >> shiftx,
        vFrags >> shifty);

    return map;
  }

  @Test
  void quadMapToIndex1_matchesReferenceImplementation_onRegularGrid() {
    int ySuperBlocks = 4;
    int uvSuperBlocks = 1;
    int hFrags = 8;
    int vFrags = 8;
    int shiftx = 1;
    int shifty = 1;

    BlockMapping mapping =
        new BlockMapping(ySuperBlocks, uvSuperBlocks, hFrags, vFrags, shiftx, shifty);
    int[][][] expected =
        buildExpectedBlockMap(ySuperBlocks, uvSuperBlocks, hFrags, vFrags, shiftx, shifty);

    int totalSBs = ySuperBlocks + uvSuperBlocks * 2;

    for (int sb = 0; sb < totalSBs; sb++) {
      for (int mb = 0; mb < 4; mb++) {
        for (int b = 0; b < 4; b++) {
          int expectedIndex = expected[sb][MB_ORDER_MAP[mb]][BLOCK_ORDER_MAP_1[mb][b]];
          int actualIndex = mapping.quadMapToIndex1(sb, mb, b);

          assertEquals(
              expectedIndex, actualIndex, "Mismatch at sb=" + sb + ", mb=" + mb + ", b=" + b);
        }
      }
    }
  }

  @Test
  void quadMapToMBTopLeft_returnsTopLeftFragmentIndex() {
    int ySuperBlocks = 4;
    int uvSuperBlocks = 1;
    int hFrags = 8;
    int vFrags = 8;
    int shiftx = 1;
    int shifty = 1;

    BlockMapping mapping =
        new BlockMapping(ySuperBlocks, uvSuperBlocks, hFrags, vFrags, shiftx, shifty);
    int[][][] expected =
        buildExpectedBlockMap(ySuperBlocks, uvSuperBlocks, hFrags, vFrags, shiftx, shifty);

    int totalSBs = ySuperBlocks + uvSuperBlocks * 2;

    for (int sb = 0; sb < totalSBs; sb++) {
      for (int mb = 0; mb < 4; mb++) {
        int expectedTopLeft = expected[sb][MB_ORDER_MAP[mb]][0];
        int actualTopLeft = mapping.quadMapToMBTopLeft(sb, mb);

        assertEquals(
            expectedTopLeft, actualTopLeft, "Top-left mismatch at sb=" + sb + ", mb=" + mb);
      }
    }
  }

  @Test
  void mapping_handlesNonMultipleOfFourFragmentDimensions_withMinusOneForUnusedBlocks() {
    int ySuperBlocks = 2;
    int uvSuperBlocks = 1;
    int hFrags = 5; // not multiple of 4
    int vFrags = 3; // not multiple of 4
    int shiftx = 1;
    int shifty = 1;

    BlockMapping mapping =
        new BlockMapping(ySuperBlocks, uvSuperBlocks, hFrags, vFrags, shiftx, shifty);
    int[][][] expected =
        buildExpectedBlockMap(ySuperBlocks, uvSuperBlocks, hFrags, vFrags, shiftx, shifty);

    int totalSBs = ySuperBlocks + uvSuperBlocks * 2;

    for (int sb = 0; sb < totalSBs; sb++) {
      for (int mb = 0; mb < 4; mb++) {
        for (int b = 0; b < 4; b++) {
          int expectedIndex = expected[sb][MB_ORDER_MAP[mb]][BLOCK_ORDER_MAP_1[mb][b]];
          int actualIndex = mapping.quadMapToIndex1(sb, mb, b);

          assertEquals(
              expectedIndex,
              actualIndex,
              "Mismatch (non-multiple-of-4) at sb=" + sb + ", mb=" + mb + ", b=" + b);
        }
      }
    }

    // Ensure at least one -1 exists
    boolean foundUnused = false;

    for (int sb = 0; sb < totalSBs && !foundUnused; sb++) {
      for (int mb = 0; mb < 4 && !foundUnused; mb++) {
        for (int b = 0; b < 4 && !foundUnused; b++) {
          int idx = mapping.quadMapToIndex1(sb, mb, b);
          if (idx == -1) {
            foundUnused = true;
          }
        }
      }
    }

    assertTrue(
        foundUnused, "Expected at least one unused block mapped to -1 for irregular dimensions");
  }

  @Test
  void fragmentIndicesAreSequentialWithinPlanes() {
    int ySuperBlocks = 4;
    int uvSuperBlocks = 1;
    int hFrags = 8;
    int vFrags = 8;
    int shiftx = 1;
    int shifty = 1;

    BlockMapping mapping =
        new BlockMapping(ySuperBlocks, uvSuperBlocks, hFrags, vFrags, shiftx, shifty);

    int maxY = hFrags * vFrags - 1;
    int minY = Integer.MAX_VALUE;
    int maxSeenY = Integer.MIN_VALUE;

    for (int sb = 0; sb < ySuperBlocks; sb++) {
      for (int mb = 0; mb < 4; mb++) {
        for (int b = 0; b < 4; b++) {
          int idx = mapping.quadMapToIndex1(sb, mb, b);
          if (idx >= 0 && idx <= maxY) {
            minY = Math.min(minY, idx);
            maxSeenY = Math.max(maxSeenY, idx);
          }
        }
      }
    }

    assertEquals(0, minY, "Y plane should start at fragment index 0");
    assertEquals(maxY, maxSeenY, "Y plane should cover up to last Y fragment index");
  }
}
