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
