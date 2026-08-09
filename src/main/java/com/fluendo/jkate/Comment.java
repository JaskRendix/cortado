/* JKate
 * Copyright (C) 2008 ogg.k.ogg.k <ogg.k.ogg.k@googlemail.com>
 *
 * Parts of JKate are based on code by Wim Taymans <wim@fluendo.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.fluendo.jkate;

import java.util.Arrays;
import java.util.Objects;

/**
 * Vorbis comments are used in Kate streams too.
 */
public class Comment {
    public String[] user_comments;
    public String vendor;

    public Comment() {
    }

    public Comment(String vendor, String[] user_comments) {
        this.vendor = vendor;
        this.user_comments = user_comments;
    }

    public String[] getUserComments() {
        return user_comments;
    }

    public void setUserComments(String[] user_comments) {
        this.user_comments = user_comments;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public void clear() {
        user_comments = null;
        vendor = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(vendor, comment.vendor) &&
                Arrays.equals(user_comments, comment.user_comments);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(vendor);
        result = 31 * result + Arrays.hashCode(user_comments);
        return result;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "vendor='" + vendor + '\'' +
                ", user_comments=" + Arrays.toString(user_comments) +
                '}';
    }
}
