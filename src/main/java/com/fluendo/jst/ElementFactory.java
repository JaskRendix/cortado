/* Copyright (C) <2004> Wim Taymans <wim@fluendo.com>
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
package com.fluendo.jst;

import com.fluendo.utils.Debug;
import java.util.ArrayList;
import java.util.List;

public class ElementFactory {

  private static final String[] PLUGINS = {
    "com.fluendo.plugin.HTTPSrc",
    "com.fluendo.plugin.VideoSink",
    "com.fluendo.plugin.AudioSinkJ2",
    "com.fluendo.plugin.Queue",
    "com.fluendo.plugin.FakeSink",
    "com.fluendo.plugin.Overlay",
    "com.fluendo.plugin.TextOverlay",
    "com.fluendo.plugin.KateOverlay",
    "com.fluendo.plugin.Selector",
    "com.fluendo.plugin.OggDemux",
    "com.fluendo.plugin.MultipartDemux",
    "com.fluendo.plugin.TheoraDec",
    "com.fluendo.plugin.VorbisDec",
    "com.fluendo.plugin.KateDec",
    "com.fluendo.plugin.JPEGDec",
    "com.fluendo.plugin.SmokeDec",
    "com.fluendo.plugin.MulawDec"
  };
  private static final List<Element> elements = new ArrayList<>();

  static {
    loadElements();
  }

  public static void loadElements() {
    elements.clear();

    try {
      for (String str : PLUGINS) {
        try {
          Class<?> cl = Class.forName(str);
          Debug.log(Debug.INFO, "registered plugin: " + str);
          Element pl = (Element) cl.getDeclaredConstructor().newInstance();
          elements.add(pl);
        } catch (Throwable t) {
            Debug.log(
                Debug.INFO,
                "Failed to register plugin: "
                  + str
                  + " ("
                  + t.getClass().getSimpleName()
                  + ": "
                  + t.getMessage()
                  + ")");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static Element dup(Element element, String name) {
    Element result = null;
    Class<?> cl = element.getClass();
    try {
      result = (Element) cl.getDeclaredConstructor().newInstance();
      if (result != null && name != null) {
        result.setName(name);
      }
      Debug.log(Debug.INFO, "create element: " + result);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  private static Element findTypeFind(byte[] data, int offset, int length) {
    int best = -1;
    Element result = null;

    for (Element element : elements) {
      int rank = element.typeFind(data, offset, length);
      if (rank > best) {
        best = rank;
        result = element;
      }
    }
    return result;
  }

  public static String typeFindMime(byte[] data, int offset, int length) {
      Element element = findTypeFind(data, offset, length);
      return element != null ? element.getMime() : null;
  }

  public static Element makeTypeFind(byte[] data, int offset, int length, String name) {
    Element element = findTypeFind(data, offset, length);
    return element != null ? dup(element, name) : null;
  }

  public static Element makeByMime(String mime, String name) {
    Element result = null;

    for (Element element : elements) {
      if (mime.equals(element.getMime())) {
        result = dup(element, name);
        break;
      }
    }
    return result;
  }

  public static Element makeByName(String name, String elemName) {
    Element result = null;

    for (Element element : elements) {
      if (name.equals(element.getFactoryName())) {
        result = dup(element, elemName);
        break;
      }
    }
    return result;
  }
}
