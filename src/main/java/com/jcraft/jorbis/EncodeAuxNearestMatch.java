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

public class EncodeAuxNearestMatch {
  int[] ptr0;
  int[] ptr1;

  int[] p; // decision points (each is an entry)
  int[] q; // decision points (each is an entry)
  int aux; // number of tree entries
  int alloc;

  public EncodeAuxNearestMatch() {}

  public int[] getPtr0() {
    return ptr0;
  }

  public void setPtr0(int[] ptr0) {
    this.ptr0 = ptr0;
  }

  public int[] getPtr1() {
    return ptr1;
  }

  public void setPtr1(int[] ptr1) {
    this.ptr1 = ptr1;
  }

  public int[] getP() {
    return p;
  }

  public void setP(int[] p) {
    this.p = p;
  }

  public int[] getQ() {
    return q;
  }

  public void setQ(int[] q) {
    this.q = q;
  }

  public int getAux() {
    return aux;
  }

  public void setAux(int aux) {
    this.aux = aux;
  }

  public int getAlloc() {
    return alloc;
  }

  public void setAlloc(int alloc) {
    this.alloc = alloc;
  }
}
