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
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class GeocacheDataSource {
	private GeoKretySQLiteHelper dbHelper;
	private final static String TABLE = GeoKretySQLiteHelper.TABLE_GEOCACHES;
	private final static String PK_COLUMN = GeoKretySQLiteHelper.COLUMN_WAYPOINT;

	private static final String FETCH_ALL = "SELECT " //
			+ PK_COLUMN + ", " //
			+ GeoKretySQLiteHelper.COLUMN_NAME + ", " //
			+ GeoKretySQLiteHelper.COLUMN_LOCATION + ", " //
			+ GeoKretySQLiteHelper.COLUMN_TYPE + ", " //
			+ GeoKretySQLiteHelper.COLUMN_STATUS //
			+ " FROM " //
			+ TABLE;

	public GeocacheDataSource(Context context) {
		dbHelper = new GeoKretySQLiteHelper(context);
	}

	private static ContentValues getValues(Geocache geocache) {
		ContentValues values = new ContentValues();
		values.put(PK_COLUMN, geocache.getCode());
		values.put(GeoKretySQLiteHelper.COLUMN_NAME, geocache.getName());
		values.put(GeoKretySQLiteHelper.COLUMN_LOCATION, geocache.getLocation());
		values.put(GeoKretySQLiteHelper.COLUMN_TYPE, geocache.getType());
		values.put(GeoKretySQLiteHelper.COLUMN_STATUS, geocache.getStatus());
		return values;
	}

	public void persistAll(Collection<Geocache> geocaches) {
		LinkedList<ContentValues> cv = new LinkedList<ContentValues>();
		for (Geocache gc : geocaches) {
			cv.add(getValues(gc));
		}
		dbHelper.persistAll(TABLE, cv);
	}

	public void removeAll() {
		dbHelper.remove(TABLE, null);
	}

	public List<Geocache> getAll() {
		LinkedList<Geocache> gcs = new LinkedList<Geocache>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(FETCH_ALL, new String[] {});
		while (cursor.moveToNext()) {
			Geocache gc = new Geocache(cursor.getString(0),
					cursor.getString(1), cursor.getString(2),
					cursor.getString(3), cursor.getString(4));
			gcs.add(gc);
		}
		cursor.close();
		return gcs;
	}
}
