package pl.nkg.geokrety.data;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.geokrety.widgets.RefreshSuccessfulListener;

public class Account {
	private static final String URL_LOGIN = "http://geokrety.org/api-login2secid.php";
	private static final String URL_EXPORT2 = "http://geokrety.org/export2.php";
	private static final String URL_BY_USERNAME = "http://opencaching.pl/okapi/services/users/by_username";
	private static final String URL_USERLOGS = "http://opencaching.pl/okapi/services/logs/userlogs";
	private static final String URL_GEOCACHES = "http://opencaching.pl/okapi/services/caches/geocaches";
	private static final String CONSUMER_KEY = "DajjA4r3QZNRHAef7XZD";
	private static final long EXPIRED = 600000;

	private String geoKretyLogin;
	private String geoKretyPassword;
	private String openCachingLogin;

	private String geoKretySecredID;
	private String openCachingUUID;
	private ArrayList<GeocacheLog> openCachingLogs;

	private ArrayList<Geokret> inventory;

	private Date lastDataLoaded;

	public Account(String geoKretyLogin, String geoKretyPassword,
			String openCachingLogin) {
		this.geoKretyLogin = geoKretyLogin;
		this.geoKretyPassword = geoKretyPassword;
		this.openCachingLogin = openCachingLogin;
	}

	public String getGeoKretyLogin() {
		return geoKretyLogin;
	}

	public void setGeoKretyLogin(String geoKretyLogin) {
		this.geoKretyLogin = geoKretyLogin;
	}

	public String getGeoKretyPassword() {
		return geoKretyPassword;
	}

	public void setGeoKretyPassword(String geoKretyPassword) {
		this.geoKretyPassword = geoKretyPassword;
	}

	public String getGeoKreySecredID() {
		return geoKretySecredID;
	}

	public String getOpenCachingLogin() {
		return openCachingLogin;
	}

	public void setOpenCachingLogin(String openCachingLogin) {
		this.openCachingLogin = openCachingLogin;
	}

	public String getOpenCachingUUID() {
		return openCachingUUID;
	}

	public ArrayList<GeocacheLog> getOpenCachingLogs() {
		return openCachingLogs;
	}

	public ArrayList<Geokret> getInventory() {
		return inventory;
	}

	public boolean expired() {
		if (lastDataLoaded == null) {
			return true;
		}
		return new Date().getTime() - lastDataLoaded.getTime() > EXPIRED;
	}

	/*
	 * public void loadData() throws MessagedException { loadSecureID();
	 * loadInventory(); loadOpenCachingUUID(); loadOpenCachingLogs();
	 * lastDataLoaded = new Date(); }
	 */

	private void loadSecureID(AsyncTask<String, Integer, Boolean> asyncTask)
			throws MessagedException {
		String[][] postData = new String[][] {
				new String[] { "login", geoKretyLogin },
				new String[] { "password", geoKretyPassword } };

		String value;
		try {
			value = Utils.httpPost(URL_LOGIN, postData);
		} catch (Exception e) {
			throw new MessagedException(R.string.login_error_message,
					e.getLocalizedMessage());
		}

		if (value != null && !value.startsWith("error")) {
			geoKretySecredID = value.trim();
		} else {
			throw new MessagedException(R.string.login_error_password_message,
					String.valueOf(value));
		}
	}

