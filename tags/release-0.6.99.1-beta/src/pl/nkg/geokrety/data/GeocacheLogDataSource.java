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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import pl.nkg.geokrety.data.GeoKretySQLiteHelper.DBOperation;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

public class GeocacheLogDataSource {

	public static final String TABLE = "logs";
	public static final String COLUMN_ID = GeoKretySQLiteHelper.COLUMN_ID;
	public static final String COLUMN_LOG_UUID = "log_uuid";
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_WAYPOINT = "cache_code";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_COMMENT = "comment";
	public static final String COLUMN_PORTAL = "portal";

	public static final String TABLE_CREATE = "CREATE TABLE " //
			+ TABLE + "(" //
				+ COLUMN_ID + " INTEGER PRIMARY KEY autoincrement, " //
			+ COLUMN_LOG_UUID + " TEXT NOT NULL, " //
			+ COLUMN_USER_ID + " INTEGER NOT NULL, " //
			+ COLUMN_WAYPOINT + " TEXT NOT NULL, " //
			+ COLUMN_TYPE + " TEXT NOT NULL, " //
			+ COLUMN_DATE + " INTEGER NOT NULL, " //
			+ COLUMN_COMMENT + " TEXT NOT NULL," //
			+ COLUMN_PORTAL + " INTEGER NOT NULL" //
			+ "); ";

	private GeoKretySQLiteHelper dbHelper;

	private static final String FETCH_ALL = "SELECT " //
			+ COLUMN_LOG_UUID + ", " //
			+ COLUMN_USER_ID + ", " //
			+ COLUMN_WAYPOINT + ", " //
			+ COLUMN_TYPE + ", " //
			+ COLUMN_DATE + ", " //
			+ COLUMN_COMMENT + ", " //
			+ COLUMN_PORTAL //
			+ " FROM " //
			+ TABLE + " ORDER BY " + COLUMN_DATE + " DESC";

	private static final String FETCH_BY_USER = "SELECT " //
            + "l." + COLUMN_ID + " AS " + COLUMN_ID + ", " //
			+ "l." + COLUMN_LOG_UUID + ", " //
			+ "l." + COLUMN_TYPE + ", " //
			+ "l." + COLUMN_DATE + ", " //
			+ "l." + COLUMN_COMMENT + ", " //
			+ "l." + COLUMN_PORTAL + "," //
            + "l." + COLUMN_WAYPOINT + ", " //
            + "c." + GeocacheDataSource.COLUMN_WAYPOINT + ", " //
            + GeocacheDataSource.FETCH_COLUMNS			
            + " FROM " //
			+ TABLE + " AS l"
			+ " LEFT JOIN " + GeocacheDataSource.TABLE + " AS c ON l." + COLUMN_WAYPOINT + " = c." + GeocacheDataSource.COLUMN_WAYPOINT
			+ " WHERE l." + COLUMN_USER_ID
			+ " = ? ORDER BY "
			+ "l." + COLUMN_DATE + " DESC";

	public GeocacheLogDataSource(GeoKretySQLiteHelper dbHelper) {
		this.dbHelper = dbHelper;
	}
	
	public static Cursor createLoadByUserIDCurosr(final SQLiteDatabase db, final long userID) {
        return db.rawQuery(FETCH_BY_USER, new String[] {
                String.valueOf(userID)
        });
    }

	private static ContentValues getValues(GeocacheLog log, long userID) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_LOG_UUID, log.getUUID());
		values.put(COLUMN_USER_ID, userID);
		values.put(COLUMN_WAYPOINT, log.getCacheCode());
		values.put(COLUMN_TYPE, log.getType());
		values.put(COLUMN_DATE, log.getDate().getTime());
		values.put(COLUMN_COMMENT, log.getComment());
		values.put(COLUMN_PORTAL, log.getPortal());
		return values;
	}

	public void store(final Collection<GeocacheLog> logs, final long userID,
			final int portal) {
		dbHelper.runOnWritableDatabase(new DBOperation() {

			@Override
			public boolean inTransaction(SQLiteDatabase db) {
				remove(db, //
						TABLE, //
						COLUMN_USER_ID + " = ? AND " + //
								COLUMN_PORTAL + " = ?", //
						String.valueOf(userID), //
						String.valueOf(portal));

				LinkedList<ContentValues> cv = new LinkedList<ContentValues>();
				for (GeocacheLog log : logs) {
					cv.add(getValues(log, userID));
				}
				persistAll(db, TABLE, cv);
				return true;
			}
		});
	}

	@Deprecated
	public SparseArray<LinkedList<GeocacheLog>> load() {
		final SparseArray<LinkedList<GeocacheLog>> logs = new SparseArray<LinkedList<GeocacheLog>>();
		dbHelper.runOnReadableDatabase(new DBOperation() {

			@Override
			public boolean inTransaction(SQLiteDatabase db) {
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
				return true;
			}
		});
		return logs;
	}

	@Deprecated
	public List<GeocacheLog> load(final long userID) {
		final LinkedList<GeocacheLog> list = new LinkedList<GeocacheLog>();
		dbHelper.runOnReadableDatabase(new DBOperation() {

			@Override
			public boolean inTransaction(SQLiteDatabase db) {
				Cursor cursor = db.rawQuery(FETCH_BY_USER,
						new String[] { String.valueOf(userID) });
				while (cursor.moveToNext()) {
					GeocacheLog log = new GeocacheLog(cursor, 1);
					list.add(log);
				}
				cursor.close();
				return true;
			}
		});
		return list;
	}
	
	public GeocacheLog[] loadLastLogs(final long userID) {
        final LinkedList<GeocacheLog> list = new LinkedList<GeocacheLog>();
        dbHelper.runOnReadableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(SQLiteDatabase db) {
                Cursor cursor = createLoadByUserIDCurosr(db, userID);
                while (cursor.moveToNext()) {
                    GeocacheLog log = new GeocacheLog(cursor, 1);
                    list.add(log);
                }
                cursor.close();
                return true;
            }
        });
        return list.toArray(new GeocacheLog[list.size()]);
    }
	
	public List<String> loadNeedUpdateList(final int portal) {
        final LinkedList<String> gks = new LinkedList<String>();
        dbHelper.runOnReadableDatabase(new GeoKretySQLiteHelper.DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {
                final Cursor cursor = db.rawQuery("SELECT l." + COLUMN_WAYPOINT
                        + " FROM " + TABLE
                        + " AS l"
                        + " LEFT JOIN " + GeocacheDataSource.TABLE + " AS g ON l."
                        + COLUMN_WAYPOINT
                        + " = g." + GeocacheDataSource.COLUMN_WAYPOINT
                        + " WHERE (g." + GeocacheDataSource.COLUMN_NAME + " IS NULL OR g."
                        + GeocacheDataSource.COLUMN_NAME + " = ?) AND l." + COLUMN_PORTAL + " = ?", new String[]{"", Integer.toString(portal)});

                while (cursor.moveToNext()) {
                    gks.add(cursor.getString(0));
                }
                cursor.close();
                return true;
            }
        });
        return gks;
    }
}
