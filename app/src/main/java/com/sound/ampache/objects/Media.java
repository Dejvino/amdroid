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

import android.os.Parcel;
import android.os.Parcelable;

public class Media extends ampacheObject implements Parcelable
{
	public String size = "";
	public String url = "";
	public String genre = "";
	public String extra = null;

	public boolean hasChildren()
	{
		return false;
	}

	public String[] allChildren()
	{
		return null;
	}

	public String childString()
	{
		return "";
	}

	public String getSize()
	{
		return size;
	}

	public String getType()
	{
		if (getType() == "Song")
			return ((Song) this).getType();

		if (getType() == "Video")
			return ((Video) this).getType();

		return "";
	}

	public String extraString()
	{
		if (getType() == "Song")
			return ((Song) this).extraString();

		if (getType() == "Video")
			return ((Video) this).extraString();

		return "";
	}

	public String getLiveUrl(String authToken)
	{
		if (getType().equals("Song"))
			return ((Song) this).liveUrl(authToken);

		if (getType().equals("Video"))
			return ((Video) this).liveUrl(authToken);

		return "";
	}

	/* for parcelable*/
	public int describeContents()
	{
		return CONTENTS_FILE_DESCRIPTOR;
	}

	public void writeToParcel(Parcel out, int flags)
	{
		if (getType() == "Song") {
			((Song) this).writeToParcel(out, flags);
			return;
		}

		if (getType() == "Video") {
			((Video) this).writeToParcel(out, flags);
			return;
		}

		// Shouldn't happen, but a generic Media object
		parcelOut(out, flags);
	}

	protected void parcelOut(Parcel out, int flags)
	{
		out.writeString(size);
		out.writeString(url);
		out.writeString(genre);
		out.writeString(extra);
	}

	public Media(Parcel in)
	{
		readFromParcel(in);
	}

	public Media()
	{
	}

	public void readFromParcel(Parcel in)
	{
		if (getType() == "Song") {
			((Song) this).readFromParcel(in);
			return;
		}

		if (getType() == "Video") {
			((Video) this).readFromParcel(in);
			return;
		}

		// Shouldn't happen, but a generic Media object
		parcelIn(in);
	}

	protected void parcelIn(Parcel in)
	{
		size = in.readString();
		url = in.readString();
		genre = in.readString();
		extra = in.readString();
	}

	public static final Parcelable.Creator<Media> CREATOR
			= new Parcelable.Creator<Media>()
	{
		public Media createFromParcel(Parcel in)
		{
			return new Media(in);
		}

		public Media[] newArray(int size)
		{
			return new Media[size];
		}
	};
}

// ex:tabstop=4 shiftwidth=4 expandtab:

