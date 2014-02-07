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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import pl.nkg.geokrety.GeoKretyApplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

/**
 * Create, update and elementary methods for GeoKrety Logger database.<br/>
 * Activity snippet:<br/>
 * <code>
 * ...
 * private SQLiteDatabase database;
 * ...
 * ...
 * protected void onResume() {
 * ...
 *     database = stateHolder.getDbHelper().openDatabase();
 * ...
 * ...
 * protected void onPause() {
 * ...
 *     closeCursorIfOpened();
 *     stateHolder.getDbHelper().closeDatabase();
 * ...
 * </code>
 * 
 * @author Michał Niedźwiecki
 */
public class GeoKretySQLiteHelper extends SQLiteOpenHelper {

    public static abstract class DBOperation {
        public static void merge(final SQLiteDatabase db, final String table,
                final String whereClause, final ContentValues values, final String... whereArgs) {
            db.update(table, values, whereClause, whereArgs);
        }

        public static void mergeSimple(final SQLiteDatabase db, final String table,
                final ContentValues values, final long id) {
            merge(db, table, COLUMNT_ID + " = ?", values, String.valueOf(id));
        }

        public static long persist(final SQLiteDatabase db, final String table,
                final ContentValues values) {
            return db.insertOrThrow(table, null, values);
        }

        public static List<Long> persistAll(final SQLiteDatabase db, final String table,
                final List<ContentValues> values) {
            final LinkedList<Long> ret = new LinkedList<Long>();
            for (final ContentValues cv : values) {
                ret.add(db.insertOrThrow(table, null, cv));
            }
            return ret;
        }

        public static void remove(final SQLiteDatabase db, final String table,
                final String whereClause, final String... whereArgs) {
            db.delete(table, whereClause, whereArgs);
        }

        public static void removeSimple(final SQLiteDatabase db, final String table, final long id) {
            remove(db, table, COLUMNT_ID + " = ?", String.valueOf(id));
        }

        public abstract boolean inTransaction(SQLiteDatabase db);

        public void postCommit() {

        }

        public void postRollback() {

        }

        public boolean preTransaction() {
            return true;
        }
    }

    public static final String COLUMNT_ID = "_id";
    private static final String DATABASE_NAME = "geokrety.db";

    private static final int DATABASE_VERSION = 7;

    private final AtomicInteger openCounter;
    private SQLiteDatabase dataBase;

