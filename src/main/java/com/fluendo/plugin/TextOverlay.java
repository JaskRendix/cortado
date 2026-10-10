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
  private Font font;
  private String text;

  private BufferedImage toBufferedImage(Image image, int width, int height) {
    if (image instanceof BufferedImage bufferedImage) {
      return bufferedImage;
    }

    BufferedImage bufferedImage =
        new BufferedImage(
            width > 0 ? width : 320,
            height > 0 ? height : 240,
            BufferedImage.TYPE_INT_RGB);

    Graphics2D g2d = bufferedImage.createGraphics();
    g2d.drawImage(image, 0, 0, null);
    g2d.dispose();

    return bufferedImage;
  }

  private void updateFont(int width) {
    int newFontSize = Math.max(width / 32, 12);

    if (font == null || newFontSize != fontSize) {
      fontSize = newFontSize;
      font = new Font("SansSerif", Font.BOLD, fontSize);
    }
  }

  private void drawText(Graphics2D g2d, int width, int height) {
    if (text == null || text.isEmpty()) {
      return;
    }

    g2d.setFont(font);
    g2d.setColor(Color.WHITE);

    FontMetrics fm = g2d.getFontMetrics();
    double textWidth = fm.stringWidth(text);

    g2d.drawString(
        text,
        (int) ((width - textWidth) / 2),
        (int) (height * 0.85));
  }

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
      Image image = component.createImage(imageProducer);

      img =
          toBufferedImage(
              image,
              component.getWidth(),
              component.getHeight());

    } else if (buf.object instanceof Image image) {

      img =
          toBufferedImage(
              image,
              image.getWidth(null),
              image.getHeight(null));

    } else {
      LOGGER.warning(() -> this + ": unknown buffer received " + buf);
      return;
    }

    Dimension d =
        component != null ? component.getSize() : new Dimension(img.getWidth(), img.getHeight());
    int w = d.width > 0 ? d.width : img.getWidth();
    int h = d.height > 0 ? d.height : img.getHeight();

    updateFont(w);

    Graphics2D g2d = img.createGraphics();

    drawText(g2d, w, h);

    g2d.dispose();
    buf.object = img;
  }

  @Override
  public boolean setProperty(String name, java.lang.Object value) {
    switch (name) {
      case "text" -> text = value != null ? value.toString() : null;
      default -> {
        return super.setProperty(name, value);
      }
    }
    return true;
  }

  @Override
  public java.lang.Object getProperty(String name) {
    return switch (name) {
      case "text" -> text;
      default -> super.getProperty(name);
    };
  }

  @Override
  public String getFactoryName() {
    return "textoverlay";
  }
}
