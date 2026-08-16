/* Copyright (C) <2008> ogg.k.ogg.k <ogg.k.ogg.k@googlemail.com>
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
import com.fluendo.utils.Debug;
import java.util.ArrayList;
import java.util.List;

/** This element receives data from N sinks, and selects one of them to send from its source. */
public class Selector extends Element {
  private final List<Pad> sinks = new ArrayList<>();
  private int selected = -1;
  private Pad selectedPad = null;

  private final Pad srcPad =
      new Pad(Pad.SRC, "src") {
        /** Pushes the event to every sink. */
        @Override
        protected boolean eventFunc(Event event) {
          boolean ret = true;
          for (Pad sink : sinks) {
            ret &= sink.pushEvent(event);
          }
          return ret;
        }
      };

  /**
   * Requests a new sink pad to be created for the given peer. The caps do not matter, as Selector
   * is a caps agnostic element.
   */
  public Pad requestSinkPad(Pad peer) {
    Pad pad =
        new Pad(Pad.SINK, "sink" + sinks.size()) {
          @Override
          protected boolean eventFunc(Event event) {
            if (selectedPad == this) {
              return srcPad.pushEvent(event);
            }
            return true;
          }

          @Override
          protected int chainFunc(Buffer buf) {
            int result = Pad.OK;

            Debug.debug("Selector got " + buf.caps + " buffer on " + this.toString());

            if (selectedPad == this) {
              Debug.debug("what a coincidence, we're selected - pushing");
              result = srcPad.push(buf);
            }

            return result;
          }

          @Override
          protected boolean activateFunc(int mode) {
            return true;
          }
        };

    sinks.add(pad);
    addPad(pad);
    return pad;
  }

  public Selector() {
    super();
    addPad(srcPad);
  }

  /** The selected sink may be selected via the "selected" property - negative to select nothing */
  @Override
  public boolean setProperty(String name, java.lang.Object value) {
    switch (name) {
      case "selected" -> {
        int newSelected = Integer.parseInt(value.toString());
        Debug.info(
            "Selector: request to select "
                + newSelected
                + " (from "
                + selected
                + "), within 0-"
                + (sinks.size() - 1));

        if (newSelected != selected) {
          srcPad.pushEvent(Event.newFlushStart());
          if (newSelected < 0 || newSelected >= sinks.size()) {
            selected = -1;
            selectedPad = null;
          } else {
            selected = newSelected;
            selectedPad = sinks.get(selected);
          }
          srcPad.pushEvent(Event.newFlushStop());
        }
      }
      default -> {
        return super.setProperty(name, value);
      }
    }

    return true;
  }

  @Override
  public java.lang.Object getProperty(String name) {
    return switch (name) {
      case "selected" -> Integer.valueOf(selected);
      default -> super.getProperty(name);
    };
  }

  @Override
  public String getFactoryName() {
    return "selector";
  }
}
