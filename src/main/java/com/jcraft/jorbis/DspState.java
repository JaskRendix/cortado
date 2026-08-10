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

public class DspState {
    static final float M_PI = 3.1415926539f;
    static final int VI_TRANSFORMB = 1;
    static final int VI_WINDOWB = 1;

    int analysisp;
    Info vi;
    int modebits;

    float[][] pcm;
    int pcm_storage;
    int pcm_current;
    int pcm_returned;

    float[] multipliers;
    int envelope_storage;
    int envelope_current;

    int eofflag;

    int lW;
    int W;
    int nW;
    int centerW;

    long granulepos;
    long sequence;

    long glue_bits;
    long time_bits;
    long floor_bits;
    long res_bits;

    float[][][][][] window;                 
    Object[][] transform;
    CodeBook[] fullbooks;
    Object[] mode;

    byte[] header;
    byte[] header1;
    byte[] header2;

    public DspState() {
        transform = new Object[2][];
        window = new float[2][][][][];
        window[0] = new float[2][][][];
        window[0][0] = new float[2][][];
        window[0][1] = new float[2][][];
        window[0][0][0] = new float[2][];
        window[0][0][1] = new float[2][];
        window[0][1][0] = new float[2][];
        window[0][1][1] = new float[2][];
        window[1] = new float[2][][][];
        window[1][0] = new float[2][][];
        window[1][1] = new float[2][][];
        window[1][0][0] = new float[2][];
        window[1][0][1] = new float[2][];
        window[1][1][0] = new float[2][];
        window[1][1][1] = new float[2][];
    }

    private static int ilog2(int v) {
        int ret = 0;
        while (v > 1) {
            ret++;
            v >>>= 1;
        }
        return (ret);
    }

    static float[] window(int type, int window_val, int left, int right) {
        float[] ret = new float[window_val];
        switch (type) {
            case 0:
                {
                    int leftbegin = window_val / 4 - left / 2;
                    int rightbegin = window_val - window_val / 4 - right / 2;
    
                    for (int i = 0; i < left; i++) {
                        float x = (float) ((i + .5) / left * M_PI / 2.);
                        x = (float) Math.sin(x);
                        x *= x;
                        x *= M_PI / 2.;
                        x = (float) Math.sin(x);
                        ret[i + leftbegin] = x;
                    }
      
                    for (int i = leftbegin + left; i < rightbegin; i++) {
                        ret[i] = 1.f;
                    }
      
                    for (int i = 0; i < right; i++) {
                        float x = (float) ((right - i - .5) / right * M_PI / 2.);
                        x = (float) Math.sin(x);
                        x *= x;
                        x *= M_PI / 2.;
                        x = (float) Math.sin(x);
                        ret[i + rightbegin] = x;
                    }
                }
                break;
            default:
                return (null);
        }
        return (ret);
    }

    int init(Info vi, boolean encp) {
        this.vi = vi;
        modebits = ilog2(vi.getModes());

        transform[0] = new Object[VI_TRANSFORMB];
        transform[1] = new Object[VI_TRANSFORMB];

        transform[0][0] = new Mdct();
        transform[1][0] = new Mdct();
        ((Mdct) transform[0][0]).init(vi.getBlocksizes()[0]);
        ((Mdct) transform[1][0]).init(vi.getBlocksizes()[1]);

        window[0][0][0] = new float[VI_WINDOWB][];
        window[0][0][1] = window[0][0][0];
        window[0][1][0] = window[0][0][0];
        window[0][1][1] = window[0][0][0];
        window[1][0][0] = new float[VI_WINDOWB][];
        window[1][0][1] = new float[VI_WINDOWB][];
        window[1][1][0] = new float[VI_WINDOWB][];
        window[1][1][1] = new float[VI_WINDOWB][];

        for (int i = 0; i < VI_WINDOWB; i++) {
            window[0][0][0][i] = window(i, vi.getBlocksizes()[0], vi.getBlocksizes()[0] / 2, vi.getBlocksizes()[0] / 2);
            window[1][0][0][i] = window(i, vi.getBlocksizes()[1], vi.getBlocksizes()[0] / 2, vi.getBlocksizes()[0] / 2);
            window[1][0][1][i] = window(i, vi.getBlocksizes()[1], vi.getBlocksizes()[0] / 2, vi.getBlocksizes()[1] / 2);
            window[1][1][0][i] = window(i, vi.getBlocksizes()[1], vi.getBlocksizes()[1] / 2, vi.getBlocksizes()[0] / 2);
            window[1][1][1][i] = window(i, vi.getBlocksizes()[1], vi.getBlocksizes()[1] / 2, vi.getBlocksizes()[1] / 2);
        }

        fullbooks = new CodeBook[vi.getBooks()];
        for (int i = 0; i < vi.getBooks(); i++) {
            fullbooks[i] = new CodeBook();
            fullbooks[i].init_decode(vi.getBookParam()[i]);
        }

        pcm_storage = 8192; 
        pcm = new float[vi.getChannels()][];
        for (int i = 0; i < vi.getChannels(); i++) {
            pcm[i] = new float[pcm_storage];
        }

        lW = 0; 
        W = 0;  

        centerW = vi.getBlocksizes()[1] / 2;
        pcm_current = centerW;

        mode = new Object[vi.getModes()];
        for (int i = 0; i < vi.getModes(); i++) {
            int mapnum = vi.getModeParam()[i].getMapping();
            int maptype = vi.getMapType()[mapnum];
            mode[i] = FuncMapping.MAPPING_P[maptype].look(this, vi.getModeParam()[i], vi.getMapParam()[mapnum]);
        }
        return (0);
    }

