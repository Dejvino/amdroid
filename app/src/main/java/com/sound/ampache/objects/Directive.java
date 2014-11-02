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
import android.util.Log;

import com.sound.ampache.amdroid;
import com.sound.ampache.net.AmpacheApiAction;

import java.net.URLEncoder;

/**
 * Description:
 *
 * @author Dejvino
 * @since 2014-08-31
 */
public class Directive implements Parcelable
{
	public static String LOG_TAG = "Ampache_Amdroid_Directive";

	public final AmpacheApiAction action;
	public final String filter;
	public final String name;

	public Directive(AmpacheApiAction action, String filter, String name)
	{
		this.action = action;
		this.filter = filter;
		this.name = name;
	}

	private Directive(Parcel in)
	{
		String actionName = in.readString();
		this.action= AmpacheApiAction.valueOf(actionName);
		this.filter = in.readString();
		this.name = in.readString();
	}

	public String getFilterForUrl()
	{
		try {
			return URLEncoder.encode(filter, "UTF-8");
		} catch (Exception e) {
			Log.e(LOG_TAG, "Cannot URL-encode filter value '" + filter + "'. Error: " + e.getMessage(), e);
			amdroid.logger.logCritical("Cannot URL-encode filter value", "Filter value: " + filter + "\n"
					+ "Error Details: " + e.toString());
			return "";
		}
	}

	@Override
	public Directive clone() throws CloneNotSupportedException
	{
		return new Directive(this.action, this.filter, this.name);
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags)
	{
		parcel.writeString(this.action.name());
		parcel.writeString(this.filter);
		parcel.writeString(this.name);
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
