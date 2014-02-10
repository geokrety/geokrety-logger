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

import java.util.ArrayList;
import java.util.List;

import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.data.GeoKretySQLiteHelper.DBOperation;
import pl.nkg.lib.okapi.SupportedOKAPI;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class UserDataSource {

    public static final String TABLE = "users";
    public static final String COLUMN_ID = GeoKretySQLiteHelper.COLUMNT_ID;
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_SECID = "secid";
    public static final String COLUMN_UUIDS = "uuids";
    public static final String COLUMN_OCLOGINS = "oc_logins";
    // TODO: COLUMN_REFRESH is not used since 0.6.0
    public static final String COLUMN_REFRESH = "refresh";
    public static final String COLUMN_HOME_LON = "home_lon";
    public static final String COLUMN_HOME_LAT = "home_lat";

    public static final String TABLE_CREATE;

    private final GeoKretySQLiteHelper dbHelper;
    private final static String DELIMITER = ";";

    private static final String FETCH_ALL;

    static {
        TABLE_CREATE = "CREATE TABLE " //
                + TABLE + "(" //
                + COLUMN_ID + " INTEGER PRIMARY KEY autoincrement, " //
                + COLUMN_USER_NAME + " TEXT NOT NULL, " //
                + COLUMN_SECID + " TEXT NOT NULL, " //
                + COLUMN_UUIDS + " TEXT NOT NULL, " //
                + COLUMN_OCLOGINS + " TEXT NOT NULL DEFAULT '', " //
                + COLUMN_REFRESH + " INTEGER NOT NULL DEFAULT 0," //
                + COLUMN_HOME_LAT + " TEXT NOT NULL DEFAULT ''," //
                + COLUMN_HOME_LON + " TEXT NOT NULL DEFAULT ''" //
                + "); ";

        FETCH_ALL = "SELECT " //
                + COLUMN_ID + ", " //
                + COLUMN_USER_NAME + ", " //
                + COLUMN_SECID + ", " //
                + COLUMN_UUIDS + ", "//
                + COLUMN_OCLOGINS + ", "//
                + COLUMN_REFRESH + ", " //
                + COLUMN_HOME_LAT + ", " //
                + COLUMN_HOME_LON //
                + " FROM " //
                + TABLE //
                + " ORDER BY " + COLUMN_USER_NAME;
    }

    public UserDataSource(final GeoKretySQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<User> getAll() {
        final ArrayList<User> accounts = new ArrayList<User>();
        dbHelper.runOnReadableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {
                final Cursor cursor = db.rawQuery(FETCH_ALL, new String[] {});
                while (cursor.moveToNext()) {
                    final User account = new User(//
                            cursor.getInt(0), //
                            cursor.getString(1), //
                            cursor.getString(2), //
                            extractUUIDs(cursor.getString(3)),
                            extractUUIDs(cursor.getString(4)));

                    account.setHomeCordLat(cursor.getString(6));
                    account.setHomeCordLon(cursor.getString(7));

                    accounts.add(account);
                }
                cursor.close();
                return true;
            }
        });
        return accounts;
    }

    public void merge(final User account) {
        dbHelper.runOnWritableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {
                mergeSimple(db, TABLE, getValues(account), account.getID());
                return true;
            }
        });
    }

    public void persist(final User account) {
        dbHelper.runOnWritableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {
                final int id = (int) persist(db, TABLE, getValues(account));
                account.setID(id);
                return true;
            }
        });
    }

    public void remove(final long id) {
        dbHelper.runOnWritableDatabase(new DBOperation() {

            @Override
            public boolean inTransaction(final SQLiteDatabase db) {
                removeSimple(db, TABLE, id);
                return true;
            }
        });
    }

    private String[] extractUUIDs(final String uuids) {
        final String[] ret = new String[SupportedOKAPI.SUPPORTED.length];
        if (Utils.isEmpty(uuids)) {
            return ret;
        }
        final String[] parsed = TextUtils.split(uuids, DELIMITER);
        for (int i = 0; i < Math.min(parsed.length, ret.length); i++) {
            ret[i] = parsed[i];
        }
        return ret;
    }

    private ContentValues getValues(final User account) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, account.getName());
        values.put(COLUMN_SECID, account.getGeoKreySecredID());
        values.put(COLUMN_UUIDS, joinUUIDs(account.getOpenCachingUUIDs()));
        values.put(COLUMN_OCLOGINS, joinUUIDs(account.getOpenCachingLogins()));
        values.put(COLUMN_HOME_LAT, account.getHomeCordLat());
        values.put(COLUMN_HOME_LON, account.getHomeCordLon());
        return values;
    }

    private String joinUUIDs(final String[] uuids) {
        if (uuids == null) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        for (final String s : uuids) {
            if (!Utils.isEmpty(s)) {
                sb.append(s);
            }
            sb.append(DELIMITER);
        }

        return sb.toString();
    }
}
