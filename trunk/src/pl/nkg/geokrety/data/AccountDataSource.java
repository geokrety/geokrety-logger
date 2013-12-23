package pl.nkg.geokrety.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class AccountDataSource {
	// private SQLiteDatabase database;
	private GeoKretySQLiteHelper dbHelper;
	/*
	 * private String[] allColumns = { GeoKretySQLiteHelper.COLUMN_USER_ID,
	 * GeoKretySQLiteHelper.COLUMN_USER_NAME, GeoKretySQLiteHelper.COLUMN_SECID
	 * };
	 */
	private static final String FETCH_ALL = "SELECT " +
			"u." + GeoKretySQLiteHelper.COLUMN_USER_ID + ", " +
					"u." + GeoKretySQLiteHelper.COLUMN_USER_NAME + "," +
					"u." + GeoKretySQLiteHelper.COLUMN_SECID + "," +
							" o. " + GeoKretySQLiteHelper.COLUMN_OPENCACHING_UUID + "," + 
							" o. " + GeoKretySQLiteHelper.COLUMN_OPENCACHING_SERVICE + 
							"FROM "
			+ GeoKretySQLiteHelper.TABLE_USERS + " u LEFT JOIN "
			+ GeoKretySQLiteHelper.TABLE_OPENCACHING_LOGINS + " o ON u."
			+ GeoKretySQLiteHelper.COLUMN_USER_ID + " = o."
			+ GeoKretySQLiteHelper.COLUMN_USER_ID + " ORDER BY u."
			+ GeoKretySQLiteHelper.COLUMN_USER_NAME;

	public AccountDataSource(Context context) {
		dbHelper = new GeoKretySQLiteHelper(context);
	}

	private ContentValues getAccountValues(Account account) {
		ContentValues values = new ContentValues();
		values.put(GeoKretySQLiteHelper.COLUMN_USER_NAME,
				account.getGeoKretyLogin());
		values.put(GeoKretySQLiteHelper.COLUMN_SECID,
				account.getGeoKreySecredID());
		return values;
	}

	private ContentValues[] getOCValues(Account account, long id) {
		ContentValues values = new ContentValues();
		values.put(GeoKretySQLiteHelper.COLUMN_USER_ID, id);
		values.put(GeoKretySQLiteHelper.COLUMN_OPENCACHING_UUID,
				account.getOpenCachingUUID());
		values.put(GeoKretySQLiteHelper.COLUMN_OPENCACHING_SERVICE, 1);
		return new ContentValues[] { values };
	}

	public void persistAccount(Account account) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = getAccountValues(account);

		db.beginTransaction();

		long userID = db.insert(GeoKretySQLiteHelper.TABLE_USERS, null, values);
		insertOCServices(db, account, userID);

		db.endTransaction();
		db.close();

		account.setID(userID);
	}

	private void insertOCServices(SQLiteDatabase db, Account account, long id) {
		for (ContentValues oc : getOCValues(account, id)) {
			db.insert(GeoKretySQLiteHelper.TABLE_OPENCACHING_LOGINS, null, oc);
		}
	}

	public void mergeAccount(Account account) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = getAccountValues(account);
		db.beginTransaction();
		db.update(GeoKretySQLiteHelper.TABLE_USERS, values,
				GeoKretySQLiteHelper.COLUMN_USER_ID + " = ?",
				new String[] { String.valueOf(account.getID()) });
		db.delete(GeoKretySQLiteHelper.TABLE_OPENCACHING_LOGINS,
				GeoKretySQLiteHelper.COLUMN_USER_ID + " = ?",
				new String[] { String.valueOf(account.getID()) });
		insertOCServices(db, account, account.getID());
		db.endTransaction();
		db.close();
	}

	public void removeAccount(long id) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		db.delete(GeoKretySQLiteHelper.TABLE_OPENCACHING_LOGINS,
				GeoKretySQLiteHelper.COLUMN_USER_ID + " = ?",
				new String[] { String.valueOf(id) });
		db.delete(GeoKretySQLiteHelper.TABLE_USERS,
				GeoKretySQLiteHelper.COLUMN_USER_ID + " = ?",
				new String[] { String.valueOf(id) });
		db.endTransaction();
		db.close();
	}

	public List<Account> getAllAccounts() {
		ArrayList<Account> accounts = new ArrayList<Account>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(FETCH_ALL, new String[] {});
		while (cursor.moveToNext()) {
			Account account = new Account(cursor.getString(1), cursor.getString(2), cursor.getString(3));
			account.setID(cursor.getLong(0));
		}
		return accounts;
	}
}
