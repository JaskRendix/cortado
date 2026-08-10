/* JOrbis
 * Copyright (C) 2000 ymnk, JCraft,Inc.
 * 
 * Written by: 2000 ymnk<ymnk@jcaft.com>
 *  
 * Many thanks to 
 *   Monty <monty@xiph.org> and 
 *   The XIPHOPHORUS Company http://www.xiph.org/ .
 * JOrbis has been based on their awesome works, Vorbis codec.
 *  
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
   
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.jcraft.jorbis;

public final class ChainingExample {

    public static void main(String[] args) {
        VorbisFile ov = null;

        try {
            ov = new VorbisFile(System.in, null, -1);
            if (ov.seekable()) {
                System.out.printf("Input bitstream contained %d logical bitstream section(s).%n", ov.streams());
                System.out.printf("Total bitstream playing time: %.2f seconds%n%n", ov.time_total(-1));
            } else {
                System.out.println("Standard input was not seekable.");
                System.out.println("First logical bitstream information:%n");
            }

            for (int i = 0; i < ov.streams(); i++) {
                Info vi = ov.getInfo(i);
                System.out.printf("\tlogical bitstream section %d information:%n", i + 1);
                System.out.printf("\t\t%dHz %d channels bitrate %dkbps serial number=%d%n",
                        vi.getRate(), vi.getChannels(), ov.bitrate(i) / 1000, ov.serialnumber(i));
                System.out.printf("\t\tcompressed length: %d bytes play time: %.2fs%n",
                        ov.raw_total(i), ov.time_total(i));
                Comment vc = ov.getComment(i);
                System.out.println(vc);
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
