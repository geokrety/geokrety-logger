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

import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.data.GeoKretySQLiteHelper.DBOperation;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class GeocacheDataSource {
	public static final String TABLE = "geocaches";
	public static final String COLUMN_ID = GeoKretySQLiteHelper.COLUMN_ID;
	public static final String COLUMN_WAYPOINT = "waypoint";
	public static final String COLUMN_GUID = "guid";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_LOCATION = "location";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_STATUS = "status";

	public static final String TABLE_CREATE = "CREATE TABLE " + TABLE + "(" //
			+ COLUMN_ID + " INTEGER PRIMARY KEY autoincrement, " //
			+ COLUMN_WAYPOINT + " TEXT NOT NULL, " //
            + COLUMN_GUID + " TEXT, " //
			+ COLUMN_NAME + " TEXT NOT NULL, " //
			+ COLUMN_LOCATION + " TEXT NOT NULL, " //
			+ COLUMN_TYPE + " TEXT NOT NULL, " //
			+ COLUMN_STATUS + " TEXT NOT NULL" //
			+ "); ";

	private GeoKretySQLiteHelper dbHelper;
	
	public static final String FETCH_COLUMNS = "c." + COLUMN_NAME + ", " //
            + "c." + COLUMN_LOCATION + ", " //
            + "c." + COLUMN_TYPE + ", " //
            + "c." + COLUMN_STATUS + ", " //
            + "c." + COLUMN_GUID;            
	
	private static final String FETCH_BY_WAYPOINT = "SELECT c." + COLUMN_WAYPOINT + ", " + FETCH_COLUMNS + " FROM " + TABLE + " AS c WHERE c." + COLUMN_WAYPOINT + " = ?";

	public GeocacheDataSource(GeoKretySQLiteHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	private static ContentValues getValues(Geocache geocache) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_WAYPOINT, geocache.getCode());
        values.put(COLUMN_GUID, geocache.getGUID());
		values.put(COLUMN_NAME, geocache.getName());
		values.put(COLUMN_LOCATION, geocache.getLocation());
		values.put(COLUMN_TYPE, geocache.getType());
		values.put(COLUMN_STATUS, geocache.getStatus());
		return values;
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
	
	public Geocache loadByWaypoint(final String wpt) {
        final LinkedList<Geocache> gcs = new LinkedList<Geocache>();
        dbHelper.runOnReadableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {

                final Cursor cursor = db.rawQuery(FETCH_BY_WAYPOINT, new String[]{wpt});
                while (cursor.moveToNext()) {
                    gcs.add(new Geocache(cursor, 0));
                }
                cursor.close();
                return true;
            }
        });
        return gcs.isEmpty() ? null : gcs.getFirst();
    }

    public void updateGeocachingCom(final Geocache gc) {
        dbHelper.runOnWritableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {

                final LinkedList<ContentValues> cv = new LinkedList<ContentValues>();
                    cv.add(getValues(gc));
                    remove(db,
                            TABLE,
                            COLUMN_WAYPOINT + " = ?",
                            gc.getCode());
                    if (!Utils.isEmpty(gc.getGUID())) {
                        remove(db,
                                TABLE,
                                COLUMN_GUID + " = ?",
                                gc.getGUID());
                    }
                persistAll(db, TABLE, cv);
                GeocacheLogDataSource.updateGeocachingComWaypoint(db);
                return true;
            }
        });
    }
}
