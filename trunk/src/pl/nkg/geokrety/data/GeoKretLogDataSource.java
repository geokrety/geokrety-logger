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

import java.util.LinkedList;
import java.util.List;

import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.data.GeoKretySQLiteHelper.DBOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GeoKretLogDataSource {

    public static final String TABLE = "geokrety_logs";
    public static final String COLUMN_ID = GeoKretySQLiteHelper.COLUMN_ID;
    public static final String COLUMN_USER_ID = "user_id";
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

    public static final String TABLE_CREATE;

    private final GeoKretySQLiteHelper dbHelper;

    // private static final String FETCH_ALL;
    private static final String FETCH_GK_LOG_COLUMNS;
    private static final String FETCH_FULL_COLUMNS;
    private static final String FETCH_OUTBOX;
    private static final String FETCH_BY_USER;
    private static final String FETCH_BY_ID;
    private static final String FETCH_FULL_USER;

    static {
        TABLE_CREATE = "CREATE TABLE " //
                + TABLE + "(" //
                + COLUMN_ID + " INTEGER PRIMARY KEY autoincrement, " //
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

        FETCH_GK_LOG_COLUMNS = "l." + COLUMN_ID + " AS " + COLUMN_ID + ", " //
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
                + "l." + COLUMN_COMMENT;

        FETCH_FULL_COLUMNS = FETCH_GK_LOG_COLUMNS + ", " //
                + "c." + GeocacheDataSource.COLUMN_WAYPOINT + ", " //
                + GeocacheDataSource.FETCH_COLUMNS + ", " //
                + "g." + GeoKretDataSource.COLUMN_TRACKING_CODE + ", " //
                + "0, " //
                + GeoKretDataSource.FETCH_COLUMNS;

        FETCH_OUTBOX = "SELECT " //
                + FETCH_GK_LOG_COLUMNS
                + ", "//
                + "u." + UserDataSource.COLUMN_SECID
                + " FROM " //
                + TABLE
                + " AS l" //
                + " JOIN " + UserDataSource.TABLE + " AS u ON l." + COLUMN_USER_ID
                + " = u."
                + UserDataSource.COLUMN_ID //
                + " WHERE l." + COLUMN_STATE + " = " + GeoKretLog.STATE_OUTBOX //
                + " ORDER BY l." + COLUMN_ID;

        FETCH_FULL_USER = "SELECT " //
                + FETCH_FULL_COLUMNS
                + " FROM "
                + TABLE
                + " AS l" //
                + " LEFT JOIN " + GeocacheDataSource.TABLE + " AS c ON l."
                + COLUMN_WAYPOINT
                + " = c." + GeocacheDataSource.COLUMN_WAYPOINT
                + " LEFT JOIN " + GeoKretDataSource.TABLE + " AS g ON l."
                + COLUMN_TRACKING_CODE
                + " = g." + GeoKretDataSource.COLUMN_TRACKING_CODE;

        FETCH_BY_USER = FETCH_FULL_USER //
                + " WHERE l." + COLUMN_USER_ID + " = ?" //
                + " ORDER BY l." + COLUMN_ID;

        FETCH_BY_ID = FETCH_FULL_USER + " WHERE l." + COLUMN_ID + " = ?";
    }

    public static Cursor createLoadByUserIDCurosr(final SQLiteDatabase db, final long userID) {
        return db.rawQuery(FETCH_BY_USER, new String[] {
                String.valueOf(userID)
        });
    }

    private static ContentValues getValues(final GeoKretLog log) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, log.getAccoundID());
        values.put(COLUMN_STATE, log.getState());
        values.put(COLUMN_PROBLEM, log.getProblem());
        values.put(COLUMN_PROBLEM_ARG, Utils.defaultIfNull(log.getProblemArg(), ""));
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

    public void delete(final long id) {
        dbHelper.runOnWritableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {
                removeSimple(db, TABLE, id);
                return true;
            }
        });
    }

    public GeoKretLog loadByID(final long logID) {
        final LinkedList<GeoKretLog> geoKretLogs = new LinkedList<GeoKretLog>();
        dbHelper.runOnReadableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {

                final Cursor cursor = db.rawQuery(FETCH_BY_ID, new String[] {
                        String.valueOf(logID)
                });
                while (cursor.moveToNext()) {
                    geoKretLogs.add(new GeoKretLog(cursor, 0, false, true));
                }
                cursor.close();
                return true;
            }
        });
        return geoKretLogs.isEmpty() ? null : geoKretLogs.getFirst();
    }

    public List<GeoKretLog> loadByUserID(final long userID) {
        final LinkedList<GeoKretLog> geoKretLogs = new LinkedList<GeoKretLog>();
        dbHelper.runOnReadableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {
                final Cursor cursor = createLoadByUserIDCurosr(db, userID);
                while (cursor.moveToNext()) {
                    geoKretLogs.add(new GeoKretLog(cursor, 0, false, true));
                }
                cursor.close();
                return true;
            }
        });
        return geoKretLogs;
    }

    public List<GeoKretLog> loadOutbox() {
        final LinkedList<GeoKretLog> outbox = new LinkedList<GeoKretLog>();
        dbHelper.runOnReadableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {
                final Cursor cursor = db.rawQuery(FETCH_OUTBOX, new String[] {});
                while (cursor.moveToNext()) {
                    outbox.add(new GeoKretLog(cursor, 0, true, false));
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
                mergeSimple(db, TABLE, getValues(log), log.getId());
                return true;
            }

        });
    }

    public void moveAllDraftsToOutbox(final long userId) {
        dbHelper.runOnWritableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {
                final ContentValues values = new ContentValues();
                values.put(COLUMN_STATE, GeoKretLog.STATE_OUTBOX);
                db.update(TABLE, values, COLUMN_USER_ID + " = ? AND " + COLUMN_STATE + " = ?",
                        new String[] {
                                Long.toString(userId), Integer.toString(GeoKretLog.STATE_DRAFT)
                        });
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

    public void removeAllLogs(final long userId, final long except) {
        dbHelper.runOnWritableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {
                db.delete(TABLE, COLUMN_USER_ID + " = ? AND " + COLUMN_ID + " != ?", new String[] {
                        Long.toString(userId), Long.toString(except)
                });
                Log.println(Log.ERROR, "ble", Long.toString(except));
                return true;
            }

        });
    }
}
