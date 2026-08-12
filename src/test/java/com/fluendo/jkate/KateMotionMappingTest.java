/* JKate
 * Copyright (C) 2008 ogg.k.ogg.k <ogg.k.ogg.k@googlemail.com>
 *
 * Parts of JKate are based on code by Wim Taymans <wim@fluendo.com>
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

package com.fluendo.jkate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KateMotionMappingTest {

    @Test
    @DisplayName("Factory: Valid indices return correct motion mapping instances")
    void testCreateMotionMappingValid() throws KateException {
        KateMotionMapping noneMapping = KateMotionMapping.createMotionMapping(0);
        assertNotNull(noneMapping);
        assertEquals(KateMotionMapping.KMM_NONE, noneMapping);

        KateMotionMapping bitmapSizeMapping = KateMotionMapping.createMotionMapping(5);
        assertNotNull(bitmapSizeMapping);
        assertEquals(KateMotionMapping.KMM_BITMAP_SIZE, bitmapSizeMapping);
    }

    @Test
    @DisplayName("Factory: Out of bounds negative index throws KateException")
    void testCreateMotionMappingNegativeIndex() {
        assertThrows(KateException.class, () -> {
            KateMotionMapping.createMotionMapping(-1);
        });
    }

    @Test
    @DisplayName("Factory: Out of bounds positive index throws KateException")
    void testCreateMotionMappingOutOfBoundsIndex() {
        assertThrows(KateException.class, () -> {
            KateMotionMapping.createMotionMapping(6);
        });
    }
}
