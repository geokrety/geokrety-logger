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
		if (Utils.isEmpty(uuids)) {
			return new String[SupportedOKAPI.SUPPORTED.length];
		}
		return TextUtils.split(uuids, DELIMITER);
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
		return accounts;
	}
}
