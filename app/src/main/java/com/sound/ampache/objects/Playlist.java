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

import com.sound.ampache.net.AmpacheApiAction;

public class Playlist extends ampacheObject
{
	public String owner = "";
	public String count = "";
	public String extra = null;

	public String getType()
	{
		return "Playlist";
	}

	public String extraString()
	{
		if (extra == null) {
			extra = owner + " - " + count;
		}
		return extra;
	}

	public boolean hasChildren()
	{
		return true;
	}

	public AmpacheApiAction childAction()
	{
		return AmpacheApiAction.PLAYLIST_SONGS;
	}

	public Directive getAllChildrenDirective()
	{
		return new Directive(AmpacheApiAction.PLAYLIST_SONGS, this.id, "Album songs");
	}

	public Playlist()
	{
	}

	public void writeToParcel(Parcel out, int flags)
	{
		super.writeToParcel(out, flags);
		out.writeString(owner);
		out.writeString(count);
		out.writeString(extra);
	}

	public Playlist(Parcel in)
	{
		super.readFromParcel(in);
		owner = in.readString();
		count = in.readString();
		extra = in.readString();
	}

	public static final Parcelable.Creator<Playlist> CREATOR
			= new Parcelable.Creator<Playlist>()
	{
		public Playlist createFromParcel(Parcel in)
		{
			return new Playlist(in);
		}

		public Playlist[] newArray(int size)
		{
			return new Playlist[size];
		}
	};
}
