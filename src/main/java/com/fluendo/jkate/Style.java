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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.fluendo.jkate;

import java.util.Objects;

/**
 * A style definition.
 */
public class Style {
    public double halign;
    public double valign;

    public Color text_color;
    public Color background_color;
    public Color draw_color;

    public KateSpaceMetric font_metric;
    public double font_width;
    public double font_height;

    public KateSpaceMetric margin_metric;
    public double left_margin;
    public double top_margin;
    public double right_margin;
    public double bottom_margin;

    public boolean bold;
    public boolean italics;
    public boolean underline;
    public boolean strike;
    public boolean justify;
    public KateWrapMode wrap_mode;

    public String font;

    public Style() {
        this.halign = 0.0;
        this.valign = 0.0;
        this.text_color = null;
        this.background_color = null;
        this.draw_color = null;
        this.font_metric = null;
        this.font_width = 0.0;
        this.font_height = 0.0;
        this.margin_metric = null;
        this.left_margin = 0.0;
        this.top_margin = 0.0;
        this.right_margin = 0.0;
        this.bottom_margin = 0.0;
        this.bold = false;
        this.italics = false;
        this.underline = false;
        this.strike = false;
        this.justify = false;
        this.wrap_mode = null;
        this.font = null;
    }

    public Style(double halign, double valign, Color text_color, Color background_color, Color draw_color,
                 KateSpaceMetric font_metric, double font_width, double font_height,
                 KateSpaceMetric margin_metric, double left_margin, double top_margin,
                 double right_margin, double bottom_margin, boolean bold, boolean italics,
                 boolean underline, boolean strike, boolean justify, KateWrapMode wrap_mode, String font) {
        this.halign = halign;
        this.valign = valign;
        this.text_color = text_color;
        this.background_color = background_color;
        this.draw_color = draw_color;
        this.font_metric = font_metric;
        this.font_width = font_width;
        this.font_height = font_height;
        this.margin_metric = margin_metric;
        this.left_margin = left_margin;
        this.top_margin = top_margin;
        this.right_margin = right_margin;
        this.bottom_margin = bottom_margin;
        this.bold = bold;
        this.italics = italics;
        this.underline = underline;
        this.strike = strike;
        this.justify = justify;
        this.wrap_mode = wrap_mode;
        this.font = font;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Style style = (Style) o;
        return Double.compare(style.halign, halign) == 0 &&
                Double.compare(style.valign, valign) == 0 &&
                Double.compare(style.font_width, font_width) == 0 &&
                Double.compare(style.font_height, font_height) == 0 &&
                Double.compare(style.left_margin, left_margin) == 0 &&
                Double.compare(style.top_margin, top_margin) == 0 &&
                Double.compare(style.right_margin, right_margin) == 0 &&
                Double.compare(style.bottom_margin, bottom_margin) == 0 &&
                bold == style.bold &&
                italics == style.italics &&
                underline == style.underline &&
                strike == style.strike &&
                justify == style.justify &&
                Objects.equals(text_color, style.text_color) &&
                Objects.equals(background_color, style.background_color) &&
                Objects.equals(draw_color, style.draw_color) &&
                font_metric == style.font_metric &&
                margin_metric == style.margin_metric &&
                wrap_mode == style.wrap_mode &&
                Objects.equals(font, style.font);
    }

    @Override
    public int hashCode() {
        return Objects.hash(halign, valign, text_color, background_color, draw_color,
                font_metric, font_width, font_height, margin_metric, left_margin,
                top_margin, right_margin, bottom_margin, bold, italics, underline,
                strike, justify, wrap_mode, font);
    }

    @Override
    public String toString() {
        return "Style{" +
                "halign=" + halign +
                ", valign=" + valign +
                ", font='" + font + '\'' +
                ", bold=" + bold +
                ", italics=" + italics +
                '}';
    }
}