    public GeoKretySQLiteHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        openCounter = new AtomicInteger(0);
    }

    public void closeDatabase() {
        synchronized (this) {
            if (openCounter.decrementAndGet() == 0) {
                dataBase.close();
            }
        }
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(UserDataSource.TABLE_CREATE);
        db.execSQL(GeoKretLogDataSource.TABLE_CREATE);
        db.execSQL(GeocacheDataSource.TABLE_CREATE);
        db.execSQL(GeocacheLogDataSource.TABLE_CREATE);
        db.execSQL(InventoryDataSource.TABLE_CREATE);
        db.execSQL(GeoKretDataSource.TABLE_CREATE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        if (oldVersion <= 5) {
            dropUncompatible6Tables(db);
            db.execSQL(GeoKretLogDataSource.TABLE_CREATE);
            db.execSQL(GeocacheDataSource.TABLE_CREATE);
            db.execSQL(GeocacheLogDataSource.TABLE_CREATE);
            db.execSQL(InventoryDataSource.TABLE_CREATE);
            importUsersFromOlderThan6(db, oldVersion);
        }

        if (oldVersion <= 6) {
            importInventoryFromOlderThan7(db, oldVersion);
            db.execSQL(GeoKretDataSource.TABLE_CREATE);
        }
    }

    public SQLiteDatabase openDatabase() {
        synchronized (this) {
            if (openCounter.incrementAndGet() == 1) {
                dataBase = getWritableDatabase();
            }
            return dataBase;
        }
    }

    public boolean runOnReadableDatabase(final DBOperation operation) {
        if (!operation.preTransaction()) {
            return false;
        }
        final SQLiteDatabase db = openDatabase();
        final boolean ret = operation.inTransaction(db);
        closeDatabase();
        if (ret) {
            operation.postCommit();
            return true;
        } else {
            operation.postRollback();
            return false;
        }
    }

    public boolean runOnWritableDatabase(final DBOperation operation) {
        if (!operation.preTransaction()) {
            return false;
        }
        final SQLiteDatabase db = openDatabase();
        db.beginTransaction();
        final boolean ret = operation.inTransaction(db);
        if (ret) {
            db.setTransactionSuccessful();
            db.endTransaction();
            closeDatabase();
            operation.postCommit();
            return true;
        } else {
            db.endTransaction();
            closeDatabase();
            operation.postRollback();
            return false;
        }
    }

    private void dropTableIfExist(final SQLiteDatabase db, final String tableName) {
        db.execSQL("DROP TABLE IF EXISTS " + tableName + ";");
    }

    private void dropUncompatible6Tables(final SQLiteDatabase db) {
        dropTableIfExist(db, GeocacheDataSource.TABLE);
        dropTableIfExist(db, GeocacheLogDataSource.TABLE);
        dropTableIfExist(db, InventoryDataSource.TABLE);
        dropTableIfExist(db, GeoKretLogDataSource.TABLE);
    }

    private void importUsersFromOlderThan6(final SQLiteDatabase db, final int oldVersion) {
        db.execSQL("ALTER TABLE " + UserDataSource.TABLE + " RENAME TO tmp_" + UserDataSource.TABLE
                + ";");
        db.execSQL(UserDataSource.TABLE_CREATE);

        final List<String> oldColumns = new LinkedList<String>(Arrays.asList("user_id", //
                UserDataSource.COLUMN_SECID, //
                UserDataSource.COLUMN_USER_NAME, //
                UserDataSource.COLUMN_UUIDS));

        final List<String> newColumns = new LinkedList<String>(Arrays.asList(
                UserDataSource.COLUMN_ID, //
                UserDataSource.COLUMN_SECID, //
                UserDataSource.COLUMN_USER_NAME, //
                UserDataSource.COLUMN_UUIDS));

        if (oldVersion >= 3) {
            oldColumns.add(UserDataSource.COLUMN_HOME_LAT);
            oldColumns.add(UserDataSource.COLUMN_HOME_LON);

            newColumns.add(UserDataSource.COLUMN_HOME_LAT);
            newColumns.add(UserDataSource.COLUMN_HOME_LON);
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO " + UserDataSource.TABLE + "(");
        sb.append(TextUtils.join(", ", newColumns));
        sb.append(") SELECT ");
        sb.append(TextUtils.join(", ", oldColumns));
        sb.append(" FROM tmp_" + UserDataSource.TABLE).append(";");

        final String query = sb.toString();

        db.execSQL(query);
        dropTableIfExist(db, "tmp_" + UserDataSource.TABLE);
    }
    
    private void importInventoryFromOlderThan7(final SQLiteDatabase db, final int oldVersion) {
        db.execSQL("ALTER TABLE " + InventoryDataSource.TABLE + " RENAME TO tmp_" + InventoryDataSource.TABLE
                + ";");
        db.execSQL(InventoryDataSource.TABLE_CREATE);

        final List<String> oldColumns = new LinkedList<String>(Arrays.asList("user_id", //
                InventoryDataSource.COLUMN_USER_ID, //
                InventoryDataSource.COLUMN_STICKY, //
                InventoryDataSource.COLUMN_TRACKING_CODE));

        final List<String> newColumns = new LinkedList<String>(Arrays.asList(
                InventoryDataSource.COLUMN_ID, //
                InventoryDataSource.COLUMN_USER_ID, //
                InventoryDataSource.COLUMN_STICKY, //
                InventoryDataSource.COLUMN_TRACKING_CODE));

        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO " + InventoryDataSource.TABLE + "(");
        sb.append(TextUtils.join(", ", newColumns));
        sb.append(") SELECT ");
        sb.append(TextUtils.join(", ", oldColumns));
        sb.append(" FROM tmp_" + InventoryDataSource.TABLE).append(";");

        final String query = sb.toString();

        db.execSQL(query);
        dropTableIfExist(db, "tmp_" + InventoryDataSource.TABLE);
    }
}
