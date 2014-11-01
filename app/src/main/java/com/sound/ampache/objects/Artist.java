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

import android.os.Parcel;
import android.os.Parcelable;

public class Artist extends ampacheObject
{
	public String albums = "";

	public boolean hasChildren()
	{
		return true;
	}

	public String extraString()
	{
		return albums;
	}

	public String getType()
	{
		return "Artist";
	}

	public String childString()
	{
		return "artist_albums";
	}

	public String[] allChildren()
	{
		String[] dir = {"artist_songs", this.id};
		return dir;
	}

	public Artist()
	{
	}

	public void writeToParcel(Parcel out, int flags)
	{
		super.writeToParcel(out, flags);
		out.writeString(albums);
	}

	public Artist(Parcel in)
	{
		super.readFromParcel(in);
		albums = in.readString();
	}

	public static final Parcelable.Creator<Artist> CREATOR
			= new Parcelable.Creator<Artist>()
	{
		public Artist createFromParcel(Parcel in)
		{
			return new Artist(in);
		}

		public Artist[] newArray(int size)
		{
			return new Artist[size];
		}
	};
}

