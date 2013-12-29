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

import java.util.ArrayList;
import java.util.List;

import pl.nkg.geokrety.Utils;
import pl.nkg.lib.okapi.SupportedOKAPI;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class AccountDataSource {
	private GeoKretySQLiteHelper dbHelper;
	private final static String DELIMITER = ";";

	private static final String FETCH_ALL = "SELECT " //
			+ GeoKretySQLiteHelper.COLUMN_USER_ID + ", " //
			+ GeoKretySQLiteHelper.COLUMN_USER_NAME + ", " //
			+ GeoKretySQLiteHelper.COLUMN_SECID + ", " //
			+ GeoKretySQLiteHelper.COLUMN_UUIDS //
			+ " FROM " //
			+ GeoKretySQLiteHelper.TABLE_USERS //
			+ " ORDER BY " + GeoKretySQLiteHelper.COLUMN_USER_NAME;

	public AccountDataSource(Context context) {
		dbHelper = new GeoKretySQLiteHelper(context);
	}

	private ContentValues getValues(Account account) {
		ContentValues values = new ContentValues();
		values.put(GeoKretySQLiteHelper.COLUMN_USER_NAME, account.getName());
		values.put(GeoKretySQLiteHelper.COLUMN_SECID,
				account.getGeoKreySecredID());
		values.put(GeoKretySQLiteHelper.COLUMN_UUIDS,
				joinUUIDs(account.getOpenCachingUUIDs()));
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

	public void persistAccount(Account account) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = getValues(account);

		db.beginTransaction();
		long userID = db.insert(GeoKretySQLiteHelper.TABLE_USERS, null, values);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();

		account.setID(userID);
	}

	public void mergeAccount(Account account) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = getValues(account);
		db.beginTransaction();
		db.update(GeoKretySQLiteHelper.TABLE_USERS, values,
				GeoKretySQLiteHelper.COLUMN_USER_ID + " = ?",
				new String[] { String.valueOf(account.getID()) });
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	public void removeAccount(long id) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		db.delete(GeoKretySQLiteHelper.TABLE_USERS,
				GeoKretySQLiteHelper.COLUMN_USER_ID + " = ?",
				new String[] { String.valueOf(id) });
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	public List<Account> getAllAccounts() {
		ArrayList<Account> accounts = new ArrayList<Account>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(FETCH_ALL, new String[] {});
		while (cursor.moveToNext()) {
			Account account = new Account(cursor.getLong(0),
					cursor.getString(1), cursor.getString(2),
					extractUUIDs(cursor.getString(3)));
			accounts.add(account);
		}
		cursor.close();
		return accounts;
	}
}
