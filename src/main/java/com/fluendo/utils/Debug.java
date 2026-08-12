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

package com.fluendo.utils;

public final class Debug {

    public static final int NONE = 0;
    public static final int ERROR = 1;
    public static final int WARNING = 2;
    public static final int INFO = 3;
    public static final int DEBUG = 4;

    public static int level = INFO;

    private static int counter = 0;
    private static long startTime = 0;

    private static final String[] PREFIX = {
        "NONE",
        "ERRO",
        "WARN",
        "INFO",
        "DBUG"
    };

    private Debug() {
        // Utility class; prevent instantiation
    }

    public static synchronized int genId() {
        return counter++;
    }

    public static String rpad(String text, int length) {
        if (text == null) {
            text = "";
        }
        if (length > text.length()) {
            return String.format("%-" + length + "s", text);
        }
        return text;
    }

    public static void log(int logLevel, String line) {
        long currentTime = System.currentTimeMillis();
        synchronized (Debug.class) {
            if (startTime == 0) {
                startTime = currentTime;
            }
            currentTime -= startTime;
        }

        if (logLevel <= level) {
            if (level >= DEBUG) {
                System.out.println("[" + rpad(Thread.currentThread().getName(), 30) + " " 
                    + rpad(Long.toString(currentTime), 6) + " " + PREFIX[logLevel] + "] " + line);
            } else {
                System.out.println("[" + PREFIX[logLevel] + "] " + line);
            }
        }
    }

    public static void error(String line) { log(ERROR, line); }
    public static void warning(String line) { log(WARNING, line); }
    public static void warn(String line) { log(WARNING, line); }
    public static void info(String line) { log(INFO, line); }
    public static void debug(String line) { log(DEBUG, line); }
}
