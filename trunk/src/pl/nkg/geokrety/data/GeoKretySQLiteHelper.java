package pl.nkg.geokrety.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GeoKretySQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_USERS = "users";
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_USER_NAME = "name";
	public static final String COLUMN_SECID = "secid";
	public static final String COLUMN_UUIDS = "uuids";

	private static final String DATABASE_NAME = "geokrety.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "CREATE TABLE " //
			+ TABLE_USERS + "(" //
			+ COLUMN_USER_ID + " INTEGER PRIMARY KEY autoincrement, " //
			+ COLUMN_USER_NAME + " TEXT NOT NULL, " //
			+ COLUMN_SECID + " TEXT NOT NULL, " //
			+ COLUMN_UUIDS + " TEXT NOT NULL" //
			+ ");";

	public GeoKretySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