	private void loadInventory(AsyncTask<String, Integer, Boolean> asyncTask)
			throws MessagedException {
		inventory = new ArrayList<Geokret>();

		String[][] getData = new String[][] {
				new String[] { "secid", geoKretySecredID },
				new String[] { "inventory", "1" } };
		try {
			String xml = Utils.httpGet(URL_EXPORT2, getData);
			Document doc = Utils.getDomElement(xml);

			NodeList nl = doc.getElementsByTagName("geokret");

			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				inventory.add(new Geokret(node));
			}
		} catch (Exception e) {
			throw new MessagedException(R.string.inventory_error_message);
		}
	}

	private void loadOpenCachingUUID(
			AsyncTask<String, Integer, Boolean> asyncTask)
			throws MessagedException {
		String[][] getData = new String[][] {
				new String[] { "username", openCachingLogin },
				new String[] { "fields", "uuid" },
				new String[] { "consumer_key", CONSUMER_KEY } };
		try {
			String jsonString = Utils.httpGet(URL_BY_USERNAME, getData);
			JSONObject json = new JSONObject(jsonString);
			openCachingUUID = json.getString("uuid");
		} catch (JSONException e) {
			throw new MessagedException(R.string.invalid_oclogin_error_message);
		} catch (Exception e) {
			throw new MessagedException(R.string.oclogs_error_message);
		}
	}

	private void loadOpenCachingLogs(
			AsyncTask<String, Integer, Boolean> asyncTask)
			throws MessagedException {
		openCachingLogs = new ArrayList<GeocacheLog>();
		String[][] getData = new String[][] {
				new String[] { "user_uuid", openCachingUUID },
				new String[] { "consumer_key", CONSUMER_KEY } };
		try {
			String jsonString = Utils.httpGet(URL_USERLOGS, getData);

			JSONArray json = new JSONArray(jsonString);

			for (int i = 0; i < json.length(); i++) {
				openCachingLogs.add(new GeocacheLog(json.getJSONObject(i)));
			}

			if (openCachingLogs.size() > 0) {
				loadOCnames();
			}
		} catch (JSONException e) {
			throw new MessagedException(R.string.invalid_oclogin_error_message);
		} catch (IOException e) {
			throw new MessagedException(R.string.oclogs_error_message);
		} catch (ParseException e) {
			throw new MessagedException(R.string.oclogs_error_message);
		}
	}

	private void loadOCnames() throws ClientProtocolException, IOException,
			JSONException {
		HashSet<String> codes = getCacheCodes();
		if (codes.size() == 0) {
			return;
		}

		String[][] getData = new String[][] {
				new String[] { "user_uuid", openCachingUUID },
				new String[] { "consumer_key", CONSUMER_KEY },
				new String[] { "fields", "name|code" },
				new String[] { "cache_codes", TextUtils.join("|", codes) },
				new String[] { "lpc", "0" } };

		String jsonString = Utils.httpGet(URL_GEOCACHES, getData);
		JSONObject json = new JSONObject(jsonString);
		for (String code : codes) {
			Geocache geocache = new Geocache(json.getJSONObject(code));
			StateHolder.getGeoacheMap().put(geocache.getCode(), geocache);
		}
	}

	private HashSet<String> getCacheCodes() {
		HashSet<String> caches = new HashSet<String>();
		for (GeocacheLog log : openCachingLogs) {
			if (!StateHolder.getGeoacheMap().containsKey(log.getCacheCode())) {
				caches.add(log.getCacheCode());
			}
		}
		return caches;
	}

	@Override
	public String toString() {
		return geoKretyLogin;
	}

	public void loadIfExpired(Activity context,
			RefreshSuccessfulListener listener) {
		if (expired()) {
			loadData(context, listener);
		} else {
			listener.onRefreshSuccessful();
		}
	}

	private AsyncTask<String, Integer, Boolean> refreshTask;

	public void loadData(final Activity context,
			final RefreshSuccessfulListener listener) {

		if (refreshTask != null) {
			return;
		}

		refreshTask = new AsyncTask<String, Integer, Boolean>() {
			private ProgressDialog dialog;
			private MessagedException excaption;

			@Override
			protected void onPreExecute() {
				try {
					dialog = ProgressDialog.show(context, context
							.getResources().getString(R.string.download_title),
							"", true);
				} catch (Throwable t) {
					t.printStackTrace();
					refreshTask.cancel(true);
					refreshTask = null;
				}
			}

			@Override
			protected Boolean doInBackground(String... params) {
				try {
					publishProgress(0);
					loadSecureID(this);
					publishProgress(1);
					loadInventory(this);
					publishProgress(2);
					loadOpenCachingUUID(this);
					publishProgress(3);
					loadOpenCachingLogs(this);
					lastDataLoaded = new Date();
					return true;
				} catch (MessagedException e) {
					excaption = e;
					return false;
				}

			}

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				if (result != null) {
					if (result) {
						Toast.makeText(context, R.string.download_finish,
								Toast.LENGTH_LONG).show();
						listener.onRefreshSuccessful();
					} else {
						Toast.makeText(context,
								excaption.getFormatedMessage(context),
								Toast.LENGTH_LONG).show();
					}
					try {
						dialog.dismiss();
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
				refreshTask = null;
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				try {
					switch (values[0]) {
					case 0:
						dialog.setMessage(context.getResources().getString(
								R.string.download_login_gk));
						break;

					case 1:
						dialog.setMessage(context.getResources().getString(
								R.string.download_getting_gk));
						break;

					case 2:
						dialog.setMessage(context.getResources().getString(
								R.string.download_getting_ocs));
						break;

					case 3:
						dialog.setMessage(context.getResources().getString(
								R.string.download_getting_names));
						break;
					}
				} catch (Throwable t) {
					t.printStackTrace();
					refreshTask.cancel(true);
					refreshTask = null;
				}

			}

		}.execute();
	}
}
