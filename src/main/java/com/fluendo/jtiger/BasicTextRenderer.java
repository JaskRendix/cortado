/* JTiger
 * Copyright (C) 2008 ogg.k.ogg.k <ogg.k.ogg.k@googlemail.com>
 *
 * Parts of JTiger are based on code by Wim Taymans <wim@fluendo.com>
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

package com.fluendo.jtiger;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

public final class BasicTextRenderer implements TextRenderer {

    private static final int SHADOW_OFFSET = 1;

    @Override
    public void renderText(Graphics g, Rectangle region, Font font, String text) {
        if (g == null || region == null || font == null || text == null || text.isBlank()) {
            return;
        }

        g.setFont(font);
        FontMetrics fm = g.getFontMetrics(font);
        
        int textWidth = fm.stringWidth(text);
        int textX = region.x + (region.width - textWidth) / 2;
        int textY = region.y + fm.getAscent();

        // Draw black outline/shadow
        g.setColor(Color.BLACK);
        g.drawString(text, textX + SHADOW_OFFSET, textY);
        g.drawString(text, textX - SHADOW_OFFSET, textY);
        g.drawString(text, textX, textY - SHADOW_OFFSET);
        g.drawString(text, textX, textY + SHADOW_OFFSET);

        // Draw main white text
        g.setColor(Color.WHITE);
        g.drawString(text, textX, textY);
    }
}
