/* JOrbis
 * Copyright (C) 2000 ymnk, JCraft,Inc.
 * 
 * Written by: 2000 ymnk<ymnk@jcaft.com>
 *  
 * Many thanks to 
 *  Monty <monty@xiph.org> and 
 *  The XIPHOPHORUS Company http://www.xiph.org/ .
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

import com.jcraft.jogg.Buffer;

class Floor0 extends FuncFloor {

    @Override
    void pack(Object i, Buffer opb) {
        InfoFloor0 info = (InfoFloor0) i;
        opb.write(info.order, 8);
        opb.write(info.rate, 16);
        opb.write(info.barkmap, 16);
        opb.write(info.ampbits, 6);
        opb.write(info.ampdB, 8);
        opb.write(info.numbooks - 1, 4);
        for (int j = 0; j < info.numbooks; j++) {
            opb.write(info.books[j], 8);
        }
    }

    @Override
    Object unpack(Info vi, Buffer opb) {
        InfoFloor0 info = new InfoFloor0();
        info.order = opb.read(8);
        info.rate = opb.read(16);
        info.barkmap = opb.read(16);
        info.ampbits = opb.read(6);
        info.ampdB = opb.read(8);
        info.numbooks = opb.read(4) + 1;

        if ((info.order < 1) ||
            (info.rate < 1) ||
            (info.barkmap < 1) ||
            (info.numbooks < 1)) {
            return null;
        }

        for (int j = 0; j < info.numbooks; j++) {
            info.books[j] = opb.read(8);
            if (info.books[j] < 0 || info.books[j] >= vi.getBooks()) {
                return null;
            }
        }
        return info;
    }

    @Override
    Object look(DspState vd, InfoMode mi, Object i) {
        float scale;
        Info vi = vd.vi;
        InfoFloor0 info = (InfoFloor0) i;
        LookFloor0 look = new LookFloor0();
        look.m = info.order;
        look.n = vi.getBlocksizes()[mi.getBlockflag()] / 2;
        look.ln = info.barkmap;
        look.vi = info;
        look.lpclook.init(look.ln, look.m);

        scale = look.ln / toBARK((float) (info.rate / 2.));

        look.linearmap = new int[look.n];
        for (int j = 0; j < look.n; j++) {
            int val = (int) Math.floor(toBARK((float) ((info.rate / 2.) / look.n * j)) * scale);
            if (val >= look.ln) {
                val = look.ln;
            }
            look.linearmap[j] = val;
        }
        return look;
    }

    static float toBARK(float f) {
        return (float) (13.1 * Math.atan(.00074 * (f)) + 2.24 * Math.atan((f) * (f) * 1.85e-8) + 1e-4 * (f));
    }

    Object state(Object i) {
        EchstateFloor0 state = new EchstateFloor0();
        InfoFloor0 info = (InfoFloor0) i;

        state.codewords = new int[info.order];
        state.curve = new float[info.barkmap];
        state.frameno = -1;
        return state;
    }

    @Override
    void freeInfo(Object i) {
    }

    @Override
    void freeLook(Object i) {
    }

    @Override
    void freeState(Object vs) {
    }

    @Override
    int forward(Block vb, Object i, float[] in, float[] out, Object vs) {
        return 0;
    }

    private float[] lsp = null;

    @Override
    Object inverse1(Block vb, Object i, Object memo) {
        LookFloor0 look = (LookFloor0) i;
        InfoFloor0 info = look.vi;
        float[] currentLsp = null;
        if (memo instanceof float[]) {
            currentLsp = (float[]) memo;
        }

        int ampraw = vb.opb.read(info.ampbits);
        if (ampraw > 0) {
            int maxval = (1 << info.ampbits) - 1;
            float amp = (float) ampraw / maxval * info.ampdB;
            int booknum = vb.opb.read(ilog(info.numbooks));

            if (booknum != -1 && booknum < info.numbooks) {
                CodeBook b = vb.vd.fullbooks[info.books[booknum]];
                float last = 0.f;

                if (currentLsp == null || currentLsp.length < look.m + 1) {
                    currentLsp = new float[look.m + 1];
                } else {
                    for (int j = 0; j < currentLsp.length; j++) {
                        currentLsp[j] = 0.f;
                    }
                }

                for (int j = 0; j < look.m; j += b.dim) {
                    if (b.decodev_set(currentLsp, j, vb.opb, b.dim) == -1) {
                        return null;
                    }
                }

                for (int j = 0; j < look.m;) {
                    for (int k = 0; k < b.dim; k++, j++) {
                        currentLsp[j] += last;
                    }
                    last = currentLsp[j - 1];
                }
                currentLsp[look.m] = amp;
                return currentLsp;
            }
        }
        return null;
    }

    @Override
    int inverse2(Block vb, Object i, Object memo, float[] out) {
        LookFloor0 look = (LookFloor0) i;
        InfoFloor0 info = look.vi;

        if (memo != null) {
            float[] currentLsp = (float[]) memo;
            float amp = currentLsp[look.m];

            Lsp.lspToCurve(out, look.linearmap, look.n, look.ln,
                currentLsp, look.m, amp, info.ampdB);
            return 1;
        }
        for (int j = 0; j < look.n; j++) {
            out[j] = 0.f;
        }
        return 0;
    }

    static float fromdB(float x) {
        return (float) (Math.exp((x) * .11512925));
    }

    private static int ilog(int v) {
        int ret = 0;
        while (v != 0) {
            ret++;
            v >>>= 1;
        }
        return ret;
    }
}

class InfoFloor0 {
    int order;
    int rate;
    int barkmap;

    int ampbits;
    int ampdB;

    int numbooks;
    int[] books = new int[16];
}

class LookFloor0 {
    int n;
    int ln;
    int m;
    int[] linearmap;

    InfoFloor0 vi;
    Lpc lpclook = new Lpc();
}

class EchstateFloor0 {
    int[] codewords;
    float[] curve;
    long frameno;
    long codes;
}
