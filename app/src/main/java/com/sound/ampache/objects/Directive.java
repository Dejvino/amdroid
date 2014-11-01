package com.sound.ampache.objects;

/* Copyright (c) 2014 David Hrdina Nemecek <dejvino@gmail.com>
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

/**
 * Description:
 *
 * @author Dejvino
 * @since 2014-08-31
 */
public class Directive implements Parcelable
{
	public String[] args;

	public Directive(String... args)
	{
		this.args = args;
	}

	private Directive(Parcel in)
	{
		args = new String[in.readInt()];
		in.readStringArray(args);
	}

	@Override
	public Directive clone() throws CloneNotSupportedException
	{
		return new Directive(args.clone());
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags)
	{
		parcel.writeInt(args.length);
		parcel.writeStringArray(args);
	}

	public static final Parcelable.Creator<Directive> CREATOR = new Parcelable.Creator<Directive>()
	{
		public Directive createFromParcel(Parcel in)
		{
			return new Directive(in);
		}

		public Directive[] newArray(int size)
		{
			return new Directive[size];
		}
	};
}
