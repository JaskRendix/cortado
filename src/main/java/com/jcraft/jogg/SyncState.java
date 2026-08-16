/* JOrbis
* Copyright (C) 2000 ymnk, JCraft,Inc.
*
* Written by: 2000 ymnk<ymnk@jcaft.com>
*
* Many thanks to
*  Monty <monty@xiph.org> and
*  The XIPHOPHORUS Company http://www.xiph.org/ .
* JOrbis has been based on their awesome works, Vorbis codec.
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Library General Public License
* as published by the Free Software Foundation; either version 2 of
* the License, or (at your option) any later version.

* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Library General Public License for more details.
*
* You should have received a copy of the GNU Library General Public
* License along with this program; if not, write to the Free Software
* Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package com.jcraft.jogg;

public class SyncState {

  public byte[] data;
  int storage;
  int fill;
  int returned;

  int unsynced;
  int headerbytes;
  int bodybytes;

  public int clear() {
    data = null;
    return 0;
  }

  public int buffer(int size) {
    if (returned != 0) {
      fill -= returned;
      if (fill > 0) {
        System.arraycopy(data, returned, data, 0, fill);
      }
      returned = 0;
    }

    if (size > storage - fill) {
      int newsize = size + fill + 4096;
      if (data != null) {
        byte[] foo = new byte[newsize];
        System.arraycopy(data, 0, foo, 0, data.length);
        data = foo;
      } else {
        data = new byte[newsize];
      }
      storage = newsize;
    }

    return fill;
  }

  public int wrote(int bytes) {
    if (fill + bytes > storage) return -1;
    fill += bytes;
    return 0;
  }

  private final Page pageseek = new Page();
  private final byte[] chksum = new byte[4];

  public int pageseek(Page og) {
    int page = returned;
    int next;
    int bytes = fill - returned;

    if (headerbytes == 0) {
      int _headerbytes, i;
      if (bytes < 27) return 0;

      if (data[page] != 'O'
          || data[page + 1] != 'g'
          || data[page + 2] != 'g'
          || data[page + 3] != 'S') {
        headerbytes = 0;
        bodybytes = 0;

        next = 0;
        for (int ii = 0; ii < bytes - 1; ii++) {
          if (data[page + 1 + ii] == 'O') {
            next = page + 1 + ii;
            break;
          }
        }
        if (next == 0) next = fill;

        returned = next;
        return -(next - page);
      }
      _headerbytes = (data[page + 26] & 0xff) + 27;
      if (bytes < _headerbytes) return 0;

      for (i = 0; i < (data[page + 26] & 0xff); i++) {
        bodybytes += (data[page + 27 + i] & 0xff);
      }
      headerbytes = _headerbytes;
    }

    if (bodybytes + headerbytes > bytes) return 0;

    synchronized (chksum) {
      System.arraycopy(data, page + 22, chksum, 0, 4);
      data[page + 22] = 0;
      data[page + 23] = 0;
      data[page + 24] = 0;
      data[page + 25] = 0;

      Page log = pageseek;
      log.header_base = data;
      log.header = page;
      log.header_len = headerbytes;

      log.body_base = data;
      log.body = page + headerbytes;
      log.body_len = bodybytes;
      log.checksum();

      if (chksum[0] != data[page + 22]
          || chksum[1] != data[page + 23]
          || chksum[2] != data[page + 24]
          || chksum[3] != data[page + 25]) {
        System.arraycopy(chksum, 0, data, page + 22, 4);

        headerbytes = 0;
        bodybytes = 0;
        next = 0;
        for (int ii = 0; ii < bytes - 1; ii++) {
          if (data[page + 1 + ii] == 'O') {
            next = page + 1 + ii;
            break;
          }
        }
        if (next == 0) next = fill;
        returned = next;
        return -(next - page);
      }
    }

    page = returned;

    if (og != null) {
      og.header_base = data;
      og.header = page;
      og.header_len = headerbytes;
      og.body_base = data;
      og.body = page + headerbytes;
      og.body_len = bodybytes;
    }

    unsynced = 0;
    returned += (bytes = headerbytes + bodybytes);
    headerbytes = 0;
    bodybytes = 0;
    return bytes;
  }

  public int pageout(Page og) {
    while (true) {
      int ret = pageseek(og);
      if (ret > 0) {
        return 1;
      }
      if (ret == 0) {
        return 0;
      }

      if (unsynced == 0) {
        unsynced = 1;
        return -1;
      }
    }
  }

  public int reset() {
    fill = 0;
    returned = 0;
    unsynced = 0;
    headerbytes = 0;
    bodybytes = 0;
    return 0;
  }

  public void init() {}
}
