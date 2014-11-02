package com.sound.ampache.objects;

/* Copyright (c) 2009 Kevin James Purdy <purdyk@onid.orst.edu>
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

public class Tag extends ampacheObject
{
	public String artists = "";
	public String albums = "";
	public String extra = null;

	public String getType()
	{
		return "Tag";
	}

	public String extraString()
	{
		if (extra == null) {
			extra = artists + " artists - " + albums + " albums";
		}
		return extra;
	}

	public boolean hasChildren()
	{
		return true;
	}

	public Directive getAllChildrenDirective()
	{
		return new Directive(AmpacheApiAction.TAG_SONGS, this.id, "Tag songs");
	}

	public AmpacheApiAction childAction()
	{
		return AmpacheApiAction.TAG_ARTISTS;
	}

	public Tag()
	{
	}

	public Tag(Parcel in)
	{
		super.readFromParcel(in);
	}

	public static final Parcelable.Creator<Tag> CREATOR
			= new Parcelable.Creator<Tag>()
	{
		public Tag createFromParcel(Parcel in)
		{
			return new Tag(in);
		}

		public Tag[] newArray(int size)
		{
			return new Tag[size];
		}
	};
}
