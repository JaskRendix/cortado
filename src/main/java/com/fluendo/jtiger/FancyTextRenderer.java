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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

public final class FancyTextRenderer implements TextRenderer {

  private static final int SHADOW_OFFSET = 1;

  @Override
  public void renderText(Graphics g, Rectangle region, Font font, String text) {
    if (g == null || region == null || font == null || text == null || text.isBlank()) {
      return;
    }

    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    AttributedString attributedText = new AttributedString(text, font.getAttributes());
    AttributedCharacterIterator characterIterator = attributedText.getIterator();
    int textEndIndex = characterIterator.getEndIndex();

    FontRenderContext fontRenderContext = g2d.getFontRenderContext();
    LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(characterIterator, fontRenderContext);

    float currentY = 0.0f;
    float maxLineWidth = Math.max(1.0f, region.width);

    while (lineMeasurer.getPosition() < textEndIndex) {
      TextLayout layout = lineMeasurer.nextLayout(maxLineWidth);
      currentY += layout.getAscent();

      float textWidth = layout.getAdvance();
      float textX = region.x + ((region.width - textWidth) / 2.0f);
      float renderY = region.y + currentY;

      // Draw black outline/shadow
      g2d.setColor(Color.BLACK);
      layout.draw(g2d, textX + SHADOW_OFFSET, renderY);
      layout.draw(g2d, textX - SHADOW_OFFSET, renderY);
      layout.draw(g2d, textX, renderY - SHADOW_OFFSET);
      layout.draw(g2d, textX, renderY + SHADOW_OFFSET);

      // Draw main white text
      g2d.setColor(Color.WHITE);
      layout.draw(g2d, textX, renderY);

      currentY += layout.getDescent() + layout.getLeading();
    }
  }
}
