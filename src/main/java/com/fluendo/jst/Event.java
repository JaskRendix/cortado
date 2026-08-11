/* Cortado - a video player java applet
 * Copyright (C) 2004 Fluendo S.L.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Street #330, Boston, MA 02111-1307, USA.
 */

package com.fluendo.jst;

public final class Event {

    public enum Type {
        FLUSH_START,
        FLUSH_STOP,
        EOS,
        NEWSEGMENT,
        SEEK
    }

    private final Type type;
    private int format;
    private boolean update;
    private long start;
    private long stop;
    private long position = -1;

    private Event(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return switch (type) {
            case SEEK ->
                "[Event] type: " + type +
                ", format: " + format +
                ", position: " + position;

            case NEWSEGMENT ->
                "[Event] type: " + type +
                (update ? ", update" : ", non-update") +
                ", format: " + format +
                ", start: " + start +
                ", stop: " + stop +
                ", position: " + position;

            default ->
                "[Event] type: " + type;
        };
    }

    public Type getType() {
        return type;
    }

    public static Event newEOS() {
        return new Event(Type.EOS);
    }

    public static Event newFlushStart() {
        return new Event(Type.FLUSH_START);
    }

    public static Event newFlushStop() {
        return new Event(Type.FLUSH_STOP);
    }

    public static Event newSeek(int format, long position) {
        Event e = new Event(Type.SEEK);
        e.format = format;
        e.position = position;
        return e;
    }

    public long parseSeekPosition() {
        return position;
    }

    public int parseSeekFormat() {
        return format;
    }

    public static Event newNewsegment(boolean update, int format, long start, long stop, long position) {
        Event e = new Event(Type.NEWSEGMENT);
        e.update = update;
        e.format = format;
        e.start = start;
        e.stop = stop;
        e.position = position;
        return e;
    }

    public boolean parseNewsegmentUpdate() {
        return update;
    }

    public int parseNewsegmentFormat() {
        return format;
    }

    public long parseNewsegmentStart() {
        return start;
    }

    public long parseNewsegmentStop() {
        return stop;
    }

    public long parseNewsegmentPosition() {
        return position;
    }
}
