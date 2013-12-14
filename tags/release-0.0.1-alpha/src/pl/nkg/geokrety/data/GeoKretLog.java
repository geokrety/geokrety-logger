package pl.nkg.geokrety.data;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.format.DateFormat;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.geokrety.widgets.RefreshSuccessfulListener;
import android.text.format.Time;
import android.widget.Toast;

public class GeoKretLog {
	private static final String URL = "http://geokrety.org/ruchy.php";
	private static final String[] LOG_TYPE_MAPPIG = { "0", "1", "3", "5", "2" };

	private String secid;
	private String nr;
	private String formname = "ruchy";
	private int logtype_mapped;
	private String data;
	private int godzina;
	private int minuta;
	private String comment;
	private String app = "GeoKrety Logger";
	private String app_ver = "alpha";
	private String mobile_lang = Locale.getDefault().toString();

	private String latlon;
	private String wpt;

	private String geoKretyLogin;

	public GeoKretLog() {
		secid = "";
		nr = "";
		logtype_mapped = 0;

		setDateAndTime(new Date());
		comment = "";
		latlon = "";
		wpt = "";
	}

	public String getSecid() {
		return secid;
	}

	public void setSecid(String secid) {
		this.secid = secid;
	}

	public String getNr() {
		return nr;
	}

	public void setNr(String nr) {
		this.nr = nr;
	}

	public String getFormname() {
		return formname;
	}

	public void setFormname(String formname) {
		this.formname = formname;
	}

	public int getLogTypeMapped() {
		return logtype_mapped;
	}

	public void setLogTypeMapped(int logtype) {
		this.logtype_mapped = logtype;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public int getGodzina() {
		return godzina;
	}

	public void setGodzina(int godzina) {
		this.godzina = godzina;
	}

	public int getMinuta() {
		return minuta;
	}

	public void setMinuta(int minuta) {
		this.minuta = minuta;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getApp_ver() {
		return app_ver;
	}

	public void setApp_ver(String app_ver) {
		this.app_ver = app_ver;
	}

	public String getMobile_lang() {
		return mobile_lang;
	}

	public void setMobile_lang(String mobile_lang) {
		this.mobile_lang = mobile_lang;
	}

	public String getLatlon() {
		return latlon;
	}

	public void setLatlon(String latlon) {
		this.latlon = latlon;
	}

	public String getWpt() {
		return wpt;
	}

	public void setWpt(String wpt) {
		this.wpt = wpt;
	}

	public String getGeoKretyLogin() {
		return geoKretyLogin;
	}

	public void setGeoKretyLogin(String geoKretyLogin) {
		this.geoKretyLogin = geoKretyLogin;
	}

	public String getFormatedTime() {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(2);
		return nf.format(getGodzina()) + ":" + nf.format(getMinuta());
	}

	public void setDateAndTime(Date date) {
		data = DateFormat.format("yyyy-MM-dd", date).toString();
		Time today = new Time(Time.getCurrentTimezone());
		today.set(date.getTime());
		godzina = today.hour;
		minuta = today.minute;
	}

	public void setDate(int year, int monthOfYear, int dayOfMonth) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(2);
		data = Integer.toString(year) + "-" + nf.format(monthOfYear) + "-"
				+ nf.format(dayOfMonth);
	}

	private AsyncTask<String, Integer, Boolean> refreshTask;

	public void submit(final Activity context, final Account currentAccount,
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
					dialog = ProgressDialog.show(
							context,
							context.getResources().getString(
									R.string.submit_title),
							context.getResources().getString(
									R.string.submit_message), true);
				} catch (Throwable t) {
					t.printStackTrace();
					refreshTask.cancel(true);
					refreshTask = null;
				}
			}

			@Override
			protected Boolean doInBackground(String... params) {
				String[][] postData = new String[][] {
						new String[] { "secid",
								currentAccount.getGeoKreySecredID() },
						new String[] { "nr", nr },
						new String[] { "formname", formname },
						new String[] { "logtype",
								LOG_TYPE_MAPPIG[logtype_mapped] },
						new String[] { "data", data },
						new String[] { "godzina", Integer.toString(godzina) },
						new String[] { "minuta", Integer.toString(minuta) },
						new String[] { "comment", comment },
						new String[] { "app", app },
						new String[] { "app_ver", app_ver },
						new String[] { "mobile_lang", mobile_lang },
						new String[] { "latlon", latlon },
						new String[] { "wpt", wpt }, };

				try {
					String value = Utils.httpPost(URL, postData);
					return true;
				} catch (Exception e) {
					excaption = new MessagedException(R.string.submit_fail,
							e.getLocalizedMessage());
				}
				return false;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				if (result != null) {
					if (result) {
						Toast.makeText(context, R.string.submit_finish,
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
