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

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.os.Bundle;
import android.text.format.DateFormat;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.geokrety.threads.LogGeoKret;
import android.text.format.Time;
import android.util.Pair;

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

	public void submit(GeoKretyApplication application, Account currentAccount, boolean force) {

		app_ver = Utils.getAppVer(application.getApplicationContext());

		application.getForegroundTaskHandler().runTask(LogGeoKret.ID,
				new Pair<GeoKretLog, Account>(this, currentAccount), force);
		// LogGeoKret.logGeoKret(this, currentAccount, logProgressDialog,
		// listener, false);
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

		try {
			String value = Utils.httpPost(URL, postData);
			String[] adsFix = value.split("<script");
			Document doc = Utils.getDomElement(adsFix[0]);
			NodeList nl = doc.getElementsByTagName("error").item(0)
					.getChildNodes();
			if (nl.getLength() > 0) {
				throw new MessagedException(R.string.submit_fail, nl.item(0).getNodeValue());
			}
			return true;
		} catch (MessagedException e) {
			throw e;
		} catch (Exception e) {
			throw new MessagedException(R.string.submit_fail,
					e.getLocalizedMessage());
		}
	}
}
