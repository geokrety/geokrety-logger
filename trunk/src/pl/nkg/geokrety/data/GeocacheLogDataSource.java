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

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

public class GeocacheLogDataSource {
	private GeoKretySQLiteHelper dbHelper;
	private final static String TABLE = GeoKretySQLiteHelper.TABLE_LOGS;
	private final static String PK_COLUMN = GeoKretySQLiteHelper.COLUMN_LOG_UUID;

	private static final String FETCH_ALL = "SELECT " //
			+ PK_COLUMN + ", " //
			+ GeoKretySQLiteHelper.COLUMN_USER_ID + ", " //
			+ GeoKretySQLiteHelper.COLUMN_WAYPOINT + ", " //
			+ GeoKretySQLiteHelper.COLUMN_TYPE + ", " //
			+ GeoKretySQLiteHelper.COLUMN_DATE + ", " //
			+ GeoKretySQLiteHelper.COLUMN_COMMENT + ", " //
			+ GeoKretySQLiteHelper.COLUMN_PORTAL //
			+ " FROM " //
			+ TABLE + " ORDER BY " + GeoKretySQLiteHelper.COLUMN_DATE + " DESC";

	public GeocacheLogDataSource(Context context) {
		dbHelper = new GeoKretySQLiteHelper(context);
	}

	private static ContentValues getValues(GeocacheLog log, int userID) {
		ContentValues values = new ContentValues();
		values.put(PK_COLUMN, log.getUUID());
		values.put(GeoKretySQLiteHelper.COLUMN_USER_ID, userID);
		values.put(GeoKretySQLiteHelper.COLUMN_WAYPOINT, log.getCacheCode());
		values.put(GeoKretySQLiteHelper.COLUMN_TYPE, log.getType());
		values.put(GeoKretySQLiteHelper.COLUMN_DATE, log.getDate().getTime());
		values.put(GeoKretySQLiteHelper.COLUMN_COMMENT, log.getComment());
		values.put(GeoKretySQLiteHelper.COLUMN_PORTAL, log.getPortal());
		return values;
	}

	public void persistAll(Collection<GeocacheLog> logs, int userID) {
		LinkedList<ContentValues> cv = new LinkedList<ContentValues>();
		for (GeocacheLog log : logs) {
			cv.add(getValues(log, userID));
		}
		dbHelper.persistAll(TABLE, cv);
	}

	public void removeAll(int userID, int portal) {
		dbHelper.remove(TABLE, GeoKretySQLiteHelper.COLUMN_USER_ID
				+ " = ? AND " + GeoKretySQLiteHelper.COLUMN_PORTAL + " = ?",
				String.valueOf(userID), String.valueOf(portal));
	}

	public SparseArray<LinkedList<GeocacheLog>> getAll() {
		SparseArray<LinkedList<GeocacheLog>> logs = new SparseArray<LinkedList<GeocacheLog>>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(FETCH_ALL, new String[] {});
		while (cursor.moveToNext()) {
			GeocacheLog log = new GeocacheLog(cursor.getString(0), //
					cursor.getString(2), //
					cursor.getString(3), //
					new Date(cursor.getLong(4)), //
					cursor.getString(5), //
					cursor.getInt(6));
			int userID = cursor.getInt(1);
			LinkedList<GeocacheLog> list = logs.get(userID);
			if (list == null) {
				list = new LinkedList<GeocacheLog>();
				logs.put(userID, list);
			}
			list.add(log);
		}
		cursor.close();
		return logs;
	}
}
