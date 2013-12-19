package pl.nkg.geokrety.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.nkg.geokrety.Utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.ListView;

public class StateHolder {
	private static StateHolder mInstance = null;
	private static final String ACCOUNTS_GKLOGINS = "accounts_logins";
	private static final String ACCOUNTS_GKPASSWORDS = "accounts_passwords";
	private static final String ACCOUNTS_OCLOGINS = "accounts_ocs";
	private static final String DEFAULT_ACCOUNT = "current_accounts";
	private static final int DEFAULT_ACCOUNT_VALUE = ListView.INVALID_POSITION;
	private static final String DEFAULT_LOGINS_VALUE = "";
	private static final String ACCOUNTS_SEPARATOR = "/";

	private static Map<String, Geocache> geoCachesMap;

	private ArrayList<Account> accountList;
	private int defaultAccount;

	private StateHolder(ContextWrapper context) {
		loadAccountList(context);
	}

	public static StateHolder getInstance(ContextWrapper context) {
		if (mInstance == null) {
			mInstance = new StateHolder(context);
		}
		return mInstance;
	}

	private void loadAccountList(ContextWrapper context) {
		SharedPreferences preferences = getPreferences(context);
		String[] gkLogins = splited(preferences.getString(ACCOUNTS_GKLOGINS,
				DEFAULT_LOGINS_VALUE));
		String[] gkPasswords = splited(preferences.getString(
				ACCOUNTS_GKPASSWORDS, DEFAULT_LOGINS_VALUE));
		String[] ocLogins = splited(preferences.getString(ACCOUNTS_OCLOGINS,
				DEFAULT_LOGINS_VALUE));
		defaultAccount = preferences.getInt(DEFAULT_ACCOUNT,
				DEFAULT_ACCOUNT_VALUE);

		accountList = new ArrayList<Account>();

		for (int i = 0; i < gkLogins.length; i++) {
			accountList.add(new Account(Utils.decode(gkLogins[i]), Utils
					.decode(gkPasswords[i]), Utils.decode(ocLogins[i])));
		}
	}

	public void storeAccountList(ContextWrapper context) {
		String[] gkLogins = new String[accountList.size()];
		String[] gkPasswords = new String[accountList.size()];
		String[] ocLogins = new String[accountList.size()];

		for (int i = 0; i < accountList.size(); i++) {
			gkLogins[i] = Utils.encode(accountList.get(i).getGeoKretyLogin());
			gkPasswords[i] = Utils.encode(accountList.get(i)
					.getGeoKretyPassword());
			ocLogins[i] = Utils
					.encode(accountList.get(i).getOpenCachingLogin());
		}

		SharedPreferences preferences = getPreferences(context);
		preferences
				.edit()
				.putString(ACCOUNTS_GKLOGINS,
						TextUtils.join(ACCOUNTS_SEPARATOR, gkLogins))
				.putString(ACCOUNTS_GKPASSWORDS,
						TextUtils.join(ACCOUNTS_SEPARATOR, gkPasswords))
				.putString(ACCOUNTS_OCLOGINS,
						TextUtils.join(ACCOUNTS_SEPARATOR, ocLogins))
				.putInt(DEFAULT_ACCOUNT, defaultAccount).commit();

	}

	public void storeDefaultAccount(ContextWrapper context) {
		getPreferences(context).edit().putInt(DEFAULT_ACCOUNT, defaultAccount)
				.commit();
	}

	private static String[] splited(String values) {
		if (values.length() == 0) {
			return new String[0];
		}
		return values.split(ACCOUNTS_SEPARATOR);
	}

	private static SharedPreferences getPreferences(ContextWrapper context) {
		return context.getSharedPreferences("pl.nkg.geokrety",
				Context.MODE_PRIVATE);
	}

	public List<Account> getAccountList() {
		return accountList;
	}

	public int getDefaultAccount() {
		return accountList.size() > 0 ? (defaultAccount < accountList.size()
				&& defaultAccount >= 0 ? defaultAccount : 0)
				: ListView.INVALID_POSITION;
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
}
