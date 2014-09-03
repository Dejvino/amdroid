package com.sound.ampache.objects;

/* Copyright (c) 2010 Jacob Alexander < haata@users.sf.net >
 *
 * +------------------------------------------------------------------------+
 * | This program is free software; you can redistribute it and/or          |
 * | modify it under the terms of the GNU General Public License            |
 * | as published by the Free Software Foundation; either version 2         |
 * | of the License, or (at your option) any later version.                 |
 * |                                                                        |
 * | This program is distributed in the hope that it will be useful,        |
 * | but WITHOUT ANY WARRANTY; without even the implied warranty of         |
 * | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          |
 * | GNU General Public License for more details.                           |
 * |                                                                        |
 * | You should have received a copy of the GNU General Public License      |
 * | along with this program; if not, write to the Free Software            |
 * | Foundation, Inc., 59 Temple Place - Suite 330,                         |
 * | Boston, MA  02111-1307, USA.                                           |
 * +------------------------------------------------------------------------+
 */

import android.os.Parcelable;
import android.os.Parcel;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.lang.ClassNotFoundException;

public class Video extends Media implements Externalizable {
    public String mime = "";
    public String resolution = "";

    public String getType() {
        return "Video";
    }

    public String extraString() {
        if ( extra == null ) {
            extra = mime + " - " + resolution;
        }

        return extra;
    }

    public String childString() {
        return "";
    }

    /* Replace the old session id with our current one */
    public String liveUrl(String authToken) {
        return url.replaceAll("sid=[^&]+","sid=" + authToken);
    }

    public boolean hasChildren() {
        return false;
    }

    public String[] allChildren() {
        return null;
    }

    public Video() {
    }

    public void writeToParcel(Parcel out, int flags) {
        super.parcelOut(out, flags);
        out.writeString(resolution);
        out.writeString(mime);
        out.writeString(genre);
    }

    public Video(Parcel in) {
        readFromParcel( in );
    }

    public void readFromParcel( Parcel in ) {
        super.parcelIn(in);
        resolution = in.readString();
        mime = in.readString();
        genre = in.readString();
    }

    public static final Parcelable.Creator<Video> CREATOR
        = new Parcelable.Creator<Video>() {
                public Video createFromParcel(Parcel in) {
                    return new Video(in);
                }

                public Video[] newArray(int size) {
                    return new Video[size];
                }
            };

    /* for external */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = (String) in.readObject();
        name = (String) in.readObject();
        url = (String) in.readObject();
        genre = (String) in.readObject();
        size = (String) in.readObject();
        resolution = (String) in.readObject();
        mime = (String) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(id);
        out.writeObject(name);
        out.writeObject(url);
        out.writeObject(genre);
        out.writeObject(size);
        out.writeObject(resolution);
        out.writeObject(mime);
    }

}

// ex:tabstop=4 shiftwidth=4 expandtab:

