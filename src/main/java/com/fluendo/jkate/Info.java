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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.fluendo.jkate;

import com.jcraft.jogg.Buffer;
import com.jcraft.jogg.Packet;
import com.fluendo.utils.Debug;
import java.util.Objects;

public class Info {
    public int bitstream_version_major = -1;
    public int bitstream_version_minor = -1;
    public KateTextEncoding text_encoding;
    public KateTextDirectionality text_directionality;
    public int num_headers = 0;
    public int granule_shift;
    public int gps_numerator;
    public int gps_denominator;
    public String language;
    public String category;
    public Region[] regions;
    public Style[] styles;
    public Curve[] curves;
    public Motion[] motions;
    public Palette[] palettes;
    public Bitmap[] bitmaps;
    public FontRange[] font_ranges;
    public FontMapping[] font_mappings;

    public KateMarkupType markup_type;
    public int original_canvas_width;
    public int original_canvas_height;

    /* used to track which header we need to decode next */
    private int probe = 0;

    public Info() {
    }

    private static int readCanvasSize(Buffer opb) {
        Objects.requireNonNull(opb, "Buffer cannot be null");
        int value = opb.read(8) | (opb.read(8) << 8);
        int base = value >> 4;
        int shift = value & 15;
        return base << shift;
    }

    private int unpackInfo(Buffer opb) {
        Objects.requireNonNull(opb, "Buffer cannot be null");
        int reserved, tmp;

        bitstream_version_major = (byte) opb.read(8);
        bitstream_version_minor = (byte) opb.read(8);
        Debug.info("Kate bitstream v" + bitstream_version_major + "." + bitstream_version_minor);

        if (bitstream_version_major > 0) {
            return Result.KATE_E_VERSION;
        }

        num_headers = (int) opb.read(8);
        if (num_headers < 1) {
            return Result.KATE_E_BAD_PACKET;
        }
        tmp = opb.read(8);
        if (tmp != 0) {
            return Result.KATE_E_BAD_PACKET;
        }
        try {
            text_encoding = KateTextEncoding.CreateTextEncoding(tmp);
            text_directionality = KateTextDirectionality.CreateTextDirectionality(opb.read(8));
        } catch (KateException e) {
            return Result.KATE_E_BAD_PACKET;
        }
        reserved = opb.read(8);
        if (bitstream_version_major == 0 && bitstream_version_minor < 3) {
            if (reserved != 0) {
                return Result.KATE_E_BAD_PACKET;
            }
        }
        granule_shift = opb.read(8);

        original_canvas_width = readCanvasSize(opb);
        original_canvas_height = readCanvasSize(opb);

        reserved = Bitwise.read32(opb);
        if (bitstream_version_major == 0 && bitstream_version_minor < 3) {
            if (reserved != 0) {
                return Result.KATE_E_BAD_PACKET;
            }
        }

        gps_numerator = Bitwise.read32(opb);
        gps_denominator = Bitwise.read32(opb);

        if (granule_shift >= 64) {
            return Result.KATE_E_BAD_PACKET;
        }
        if (gps_numerator == 0 || gps_denominator == 0) {
            return Result.KATE_E_BAD_PACKET;
        }

        byte[] buffer16 = new byte[16];
        int bufferCharacters;

        language = "";
        Bitwise.readbuf(opb, buffer16, 16);
        if (buffer16[15] != 0) {
            return Result.KATE_E_BAD_PACKET;
        }
        for (bufferCharacters = 0; bufferCharacters < 16; ++bufferCharacters) {
            if (buffer16[bufferCharacters] == 0) break;
        }
        language = new String(buffer16, 0, bufferCharacters);

        category = "";
        Bitwise.readbuf(opb, buffer16, 16);
        if (buffer16[15] != 0) {
            return Result.KATE_E_BAD_PACKET;
        }
        for (bufferCharacters = 0; bufferCharacters < 16; ++bufferCharacters) {
            if (buffer16[bufferCharacters] == 0) break;
        }
        category = new String(buffer16, 0, bufferCharacters);

        /* end of packet */
        if (opb.read(1) != -1) {
            return Result.KATE_E_BAD_PACKET;
        }

        return 0;
    }