    public int synthesis_init(Info vi) {
        init(vi, false);
        pcm_returned = centerW;
        centerW -= vi.getBlocksizes()[W] / 4 + vi.getBlocksizes()[lW] / 4;
        granulepos = -1;
        sequence = -1;
        return (0);
    }

    DspState(Info vi) {
        this();
        init(vi, false);
        pcm_returned = centerW;
        centerW -= vi.getBlocksizes()[W] / 4 + vi.getBlocksizes()[lW] / 4;
        granulepos = -1;
        sequence = -1;
    }

    public int synthesis_blockin(Block vb) {
        if (centerW > vi.getBlocksizes()[1] / 2 && pcm_returned > 8192) {
            int shiftPCM = centerW - vi.getBlocksizes()[1] / 2;
            shiftPCM = (pcm_returned < shiftPCM ? pcm_returned : shiftPCM);

            pcm_current -= shiftPCM;
            centerW -= shiftPCM;
            pcm_returned -= shiftPCM;
            if (shiftPCM != 0) {
                for (int i = 0; i < vi.getChannels(); i++) {
                    System.arraycopy(pcm[i], shiftPCM, pcm[i], 0, pcm_current);
                }
            }
        }

        lW = W;
        W = vb.W;
        nW = -1;

        glue_bits += vb.glue_bits;
        time_bits += vb.time_bits;
        floor_bits += vb.floor_bits;
        res_bits += vb.res_bits;

        if (sequence + 1 != vb.sequence) granulepos = -1; 

        sequence = vb.sequence;

        {
            int sizeW = vi.getBlocksizes()[W];
            int _centerW = centerW + vi.getBlocksizes()[lW] / 4 + sizeW / 4;
            int beginW = _centerW - sizeW / 2;
            int endW = beginW + sizeW;
            int beginSl = 0;
            int endSl = 0;

            if (endW > pcm_storage) {
                pcm_storage = endW + vi.getBlocksizes()[1];
                for (int i = 0; i < vi.getChannels(); i++) {
                    float[] foo = new float[pcm_storage];
                    System.arraycopy(pcm[i], 0, foo, 0, pcm[i].length);
                    pcm[i] = foo;
                }
            }

            switch (W) {
                case 0:
                    beginSl = 0;
                    endSl = vi.getBlocksizes()[0] / 2;
                    break;
                case 1:
                    beginSl = vi.getBlocksizes()[1] / 4 - vi.getBlocksizes()[lW] / 4;
                    endSl = beginSl + vi.getBlocksizes()[lW] / 2;
                    break;
            }

            for (int j = 0; j < vi.getChannels(); j++) {
                int _pcm = beginW;
                int i = 0;
                for (i = beginSl; i < endSl; i++) {
                    pcm[j][_pcm + i] += vb.pcm[j][i];
                }
                for (; i < sizeW; i++) {
                    pcm[j][_pcm + i] = vb.pcm[j][i];
                }
            }

            if (granulepos == -1) {
                granulepos = vb.granulepos;
            } else {
                granulepos += (_centerW - centerW);
                if (vb.granulepos != -1 && granulepos != vb.granulepos) {
                    if (granulepos > vb.granulepos && vb.eofflag != 0) {
                        _centerW -= (granulepos - vb.granulepos);
                    }
                    granulepos = vb.granulepos;
                }
            }

            centerW = _centerW;
            pcm_current = endW;
            if (vb.eofflag != 0) eofflag = 1;
        }
        return (0);
    }

    public int synthesis_pcmout(float[][][] _pcm, int[] index) {
        if (pcm_returned < centerW) {
            if (_pcm != null) {
                for (int i = 0; i < vi.getChannels(); i++) {
                    index[i] = pcm_returned;
                }
                _pcm[0] = pcm;
            }
            return (centerW - pcm_returned);
        }
        return (0);
    }

    public int synthesis_read(int bytes) {
        if (bytes != 0 && pcm_returned + bytes > centerW) return (-1);
        pcm_returned += bytes;
        return (0);
    }

    public void clear() {
    }
}
