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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class Database
{

	public String type = "Invalid";

	protected String DATABASE_NAME = "unnamed_database.db";
	protected int DATABASE_VERSION = 1;
	protected String TABLE_NAME = "unnamed_table";

	protected ArrayList<String> COLUMN_NAMES;
	protected String TABLE_CREATE = "";

	protected SQLiteDatabase db;

	private Context context;


	// Constructors **************************************************

	public Database(Context context)
	{
		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);

		db = openHelper.getWritableDatabase();

		COLUMN_NAMES.add("id");
		COLUMN_NAMES.add("name");
		COLUMN_NAMES.add("last_change");

		TABLE_CREATE = " (" + COLUMN_NAMES.get(0) + " INTEGER PRIMARY TEXT, " + COLUMN_NAMES.get(1) + " TEXT, " + COLUMN_NAMES.get(2) + " TEXT";
	}


	// Gets **********************************************************

	public SQLiteDatabase getDb()
	{
		return db;
	}

	public ArrayList<String> getColumns()
	{
		return COLUMN_NAMES;
	}

	protected String columnList()
	{
		String out = "(";

		// Build list of Columns for SQL query e.g. (id, names, last_change)
		for (int c = 0; c < COLUMN_NAMES.size(); c++) {
			if (c > 0)
				out += ", ";

			out += COLUMN_NAMES.get(c);
		}

		out += ")";

		return out;
	}


	// Functions *****************************************************

	public Cursor query(String sqlSearch)
	{
		return db.query(TABLE_NAME, (String[]) COLUMN_NAMES.toArray(), sqlSearch, null, null, null, null);
	}

	public void deleteAll()
	{
		db.delete(TABLE_NAME, null, null);
	}

	// To be called within the constructor of the derived class
	protected void completeTableCreateParam()
	{
		// Complete TABLE_CREATE contents
		for (int c = 3; c < COLUMN_NAMES.size(); c++)
			TABLE_CREATE += ", " + COLUMN_NAMES.get(c) + " TEXT";

		TABLE_CREATE += ")";
	}


	// Helpers *******************************************************

	private class OpenHelper extends SQLiteOpenHelper
	{

		OpenHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL("CREATE TABLE " + TABLE_NAME + TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			Log.w("Amdroid", "Database/OpenHelper - Upgrading database, this will drop tables and recreate it.");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}

/*
   public List<String> selectAll() {
      List<String> list = new ArrayList<String>();
      Cursor cursor = this.db.query(TABLE_NAME, new String[] { "name" }, null, null, null, null, "name desc");
      if (cursor.moveToFirst()) {
         do {
            list.add(cursor.getString(0)); 
         } while (cursor.moveToNext());
      }
      if (cursor != null && !cursor.isClosed()) {
         cursor.close();
      }
      return list;
   }
*/
}

