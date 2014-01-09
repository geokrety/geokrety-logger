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
import java.util.List;

import pl.nkg.geokrety.data.GeoKretySQLiteHelper.DBOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class GeoKretLogDataSource {

	public static final String			TABLE					= "geokrety_logs";
	public static final String			COLUMN_LOG_ID			= "id";
	public static final String			COLUMN_USER_ID			= "account_id";
	public static final String			COLUMN_STATE			= "state";
	public static final String			COLUMN_PROBLEM			= "problem_id";
	public static final String			COLUMN_PROBLEM_ARG		= "problem_arg";
	public static final String			COLUMN_TRACKING_CODE	= "tracking_code";
	public static final String			COLUMN_WAYPOINT			= "waypoint";
	public static final String			COLUMN_FORMNAME			= "formname";
	public static final String			COLUMN_LATLON			= "latlon";
	public static final String			COLUMN_LOG_TYPE			= "log_type";
	public static final String			COLUMN_DATE				= "date";
	public static final String			COLUMN_HOUR				= "hour";
	public static final String			COLUMN_MINUTE			= "minute";
	public static final String			COLUMN_COMMENT			= "comment";

	public static final String			TABLE_CREATE;

	private final GeoKretySQLiteHelper	dbHelper;
	private final static String			PK_COLUMN				= COLUMN_LOG_ID;

	//private static final String			FETCH_ALL;
	private static final String			FETCH_OUTBOX;

	static {
		TABLE_CREATE = "CREATE TABLE " //
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
/*
		FETCH_ALL = "SELECT " //
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
*/
		FETCH_OUTBOX = "SELECT " //
				+ "l." + COLUMN_LOG_ID + ", " //
				+ "l." + COLUMN_USER_ID + ", " //
				+ "l." + COLUMN_STATE + ", " //
				+ "l." + COLUMN_PROBLEM + ", " //
				+ "l." + COLUMN_PROBLEM_ARG + ", " //
				+ "l." + COLUMN_TRACKING_CODE + ", " //
				+ "l." + COLUMN_WAYPOINT + ", " //
				+ "l." + COLUMN_FORMNAME + ", " //
				+ "l." + COLUMN_LATLON + ", " //
				+ "l." + COLUMN_LOG_TYPE + ", " //
				+ "l." + COLUMN_DATE + ", " //
				+ "l." + COLUMN_HOUR + ", " //
				+ "l." + COLUMN_MINUTE + ", " //
				+ "l." + COLUMN_COMMENT + ", "//
				+ "u." + AccountDataSource.COLUMN_SECID + " FROM " //
				+ TABLE + " l" //
				+ " JOIN " + AccountDataSource.TABLE + " u ON l." + COLUMN_USER_ID + " = u." + AccountDataSource.COLUMN_USER_ID //
				+ " WHERE l." + COLUMN_STATE + " = " + GeoKretLog.STATE_OUTBOX //
				+ " ORDER BY " + COLUMN_LOG_ID;
	}

	private static ContentValues getValues(final GeoKretLog log) {
		final ContentValues values = new ContentValues();
		values.put(COLUMN_USER_ID, log.getAccoundID());
		values.put(COLUMN_STATE, log.getState());
		values.put(COLUMN_PROBLEM, log.getProblem());
		values.put(COLUMN_PROBLEM_ARG, log.getProblemArg());
		values.put(COLUMN_TRACKING_CODE, log.getNr());
		values.put(COLUMN_WAYPOINT, log.getWpt());
		values.put(COLUMN_FORMNAME, log.getFormname());
		values.put(COLUMN_LATLON, log.getLatlon());
		values.put(COLUMN_LOG_TYPE, log.getLogTypeMapped());
		values.put(COLUMN_DATE, log.getData());
		values.put(COLUMN_HOUR, log.getGodzina());
		values.put(COLUMN_MINUTE, log.getMinuta());
		values.put(COLUMN_COMMENT, log.getComment());
		return values;
	}

	public GeoKretLogDataSource(final GeoKretySQLiteHelper dbHelper) {
		this.dbHelper = dbHelper;
	}
/*
	@Deprecated
	public SparseArray<LinkedList<GeoKretLog>> load() {
		final SparseArray<LinkedList<GeoKretLog>> logs = new SparseArray<LinkedList<GeoKretLog>>();
		dbHelper.runOnReadableDatabase(new DBOperation() {

			@Override
			public boolean inTransaction(final SQLiteDatabase db) {
				final Cursor cursor = db.rawQuery(FETCH_ALL, new String[] {});
				while (cursor.moveToNext()) {
					final GeoKretLog log = new GeoKretLog( //
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
							cursor.getString(13), //
							"" //
					);
					final int userID = cursor.getInt(1);
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
*/
	public List<GeoKretLog> loadOutbox() {
		final LinkedList<GeoKretLog> outbox = new LinkedList<GeoKretLog>();
		dbHelper.runOnReadableDatabase(new DBOperation() {

			@Override
			public boolean inTransaction(final SQLiteDatabase db) {
				final Cursor cursor = db.rawQuery(FETCH_OUTBOX, new String[] {});
				while (cursor.moveToNext()) {
					final GeoKretLog log = new GeoKretLog( //
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
							cursor.getString(13), //
							cursor.getString(14) //
					);
					outbox.add(log);
				}
				cursor.close();
				return true;
			}
		});
		return outbox;
	}

	public void merge(final GeoKretLog log) {
		dbHelper.runOnWritableDatabase(new DBOperation() {

			@Override
			public boolean inTransaction(final SQLiteDatabase db) {
				mergeSimple(db, TABLE, getValues(log), PK_COLUMN, String.valueOf(log.getId()));
				return true;
			}

		});
	}

	public void persist(final GeoKretLog log) {
		dbHelper.runOnWritableDatabase(new DBOperation() {

			@Override
			public boolean inTransaction(final SQLiteDatabase db) {
				final int id = (int) persist(db, TABLE, getValues(log));
				log.setId(id);
				return true;
			}

		});
	}
}
