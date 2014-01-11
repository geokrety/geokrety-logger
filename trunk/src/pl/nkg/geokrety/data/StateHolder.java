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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.util.SparseArray;
import android.widget.ListView;

public class StateHolder {
	private static final String				DEFAULT_ACCOUNT			= "current_accounts";
	private static final int				DEFAULT_ACCOUNT_VALUE	= ListView.INVALID_POSITION;

	private static Map<String, Geocache>	geoCachesMap;

	public static Map<String, Geocache> getGeoacheMap() {
		if (geoCachesMap == null) {
			geoCachesMap = Collections.synchronizedMap(new HashMap<String, Geocache>());
		}
		return geoCachesMap;
	}

	private static SharedPreferences getPreferences(final Context context) {
		return context.getSharedPreferences("pl.nkg.geokrety", Context.MODE_PRIVATE);
	}

	private List<Account>				accountList;
	private int							defaultAccount;
	private final AccountDataSource		accountDataSource;
	private final GeocacheLogDataSource	geocacheLogDataSource;
	private final GeoKretDataSource		geoKretDataSource;

	private final GeocacheDataSource	geocacheDataSource;

	private final GeoKretLogDataSource	geoKretLogDataSource;
	
	private Long reservedLog;
	private Long editLog;
	
	private final GeoKretySQLiteHelper dbHelper;

	public StateHolder(final Context context) {
		dbHelper = new GeoKretySQLiteHelper(context);
		accountDataSource = new AccountDataSource(dbHelper);
		geocacheLogDataSource = new GeocacheLogDataSource(dbHelper);
		geoKretDataSource = new GeoKretDataSource(dbHelper);
		geocacheDataSource = new GeocacheDataSource(dbHelper);
		geoKretLogDataSource = new GeoKretLogDataSource(dbHelper);

		accountList = Collections.synchronizedList(accountDataSource.getAll());

		geoCachesMap = new HashMap<String, Geocache>();
		for (final Geocache gc : geocacheDataSource.load()) {
			geoCachesMap.put(gc.getCode(), gc);
		}

		final SparseArray<LinkedList<Geokret>> gks = geoKretDataSource.load();
		final SparseArray<LinkedList<GeocacheLog>> logs = geocacheLogDataSource.load();
		//final SparseArray<LinkedList<GeoKretLog>> geoKretLogs = geoKretLogDataSource.load();

		for (final Account account : accountList) {
			account.setOpenCachingLogs(logs.get((int)account.getID()));
			account.setInventory(gks.get((int)account.getID()));
			//account.setGeoKretyLogs(geoKretLogs.get(account.getID()));
		}
	}

	public GeoKretySQLiteHelper getDbHelper() {
		return dbHelper;
	}

	public Account getAccountByID(final long id) {
		for (final Account account : getAccountList()) {
			if (account.getID() == id) {
				return account;
			}
		}
		return null;
	}

	public AccountDataSource getAccountDataSource() {
		return accountDataSource;
	}

	public List<Account> getAccountList() {
		return accountList;
	}

	public int getDefaultAccount() {
		return accountList.size() > 0 //
		? defaultAccount < accountList.size() && defaultAccount >= 0 //
		? defaultAccount : 0 //
				: DEFAULT_ACCOUNT_VALUE;
	}

	public GeocacheDataSource getGeocacheDataSource() {
		return geocacheDataSource;
	}

	public GeocacheLogDataSource getGeocacheLogDataSource() {
		return geocacheLogDataSource;
	}

	public GeoKretDataSource getGeoKretDataSource() {
		return geoKretDataSource;
	}

	public GeoKretLogDataSource getGeoKretLogDataSource() {
		return geoKretLogDataSource;
	}

	public void setDefaultAccount(final int defaultAccount) {
		this.defaultAccount = defaultAccount;
	}

	public void storeDefaultAccount(final Context context) {
		getPreferences(context).edit().putInt(DEFAULT_ACCOUNT, defaultAccount).commit();
	}

	public void storeGeoCachingNames() {
		getGeocacheDataSource().store(geoCachesMap.values());
	}
	
	public boolean lockForLog(long logID) {
		synchronized(this) {
			if (editLog != null && editLog == logID) {
				return false;
			} else {
				reservedLog = logID;
				return true;
			}
		}
	}
	
	public void releaseLockForLog(long logID) {
		synchronized(this) {
			reservedLog = null;
		}
	}
	
	public boolean lockForEdit(long logID) {
		synchronized(this) {
			if (reservedLog != null && reservedLog == logID) {
				return false;
			} else {
				editLog = logID;
				return true;
			}
		}
	}
	
	public void releaseLockForEdit(long logID) {
		synchronized(this) {
			editLog = null;
		}
	}

}
