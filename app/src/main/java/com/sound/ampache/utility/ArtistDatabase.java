package com.sound.ampache.utility;

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


import android.content.Context;
import android.util.Log;

import com.sound.ampache.objects.Artist;

import java.util.ArrayList;
import java.util.Date;

public class ArtistDatabase extends Database {

	// Constructors **************************************************

	ArtistDatabase( Context context, String databaseName ) {
		super( context );

		type = "Artist";

		DATABASE_NAME = databaseName;
		TABLE_NAME = "ArtistCache";

		COLUMN_NAMES.add( "albums" );

		completeTableCreateParam();
	}


	// Functions *****************************************************

	public boolean addEntry( String id, String artistName, String numberOfAlbums ) {
		// Get the current time, and convert it to a string
		String time = Long.toString( ( new Date() ).getTime() / 1000 );

		// Build Insert Query
		String queryString = "INSERT INTO " + TABLE_NAME + " " + columnList() + " VALUES (" +
				id + ", '" +
				artistName + "', '" +
				numberOfAlbums + "', '" +
				time + ");";

		try {
			db.execSQL( queryString );
		}
		catch ( Exception exc ) {
			// Damn...
			Log.e( "Amdroid", "Database/Artist - Insertion Failure... " + queryString );
			return false;
		}

		return true;
	}

	public boolean addEntry( Artist entry ) {
		return addEntry( entry.id, entry.name, entry.albums );
	}

	public boolean addEntries( ArrayList<Artist> list ) {
		for ( int c = 0; c < list.size(); c++ )
			if ( !addEntry( list.get( c ) ) )
				return false;

		return true;
	}
}

