package pl.nkg.geokrety.data;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import org.w3c.dom.Document;

import android.os.Bundle;
import android.text.format.DateFormat;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.dialogs.LogProgressDialog;
import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.geokrety.threads.LogGeoKret;
import pl.nkg.geokrety.widgets.LogSuccessfulListener;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;
import android.text.format.Time;

public class GeoKretLog implements Serializable {
	private static final long serialVersionUID = 907039083028176080L;

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

	public GeoKretLog(Bundle savedInstanceState) {
		this();
		if (savedInstanceState != null) {

			secid = savedInstanceState.getString("secid");
			nr = savedInstanceState.getString("nr");
			logtype_mapped = savedInstanceState.getInt("logtype_mapped");
			data = savedInstanceState.getString("data");
			godzina = savedInstanceState.getInt("godzina");
			minuta = savedInstanceState.getInt("minuta");
			comment = savedInstanceState.getString("comment");

			latlon = savedInstanceState.getString("latlon");
			wpt = savedInstanceState.getString("wpt");

			geoKretyLogin = savedInstanceState.getString("geoKretyLogin");
		}
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

	public void submit(final ManagedDialogsActivity context,
			LogProgressDialog logProgressDialog, final Account currentAccount,
			final LogSuccessfulListener listener) {

		app_ver = Utils.getAppVer(context);
		LogGeoKret.logGeoKret(this, currentAccount, logProgressDialog,
				listener, false);
	}

	public static boolean checkIgnoreLocation(int logtype_mapped) {
		return logtype_mapped == 1 || logtype_mapped == 4;
	}

	public void storeToBundle(Bundle outState) {
		outState.putString("secid", secid);
		outState.putString("nr", nr);
		outState.putInt("logtype_mapped", logtype_mapped);
		outState.putString("data", data);
		outState.putInt("godzina", godzina);
		outState.putInt("minuta", minuta);
		outState.putString("comment", comment);

		outState.putString("latlon", latlon);
		outState.putString("wpt", wpt);

		outState.putString("geoKretyLogin", geoKretyLogin);
	}

	public boolean submitBackground(Account account) throws MessagedException {
		boolean ignoreLocation = checkIgnoreLocation(logtype_mapped);

		String[][] postData = new String[][] {
				new String[] { "secid", account.getGeoKreySecredID() },
				new String[] { "nr", nr },
				new String[] { "formname", formname },
				new String[] { "logtype", LOG_TYPE_MAPPIG[logtype_mapped] },
				new String[] { "data", data },
				new String[] { "godzina", Integer.toString(godzina) },
				new String[] { "minuta", Integer.toString(minuta) },
				new String[] { "comment", comment },
				new String[] { "app", app },
				new String[] { "app_ver", app_ver },
				new String[] { "mobile_lang", mobile_lang },
				new String[] { "latlon", ignoreLocation ? "" : latlon },
				new String[] { "wpt", ignoreLocation ? "" : wpt }, };

		while (true) {
			try {
				String value = Utils.httpPost(URL, postData);
				Document doc = Utils.getDomElement(value);
				String error = doc.getElementsByTagName("error").item(0)
						.getTextContent();
				if (error.length() == 0) {
					return true;
				} else {
					if (error.equals("Wrong secid identifier")) {
						account.loadSecureID(null);
						continue;
					} else {
						throw new MessagedException(R.string.submit_fail, error);
					}
				}
			} catch (Exception e) {
				throw new MessagedException(R.string.submit_fail,
						e.getLocalizedMessage());
			}
		}
	}
}