    static int checkEOP(Buffer opb) {
        Objects.requireNonNull(opb, "Buffer cannot be null");
        int bits = 7 & (8 - (opb.bits() & 7));
        if (bits > 0) {
            if (opb.read(bits) != 0) {
                return Result.KATE_E_BAD_PACKET;
            }
        }
        if (opb.look1() != -1) {
            return Result.KATE_E_BAD_PACKET;
        }
        return 0;
    }

    private int unpackComment(Comment kc, Buffer opb) {
        Objects.requireNonNull(kc, "Comment cannot be null");
        Objects.requireNonNull(opb, "Buffer cannot be null");
        int i;
        int len;
        byte[] tmp;
        int comments;

        len = Bitwise.read32(opb);
        if (len < 0) {
            return Result.KATE_E_BAD_PACKET;
        }

        tmp = new byte[len];
        Bitwise.readbuf(opb, tmp, len);
        kc.vendor = new String(tmp);

        comments = Bitwise.read32(opb);
        if (comments < 0) {
            kc.clear();
            return Result.KATE_E_BAD_PACKET;
        }
        kc.user_comments = new String[comments];
        for (i = 0; i < comments; i++) {
            len = Bitwise.read32(opb);
            if (len < 0) {
                kc.clear();
                return Result.KATE_E_BAD_PACKET;
            }

            tmp = new byte[len];
            Bitwise.readbuf(opb, tmp, len);
            kc.user_comments[i] = new String(tmp);
        }
        return 0;
    }

    public Region unpackRegion(Buffer opb) throws KateException {
        Objects.requireNonNull(opb, "Buffer cannot be null");
        Region kr = new Region();

        kr.metric = KateSpaceMetric.CreateSpaceMetric(opb.read(8));
        kr.x = Bitwise.read32v(opb);
        kr.y = Bitwise.read32v(opb);
        kr.w = Bitwise.read32v(opb);
        kr.h = Bitwise.read32v(opb);
        kr.style = Bitwise.read32v(opb);

        if (((bitstream_version_major << 8) | bitstream_version_minor) >= 0x0002) {
            Bitwise.read32v(opb);
            kr.clip = (opb.read1() != 0);
        } else {
            kr.clip = false;
        }

        Bitwise.skipWarp(opb);
        return kr;
    }

    private int unpackRegions(Buffer opb) {
        int nregions = Bitwise.read32v(opb);
        if (nregions < 0) return Result.KATE_E_BAD_PACKET;
        regions = new Region[nregions];
        for (int n = 0; n < nregions; ++n) {
            try {
                regions[n] = unpackRegion(opb);
            } catch (KateException ke) {
                regions = null;
                return Result.KATE_E_BAD_PACKET;
            }
        }

        Bitwise.skipWarp(opb);
        return checkEOP(opb);
    }

    private Color unpackColor(Buffer opb) {
        Objects.requireNonNull(opb, "Buffer cannot be null");
        Color color = new Color();
        color.r = (byte) opb.read(8);
        color.g = (byte) opb.read(8);
        color.b = (byte) opb.read(8);
        color.a = (byte) opb.read(8);
        return color;
    }

