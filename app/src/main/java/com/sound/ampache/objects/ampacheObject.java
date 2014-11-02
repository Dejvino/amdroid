package com.sound.ampache.objects;

/* Copyright (c) 2008 Kevin James Purdy <purdyk@onid.orst.edu>
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

public abstract class ampacheObject implements Parcelable
{
	public String id = "";
	public String name = "";

	public String getId()
	{
		return id;
	}

	public String toString()
	{
		return name;
	}

	abstract public String extraString();

	abstract public String getType();

	abstract public AmpacheApiAction childAction();

	abstract public Directive getAllChildrenDirective();

	abstract public boolean hasChildren();

	/* for parcelable*/
	public int describeContents()
	{
		return CONTENTS_FILE_DESCRIPTOR;
	}

	public void writeToParcel(Parcel out, int flags)
	{
		out.writeString(id);
		out.writeString(name);
	}

	public void readFromParcel(Parcel in)
	{
		id = in.readString();
		name = in.readString();
	}
}
