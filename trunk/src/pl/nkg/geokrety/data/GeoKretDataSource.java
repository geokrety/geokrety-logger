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
import java.util.LinkedList;

import pl.nkg.geokrety.data.GeoKretySQLiteHelper.DBOperation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

public class GeoKretDataSource {

	public static final String TABLE = "inventory";
	public static final String COLUMN_GK_CODE = "geokret_code"; // id
	public static final String COLUMN_DISTANCE = "dist";
	public static final String COLUMN_OWNER_ID = "owner_id";
	public static final String COLUMN_STATE = "state";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_TRACKING_CODE = "tracking_code"; // nr
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_STICKY = "sticky";

	public static final String TABLE_CODE = "CREATE TABLE " + TABLE + "(" //
			+ COLUMN_GK_CODE + " INTEGER NOT NULL, " //
			+ COLUMN_DISTANCE + " INTEGER NOT NULL, " //
			+ COLUMN_OWNER_ID + " INTEGER NOT NULL, " //
			+ COLUMN_STATE + " INTEGER, " //
			+ COLUMN_TYPE + " INTEGER NOT NULL, " //
			+ COLUMN_NAME + " TEXT NOT NULL, " //
			+ COLUMN_TRACKING_CODE + " TEXT PRIMARY KEY, " //
			+ COLUMN_USER_ID + " INTEGER NOT NULL, " //
			+ COLUMN_STICKY + " INTEGER NOT NULL DEFAULT 0" //
			+ ");";

	private GeoKretySQLiteHelper dbHelper;

	private static final String FETCH_ALL = "SELECT " //
			+ COLUMN_GK_CODE + ", " //
			+ COLUMN_DISTANCE + ", " //
			+ COLUMN_OWNER_ID + ", " //
			+ COLUMN_STATE + ", " //
			+ COLUMN_TYPE + ", " //
			+ COLUMN_NAME + ", " //
			+ COLUMN_TRACKING_CODE + ", " //
			+ COLUMN_USER_ID + ", " //
			+ COLUMN_STICKY //
			+ " FROM " //
			+ TABLE + " ORDER BY " + COLUMN_GK_CODE;

	public GeoKretDataSource(Context context) {
		dbHelper = new GeoKretySQLiteHelper(context);
	}

	private static ContentValues getValues(Geokret geokret, int userID) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_GK_CODE, geokret.getID());
		values.put(COLUMN_DISTANCE, geokret.getDist());
		values.put(COLUMN_OWNER_ID, geokret.getOwnerID());
		values.put(COLUMN_STATE, geokret.getState());
		values.put(COLUMN_TYPE, geokret.getType());
		values.put(COLUMN_NAME, geokret.getName());
		values.put(COLUMN_TRACKING_CODE, geokret.getTackingCode());
		values.put(COLUMN_USER_ID, userID);
		values.put(COLUMN_STICKY, geokret.isSticky());
		return values;
	}

	public void store(final Collection<Geokret> logs, final int userID,
			final int portal) {
		dbHelper.runOnWritableDatabase(new DBOperation() {

			@Override
			public boolean inTransaction(SQLiteDatabase db) {
				remove(db, //
						TABLE, //
						COLUMN_USER_ID + " = ?", //
						String.valueOf(userID));

				LinkedList<ContentValues> cv = new LinkedList<ContentValues>();
				for (Geokret log : logs) {
					cv.add(getValues(log, userID));
				}
				persistAll(db, TABLE, cv);
				return true;
			}
		});
	}

	public SparseArray<LinkedList<Geokret>> load() {
		final SparseArray<LinkedList<Geokret>> gks = new SparseArray<LinkedList<Geokret>>();
		dbHelper.runOnReadableDatabase(new GeoKretySQLiteHelper.DBOperation() {

			@Override
			public boolean inTransaction(SQLiteDatabase db) {
				Cursor cursor = db.rawQuery(FETCH_ALL, new String[] {});
				while (cursor.moveToNext()) {
					Geokret gk = new Geokret(cursor.getInt(0), //
							cursor.getInt(1), //
							cursor.getInt(2), //
							cursor.getInt(3), //
							cursor.getInt(4), //
							cursor.getString(5), //
							cursor.getString(6), //
							cursor.getInt(8) > 0);
					int userID = cursor.getInt(7);
					LinkedList<Geokret> list = gks.get(userID);
					if (list == null) {
						list = new LinkedList<Geokret>();
						gks.put(userID, list);
					}
					list.add(gk);
				}
				cursor.close();
				return true;
			}
		});
		return gks;
	}
}
