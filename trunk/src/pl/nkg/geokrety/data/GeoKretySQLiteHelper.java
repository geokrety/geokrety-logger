/*
 * Copyright (C) 2013 Michał Niedźwiecki
 * 
 * This file is part of GeoKrety Logger
 * http://geokretylog.sourceforge.net/
 * 
 * GeoKrety Logger is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * or see <http://www.gnu.org/licenses/>
 */
package pl.nkg.geokrety.data;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GeoKretySQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_USERS = "users";
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_USER_NAME = "name";
	public static final String COLUMN_SECID = "secid";
	public static final String COLUMN_UUIDS = "uuids";

	public static final String TABLE_LOGS = "logs";
	public static final String COLUMN_LOG_UUID = "log_uuid";
	// public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_WAYPOINT = "cache_code";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_COMMENT = "comment";
	public static final String COLUMN_PORTAL = "portal";

	public static final String TABLE_GEOCACHES = "geocaches";
	// public static final String COLUMN_WAYPOINT = "waypoint";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_LOCATION = "location";
	// public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_STATUS = "status";

	public static final String TABLE_INVENTORY = "inventory";
	public static final String COLUMN_TRACKING_CODE = "tracking_code";
	// public static final String COLUMN_USER_ID = "user_id";
	// public static final String COLUMN_NAME = "name";
	public static final String COLUMN_OWNER_ID = "owner_id";
	public static final String COLUMN_STICKY = "sticky";

	private static final String DATABASE_NAME = "geokrety.db";
	private static final int DATABASE_VERSION = 2;

	// Database creation sql statement
	private static final String DATABASE_CREATE_V1 = "CREATE TABLE " //
			+ TABLE_USERS + "(" //
			+ COLUMN_USER_ID + " INTEGER PRIMARY KEY autoincrement, " //
			+ COLUMN_USER_NAME + " TEXT NOT NULL, " //
			+ COLUMN_SECID + " TEXT NOT NULL, " //
			+ COLUMN_UUIDS + " TEXT NOT NULL" //
			+ ");";

	private static final String DATABASE_CREATE_V2 = "CREATE TABLE " //
			+ TABLE_LOGS + "(" //
			+ COLUMN_LOG_UUID + " TEXT PRIMARY KEY, " //
			+ COLUMN_USER_ID + " INTEGER NOT NULL, " //
			+ COLUMN_WAYPOINT + " TEXT NOT NULL, " //
			+ COLUMN_TYPE + " TEXT NOT NULL, " //
			+ COLUMN_DATE + " INTEGER NOT NULL, " //
			+ COLUMN_COMMENT + " TEXT NOT NULL," //
			+ COLUMN_PORTAL + " INTEGER NOT NULL" //
			+ ");" //
			//
			+ "CREATE TABLE " + TABLE_GEOCACHES + "(" //
			+ COLUMN_WAYPOINT + " TEXT PRIMARY KEY, " //
			+ COLUMN_NAME + " TEXT NOT NULL, " //
			+ COLUMN_LOCATION + " TEXT NOT NULL, " //
			+ COLUMN_TYPE + " TEXT NOT NULL, " //
			+ COLUMN_STATUS + " TEXT NOT NULL" //
			+ ");" //
			//
			+ "CREATE TABLE " + TABLE_INVENTORY + "(" //
			+ COLUMN_TRACKING_CODE + " TEXT PRIMARY KEY, " //
			+ COLUMN_USER_ID + " INTEGER NOT NULL, " //
			+ COLUMN_NAME + " TEXT NOT NULL, " //
			+ COLUMN_STICKY + " BOOLEAN NOT NULL DEFAULT 0" //
			+ ");"; //

	public GeoKretySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE_V1);
		db.execSQL(DATABASE_CREATE_V2);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion == 2) {
			db.execSQL(DATABASE_CREATE_V2);
		}
	}

	public long persist(String table, ContentValues values) {
		SQLiteDatabase db = getWritableDatabase();

		db.beginTransaction();
		long id = db.insertOrThrow(table, null, values);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();

		return id;
	}

	public List<Long> persistAll(String table, List<ContentValues> values) {
		LinkedList<Long> ret = new LinkedList<Long>();
		SQLiteDatabase db = getWritableDatabase();

		db.beginTransaction();
		for (ContentValues cv : values) {
			long id = db.insertOrThrow(table, null, cv);
			ret.add(id);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();

		return ret;
	}

	public void merge(String table, String whereClause, ContentValues values,
			String... whereArgs) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		db.update(table, values, whereClause, whereArgs);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	public void mergeSimple(String table, ContentValues values,
			String pkColumn, String pkValue) {
		merge(table, pkColumn + " = ?", values, pkValue);
	}

	public void removeSimple(String table, String pkColumn, String pkValue) {
		remove(table, pkColumn + " = ?", pkValue);
	}

	public void remove(String table, String whereClause, String... whereArgs) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		db.delete(table, whereClause, whereArgs);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}
}
