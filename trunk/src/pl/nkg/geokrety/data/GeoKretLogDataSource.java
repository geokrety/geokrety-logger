/*
 * Copyright (C) 2013, 2014 Michał Niedźwiecki
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

import pl.nkg.geokrety.data.GeoKretySQLiteHelper.DBOperation;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

public class GeoKretLogDataSource {

	public static final String TABLE = "geokrety_logs";
	public static final String COLUMN_LOG_ID = "id";
	public static final String COLUMN_USER_ID = "account_id";
	public static final String COLUMN_STATE = "state";
	public static final String COLUMN_PROBLEM = "problem_id";
	public static final String COLUMN_PROBLEM_ARG = "problem_arg";
	public static final String COLUMN_TRACKING_CODE = "tracking_code";
	public static final String COLUMN_WAYPOINT = "waypoint";
	public static final String COLUMN_FORMNAME = "formname";
	public static final String COLUMN_LATLON = "latlon";
	public static final String COLUMN_LOG_TYPE = "log_type";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_HOUR = "hour";
	public static final String COLUMN_MINUTE = "minute";
	public static final String COLUMN_COMMENT = "comment";

	public static final String TABLE_CREATE = "CREATE TABLE " //
			+ TABLE + "(" //
			+ COLUMN_LOG_ID + " INTEGER PRIMARY KEY autoincrement, " //
			+ COLUMN_USER_ID + " INTEGER NOT NULL, " //
			+ COLUMN_STATE + " INTEGER NOT NULL, " //
			+ COLUMN_PROBLEM + " INTEGER NOT NULL, " //
			+ COLUMN_PROBLEM_ARG + " TEXT NOT NULL, " //
			+ COLUMN_TRACKING_CODE + " TEXT NOT NULL, " //
			+ COLUMN_WAYPOINT + " TEXT NOT NULL, " //
			+ COLUMN_FORMNAME + " TEXT NOT NULL DEFAULT('ruchy'), " //
			+ COLUMN_LATLON + " TEXT NOT NULL, " //
			+ COLUMN_LOG_TYPE + " INTEGER NOT NULL, " //
			+ COLUMN_DATE + " TEXT NOT NULL, " //
			+ COLUMN_HOUR + " INTEGER NOT NULL, " //
			+ COLUMN_MINUTE + " INTEGER NOT NULL, " //
			+ COLUMN_COMMENT + " TEXT NOT NULL" //
			+ "); ";

	private GeoKretySQLiteHelper dbHelper;
	private final static String PK_COLUMN = COLUMN_LOG_ID;

	private static final String FETCH_ALL = "SELECT " //
			+ PK_COLUMN + ", " //
			+ COLUMN_LOG_ID + ", " //
			+ COLUMN_USER_ID + ", " //
			+ COLUMN_STATE + ", " //
			+ COLUMN_PROBLEM + ", " //
			+ COLUMN_PROBLEM_ARG + ", " //
			+ COLUMN_TRACKING_CODE + ", " //
			+ COLUMN_WAYPOINT + ", " //
			+ COLUMN_FORMNAME + ", " //
			+ COLUMN_LATLON + ", " //
			+ COLUMN_LOG_TYPE + ", " //
			+ COLUMN_DATE + ", " //
			+ COLUMN_HOUR + ", " //
			+ COLUMN_MINUTE + ", " //
			+ COLUMN_COMMENT //
			+ " FROM " //
			+ TABLE + " ORDER BY " + COLUMN_LOG_ID;

	public GeoKretLogDataSource(GeoKretySQLiteHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	private static ContentValues getValues(GeoKretLog log) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_USER_ID, log.getAccoundID());
		values.put(COLUMN_STATE, log.getState());
		values.put(COLUMN_PROBLEM, log.getProblem());
		values.put(COLUMN_PROBLEM_ARG, log.getProblemArg());
		values.put(COLUMN_TRACKING_CODE, log.getNr());
		values.put(COLUMN_WAYPOINT, log.getWpt());
		values.put(COLUMN_FORMNAME, log.getFormname());
		values.put(COLUMN_LATLON, log.getLatlon());
		values.put(COLUMN_LOG_TYPE, log.getLogType());
		values.put(COLUMN_DATE, log.getData());
		values.put(COLUMN_HOUR, log.getGodzina());
		values.put(COLUMN_MINUTE, log.getMinuta());
		values.put(COLUMN_COMMENT, log.getComment());
		return values;
	}

	public void persist(final GeoKretLog log) {
		dbHelper.runOnWritableDatabase(new DBOperation() {

			@Override
			public boolean inTransaction(SQLiteDatabase db) {
				int id = (int) persist(db, TABLE, getValues(log));
				log.setId(id);
				return true;
			}

		});
	}

	public void merge(final GeoKretLog log) {
		dbHelper.runOnWritableDatabase(new DBOperation() {

			@Override
			public boolean inTransaction(SQLiteDatabase db) {
				merge(db, TABLE, PK_COLUMN, getValues(log), PK_COLUMN,
						String.valueOf(log.getId()));
				return true;
			}

		});
	}

	public SparseArray<LinkedList<GeoKretLog>> load() {
		final SparseArray<LinkedList<GeoKretLog>> logs = new SparseArray<LinkedList<GeoKretLog>>();
		dbHelper.runOnReadableDatabase(new DBOperation() {

			@Override
			public boolean inTransaction(SQLiteDatabase db) {
				Cursor cursor = db.rawQuery(FETCH_ALL, new String[] {});
				while (cursor.moveToNext()) {
					GeoKretLog log = new GeoKretLog( //
							cursor.getInt(0), //
							cursor.getInt(1), //
							cursor.getInt(2), //
							cursor.getInt(3), //
							cursor.getString(4), //
							cursor.getString(5), //
							cursor.getString(6), //
							cursor.getString(7), //
							cursor.getString(8), //
							cursor.getInt(9), //
							cursor.getString(10), //
							cursor.getInt(11), //
							cursor.getInt(12), //
							cursor.getString(13)//
					);
					int userID = cursor.getInt(1);
					LinkedList<GeoKretLog> list = logs.get(userID);
					if (list == null) {
						list = new LinkedList<GeoKretLog>();
						logs.put(userID, list);
					}
					list.add(log);
				}
				cursor.close();
				return true;
			}
		});
		return logs;
	}
}
