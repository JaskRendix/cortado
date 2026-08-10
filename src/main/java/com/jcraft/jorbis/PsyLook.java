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

// psychoacoustic setup
public class PsyLook {
    private int n;
    private PsyInfo vi;

    private float[][][] tonecurves;
    private float[][] peakatt;
    private float[][][] noisecurves;

    private float[] ath;
    private int[] octave;

    public PsyLook() {
    }

    public void init(PsyInfo vi, int n, int rate) {
        // Initialization code remains commented out as in original source,
        // or can be modernized if implementation is enabled.
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public PsyInfo getVi() {
        return vi;
    }

    public void setVi(PsyInfo vi) {
        this.vi = vi;
    }

    public float[][][] getTonecurves() {
        return tonecurves;
    }

    public void setTonecurves(float[][][] tonecurves) {
        this.tonecurves = tonecurves;
    }

    public float[][] getPeakatt() {
        return peakatt;
    }

    public void setPeakatt(float[][] peakatt) {
        this.peakatt = peakatt;
    }

    public float[][][] getNoisecurves() {
        return noisecurves;
    }

    public void setNoisecurves(float[][][] noisecurves) {
        this.noisecurves = noisecurves;
    }

    public float[] getAth() {
        return ath;
    }

    public void setAth(float[] ath) {
        this.ath = ath;
    }

    public int[] getOctave() {
        return octave;
    }

    public void setOctave(int[] octave) {
        this.octave = octave;
    }
}
