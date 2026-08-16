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
public class PsyInfo {
  public int athp;
  public int decayp;
  public int smoothp;
  public int noisefitp;
  public int noisefitSubblock;
  public float noisefitThreshdB;

  public float athAtt;

  public int tonemaskp;
  public final float[] toneatt125Hz = new float[5];
  public final float[] toneatt250Hz = new float[5];
  public final float[] toneatt500Hz = new float[5];
  public final float[] toneatt1000Hz = new float[5];
  public final float[] toneatt2000Hz = new float[5];
  public final float[] toneatt4000Hz = new float[5];
  public final float[] toneatt8000Hz = new float[5];

  public int peakattp;
  public final float[] peakatt125Hz = new float[5];
  public final float[] peakatt250Hz = new float[5];
  public final float[] peakatt500Hz = new float[5];
  public final float[] peakatt1000Hz = new float[5];
  public final float[] peakatt2000Hz = new float[5];
  public final float[] peakatt4000Hz = new float[5];
  public final float[] peakatt8000Hz = new float[5];

  public int noisemaskp;
  public final float[] noiseatt125Hz = new float[5];
  public final float[] noiseatt250Hz = new float[5];
  public final float[] noiseatt500Hz = new float[5];
  public final float[] noiseatt1000Hz = new float[5];
  public final float[] noiseatt2000Hz = new float[5];
  public final float[] noiseatt4000Hz = new float[5];
  public final float[] noiseatt8000Hz = new float[5];

  public float maxCurveDb;

  public float attackCoeff;
  public float decayCoeff;

  public PsyInfo() {}

  public void free() {}
}
