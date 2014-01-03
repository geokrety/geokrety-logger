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

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GeoKretySQLiteHelper extends SQLiteOpenHelper {



	public static final String TABLE_INVENTORY = "inventory";
	public static final String COLUMN_GEOKRET_CODE = "geokret_code";
	public static final String COLUMN_TRACKING_CODE = "tracking_code";
	// public static final String COLUMN_USER_ID = "user_id";
	// public static final String COLUMN_NAME = "name";
	public static final String COLUMN_OWNER_ID = "owner_id";
	public static final String COLUMN_DISTANCE = "dist";
	public static final String COLUMN_STICKY = "sticky";

	/*
	 private int id;
	private int dist;
	private int owner_id;
	private Integer state;
	private int type;
	private String name;
	private String nr;
	 */
	
	private static final String DATABASE_NAME = "geokrety.db";
	private static final int DATABASE_VERSION = 2;

	// Database creation sql statement
	private static final String DATABASE_CREATE_V1 = AccountDataSource.TABLE_CREATE;

	private static final String DATABASE_CREATE_V2 = //
			GeocacheDataSource.TABLE_CREATE + //
			GeocacheLogDataSource.TABLE_CREATE + //
			GeoKretDataSource.TABLE_CODE;

	public GeoKretySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE_V1);
		db.execSQL(DATABASE_CREATE_V2);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion == 2) {
			db.execSQL(DATABASE_CREATE_V2);
		}
	}

	/*public long persist(String table, ContentValues values) {
		SQLiteDatabase db = getWritableDatabase();

		db.beginTransaction();
		long id = db.insertOrThrow(table, null, values);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();

		return id;
	}

	public List<Long> persistAll(String table, List<ContentValues> values) {
		LinkedList<Long> ret = new LinkedList<Long>();
		SQLiteDatabase db = getWritableDatabase();

		db.beginTransaction();
		for (ContentValues cv : values) {
			long id = db.insertOrThrow(table, null, cv);
			ret.add(id);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();

		return ret;
	}

	public void merge(String table, String whereClause, ContentValues values,
			String... whereArgs) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		db.update(table, values, whereClause, whereArgs);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	public void mergeSimple(String table, ContentValues values,
			String pkColumn, String pkValue) {
		merge(table, pkColumn + " = ?", values, pkValue);
	}

	public void removeSimple(String table, String pkColumn, String pkValue) {
		remove(table, pkColumn + " = ?", pkValue);
	}

	public void remove(String table, String whereClause, String... whereArgs) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		db.delete(table, whereClause, whereArgs);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}*/

	public boolean runOnReadableDatabase(DBOperation operation) {
		if (!operation.preTransaction()) {
			return false;
		}
		SQLiteDatabase db = getReadableDatabase();
		boolean ret = operation.inTransaction(db);
		db.close();
		if (ret) {
			operation.postCommit();
			return true;
		} else {
			operation.postRollback();
			return false;
		}
	}

	public boolean runOnWritableDatabase(DBOperation operation) {
		if (!operation.preTransaction()) {
			return false;
		}
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		boolean ret = operation.inTransaction(db);
		if (ret) {
			db.setTransactionSuccessful();
			db.endTransaction();
			db.close();
			operation.postCommit();
			return true;
		} else {
			db.endTransaction();
			db.close();
			operation.postRollback();
			return false;
		}
	}

	public static abstract class DBOperation {
		public boolean preTransaction() {
			return true;
		}

		public abstract boolean inTransaction(SQLiteDatabase db);

		public void postCommit() {

		}

		public void postRollback() {

		}

		public static long persist(SQLiteDatabase db, String table,
				ContentValues values) {
			return db.insertOrThrow(table, null, values);
		}

		public static List<Long> persistAll(SQLiteDatabase db, String table,
				List<ContentValues> values) {
			LinkedList<Long> ret = new LinkedList<Long>();
			for (ContentValues cv : values) {
				ret.add(db.insertOrThrow(table, null, cv));
			}
			return ret;
		}

		public static void merge(SQLiteDatabase db, String table,
				String whereClause, ContentValues values, String... whereArgs) {
			db.update(table, values, whereClause, whereArgs);
		}

		public static void mergeSimple(SQLiteDatabase db, String table,
				ContentValues values, String pkColumn, String pkValue) {
			merge(db, table, pkColumn + " = ?", values, pkValue);
		}

		public static void removeSimple(SQLiteDatabase db, String table,
				String pkColumn, String pkValue) {
			remove(db, table, pkColumn + " = ?", pkValue);
		}

		public static void remove(SQLiteDatabase db, String table,
				String whereClause, String... whereArgs) {
			db.delete(table, whereClause, whereArgs);
		}
	}
}