    public Style unpackStyle(Buffer opb) throws KateException {
        Objects.requireNonNull(opb, "Buffer cannot be null");
        Style ks = new Style();

        double[][] floats = Bitwise.readFloats(opb, 8, 1);
        if (floats == null || floats[0].length < 8) {
            throw new KateBadPacketException();
        }
        int idx = 0;
        ks.halign = floats[0][idx++];
        ks.valign = floats[0][idx++];
        ks.font_width = floats[0][idx++];
        ks.font_height = floats[0][idx++];
        ks.left_margin = floats[0][idx++];
        ks.top_margin = floats[0][idx++];
        ks.right_margin = floats[0][idx++];
        ks.bottom_margin = floats[0][idx++];
        ks.text_color = unpackColor(opb);
        ks.background_color = unpackColor(opb);
        ks.draw_color = unpackColor(opb);
        ks.font_metric = KateSpaceMetric.CreateSpaceMetric(opb.read(8));
        ks.margin_metric = KateSpaceMetric.CreateSpaceMetric(opb.read(8));
        ks.bold = opb.read1() != 0;
        ks.italics = opb.read1() != 0;
        ks.underline = opb.read1() != 0;
        ks.strike = opb.read1() != 0;

        if (((bitstream_version_major << 8) | bitstream_version_minor) >= 0x0002) {
            Bitwise.read32v(opb);
            ks.justify = opb.read1() != 0;
            int len = Bitwise.read32v(opb);
            if (len < 0)
                throw new KateBadPacketException();
            byte[] s = new byte[len];
            Bitwise.readbuf(opb, s, len);
            ks.font = new String(s);
        } else {
            ks.justify = false;
            ks.font = null;
        }

        if (((bitstream_version_major << 8) | bitstream_version_minor) >= 0x0004) {
            Bitwise.read32v(opb);
            ks.wrap_mode = KateWrapMode.CreateWrapMode(Bitwise.read32v(opb));
        } else {
            ks.wrap_mode = KateWrapMode.kate_wrap_word;
        }

        Bitwise.skipWarp(opb);
        return ks;
    }

    private int unpackStyles(Buffer opb) {
        int nstyles = Bitwise.read32v(opb);
        if (nstyles < 0) return Result.KATE_E_BAD_PACKET;
        styles = new Style[nstyles];
        for (int n = 0; n < nstyles; ++n) {
            try {
                styles[n] = unpackStyle(opb);
            } catch (KateException ke) {
                styles = null;
                return Result.KATE_E_BAD_PACKET;
            }
        }

        Bitwise.skipWarp(opb);
        return checkEOP(opb);
    }

    private Curve unpackCurve(Buffer opb) throws KateException {
        Curve kc = new Curve();

        kc.type = KateCurveType.CreateCurveType(opb.read(8));
        kc.npts = Bitwise.read32v(opb);
        if (kc.npts < 0)
            throw new KateBadPacketException();
        Bitwise.skipWarp(opb);

        kc.pts = Bitwise.readFloats(opb, kc.npts, 2);
        return kc;
    }

    private int unpackCurves(Buffer opb) {
        int ncurves = Bitwise.read32v(opb);
        if (ncurves < 0) return Result.KATE_E_BAD_PACKET;
        curves = new Curve[ncurves];
        for (int n = 0; n < ncurves; ++n) {
            try {
                curves[n] = unpackCurve(opb);
            } catch (KateException ke) {
                curves = null;
                return Result.KATE_E_BAD_PACKET;
            }
        }

        Bitwise.skipWarp(opb);
        return checkEOP(opb);
    }

    public Motion unpackMotion(Buffer opb) throws KateException {
        Motion km = new Motion();

        int ncurves = Bitwise.read32v(opb);
        if (ncurves < 0)
            throw new KateBadPacketException();
        km.curves = new Curve[ncurves];
        for (int n = 0; n < ncurves; ++n) {
            if (opb.read1() != 0) {
                int idx = Bitwise.read32v(opb);
                if (this.curves == null || idx < 0 || idx >= this.curves.length)
                    throw new KateBadPacketException();
                km.curves[n] = this.curves[idx];
            } else {
                km.curves[n] = unpackCurve(opb);
            }
        }

        double[][] floats = Bitwise.readFloats(opb, ncurves, 1);
        if (floats == null || floats[0].length < ncurves) {
            throw new KateBadPacketException();
        }
        km.durations = new double[ncurves];
        for (int n = 0; n < ncurves; ++n) {
            km.durations[n] = floats[0][n];
        }

        km.x_mapping = KateMotionMapping.CreateMotionMapping(opb.read(8));
        km.y_mapping = KateMotionMapping.CreateMotionMapping(opb.read(8));
        km.semantics = KateMotionSemantics.CreateMotionSemantics(opb.read(8));
        km.periodic = (opb.read1() != 0);

        Bitwise.skipWarp(opb);
        return km;
    }

