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
  private float[] toneatt125Hz = new float[5];
  private float[] toneatt250Hz = new float[5];
  private float[] toneatt500Hz = new float[5];
  private float[] toneatt1000Hz = new float[5];
  private float[] toneatt2000Hz = new float[5];
  private float[] toneatt4000Hz = new float[5];
  private float[] toneatt8000Hz = new float[5];

  private int peakattp;
  private float[] peakatt125Hz = new float[5];
  private float[] peakatt250Hz = new float[5];
  private float[] peakatt500Hz = new float[5];
  private float[] peakatt1000Hz = new float[5];
  private float[] peakatt2000Hz = new float[5];
  private float[] peakatt4000Hz = new float[5];
  private float[] peakatt8000Hz = new float[5];

  private int noisemaskp;
  private float[] noiseatt125Hz = new float[5];
  private float[] noiseatt250Hz = new float[5];
  private float[] noiseatt500Hz = new float[5];
  private float[] noiseatt1000Hz = new float[5];
  private float[] noiseatt2000Hz = new float[5];
  private float[] noiseatt4000Hz = new float[5];
  private float[] noiseatt8000Hz = new float[5];

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
    this.toneatt125Hz = toneatt125Hz;
  }

  public float[] getToneatt250Hz() {
    return toneatt250Hz;
  }

  public void setToneatt250Hz(float[] toneatt250Hz) {
    this.toneatt250Hz = toneatt250Hz;
  }

  public float[] getToneatt500Hz() {
    return toneatt500Hz;
  }

  public void setToneatt500Hz(float[] toneatt500Hz) {
    this.toneatt500Hz = toneatt500Hz;
  }

  public float[] getToneatt1000Hz() {
    return toneatt1000Hz;
  }

  public void setToneatt1000Hz(float[] toneatt1000Hz) {
    this.toneatt1000Hz = toneatt1000Hz;
  }

  public float[] getToneatt2000Hz() {
    return toneatt2000Hz;
  }

  public void setToneatt2000Hz(float[] toneatt2000Hz) {
    this.toneatt2000Hz = toneatt2000Hz;
  }

  public float[] getToneatt4000Hz() {
    return toneatt4000Hz;
  }

  public void setToneatt4000Hz(float[] toneatt4000Hz) {
    this.toneatt4000Hz = toneatt4000Hz;
  }

  public float[] getToneatt8000Hz() {
    return toneatt8000Hz;
  }

  public void setToneatt8000Hz(float[] toneatt8000Hz) {
    this.toneatt8000Hz = toneatt8000Hz;
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
    this.peakatt125Hz = peakatt125Hz;
  }

  public float[] getPeakatt250Hz() {
    return peakatt250Hz;
  }

  public void setPeakatt250Hz(float[] peakatt250Hz) {
    this.peakatt250Hz = peakatt250Hz;
  }

  public float[] getPeakatt500Hz() {
    return peakatt500Hz;
  }

  public void setPeakatt500Hz(float[] peakatt500Hz) {
    this.peakatt500Hz = peakatt500Hz;
  }

  public float[] getPeakatt1000Hz() {
    return peakatt1000Hz;
  }

  public void setPeakatt1000Hz(float[] peakatt1000Hz) {
    this.peakatt1000Hz = peakatt1000Hz;
  }

  public float[] getPeakatt2000Hz() {
    return peakatt2000Hz;
  }

  public void setPeakatt2000Hz(float[] peakatt2000Hz) {
    this.peakatt2000Hz = peakatt2000Hz;
  }

  public float[] getPeakatt4000Hz() {
    return peakatt4000Hz;
  }

  public void setPeakatt4000Hz(float[] peakatt4000Hz) {
    this.peakatt4000Hz = peakatt4000Hz;
  }

  public float[] getPeakatt8000Hz() {
    return peakatt8000Hz;
  }

  public void setPeakatt8000Hz(float[] peakatt8000Hz) {
    this.peakatt8000Hz = peakatt8000Hz;
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
    this.noiseatt125Hz = noiseatt125Hz;
  }

  public float[] getNoiseatt250Hz() {
    return noiseatt250Hz;
  }

  public void setNoiseatt250Hz(float[] noiseatt250Hz) {
    this.noiseatt250Hz = noiseatt250Hz;
  }

  public float[] getNoiseatt500Hz() {
    return noiseatt500Hz;
  }

  public void setNoiseatt500Hz(float[] noiseatt500Hz) {
    this.noiseatt500Hz = noiseatt500Hz;
  }

  public float[] getNoiseatt1000Hz() {
    return noiseatt1000Hz;
  }

  public void setNoiseatt1000Hz(float[] noiseatt1000Hz) {
    this.noiseatt1000Hz = noiseatt1000Hz;
  }

  public float[] getNoiseatt2000Hz() {
    return noiseatt2000Hz;
  }

  public void setNoiseatt2000Hz(float[] noiseatt2000Hz) {
    this.noiseatt2000Hz = noiseatt2000Hz;
  }

  public float[] getNoiseatt4000Hz() {
    return noiseatt4000Hz;
  }

  public void setNoiseatt4000Hz(float[] noiseatt4000Hz) {
    this.noiseatt4000Hz = noiseatt4000Hz;
  }

  public float[] getNoiseatt8000Hz() {
    return noiseatt8000Hz;
  }

  public void setNoiseatt8000Hz(float[] noiseatt8000Hz) {
    this.noiseatt8000Hz = noiseatt8000Hz;
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

  @Deprecated
  public float[] toneatt_125Hz() {
    return toneatt125Hz;
  }

  @Deprecated
  public float[] toneatt_250Hz() {
    return toneatt250Hz;
  }

  @Deprecated
  public float[] toneatt_500Hz() {
    return toneatt500Hz;
  }

  @Deprecated
  public float[] toneatt_1000Hz() {
    return toneatt1000Hz;
  }

  @Deprecated
  public float[] toneatt_2000Hz() {
    return toneatt2000Hz;
  }

  @Deprecated
  public float[] toneatt_4000Hz() {
    return toneatt4000Hz;
  }

  @Deprecated
  public float[] toneatt_8000Hz() {
    return toneatt8000Hz;
  }

  @Deprecated
  public float[] peakatt_125Hz() {
    return peakatt125Hz;
  }

  @Deprecated
  public float[] peakatt_250Hz() {
    return peakatt250Hz;
  }

  @Deprecated
  public float[] peakatt_500Hz() {
    return peakatt500Hz;
  }

  @Deprecated
  public float[] peakatt_1000Hz() {
    return peakatt1000Hz;
  }

  @Deprecated
  public float[] peakatt_2000Hz() {
    return peakatt2000Hz;
  }

  @Deprecated
  public float[] peakatt_4000Hz() {
    return peakatt4000Hz;
  }

  @Deprecated
  public float[] peakatt_8000Hz() {
    return peakatt8000Hz;
  }

  @Deprecated
  public float[] noiseatt_125Hz() {
    return noiseatt125Hz;
  }

  @Deprecated
  public float[] noiseatt_250Hz() {
    return noiseatt250Hz;
  }

  @Deprecated
  public float[] noiseatt_500Hz() {
    return noiseatt500Hz;
  }

  @Deprecated
  public float[] noiseatt_1000Hz() {
    return noiseatt1000Hz;
  }

  @Deprecated
  public float[] noiseatt_2000Hz() {
    return noiseatt2000Hz;
  }

  @Deprecated
  public float[] noiseatt_4000Hz() {
    return noiseatt4000Hz;
  }

  @Deprecated
  public float[] noiseatt_8000Hz() {
    return noiseatt8000Hz;
  }
}
