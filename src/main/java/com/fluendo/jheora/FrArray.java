/* Jheora
 * Copyright (C) 2004 Fluendo S.L.
 *
 * Written by: 2004 Wim Taymans <wim@fluendo.com>
 *
 * Many thanks to
 *   The Xiph.Org Foundation http://www.xiph.org/
 * Jheora was based on their Theora reference decoder.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.fluendo.jheora;

import com.fluendo.utils.MemUtils;
import com.jcraft.jogg.Buffer;

public final class FrArray {
  private int bitPattern;
  private byte bitsSoFar;
  private byte nextBit;
  private int bitsLeft;

  public FrArray() {}

  public void init() {
    /* Initialise the decoding of a run. */
    bitPattern = 0;
    bitsSoFar = 0;
  }

  private int decodeBlockRun(int bitValue) {
    /* Add in the new bit value. */
    bitsSoFar++;
    bitPattern = (bitPattern << 1) + (bitValue & 1);

    /* Coding scheme:
       Codeword         RunLength
       0x                1-2
       10x               3-4
       110x              5-6
       1110xx            7-10
       11110xx          11-14
       11111xxxx        15-30
    */

    switch (bitsSoFar) {
      case 2 -> {
        /* If bit 1 is clear */
        if ((bitPattern & 0x0002) == 0) {
          bitsLeft = (bitPattern & 0x0001) + 1;
          return 1;
        }
      }
      case 3 -> {
        /* If bit 1 is clear */
        if ((bitPattern & 0x0002) == 0) {
          bitsLeft = (bitPattern & 0x0001) + 3;
          return 1;
        }
      }
      case 4 -> {
        /* If bit 1 is clear */
        if ((bitPattern & 0x0002) == 0) {
          bitsLeft = (bitPattern & 0x0001) + 5;
          return 1;
        }
      }
      case 6 -> {
        /* If bit 2 is clear */
        if ((bitPattern & 0x0004) == 0) {
          bitsLeft = (bitPattern & 0x0003) + 7;
          return 1;
        }
      }
      case 7 -> {
        /* If bit 2 is clear */
        if ((bitPattern & 0x0004) == 0) {
          bitsLeft = (bitPattern & 0x0003) + 11;
          return 1;
        }
      }
      case 9 -> {
        bitsLeft = (bitPattern & 0x000F) + 15;
        return 1;
      }
    }

    return 0;
  }

  private int decodeSbRun(int bitValue) {
    /* Add in the new bit value. */
    bitsSoFar++;
    bitPattern = (bitPattern << 1) + (bitValue & 1);

    /* Coding scheme:
       Codeword             RunLength
       0                        1
       10x                     2-3
       110x                    4-5
       1110xx                  6-9
       11110xxx              10-17
       111110xxxx            18-33
       111111xxxxxxxxxxxx  34-4129
    */

    switch (bitsSoFar) {
      case 1 -> {
        if (bitPattern == 0) {
          bitsLeft = 1;
          return 1;
        }
      }
      case 3 -> {
        /* Bit 1 clear */
        if ((bitPattern & 0x0002) == 0) {
          bitsLeft = (bitPattern & 0x0001) + 2;
          return 1;
        }
      }
      case 4 -> {
        /* Bit 1 clear */
        if ((bitPattern & 0x0002) == 0) {
          bitsLeft = (bitPattern & 0x0001) + 4;
          return 1;
        }
      }
      case 6 -> {
        /* Bit 2 clear */
        if ((bitPattern & 0x0004) == 0) {
          bitsLeft = (bitPattern & 0x0003) + 6;
          return 1;
        }
      }
      case 8 -> {
        /* Bit 3 clear */
        if ((bitPattern & 0x0008) == 0) {
          bitsLeft = (bitPattern & 0x0007) + 10;
          return 1;
        }
      }
      case 10 -> {
        /* Bit 4 clear */
        if ((bitPattern & 0x0010) == 0) {
          bitsLeft = (bitPattern & 0x000F) + 18;
          return 1;
        }
      }
      case 18 -> {
        bitsLeft = (bitPattern & 0x0FFF) + 34;
        return 1;
      }
    }
    return 0;
  }

  private void getNextBInit(Buffer opb) {
    long ret = opb.readB(1);
    nextBit = (byte) ret;

    /* Read run length */
    init();
    do {
      ret = opb.readB(1);
    } while (decodeBlockRun((int) ret) == 0);
  }

  private byte getNextBBit(Buffer opb) {
    long ret;
    if (bitsLeft == 0) {
      /* Toggle the value. */
      nextBit = (byte) (nextBit ^ 1);

      /* Read next run */
      init();
      do {
        ret = opb.readB(1);
      } while (decodeBlockRun((int) ret) == 0);
    }

    /* Have read a bit */
    bitsLeft--;

    /* Return next bit value */
    return nextBit;
  }

  private void getNextSbInit(Buffer opb) {
    long ret = opb.readB(1);
    nextBit = (byte) ret;

    /* Read run length */
    init();
    do {
      ret = opb.readB(1);
    } while (decodeSbRun((int) ret) == 0);
  }

  private byte getNextSbBit(Buffer opb) {
    long ret;
    if (bitsLeft == 0) {
      /* Toggle the value. */
      nextBit = (byte) (nextBit ^ 1);

      /* Read next run */
      init();
      do {
        ret = opb.readB(1);
      } while (decodeSbRun((int) ret) == 0);
    }

    /* Have read a bit */
    bitsLeft--;

    /* Return next bit value */
    return nextBit;
  }

  private final short[] empty64 = new short[64];

  public void quadDecodeDisplayFragments(Playback pbi) {
    int sb, mb, b;
    int dataToDecode;
    int dfIndex;
    int mbIndex = 0;
    Buffer opb = pbi.opb;

    /* Reset various data structures common to key frames and inter frames. */
    pbi.CodedBlockIndex = 0;
    MemUtils.set(pbi.display_fragments, 0, 0, pbi.UnitFragments);

    /* For "Key frames" mark all blocks as coded and return. */
    /* Else initialise the ArrayPtr array to 0 (all blocks uncoded by default) */
    if (pbi.getFrameType() == Constants.BASE_FRAME) {
      MemUtils.set(pbi.SBFullyFlags, 0, 1, pbi.SuperBlocks);
      MemUtils.set(pbi.SBCodedFlags, 0, 1, pbi.SuperBlocks);
      MemUtils.set(pbi.MBCodedFlags, 0, 0, pbi.MacroBlocks);
    } else {
      MemUtils.set(pbi.SBFullyFlags, 0, 0, pbi.SuperBlocks);
      MemUtils.set(pbi.MBCodedFlags, 0, 0, pbi.MacroBlocks);

      /* Un-pack the list of partially coded Super-Blocks */
      getNextSbInit(opb);
      for (sb = 0; sb < pbi.SuperBlocks; sb++) {
        pbi.SBCodedFlags[sb] = getNextSbBit(opb);
      }

      /* Scan through the list of super blocks. Unless all are marked
      as partially coded we have more to do. */
      dataToDecode = 0;
      for (sb = 0; sb < pbi.SuperBlocks; sb++) {
        if (pbi.SBCodedFlags[sb] == 0) {
          dataToDecode = 1;
          break;
        }
      }

      /* Are there further block map bits to decode ? */
      if (dataToDecode != 0) {
        /* Un-pack the Super-Block fully coded flags. */
        getNextSbInit(opb);
        for (sb = 0; sb < pbi.SuperBlocks; sb++) {
          /* Skip blocks already marked as partially coded */
          while ((sb < pbi.SuperBlocks) && (pbi.SBCodedFlags[sb] != 0)) {
            sb++;
          }

          if (sb < pbi.SuperBlocks) {
            pbi.SBFullyFlags[sb] = getNextSbBit(opb);

            if (pbi.SBFullyFlags[sb] != 0) {
              /* If SB is fully coded. */
              pbi.SBCodedFlags[sb] = 1; /* Mark the SB as coded */
            }
          }
        }
      }

      /* Scan through the list of coded super blocks. If at least one
      is marked as partially coded then we have a block list to
      decode. */
      for (sb = 0; sb < pbi.SuperBlocks; sb++) {
        if ((pbi.SBCodedFlags[sb] != 0) && (pbi.SBFullyFlags[sb] == 0)) {
          /* Initialise the block list decoder. */
          getNextBInit(opb);
          break;
        }
      }
    }

    /* Decode the block data from the bit stream. */
    for (sb = 0; sb < pbi.SuperBlocks; sb++) {
      for (mb = 0; mb < 4; mb++) {
        /* If MB is in the frame */
        if (pbi.BlockMap.quadMapToMBTopLeft(sb, mb) >= 0) {
          /* Only read block level data if SB was fully or partially coded */
          if (pbi.SBCodedFlags[sb] != 0) {
            for (b = 0; b < 4; b++) {
              /* If block is valid (in frame)... */
              dfIndex = pbi.BlockMap.quadMapToIndex1(sb, mb, b);
              if (dfIndex >= 0) {
                if (pbi.SBFullyFlags[sb] != 0) {
                  pbi.display_fragments[dfIndex] = 1;
                } else {
                  pbi.display_fragments[dfIndex] = getNextBBit(opb);
                }

                /* Create linear list of coded block indices */
                if (pbi.display_fragments[dfIndex] != 0) {
                  pbi.MBCodedFlags[mbIndex] = 1;
                  pbi.CodedBlockList[pbi.CodedBlockIndex] = dfIndex;
                  /* Clear down the pbi.QFragData structure for this coded block. */
                  System.arraycopy(empty64, 0, pbi.QFragData[dfIndex], 0, 64);
                  pbi.CodedBlockIndex++;
                }
              }
            }
          }
          mbIndex++;
        }
      }
    }
  }

  public CodingMode unpackMode(Buffer opb) {
    /* Coding scheme:
       Token                     Codeword        Bits
       Entry   0 (most frequent)  0               1
       Entry   1                  10              2
       Entry   2                  110             3
       Entry   3                  1110            4
       Entry   4                  11110           5
       Entry   5                  111110          6
       Entry   6                  1111110         7
       Entry   7                  1111111         7
    */

    /* Initialise the decoding. */
    bitsSoFar = 0;
    bitPattern = opb.readB(1);

    /* Do we have a match */
    if (bitPattern == 0) {
      return CodingMode.MODES[0];
    }

    /* Get the next bit */
    bitPattern = (bitPattern << 1) | opb.readB(1);

    /* Do we have a match */
    if (bitPattern == 0x0002) {
      return CodingMode.MODES[1];
    }

    bitPattern = (bitPattern << 1) | opb.readB(1);

    /* Do we have a match */
    if (bitPattern == 0x0006) {
      return CodingMode.MODES[2];
    }

    bitPattern = (bitPattern << 1) | opb.readB(1);

    /* Do we have a match */
    if (bitPattern == 0x000E) {
      return CodingMode.MODES[3];
    }

    bitPattern = (bitPattern << 1) | opb.readB(1);

    /* Do we have a match */
    if (bitPattern == 0x001E) {
      return CodingMode.MODES[4];
    }

    bitPattern = (bitPattern << 1) | opb.readB(1);

    /* Do we have a match */
    if (bitPattern == 0x003E) {
      return CodingMode.MODES[5];
    }

    bitPattern = (bitPattern << 1) | opb.readB(1);

    /* Do we have a match */
    if (bitPattern == 0x007E) {
      return CodingMode.MODES[6];
    } else {
      return CodingMode.MODES[7];
    }
  }
}
