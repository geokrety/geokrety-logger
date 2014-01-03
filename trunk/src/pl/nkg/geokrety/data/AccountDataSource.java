/*
 * Copyright (C) 2013, 2014 Michał Niedźwiecki
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.data.GeoKretySQLiteHelper.DBOperation;
import pl.nkg.lib.okapi.SupportedOKAPI;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class AccountDataSource {

	public static final String TABLE = "users";
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_USER_NAME = "name";
	public static final String COLUMN_SECID = "secid";
	public static final String COLUMN_UUIDS = "uuids";
	public static final String COLUMN_REFRESH = "refresh";

	public static final String TABLE_CREATE = "CREATE TABLE " //
			+ TABLE + "(" //
			+ COLUMN_USER_ID + " INTEGER PRIMARY KEY autoincrement, " //
			+ COLUMN_USER_NAME + " TEXT NOT NULL, " //
			+ COLUMN_SECID + " TEXT NOT NULL, " //
			+ COLUMN_UUIDS + " TEXT NOT NULL, " //
			+ COLUMN_REFRESH + " INTEGER NOT NULL" //
			+ "); ";

	private GeoKretySQLiteHelper dbHelper;
	private final static String DELIMITER = ";";
	private final static String PK_COLUMN = COLUMN_USER_ID;

	private static final String FETCH_ALL = "SELECT " //
			+ PK_COLUMN + ", " //
			+ COLUMN_USER_NAME + ", " //
			+ COLUMN_SECID + ", " //
			+ COLUMN_UUIDS + ", "//
			+ COLUMN_REFRESH //
			+ " FROM " //
			+ TABLE //
			+ " ORDER BY " + COLUMN_USER_NAME;

	public AccountDataSource(GeoKretySQLiteHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	private ContentValues getValues(Account account) {
		Date lastLoadedDate = account.getLastDataLoaded();
		long storedLastLoadedDate = lastLoadedDate == null ? 0 : lastLoadedDate.getTime();
		
		ContentValues values = new ContentValues();
		values.put(COLUMN_USER_NAME, account.getName());
		values.put(COLUMN_SECID, account.getGeoKreySecredID());
		values.put(COLUMN_UUIDS, joinUUIDs(account.getOpenCachingUUIDs()));
		values.put(COLUMN_REFRESH, storedLastLoadedDate);
		return values;
	}

	private String joinUUIDs(String[] uuids) {
		if (uuids == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (String s : uuids) {
			if (!Utils.isEmpty(s)) {
				sb.append(s);
			}
			sb.append(DELIMITER);
		}

		return sb.toString();
	}

	private String[] extractUUIDs(String uuids) {
		String[] ret = new String[SupportedOKAPI.SUPPORTED.length];
		if (Utils.isEmpty(uuids)) {
			return ret;
		}
		String[] parsed = TextUtils.split(uuids, DELIMITER);
		for (int i = 0; i < Math.min(parsed.length, ret.length); i++) {
			ret[i] = parsed[i];
		}
		return ret;
	}

	public void persist(final Account account) {
		dbHelper.runOnWritableDatabase(new DBOperation() {

			@Override
			public boolean inTransaction(SQLiteDatabase db) {
				int id = (int) persist(db, TABLE, getValues(account));
				account.setID(id);
				return true;
			}
		});
	}

	public void merge(final Account account) {
		dbHelper.runOnWritableDatabase(new DBOperation() {

			@Override
			public boolean inTransaction(SQLiteDatabase db) {
				mergeSimple(db, TABLE, getValues(account), PK_COLUMN,
						String.valueOf(account.getID()));
				return true;
			}
		});
	}

	public void storeLastLoadedDate(final Account account) {
		dbHelper.runOnWritableDatabase(new DBOperation() {

			@Override
			public boolean inTransaction(SQLiteDatabase db) {
				ContentValues values = new ContentValues();
				values.put(COLUMN_REFRESH, account.getLastDataLoaded()
						.getTime());
				mergeSimple(db, TABLE, getValues(account), PK_COLUMN,
						String.valueOf(values));
				return true;
			}
		});
	}

	public void remove(final long id) {
		dbHelper.runOnWritableDatabase(new DBOperation() {

			@Override
			public boolean inTransaction(SQLiteDatabase db) {
				removeSimple(db, TABLE, PK_COLUMN, String.valueOf(id));
				return true;
			}
		});
	}

	public List<Account> getAll() {
		final ArrayList<Account> accounts = new ArrayList<Account>();
		dbHelper.runOnReadableDatabase(new DBOperation() {

			@Override
			public boolean inTransaction(SQLiteDatabase db) {
				Cursor cursor = db.rawQuery(FETCH_ALL, new String[] {});
				while (cursor.moveToNext()) {
					Account account = new Account(//
							cursor.getInt(0), //
							cursor.getString(1), //
							cursor.getString(2), //
							extractUUIDs(cursor.getString(3)));
					
					long time = cursor.getLong(4);
					if (time > 0) {
						account.setLastDataLoaded(new Date(time));
					}
					
					accounts.add(account);
				}
				cursor.close();
				return true;
			}
		});
		return accounts;
	}
}
