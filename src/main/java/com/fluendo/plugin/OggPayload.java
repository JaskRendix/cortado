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

package com.fluendo.plugin;

import java.util.*;
import com.jcraft.jogg.*;

public interface OggPayload {
    /**
     * Check if the packet contains the signature
     * of the payload.
     */
    boolean isType(Packet op);

    /**
     * Initialize the payload with a header packet.
     * Returns < 0 for error, 0 if OK, 1 if OK and ready for decoding data.
     */
    int takeHeader(Packet op);

    /**
     * Check if the packet contains a header packet
     */
    boolean isHeader(Packet op);

    /**
     * Check if the packet contains a keyframe
     */
    boolean isKeyFrame(Packet op);

    /**
     * Get the first timestamp of the list of packets
     */
    long getFirstTs(Vector<Packet> packets);

    /**
     * Convert the granule pos to a timestamp
     */
    long granuleToTime(long gp);

    /**
     * Get mime type
     */
    String getMime();

    /**
     * Get mime type from the given packet
     */
    String getMime(Packet op);

    /**
     * Check if the stream is discontinuous (eg, no need to wait
     * for data on this stream before playing)
     */
    boolean isDiscontinuous();
}