    private int unpackMotions(Buffer opb) {
        int nmotions = Bitwise.read32v(opb);
        if (nmotions < 0) return Result.KATE_E_BAD_PACKET;
        motions = new Motion[nmotions];
        for (int n = 0; n < nmotions; ++n) {
            try {
                motions[n] = unpackMotion(opb);
            } catch (KateException ke) {
                motions = null;
                return Result.KATE_E_BAD_PACKET;
            }
        }

        Bitwise.skipWarp(opb);
        return checkEOP(opb);
    }

    public Palette unpackPalette(Buffer opb) throws KateException {
        Palette kp = new Palette();

        int ncolors = opb.read(8) + 1;
        kp.colors = new Color[ncolors];
        for (int n = 0; n < ncolors; ++n) {
            kp.colors[n] = unpackColor(opb);
        }

        Bitwise.skipWarp(opb);
        return kp;
    }

    private int unpackPalettes(Buffer opb) {
        int npalettes = Bitwise.read32v(opb);
        if (npalettes < 0) return Result.KATE_E_BAD_PACKET;
        palettes = new Palette[npalettes];
        for (int n = 0; n < npalettes; ++n) {
            try {
                palettes[n] = unpackPalette(opb);
            } catch (KateException ke) {
                palettes = null;
                return Result.KATE_E_BAD_PACKET;
            }
        }

        Bitwise.skipWarp(opb);
        return checkEOP(opb);
    }

    public Bitmap unpackBitmap(Buffer opb) throws KateException {
        Bitmap kb = new Bitmap();

        kb.width = Bitwise.read32v(opb);
        kb.height = Bitwise.read32v(opb);
        kb.bpp = opb.read(8);
        if (kb.width < 0 || kb.height < 0 || kb.bpp < 0 || kb.bpp > 8)
            throw new KateBadPacketException();

        if (kb.bpp == 0) {
            kb.type = KateBitmapType.CreateBitmapType(opb.read(8));
            kb.palette = -1;
            if (kb.type == KateBitmapType.kate_bitmap_type_paletted) {
                int encoding = opb.read(8);
                switch (encoding) {
                    case 1: /* RLE */
                        kb.bpp = Bitwise.read32v(opb);
                        kb.palette = Bitwise.read32v(opb);
                        kb.pixels = RLE.decodeRLE(opb, kb.width, kb.height, kb.bpp);
                        break;
                    default:
                        Debug.warning("Unsupported bitmap type");
                        throw new KateBadPacketException();
                }
            } else if (kb.type == KateBitmapType.kate_bitmap_type_png) {
                kb.size = Bitwise.read32(opb);
                if (kb.size < 0) throw new KateBadPacketException();
                kb.pixels = new byte[kb.size];
                Bitwise.readbuf(opb, kb.pixels, kb.size);
            } else {
                Debug.warning("Unsupported bitmap type");
                throw new KateBadPacketException();
            }
        } else {
            kb.type = KateBitmapType.kate_bitmap_type_paletted;
            kb.palette = Bitwise.read32v(opb);
            int npixels = kb.width * kb.height;
            if (npixels < 0) throw new KateBadPacketException();
            kb.pixels = new byte[npixels];
            for (int n = 0; n < npixels; ++n)
                kb.pixels[n] = (byte) opb.read(kb.bpp);
        }

        if (((bitstream_version_major << 8) | bitstream_version_minor) >= 0x0004) {
            Bitwise.read32v(opb);
            kb.x_offset = Bitwise.read32v(opb);
            kb.y_offset = Bitwise.read32v(opb);
        } else {
            kb.x_offset = 0;
            kb.y_offset = 0;
        }

        Bitwise.skipWarp(opb);
        return kb;
    }

    private int unpackBitmaps(Buffer opb) {
        int nbitmaps = Bitwise.read32v(opb);
        if (nbitmaps < 0) return Result.KATE_E_BAD_PACKET;
        bitmaps = new Bitmap[nbitmaps];
        for (int n = 0; n < nbitmaps; ++n) {
            try {
                bitmaps[n] = unpackBitmap(opb);
            } catch (KateException ke) {
                bitmaps = null;
                return Result.KATE_E_BAD_PACKET;
            }
        }

        Bitwise.skipWarp(opb);
        return checkEOP(opb);
    }

