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

import java.text.NumberFormat;
import java.util.Date;

import android.os.Bundle;

import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.lib.gkapi.GeoKretyProvider;
import android.text.format.Time;

public class GeoKretLog {
	private static final String[] LOG_TYPE_MAP = { "0", "1", "3", "5", "2" };

	private String secid;
	private String nr;
	private String formname = "ruchy";
	private int logtype_mapped;
	private String data;
	private int godzina;
	private int minuta;
	private String comment;

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
	
	public String getLogType() {
		return LOG_TYPE_MAP[logtype_mapped];
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
		//data = DateFormat.format("yyyy-MM-dd", date).toString();
		Time today = new Time(Time.getCurrentTimezone());
		today.set(date.getTime());
		data = today.format("%Y-%m-%d");
		godzina = today.hour;
		minuta = today.minute;
	}

	public void setDate(int year, int monthOfYear, int dayOfMonth) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(2);
		data = Integer.toString(year) + "-" + nf.format(monthOfYear) + "-"
				+ nf.format(dayOfMonth);
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

	public boolean submitLog(Account account) throws MessagedException {
		return GeoKretyProvider.submitLog(account.getGeoKreySecredID(), this);
	}
}
