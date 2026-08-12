/* Cortado - a video player java applet
 * Copyright (C) 2004 Fluendo S.L.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Street #330, Boston, MA 02111-1307, USA.
 */

package com.fluendo.utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class MemUtils {

    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private MemUtils() {
        // Utility class; prevent instantiation
    }

    public static int cmp(byte[] mem1, byte[] mem2, int len) {
        for (int i = 0; i < len; i++) {
            if (mem1[i] != mem2[i]) {
                return (mem1[i] < mem2[i]) ? -i : i;
            }
        }
        return 0;
    }

    public static void set(byte[] mem, int offset, int val, int len) {
        Arrays.fill(mem, offset, offset + len, (byte) val);
    }

    public static void set(short[] mem, int offset, int val, int len) {
        Arrays.fill(mem, offset, offset + len, (short) val);
    }

    public static void set(int[] mem, int offset, int val, int len) {
        Arrays.fill(mem, offset, offset + len, val);
    }

    public static void set(Object[] mem, int offset, Object val, int len) {
        Arrays.fill(mem, offset, offset + len, val);
    }

    /**
     * Checks if a given byte array starts with a specified pattern at an offset.
     */
    public static boolean startsWith(byte[] arr, int offset, int len, byte[] pattern) {
        int patternLength = pattern.length;

        if (len < patternLength || offset < 0 || offset + patternLength > arr.length) {
            return false;
        }

        for (int i = 0; i < patternLength; i++) {
            if (arr[offset + i] != pattern[i]) {
                return false;
            }
        }

        return true;
    }

    public static void dump(byte[] mem, int start, int len) {
        StringBuilder hexString = new StringBuilder(50);
        StringBuilder charString = new StringBuilder(18);
        String vis = new String(mem, start, len, StandardCharsets.ISO_8859_1);

        int i = 0;
        int j = 0;

        while (i < len) {
            int b = mem[i + start] & 0xFF;

            if (b > 0x20 && b < 0x7F) {
                charString.append(vis.charAt(i));
            } else {
                charString.append('.');
            }

            hexString.append(HEX_DIGITS[b / 16]);
            hexString.append(HEX_DIGITS[b % 16]);
            hexString.append(' ');

            j++;
            i++;

            if (j == 16 || i == len) {
                System.out.println((i - j) + "  " + hexString.toString() + charString.toString());

                hexString.setLength(0);
                charString.setLength(0);
                j = 0;
            }
        }
    }
}