    private FontRange unpackFontRange(Buffer opb) {
        FontRange fr = new FontRange();
        fr.first_code_point = Bitwise.read32v(opb);
        fr.last_code_point = Bitwise.read32v(opb);
        fr.first_bitmap = Bitwise.read32v(opb);
        Bitwise.skipWarp(opb);
        return fr;
    }

    private int unpackFontMappings(Buffer opb) {
        int nranges = Bitwise.read32v(opb);
        if (nranges < 0)
            return Result.KATE_E_BAD_PACKET;
        if (nranges > 0) {
            font_ranges = new FontRange[nranges];
            for (int n = 0; n < nranges; ++n) {
                font_ranges[n] = unpackFontRange(opb);
            }
        }

        int nmappings = Bitwise.read32v(opb);
        if (nmappings < 0)
            return Result.KATE_E_BAD_PACKET;
        if (nmappings > 0) {
            font_mappings = new FontMapping[nmappings];
            for (int n = 0; n < nmappings; ++n) {
                font_mappings[n] = new FontMapping();
                nranges = Bitwise.read32v(opb);
                if (nranges < 0)
                    return Result.KATE_E_BAD_PACKET;
                if (nranges > 0) {
                    FontRange[] fr = new FontRange[nranges];
                    for (int l = 0; l < nranges; ++l) {
                        if (opb.read1() != 0) {
                            int idx = Bitwise.read32v(opb);
                            if (this.font_ranges == null || idx < 0 || idx >= this.font_ranges.length)
                                return Result.KATE_E_BAD_PACKET;
                            fr[l] = this.font_ranges[idx];
                        } else {
                            fr[l] = unpackFontRange(opb);
                        }
                    }
                    font_mappings[n].ranges = fr;
                } else {
                    font_mappings[n] = null;
                }
            }
        }

        Bitwise.skipWarp(opb);
        return checkEOP(opb);
    }

    public void clear() {
        num_headers = 0;
        regions = null;
        styles = null;
        curves = null;
        motions = null;
        palettes = null;
        bitmaps = null;
        font_ranges = null;
        font_mappings = null;
        probe = 0;
    }

    public int decodeHeader(Comment kc, Packet op) {
        Objects.requireNonNull(op, "Packet cannot be null");
        long ret;
        Buffer opb = new Buffer();

        opb.readinit(op.packet_base, op.packet, op.bytes);

        byte[] id = new byte[7];
        int typeflag = opb.read(8);

        /* header types have the MSB set */
        if ((typeflag & 0x80) == 0) {
            return Result.KATE_E_BAD_PACKET;
        }

        /* Kate header magic */
        Bitwise.readbuf(opb, id, 7);
        if (!"kate\0\0\0".equals(new String(id))) {
            return Result.KATE_E_NOT_KATE;
        }

        if (op.packetno < num_headers) {
            if (probe != op.packetno) return Result.KATE_E_BAD_PACKET;
        }

        /* reserved 0 byte */
        if (opb.read(8) != 0)
            return Result.KATE_E_BAD_PACKET;

        Debug.debug("decodeHeader: packet type " + typeflag + ", probe " + probe);

        switch (probe) {
            case 0:
                if (op.b_o_s == 0) {
                    return Result.KATE_E_BAD_PACKET;
                }
                ret = unpackInfo(opb);
                break;

            case 1:
                ret = unpackComment(kc, opb);
                break;

            case 2:
                ret = unpackRegions(opb);
                break;

            case 3:
                ret = unpackStyles(opb);
                break;

            case 4:
                ret = unpackCurves(opb);
                break;

            case 5:
                ret = unpackMotions(opb);
                break;

            case 6:
                ret = unpackPalettes(opb);
                break;

            case 7:
                ret = unpackBitmaps(opb);
                break;

            case 8:
                ret = unpackFontMappings(opb);
                if (ret == 0) {
                    Debug.debug("Found last known header, returning 1");
                    ret = 1;
                }
                break;

            default:
                ret = 0;
                break;
        }

        if (ret >= 0) {
            ++probe;
        }

        return (int) ret;
    }
}
