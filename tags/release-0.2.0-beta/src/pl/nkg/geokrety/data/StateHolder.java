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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ListView;

public class StateHolder {
	private static final String DEFAULT_ACCOUNT = "current_accounts";
	private static final int DEFAULT_ACCOUNT_VALUE = ListView.INVALID_POSITION;

	private static Map<String, Geocache> geoCachesMap;

	private List<Account> accountList;
	private int defaultAccount;

	private final AccountDataSource dataSource;

	public StateHolder(Context context) {
		dataSource = new AccountDataSource(context);
		accountList = dataSource.getAllAccounts();
	}

	public AccountDataSource getAccountDataSource() {
		return dataSource;
	}

	public void storeDefaultAccount(Context context) {
		getPreferences(context).edit().putInt(DEFAULT_ACCOUNT, defaultAccount)
				.commit();
	}

	private static SharedPreferences getPreferences(Context context) {
		return context.getSharedPreferences("pl.nkg.geokrety",
				Context.MODE_PRIVATE);
	}

	public List<Account> getAccountList() {
		return accountList;
	}

	public int getDefaultAccount() {
		return accountList.size() > 0 ? (defaultAccount < accountList.size()
				&& defaultAccount >= 0 ? defaultAccount : 0)
				: DEFAULT_ACCOUNT_VALUE;
	}

	public void setDefaultAccount(int defaultAccount) {
		this.defaultAccount = defaultAccount;
	}

	public static Map<String, Geocache> getGeoacheMap() {
		if (geoCachesMap == null) {
			geoCachesMap = Collections
					.synchronizedMap(new HashMap<String, Geocache>());
		}
		return geoCachesMap;
	}

	public Account getAccountByID(long id) {
		for (Account account : getAccountList()) {
			if (account.getID() == id) {
				return account;
			}
		}
		return null;
	}
}
