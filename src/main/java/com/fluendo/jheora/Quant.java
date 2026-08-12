/* Jheora
 * Copyright (C) 2004 Fluendo S.L.
 * 
 * Written by: 2004 Wim Taymans <wim@fluendo.com>
 *             2008 Maik Merten <maikmerten@googlemail.com>
 *  
 * Many thanks to 
 *   The Xiph.Org Foundation http://www.xiph.org/
 * Jheora was based on their Theora reference decoder.
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

package com.fluendo.jheora;

import com.jcraft.jogg.Buffer;

public final class Quant 
{ 
    private static int ilog(long v) {
        int ret = 0;
        while (v != 0) {
            ret++;
            v >>= 1;
        }
        return ret;
    }

    public static int readQTables(Info ci, Buffer opb) {
        /* Variable names according to Theora spec where it makes sense.
         * I *know* this may violate Java coding style rules, but I consider
         * readability against the Theora spec to be more important */
         
        long nbits, value;
        int x, bmi, nbms;

        /* A 2 × 3 array containing the number of quant ranges for a
           given qti and pli , respectively. This is at most 63. */
        int[][] nqrs = new int[2][3];
        
        /* A 2 × 3 × 63 array of the sizes of each quant range for a
           given qti and pli , respectively. Only the first nqrs[qti ][pli ]
           values are used. */
        int[][][] qrsizes = new int[2][3][63];
        
        /* A 2 × 3 × 64 array of the bmi ’s used for each quant
           range for a given qti and pli, respectively. Only the first
           (nqrs[qti ][pli ] + 1) values are used. */
        int[][][] qrbmis = new int[2][3][64];
        
        int qri, qi, qtj, plj;
        
        /* 1. Read a 4-bit unsigned integer. Assign nbits the value read, plus one. */
        nbits = opb.readB(4); 
        nbits++;
        
        /* 2. For each consecutive value of qi from 0 to 63, inclusive:
             (a) Read an nbits-bit unsigned integer as acscale[qi ]. */
        for (x = 0; x < 64; x++) {
            value = opb.readB((int) nbits);
            if (nbits < 0) return Result.BADHEADER;
            ci.AcScaleFactorTable[x] = (int) value;
        }
        
        /* 3. Read a 4-bit unsigned integer. Assign nbits the value read, plus one. */
        nbits = opb.readB(4); 
        nbits++;
        
        /* 4. For each consecutive value of qi from 0 to 63, inclusive:
             (a) Read an nbits-bit unsigned integer as dcscale[qi ]. */
        for (x = 0; x < Constants.Q_TABLE_SIZE; x++) {
            value = opb.readB((int) nbits);
            if (nbits < 0) return Result.BADHEADER;
            ci.DcScaleFactorTable[x] = (short) value;
        }
        
        /* 5. Read a 9-bit unsigned integer. Assign nbms the value decoded, plus
           one. nbms MUST be no greater than 384. */
        nbms = opb.readB(9); 
        nbms++;
        if (nbms > 384) {
            return Result.BADHEADER;
        }
        ci.MaxQMatrixIndex = nbms;
        
        /* 6. For each consecutive value of bmi from 0 to (nbms - 1), inclusive:
             (a) For each consecutive value of ci from 0 to 63, inclusive:
                   i. Read an 8-bit unsigned integer as bms[bmi ][ci ]. */
        
        ci.qmats = new short[nbms * 64];
        for (bmi = 0; bmi < nbms; bmi++) {
            for (x = 0; x < 64; x++) {
                value = opb.readB(8);
                if (nbits < 0) return Result.BADHEADER;
                ci.qmats[(bmi << 6) + x] = (short) value;
            }
        }
        
        /* 7. For each consecutive value of qti from 0 to 1, inclusive: */
        for (int qti = 0; qti <= 1; ++qti) {
            /* (a) For each consecutive value of pli from 0 to 2, inclusive: */
            for (int pli = 0; pli <= 2; ++pli) {
                int newqr;
                if (qti > 0 || pli > 0) {
                    /* i. If qti > 0 or pli > 0, read a 1-bit unsigned integer as newqr. */
                    newqr = opb.readB(1);
                } else {
                    /* ii. Else, assign newqr the value one. */
                    newqr = 1;
                }
                    
                if (newqr == 0) {
                    /* If newqr is zero, then we are copying a previously defined set
                       of quant ranges. In that case: */ 
                        
                    int rpqr;
                    if (qti > 0) {
                        /* A. If qti > 0, read a 1-bit unsigned integer as rpqr. */
                        rpqr = opb.readB(1);
                    } else {
                        /* B. Else, assign rpqr the value zero. */
                        rpqr = 0;
                    }
                        
                    if (rpqr == 1) {
                        /* C. If rpqr is one, assign qtj the value (qti - 1) and assign plj
                           the value pli . This selects the set of quant ranges defined
                           for the same color plane as this one, but for the previous
                           quantization type. */
                        qtj = qti - 1;
                        plj = pli;
                    } else {
                        /* D. Else assign qtj the value (3 * qti + pli - 1)//3 and assign plj
                           the value (pli + 2)%3. This selects the most recent set of
                           quant ranges defined. */
                        qtj = (3 * qti + pli - 1) / 3;
                        plj = (pli + 2) % 3;
                    }
                        
                    /* E. Assign nqrs[qti ][pli ] the value nqrs[qtj ][plj ]. */
                    nqrs[qti][pli] = nqrs[qtj][plj];
                        
                    /* F. Assign qrsizes[qti ][pli ] the values in qrsizes[qtj ][plj ]. */
                    qrsizes[qti][pli] = qrsizes[qtj][plj];
                        
                    /* G. Assign qrbmis[qti ][pli ] the values in qrbmis[qtj ][plj ]. */
                    qrbmis[qti][pli] = qrbmis[qtj][plj];
                        
                } else {
                    /* Else, newqr is one, which indicates that we are defining a new
                       set of quant ranges. In that case: */ 
                        
                    /* A. Assign qri the value zero. */
                    qri = 0;

                    /* B. Assign qi the value zero. */
                    qi = 0;
                            
                    /* C. Read an ilog(nbms - 1)-bit unsigned integer as
                           qrbmis[qti ][pli ][qri ]. If this is greater than or equal to
                           nbms, stop. The stream is undecodable. */
                    qrbmis[qti][pli][qri] = opb.readB(ilog(nbms - 1));
                    if (qrbmis[qti][pli][qri] >= nbms) {
                        System.out.println("bad header (1)");
                        return Result.BADHEADER;
                    }
                            
                    do {
                        /* D. Read an ilog(62 - qi )-bit unsigned integer. Assign
                               qrsizes[qti ][pli ][qri ] the value read, plus one. */
                        qrsizes[qti][pli][qri] = opb.readB(ilog(62 - qi)) + 1;
                            
                        /* E. Assign qi the value qi + qrsizes[qti ][pli ][qri ]. */
                        qi = qi + qrsizes[qti][pli][qri];
                            
                        /* F. Assign qri the value qri + 1. */
                        qri = qri + 1;
                            
                        /* G. Read an ilog(nbms - 1)-bit unsigned integer as
                               qrbmis[qti ][pli ][qri ]. */
                        qrbmis[qti][pli][qri] = opb.readB(ilog(nbms - 1));
                            
                        /* H. If qi is less than 63, go back to step 7(a)ivD. */
                    } while (qi < 63);
                            
                    /* I. If qi is greater than 63, stop. The stream is undecodable. */
                    if (qi > 63) {
                        System.out.println("bad header (2): " + qi);
                        return Result.BADHEADER;
                    }
                            
                    /* J. Assign nqrs[qti ][pli ] the value qri . */
                    nqrs[qti][pli] = qri;
                }
            }
        }
        
        /* Compute all 384 matrices */
        for (int coding = 0; coding < 2; ++coding) {
            for (int plane = 0; plane < 3; ++plane) {
                for (int quality = 0; quality < 64; ++quality) {
                    short[] scaledmat = compQuantMatrix(ci.AcScaleFactorTable, ci.DcScaleFactorTable, ci.qmats, nqrs, qrsizes, qrbmis, coding, plane, quality);
                    for (int coeff = 0; coeff < 64; ++coeff) {
                        int j = Constants.DEQUANT_INDEX[coeff];
                        ci.dequant_tables[coding][plane][quality][coeff] = scaledmat[j];
                    }
                }
            }
        }
    
        return 0;
    }
    
    static short[] compQuantMatrix(int[] acscale, short[] dcscale, short[] bms, int[][] nqrs, 
            int[][][] qrsizes, int[][][] qrbmis, int qti, int pli, int qi) {

        /* Variable names according to Theora spec where it makes sense.
         * I *know* this may violate Java coding style rules, but I consider
         * readability against the Theora spec to be more important */
         
        short[] qmat = new short[64];
        int qri, qrj;
        
        for (qri = 0; qri < 63; ++qri) {
            int sum1 = 0;
            for (qrj = 0; qrj < qri; ++qrj) {
                sum1 += qrsizes[qti][pli][qrj];
            }
                
            int sum2 = 0;
            for (qrj = 0; qrj <= qri; ++qrj) {
                sum2 += qrsizes[qti][pli][qrj];
            }
                
            if (qi >= sum1 && qi <= sum2)
                break;
        }
            
        int qistart = 0;
        for (qrj = 0; qrj < qri; ++qrj) {
            qistart += qrsizes[qti][pli][qrj];
        }
            
        int qiend = 0;
        for (qrj = 0; qrj <= qri; ++qrj) {
            qiend += qrsizes[qti][pli][qrj];
        }
            
        int bmi = qrbmis[qti][pli][qri];
        int bmj = qrbmis[qti][pli][qri + 1];
            
        int[] bm = new int[64];
        int qmin;
        
        for (int ci = 0; ci < 64; ++ci) {
            bm[ci] = (2 * (qiend - qi) * bms[(bmi << 6) + ci]
                  + 2 * (qi - qistart) * bms[(bmj << 6) + ci]
                  + qrsizes[qti][pli][qri]) / (2 * qrsizes[qti][pli][qri]);
              
            if (ci == 0 && qti == 0)
                qmin = 16;
            else if (ci > 0 && qti == 0)
                qmin = 8;
            else if (ci == 0 && qti == 1)
                qmin = 32;
            else 
                qmin = 16;
                
            int qscale;
            if (ci == 0) {
                qscale = dcscale[qi];
            } else {
                qscale = acscale[qi];
            }
                
            qmat[ci] = (short) Math.max(qmin, Math.min((qscale * bm[ci] / 100) * 4, 4096));
        }
            
        return qmat;
    }
}
