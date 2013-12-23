package pl.nkg.geokrety.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GeoKretySQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_USERS = "users";
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_USER_NAME = "name";
	public static final String COLUMN_USER_PASSWORD = "password"; // TODO: will be deprecated in future
	public static final String COLUMN_SECID = "secid";

	public static final String TABLE_OPENCACHING_LOGINS = "opencaching";
	public static final String COLUMN_OPENCACHING_UUID = "uuid";
	public static final String COLUMN_OPENCACHING_LOGIN = "login";
	public static final String COLUMN_OPENCACHING_SERVICE = "service";
	

	private static final String DATABASE_NAME = "geokrety.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "CREATE TABLE "
			+ TABLE_USERS + "(" + COLUMN_USER_ID
			+ " integer PRIMARY KEY autoincrement, " + COLUMN_USER_NAME
			+ " VARCHAR(255) NOT NULL, " + COLUMN_SECID + " CHAR(128) NOT NULL);" + "CREATE TABLE "
			+ TABLE_OPENCACHING_LOGINS + "(" + COLUMN_USER_ID
			+ " integer NOT NULL, " + COLUMN_OPENCACHING_UUID
			+ " VARCHAR(255) NOT NULL, " + COLUMN_OPENCACHING_SERVICE + " integer NOT NULL);";

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
