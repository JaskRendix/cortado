/* Copyright (C) <2008> ogg.k.ogg.k <ogg.k.ogg.k@googlecode.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package com.fluendo.plugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.util.logging.Logger;

/** This class displays a simple text string on top of incoming video. */
public class TextOverlay extends Overlay {
  private static final Logger LOGGER = Logger.getLogger(TextOverlay.class.getName());

  private int fontSize = -1;
  private Font font = null;
  private String text = null;

  public TextOverlay() {
    super();
  }

  /** Display a text string (from a property) onto the image. */
  @Override
  protected void overlay(com.fluendo.jst.Buffer buf) {
    BufferedImage img;

    if (buf.object instanceof BufferedImage bufferedImage) {
      img = bufferedImage;
    } else if (buf.object instanceof ImageProducer imageProducer) {
      Image awtImg = component.createImage(imageProducer);
      if (awtImg instanceof BufferedImage bufferedImage) {
        img = bufferedImage;
      } else {
        img =
            new BufferedImage(
                component.getWidth() > 0 ? component.getWidth() : 320,
                component.getHeight() > 0 ? component.getHeight() : 240,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(awtImg, 0, 0, null);
        g2d.dispose();
      }
    } else if (buf.object instanceof Image awtImg) {
      if (awtImg instanceof BufferedImage bufferedImage) {
        img = bufferedImage;
      } else {
        img =
            new BufferedImage(
                awtImg.getWidth(null) > 0 ? awtImg.getWidth(null) : 320,
                awtImg.getHeight(null) > 0 ? awtImg.getHeight(null) : 240,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(awtImg, 0, 0, null);
        g2d.dispose();
      }
    } else {
      LOGGER.warning(() -> this + ": unknown buffer received " + buf);
      return;
    }

    Dimension d =
        component != null ? component.getSize() : new Dimension(img.getWidth(), img.getHeight());
    int w = d.width > 0 ? d.width : img.getWidth();
    int h = d.height > 0 ? d.height : img.getHeight();

    int newFontSize = w / 32;
    if (newFontSize < 12) {
      newFontSize = 12;
    }
    if (font == null || newFontSize != fontSize) {
      fontSize = newFontSize;
      font = new Font("SansSerif", Font.BOLD, fontSize);
    }

    Graphics2D g2d = img.createGraphics();

    if (text != null && !text.isEmpty()) {
      g2d.setFont(font);
      g2d.setColor(Color.WHITE);
      FontMetrics fm = g2d.getFontMetrics();
      double tw = fm.stringWidth(text);
      g2d.drawString(text, (int) ((w - tw) / 2), (int) (h * 0.85));
    }

    g2d.dispose();
    buf.object = img;
  }

  @Override
  public boolean setProperty(String name, java.lang.Object value) {
    if ("text".equals(name)) {
      text = value != null ? value.toString() : null;
    } else {
      return super.setProperty(name, value);
    }
    return true;
  }

  @Override
  public java.lang.Object getProperty(String name) {
    if ("text".equals(name)) {
      return text;
    } else {
      return super.getProperty(name);
    }
  }

  @Override
  public String getFactoryName() {
    return "textoverlay";
  }
}
