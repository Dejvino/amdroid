package com.sound.ampache.objects;

/* Copyright (c) 2008 Kevin James Purdy <purdyk@onid.orst.edu>
 * Copyright (c) 2010 Jacob Alexander   < haata@users.sf.net >
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

public class Song extends Media implements Externalizable {
    public String album = "";
    public String art = "";
    public String artist = "";
    public String time = "";

    public String getType() {
        return "Song";
    }

    public String extraString() {
        if (extra == null) {
            extra = artist + " - " + album;
        }

        return extra;
    }

    public String childString() {
        return "";
    }

    /* Replace the old session id with our current one */
    public String liveUrl() {
        return url.replaceAll("sid=[^&]+","sid=" + com.sound.ampache.amdroid.comm.authToken).replaceFirst(".ogg$", ".mp3");
    }

    /* Replace old session id, to use with the Album Art */
    public String liveArt() {
        return art.replaceAll("auth=[^&]+","auth=" + com.sound.ampache.amdroid.comm.authToken);
    }
    
    public boolean hasChildren() {
        return false;
    }

    public String[] allChildren() {
        return null;
    }

    public Song() {
    }

    public void writeToParcel(Parcel out, int flags) {
        super.parcelOut(out, flags);
        out.writeString(artist);
        out.writeString(art);
        out.writeString(url);
        out.writeString(album);
        out.writeString(time);
    }

    public Song( Parcel in ) {
        readFromParcel( in );
    }

    public void readFromParcel( Parcel in ) {
        super.parcelIn(in);
        artist = in.readString();
        art = in.readString();
        url = in.readString();
        album = in.readString();
        time = in.readString();
    }

    public static final Parcelable.Creator<Song> CREATOR
        = new Parcelable.Creator<Song>() {
                public Song createFromParcel(Parcel in) {
                    return new Song(in);
                }

                public Song[] newArray(int size) {
                    return new Song[size];
                }
            };

    /* for external */

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = (String) in.readObject();
        name = (String) in.readObject();
        artist = (String) in.readObject();
        art = (String) in.readObject();
        url = (String) in.readObject();
        album = (String) in.readObject();
        genre = (String) in.readObject();
        time = (String) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(id);
        out.writeObject(name);
        out.writeObject(artist);
        out.writeObject(art);
        out.writeObject(url);
        out.writeObject(album);
        out.writeObject(genre);
        out.writeObject(time);
    }

}

// ex:tabstop=4 shiftwidth=4 expandtab:

