/*
 * Copyright (C) 2013, 2014 Michał Niedźwiecki
 * 
 * This file is a part of GeoKrety Logger
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
import java.util.List;

import pl.nkg.geokrety.data.GeoKretySQLiteHelper.DBOperation;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class GeocacheDataSource {
	public static final String TABLE = "geocaches";
	public static final String COLUMN_ID = GeoKretySQLiteHelper.COLUMNT_ID;
	public static final String COLUMN_WAYPOINT = "waypoint";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_LOCATION = "location";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_STATUS = "status";

	public static final String TABLE_CREATE = "CREATE TABLE " + TABLE + "(" //
			+ COLUMN_ID + " INTEGER PRIMARY KEY autoincrement, " //
			+ COLUMN_WAYPOINT + " TEXT NOT NULL, " //
			+ COLUMN_NAME + " TEXT NOT NULL, " //
			+ COLUMN_LOCATION + " TEXT NOT NULL, " //
			+ COLUMN_TYPE + " TEXT NOT NULL, " //
			+ COLUMN_STATUS + " TEXT NOT NULL" //
			+ "); ";

	private GeoKretySQLiteHelper dbHelper;

	private static final String FETCH_ALL = "SELECT " //
			+ COLUMN_WAYPOINT + ", " //
			+ COLUMN_NAME + ", " //
			+ COLUMN_LOCATION + ", " //
			+ COLUMN_TYPE + ", " //
			+ COLUMN_STATUS //
			+ " FROM " //
			+ TABLE;

	public GeocacheDataSource(GeoKretySQLiteHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	private static ContentValues getValues(Geocache geocache) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_WAYPOINT, geocache.getCode());
		values.put(COLUMN_NAME, geocache.getName());
		values.put(COLUMN_LOCATION, geocache.getLocation());
		values.put(COLUMN_TYPE, geocache.getType());
		values.put(COLUMN_STATUS, geocache.getStatus());
		return values;
	}

	@Deprecated
	public List<Geocache> load() {
		final LinkedList<Geocache> gcs = new LinkedList<Geocache>();
		dbHelper.runOnReadableDatabase(new DBOperation() {

			@Override
			public boolean inTransaction(SQLiteDatabase db) {
				Cursor cursor = db.rawQuery(FETCH_ALL, new String[] {});
				while (cursor.moveToNext()) {
					Geocache gc = new Geocache(//
							cursor.getString(0), //
							cursor.getString(1), //
							cursor.getString(2), //
							cursor.getString(3), //
							cursor.getString(4));
					gcs.add(gc);
				}
				cursor.close();
				return true;
			}
		});
		return gcs;
	}

	@Deprecated
	public void store(final Collection<Geocache> geocaches) {
		dbHelper.runOnWritableDatabase(new DBOperation() {

			@Override
			public boolean inTransaction(SQLiteDatabase db) {
				remove(db, TABLE, null);
				LinkedList<ContentValues> cv = new LinkedList<ContentValues>();
				for (Geocache gc : geocaches) {
					cv.add(getValues(gc));
				}
				persistAll(db, TABLE, cv);
				return true;
			}
		});
	}
	
	public void update(final Collection<Geocache> geocacheList) {
        dbHelper.runOnWritableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {

                final LinkedList<ContentValues> cv = new LinkedList<ContentValues>();
                for (final Geocache gc : geocacheList) {
                    cv.add(getValues(gc));
                    remove(db,
                            TABLE,
                            COLUMN_WAYPOINT + " = ?",
                            gc.getCode());
                }
                persistAll(db, TABLE, cv);
                return true;
            }
        });
    }
}
