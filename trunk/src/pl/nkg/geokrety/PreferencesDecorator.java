package pl.nkg.geokrety;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.ListView;

public class PreferencesDecorator {
	private final SharedPreferences preferences;
	private List<String> logins;
	private List<String> passwords;

	public PreferencesDecorator(ContextWrapper content) {
		preferences = content.getSharedPreferences("pl.nkg.geokrety",
				Context.MODE_PRIVATE);
	}

	public int getCurrentAccount() {
		return preferences.getInt("current_accounts", ListView.INVALID_POSITION);
	}

	public void setCurrentAccount(int nr) {
		preferences.edit().putInt("current_accounts", nr).commit();
	}

	public String getAccountLogin() {
		return getAccountLogin(getCurrentAccount());
	}

	public String getAccountPassword() {
		return getAccountPassword(getCurrentAccount());

	}

	private void updateLogins() {
		if (logins == null) {
			logins = getSplited("accounts_logins");
		}
	}

	private void updatePasswords() {
		if (passwords == null) {
			passwords = getSplited("accounts_passwords");
		}
	}
	
	private List<String> getSplited(String name) {
		String str = preferences.getString(name, "");
		if (str.length() == 0) {
			return new LinkedList<String>();
		}
		return new ArrayList<String>(Arrays.asList(str.split("/")));
	}

	public String getAccountLogin(int nr) {
		updateLogins();
		return Utils.decode(logins.get(nr));
	}

	public String getAccountPassword(int nr) {
		updatePasswords();
		return Utils.decode(passwords.get(nr));
	}

	public void setAccountLogin(int nr, String value) {
		updateLogins();
		logins.set(nr, Utils.encode(value));
	}

	public void setAccountPassword(int nr, String value) {
		updatePasswords();
		passwords.set(nr, Utils.encode(value));
	}

	public void addAccount(String login, String password) {
		updateLogins();
		updatePasswords();
		logins.add(login);
		passwords.add(password);
	}

	public void flushAccount() {
		updateLogins();
		updatePasswords();
		preferences.edit()
				.putString("accounts_logins", TextUtils.join("/", logins))
				.putString("accounts_passwords", TextUtils.join("/", passwords))
				.commit();
	}

	public void commit() {
		preferences.edit().commit();
	}

	public List<String> getAccountList() {
		updateLogins();
		return logins;
	}

	public void removeAccount(int nr) {
		updateLogins();
		updatePasswords();
		logins.remove(nr);
		passwords.remove(nr);
	}
}
