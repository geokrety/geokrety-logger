/*
 * Copyright (C) 2014 Michał Niedźwiecki
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

public class GeoKretDataSource {

    public static final String TABLE = "geokrets";
    public static final String COLUMN_ID = GeoKretySQLiteHelper.COLUMN_ID;
    public static final String COLUMN_GK_CODE = "geokret_code"; // id
    public static final String COLUMN_DISTANCE = "dist";
    public static final String COLUMN_OWNER_ID = "owner_id";
    public static final String COLUMN_STATE = "state";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TRACKING_CODE = "tracking_code"; // nr
    public static final String COLUMN_SYNCHRO_STATE = "synchro_state";
    public static final String COLUMN_SYNCHRO_ERROR = "synchro_error";

    public static final int SYNCHRO_STATE_SYNCHRONIZED = 1;
    public static final int SYNCHRO_STATE_UNSYNCHRONIZED = 0;
    public static final int SYNCHRO_STATE_ERROR = -1;

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE + "(" //
            + COLUMN_ID + " INTEGER PRIMARY KEY autoincrement, " //
            + COLUMN_GK_CODE + " INTEGER, " //
            + COLUMN_DISTANCE + " INTEGER, " //
            + COLUMN_OWNER_ID + " INTEGER, " //
            + COLUMN_STATE + " INTEGER, " //
            + COLUMN_TYPE + " INTEGER, " //
            + COLUMN_NAME + " TEXT, " //
            + COLUMN_TRACKING_CODE + " TEXT NOT NULL," //
            + COLUMN_SYNCHRO_STATE + " INTEGER DEFAULT 0," //
            + COLUMN_SYNCHRO_ERROR + " TEXT" //
            + "); ";
    
    public static final String FETCH_COLUMNS = "g." + COLUMN_GK_CODE + ", " //
            + "g." + COLUMN_DISTANCE + ", " //
            + "g." + COLUMN_OWNER_ID + ", " //
            + "g." + COLUMN_STATE + ", " //
            + "g." + COLUMN_TYPE + ", " //
            + "g." + COLUMN_NAME + ", " //
            + "g." + COLUMN_SYNCHRO_STATE + ", " //
            + "g." + COLUMN_SYNCHRO_ERROR;

    public static final String FETCH_BY_ID = "SELECT g." + COLUMN_TRACKING_CODE + ", 0 AS sticky, " + FETCH_COLUMNS + " FROM " + TABLE + " AS g WHERE g." + COLUMN_TRACKING_CODE + " = ?";
    

    private static ContentValues getValues(final GeoKret geokret) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_GK_CODE, geokret.getGeoKretId());
        values.put(COLUMN_DISTANCE, geokret.getDist());
        values.put(COLUMN_OWNER_ID, geokret.getOwnerId());
        values.put(COLUMN_STATE, geokret.getState());
        values.put(COLUMN_TYPE, geokret.getType());
        values.put(COLUMN_NAME, geokret.getName());
        values.put(COLUMN_TRACKING_CODE, geokret.getTrackingCode());
        values.put(COLUMN_SYNCHRO_STATE, geokret.getSynchroState());
        values.put(COLUMN_SYNCHRO_ERROR, geokret.getSynchroError());
        return values;
    }

    private final GeoKretySQLiteHelper dbHelper;

    public GeoKretDataSource(final GeoKretySQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Deprecated
    public List<String> loadNeedUpdateList() {
        final LinkedList<String> gks = new LinkedList<String>();
        dbHelper.runOnReadableDatabase(new GeoKretySQLiteHelper.DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {
                final Cursor cursor = db.query(TABLE, new String[] {
                    COLUMN_TRACKING_CODE
                }, COLUMN_SYNCHRO_STATE + " < 1", null, null, null, null);
                while (cursor.moveToNext()) {
                    gks.add(cursor.getString(0));
                }
                cursor.close();
                return true;
            }
        });
        return gks;
    }

    public void update(final Collection<GeoKret> geoKretList) {
        dbHelper.runOnWritableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {

                final LinkedList<ContentValues> cv = new LinkedList<ContentValues>();
                for (final GeoKret log : geoKretList) {
                    cv.add(getValues(log));
                    remove(db,
                            TABLE,
                            COLUMN_TRACKING_CODE + " = ?",
                            log.getTrackingCode());
                }
                persistAll(db, TABLE, cv);
                return true;
            }
        });
    }
    
    public GeoKret loadByTrackingCode(final CharSequence tc) {
        final LinkedList<GeoKret> geoKretLogs = new LinkedList<GeoKret>();
        dbHelper.runOnReadableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {

                final Cursor cursor = db.rawQuery(FETCH_BY_ID, new String[]{tc.toString()});
                while (cursor.moveToNext()) {
                    geoKretLogs.add(new GeoKret(cursor, 0));
                }
                cursor.close();
                return true;
            }
        });
        return geoKretLogs.isEmpty() ? null : geoKretLogs.getFirst();
    }
}
