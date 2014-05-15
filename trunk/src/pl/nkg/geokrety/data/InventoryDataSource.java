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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import pl.nkg.geokrety.data.GeoKretySQLiteHelper.DBOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class InventoryDataSource {

    public static final String TABLE = "inventory";
    public static final String COLUMN_ID = GeoKretySQLiteHelper.COLUMN_ID;
    public static final String COLUMN_TRACKING_CODE = "tracking_code"; // nr
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_STICKY = "sticky";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE + "(" //
            + COLUMN_ID + " INTEGER PRIMARY KEY autoincrement, " //
            + COLUMN_TRACKING_CODE + " TEXT NOT NULL, " //
            + COLUMN_USER_ID + " INTEGER NOT NULL, " //
            + COLUMN_STICKY + " INTEGER NOT NULL DEFAULT 0" //
            + "); ";

    private static final String FETCH_TRACKING_CODE_BY_USER_ID = "SELECT " + COLUMN_TRACKING_CODE
            + " FROM " + TABLE + " WHERE " + COLUMN_ID + " = ?";

    public static void appendUnsendedGrabbed(final SQLiteDatabase db, final long userId) {
        final HashSet<String> saved = new HashSet<String>(
                loadTrackingCodeByUserIDCurosr(db, userId));
        final LinkedList<ContentValues> cv = new LinkedList<ContentValues>();
        for (final String tc : GeoKretLogDataSource.importTrackingCodesFromLogs(db, userId)) {
            if (!saved.contains(tc)) {
                final GeoKret geoKret = new GeoKret(tc,
                        GeoKretDataSource.SYNCHRO_STATE_UNSYNCHRONIZED, null);
                cv.add(getValues(geoKret, userId));
            }
        }
        DBOperation.persistAll(db, TABLE, cv);
    }

    public static Cursor createLoadByUserIDCurosr(final SQLiteDatabase db, final long userID) {
        return db.rawQuery(FETCH_BY_USER, new String[] {
                String.valueOf(userID)
        });
    }

    public static Cursor createLoadTrackingCodeByUserIDCurosr(final SQLiteDatabase db,
            final long userID) {
        return db.rawQuery(FETCH_TRACKING_CODE_BY_USER_ID, new String[] {
                String.valueOf(userID)
        });
    }

    public static List<String> loadTrackingCodeByUserIDCurosr(final SQLiteDatabase db,
            final long userID) {
        final Cursor c = createLoadTrackingCodeByUserIDCurosr(db, userID);

        final List<String> ret = new LinkedList<String>();
        while (c.moveToNext()) {
            ret.add(c.getString(0));
        }
        c.close();

        return ret;
    }

    private final GeoKretySQLiteHelper dbHelper;

    private static final String PREFIX_FETCH_BY = "SELECT " //
            + "i." + COLUMN_ID + " AS " + COLUMN_ID + ", " //
            + "i." + COLUMN_TRACKING_CODE + ", " //
            + "i." + COLUMN_STICKY + ", " //
            + GeoKretDataSource.FETCH_COLUMNS
            + " FROM " + TABLE
            + " AS i" //
            + " LEFT JOIN " + GeoKretDataSource.TABLE + " AS g ON i." + COLUMN_TRACKING_CODE
            + " = g." + GeoKretDataSource.COLUMN_TRACKING_CODE;
    private static final String FETCH_BY_USER = PREFIX_FETCH_BY + " WHERE i." + COLUMN_USER_ID
            + " = ? ORDER BY g." + GeoKretDataSource.COLUMN_NAME;

    private static final String FETCH_BY_ID = PREFIX_FETCH_BY + " WHERE i." + COLUMN_ID + " = ? ";

    private static ContentValues getValues(final GeoKret geokret, final long userID) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_TRACKING_CODE, geokret.getTrackingCode());
        values.put(COLUMN_USER_ID, userID);
        values.put(COLUMN_STICKY, geokret.isSticky());
        return values;
    }

    public InventoryDataSource(final GeoKretySQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public GeoKret loadByID(final long logID) {
        final LinkedList<GeoKret> geoKretLogs = new LinkedList<GeoKret>();
        dbHelper.runOnReadableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {

                final Cursor cursor = db.rawQuery(FETCH_BY_ID, new String[] {
                        String.valueOf(logID)
                });
                while (cursor.moveToNext()) {
                    geoKretLogs.add(new GeoKret(cursor, 1));
                }
                cursor.close();
                return true;
            }
        });
        return geoKretLogs.isEmpty() ? null : geoKretLogs.getFirst();
    }

    public GeoKret[] loadInventory(final long id) {
        final LinkedList<GeoKret> geoKrets = new LinkedList<GeoKret>();
        dbHelper.runOnReadableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {

                final Cursor cursor = createLoadByUserIDCurosr(db, id);
                while (cursor.moveToNext()) {
                    geoKrets.add(new GeoKret(cursor, 1));
                }
                cursor.close();
                return true;
            }
        });
        return geoKrets.toArray(new GeoKret[geoKrets.size()]);
    }

    public List<String> loadNeedUpdateList() {
        final LinkedList<String> gks = new LinkedList<String>();
        dbHelper.runOnReadableDatabase(new GeoKretySQLiteHelper.DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {
                final Cursor cursor = db.query(TABLE, new String[] {
                        COLUMN_TRACKING_CODE
                }, COLUMN_STICKY + " = 1 AND " + COLUMN_USER_ID, null, null, null, null);

                while (cursor.moveToNext()) {
                    gks.add(cursor.getString(0));
                }
                cursor.close();
                return true;
            }
        });
        return gks;
    }

    public List<String> loadStickyList(final long userId) {
        final LinkedList<String> gks = new LinkedList<String>();
        dbHelper.runOnReadableDatabase(new GeoKretySQLiteHelper.DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {
                final Cursor cursor = db.query(TABLE, new String[] {
                        COLUMN_TRACKING_CODE
                }, COLUMN_STICKY + " != 0 AND " + COLUMN_USER_ID + " = ?", new String[] {
                        Long.toString(userId)
                }, null, null, null);
                while (cursor.moveToNext()) {
                    gks.add(cursor.getString(0));
                }
                cursor.close();
                return true;
            }
        });
        return gks;
    }

    public void storeInventory(final Collection<GeoKret> geoKretList, final long userId,
            final boolean removeNoSticky, final String... trackingCodesToRemove) {
        dbHelper.runOnWritableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {
                if (removeNoSticky) {
                    remove(db,
                            TABLE,
                            COLUMN_USER_ID + " = ? AND " + COLUMN_STICKY + " = 0",
                            String.valueOf(userId));
                }

                for (final String trackingCodeToRemove : trackingCodesToRemove) {
                    remove(db,
                            TABLE,
                            COLUMN_USER_ID + " = ? AND " + COLUMN_TRACKING_CODE + " = ?",
                            String.valueOf(userId), trackingCodeToRemove);
                }

                final LinkedList<ContentValues> cv = new LinkedList<ContentValues>();
                for (final GeoKret geoKret : geoKretList) {
                    remove(db,
                            TABLE,
                            COLUMN_USER_ID + " = ? AND " + COLUMN_TRACKING_CODE + " = ?",
                            String.valueOf(userId), geoKret.getTrackingCode());
                    cv.add(getValues(geoKret, userId));
                }
                persistAll(db, TABLE, cv);

                appendUnsendedGrabbed(db, userId);

                return true;
            }
        });
    }

}
