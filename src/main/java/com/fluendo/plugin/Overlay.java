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

import com.fluendo.jst.Buffer;
import com.fluendo.jst.Element;
import com.fluendo.jst.Event;
import com.fluendo.jst.Pad;
import java.awt.Component;
import java.awt.Frame;
import java.util.logging.Logger;

/**
 * This is a base overlay element, just passes images from sink to source. Extend this and override
 * the overlay function to draw something onto images as they go from sink to source.
 */
public class Overlay extends Element {
  private static final Logger LOGGER = Logger.getLogger(Overlay.class.getName());

  protected Component component;

  private final Pad videoSrcPad =
      new Pad(Pad.SRC, "videosrc") {
        @Override
        protected boolean eventFunc(Event event) {
          return videoSinkPad.pushEvent(event);
        }
      };

  private final Pad videoSinkPad =
      new Pad(Pad.SINK, "videosink") {
        @Override
        protected boolean eventFunc(Event event) {
          return videoSrcPad.pushEvent(event);
        }

        /**
         * Receives an image, allows a derived class to overlay whatever it wants on it, and sends
         * it to the video source pad.
         */
        @Override
        protected int chainFunc(Buffer buf) {
          int result;

          LOGGER.fine(() -> (parent != null ? parent.getName() : "Overlay") + " <<< " + buf);

          overlay(buf);

          result = videoSrcPad.push(buf);
          if (result != Pad.OK) {
            LOGGER.warning(
                () ->
                    (parent != null ? parent.getName() : "Overlay")
                        + ": failed to push buffer to video source pad: "
                        + result);
          }

          return result;
        }

        @Override
        protected boolean activateFunc(int mode) {
          return true;
        }
      };

  public Overlay() {
    super();
    addPad(videoSinkPad);
    addPad(videoSrcPad);
  }

  /**
   * This function may be overridden to draw whatever the derived class wants onto the incoming
   * image. By default, the image is passed without alteration.
   */
  protected void overlay(Buffer buf) {
    // straight pass through by default
  }

  @Override
  public boolean setProperty(String name, java.lang.Object value) {
    switch (name) {
      case "component" -> component = (Component) value;
      default -> {
        return super.setProperty(name, value);
      }
    }
    return true;
  }

  @Override
  public java.lang.Object getProperty(String name) {
    return switch (name) {
      case "component" -> component;
      default -> super.getProperty(name);
    };
  }

  @Override
  protected int changeState(int transition) {
    if (currentState == STOP && pendingState == PAUSE && component == null) {
      component = new Frame();
    }
    return super.changeState(transition);
  }

  @Override
  public String getFactoryName() {
    return "overlay";
  }
}
