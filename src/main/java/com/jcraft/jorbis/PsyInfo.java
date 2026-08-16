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
  private int athp;
  private int decayp;
  private int smoothp;
  private int noisefitp;
  private int noisefitSubblock;
  private float noisefitThreshdB;

  private float athAtt;

  private int tonemaskp;
  private final float[] toneatt125Hz = new float[5];
  private final float[] toneatt250Hz = new float[5];
  private final float[] toneatt500Hz = new float[5];
  private final float[] toneatt1000Hz = new float[5];
  private final float[] toneatt2000Hz = new float[5];
  private final float[] toneatt4000Hz = new float[5];
  private final float[] toneatt8000Hz = new float[5];

  private int peakattp;
  private final float[] peakatt125Hz = new float[5];
  private final float[] peakatt250Hz = new float[5];
  private final float[] peakatt500Hz = new float[5];
  private final float[] peakatt1000Hz = new float[5];
  private final float[] peakatt2000Hz = new float[5];
  private final float[] peakatt4000Hz = new float[5];
  private final float[] peakatt8000Hz = new float[5];

  private int noisemaskp;
  private final float[] noiseatt125Hz = new float[5];
  private final float[] noiseatt250Hz = new float[5];
  private final float[] noiseatt500Hz = new float[5];
  private final float[] noiseatt1000Hz = new float[5];
  private final float[] noiseatt2000Hz = new float[5];
  private final float[] noiseatt4000Hz = new float[5];
  private final float[] noiseatt8000Hz = new float[5];

  private float maxCurveDb;

  private float attackCoeff;
  private float decayCoeff;

  public PsyInfo() {}

  public void free() {}

  public int getAthp() {
    return athp;
  }

  public void setAthp(int athp) {
    this.athp = athp;
  }

  public int getDecayp() {
    return decayp;
  }

  public void setDecayp(int decayp) {
    this.decayp = decayp;
  }

  public int getSmoothp() {
    return smoothp;
  }

  public void setSmoothp(int smoothp) {
    this.smoothp = smoothp;
  }

  public int getNoisefitp() {
    return noisefitp;
  }

  public void setNoisefitp(int noisefitp) {
    this.noisefitp = noisefitp;
  }

  public int getNoisefitSubblock() {
    return noisefitSubblock;
  }

  public void setNoisefitSubblock(int noisefitSubblock) {
    this.noisefitSubblock = noisefitSubblock;
  }

  public float getNoisefitThreshdB() {
    return noisefitThreshdB;
  }

  public void setNoisefitThreshdB(float noisefitThreshdB) {
    this.noisefitThreshdB = noisefitThreshdB;
  }

  public float getAthAtt() {
    return athAtt;
  }

  public void setAthAtt(float athAtt) {
    this.athAtt = athAtt;
  }

  public int getTonemaskp() {
    return tonemaskp;
  }

  public void setTonemaskp(int tonemaskp) {
    this.tonemaskp = tonemaskp;
  }

  public float[] getToneatt125Hz() {
    return toneatt125Hz;
  }

  public void setToneatt125Hz(float[] toneatt125Hz) {
    System.arraycopy(toneatt125Hz, 0, this.toneatt125Hz, 0, Math.min(toneatt125Hz.length, this.toneatt125Hz.length));
  }

  public float[] getToneatt250Hz() {
    return toneatt250Hz;
  }

  public void setToneatt250Hz(float[] toneatt250Hz) {
    System.arraycopy(toneatt250Hz, 0, this.toneatt250Hz, 0, Math.min(toneatt250Hz.length, this.toneatt250Hz.length));
  }

  public float[] getToneatt500Hz() {
    return toneatt500Hz;
  }

  public void setToneatt500Hz(float[] toneatt500Hz) {
    System.arraycopy(toneatt500Hz, 0, this.toneatt500Hz, 0, Math.min(toneatt500Hz.length, this.toneatt500Hz.length));
  }

  public float[] getToneatt1000Hz() {
    return toneatt1000Hz;
  }

  public void setToneatt1000Hz(float[] toneatt1000Hz) {
    System.arraycopy(toneatt1000Hz, 0, this.toneatt1000Hz, 0, Math.min(toneatt1000Hz.length, this.toneatt1000Hz.length));
  }

  public float[] getToneatt2000Hz() {
    return toneatt2000Hz;
  }

  public void setToneatt2000Hz(float[] toneatt2000Hz) {
    System.arraycopy(toneatt2000Hz, 0, this.toneatt2000Hz, 0, Math.min(toneatt2000Hz.length, this.toneatt2000Hz.length));
  }

  public float[] getToneatt4000Hz() {
    return toneatt4000Hz;
  }

  public void setToneatt4000Hz(float[] toneatt4000Hz) {
    System.arraycopy(toneatt4000Hz, 0, this.toneatt4000Hz, 0, Math.min(toneatt4000Hz.length, this.toneatt4000Hz.length));
  }

  public float[] getToneatt8000Hz() {
    return toneatt8000Hz;
  }

  public void setToneatt8000Hz(float[] toneatt8000Hz) {
    System.arraycopy(toneatt8000Hz, 0, this.toneatt8000Hz, 0, Math.min(toneatt8000Hz.length, this.toneatt8000Hz.length));
  }

  public int getPeakattp() {
    return peakattp;
  }

  public void setPeakattp(int peakattp) {
    this.peakattp = peakattp;
  }

  public float[] getPeakatt125Hz() {
    return peakatt125Hz;
  }

  public void setPeakatt125Hz(float[] peakatt125Hz) {
    System.arraycopy(peakatt125Hz, 0, this.peakatt125Hz, 0, Math.min(peakatt125Hz.length, this.peakatt125Hz.length));
  }

  public float[] getPeakatt250Hz() {
    return peakatt250Hz;
  }

  public void setPeakatt250Hz(float[] peakatt250Hz) {
    System.arraycopy(peakatt250Hz, 0, this.peakatt250Hz, 0, Math.min(peakatt250Hz.length, this.peakatt250Hz.length));
  }

  public float[] getPeakatt500Hz() {
    return peakatt500Hz;
  }

  public void setPeakatt500Hz(float[] peakatt500Hz) {
    System.arraycopy(peakatt500Hz, 0, this.peakatt500Hz, 0, Math.min(peakatt500Hz.length, this.peakatt500Hz.length));
  }

  public float[] getPeakatt1000Hz() {
    return peakatt1000Hz;
  }

  public void setPeakatt1000Hz(float[] peakatt1000Hz) {
    System.arraycopy(peakatt1000Hz, 0, this.peakatt1000Hz, 0, Math.min(peakatt1000Hz.length, this.peakatt1000Hz.length));
  }

  public float[] getPeakatt2000Hz() {
    return peakatt2000Hz;
  }

  public void setPeakatt2000Hz(float[] peakatt2000Hz) {
    System.arraycopy(peakatt2000Hz, 0, this.peakatt2000Hz, 0, Math.min(peakatt2000Hz.length, this.peakatt2000Hz.length));
  }

  public float[] getPeakatt4000Hz() {
    return peakatt4000Hz;
  }

  public void setPeakatt4000Hz(float[] peakatt4000Hz) {
    System.arraycopy(peakatt4000Hz, 0, this.peakatt4000Hz, 0, Math.min(peakatt4000Hz.length, this.peakatt4000Hz.length));
  }

  public float[] getPeakatt8000Hz() {
    return peakatt8000Hz;
  }

  public void setPeakatt8000Hz(float[] peakatt8000Hz) {
    System.arraycopy(peakatt8000Hz, 0, this.peakatt8000Hz, 0, Math.min(peakatt8000Hz.length, this.peakatt8000Hz.length));
  }

  public int getNoisemaskp() {
    return noisemaskp;
  }

  public void setNoisemaskp(int noisemaskp) {
    this.noisemaskp = noisemaskp;
  }

  public float[] getNoiseatt125Hz() {
    return noiseatt125Hz;
  }

  public void setNoiseatt125Hz(float[] noiseatt125Hz) {
    System.arraycopy(noiseatt125Hz, 0, this.noiseatt125Hz, 0, Math.min(noiseatt125Hz.length, this.noiseatt125Hz.length));
  }

  public float[] getNoiseatt250Hz() {
    return noiseatt250Hz;
  }

  public void setNoiseatt250Hz(float[] noiseatt250Hz) {
    System.arraycopy(noiseatt250Hz, 0, this.noiseatt250Hz, 0, Math.min(noiseatt250Hz.length, this.noiseatt250Hz.length));
  }

  public float[] getNoiseatt500Hz() {
    return noiseatt500Hz;
  }

  public void setNoiseatt500Hz(float[] noiseatt500Hz) {
    System.arraycopy(noiseatt500Hz, 0, this.noiseatt500Hz, 0, Math.min(noiseatt500Hz.length, this.noiseatt500Hz.length));
  }

  public float[] getNoiseatt1000Hz() {
    return noiseatt1000Hz;
  }

  public void setNoiseatt1000Hz(float[] noiseatt1000Hz) {
    System.arraycopy(noiseatt1000Hz, 0, this.noiseatt1000Hz, 0, Math.min(noiseatt1000Hz.length, this.noiseatt1000Hz.length));
  }

  public float[] getNoiseatt2000Hz() {
    return noiseatt2000Hz;
  }

  public void setNoiseatt2000Hz(float[] noiseatt2000Hz) {
    System.arraycopy(noiseatt2000Hz, 0, this.noiseatt2000Hz, 0, Math.min(noiseatt2000Hz.length, this.noiseatt2000Hz.length));
  }

  public float[] getNoiseatt4000Hz() {
    return noiseatt4000Hz;
  }

  public void setNoiseatt4000Hz(float[] noiseatt4000Hz) {
    System.arraycopy(noiseatt4000Hz, 0, this.noiseatt4000Hz, 0, Math.min(noiseatt4000Hz.length, this.noiseatt4000Hz.length));
  }

  public float[] getNoiseatt8000Hz() {
    return noiseatt8000Hz;
  }

  public void setNoiseatt8000Hz(float[] noiseatt8000Hz) {
    System.arraycopy(noiseatt8000Hz, 0, this.noiseatt8000Hz, 0, Math.min(noiseatt8000Hz.length, this.noiseatt8000Hz.length));
  }

  public float getMaxCurveDb() {
    return maxCurveDb;
  }

  public void setMaxCurveDb(float maxCurveDb) {
    this.maxCurveDb = maxCurveDb;
  }

  public float getAttackCoeff() {
    return attackCoeff;
  }

  public void setAttackCoeff(float attackCoeff) {
    this.attackCoeff = attackCoeff;
  }

  public float getDecayCoeff() {
    return decayCoeff;
  }

  public void setDecayCoeff(float decayCoeff) {
    this.decayCoeff = decayCoeff;
  }
}
