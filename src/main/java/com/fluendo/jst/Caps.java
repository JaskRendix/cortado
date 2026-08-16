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

import java.util.HashMap;
import java.util.Map;

public class Caps {
  protected String mime;
  protected final Map<String, java.lang.Object> fields = new HashMap<>();

  public synchronized String getMime() {
    return mime;
  }

  public synchronized void setMime(String newMime) {
    mime = newMime;
  }

  public Caps(String mime) {
    super();
    int sep1, sep2, sep3;
    int len;

    len = mime.length();
    sep1 = 0;
    sep2 = mime.indexOf(';');
    if (sep2 == -1) sep2 = len;

    this.mime = mime.substring(0, sep2).trim();
    while (sep2 < len) {
      sep1 = sep2 + 1;
      sep2 = mime.indexOf('=', sep1);
      if (sep2 == -1) break;
      sep3 = mime.indexOf(';', sep2);
      if (sep3 == -1) sep3 = len;
      setField(mime.substring(sep1, sep2).trim(), mime.substring(sep2 + 1, sep3).trim());
      sep2 = sep3;
    }
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();

    buf.append("Caps: ").append(mime).append("\n");
    for (Map.Entry<String, java.lang.Object> entry : fields.entrySet()) {
      buf.append(" \"")
          .append(entry.getKey())
          .append("\": \"")
          .append(entry.getValue())
          .append("\"\n");
    }
    return buf.toString();
  }

  public synchronized void setField(String key, java.lang.Object value) {
    fields.put(key, value);
  }

  public synchronized void setFieldInt(String key, int value) {
    fields.put(key, Integer.valueOf(value));
  }

  public synchronized java.lang.Object getField(String key) {
    return fields.get(key);
  }

  public synchronized int getFieldInt(String key, int def) {
    java.lang.Object obj = fields.get(key);
    if (obj instanceof Integer integer) {
      return integer.intValue();
    } else if (obj instanceof String str) {
      try {
        return Integer.parseInt(str);
      } catch (NumberFormatException e) {
        return def;
      }
    }
    return def;
  }

  public synchronized String getFieldString(String key, String def) {
    java.lang.Object obj = fields.get(key);
    if (obj != null) {
      return obj.toString();
    }
    return def;
  }
}
