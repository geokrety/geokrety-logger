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

import java.text.NumberFormat;
import java.util.Date;

import android.os.Bundle;

import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.lib.gkapi.GeoKretyProvider;
import android.text.format.Time;

public class GeoKretLog {
	private static final String[] LOG_TYPE_MAP = { "0", "1", "3", "5", "2" };

	public static final String TRACKING_CODE = "nr";
	public static final String LOG_TYPE = "logtype_mapped";
	public static final String DATE = "data";
	public static final String HOUR = "godzina";
	public static final String MINUTE = "minuta";
	public static final String COMMENT = "comment";
	public static final String LATLON = "latlon";
	public static final String WAYPOINT = "wpt";
	public static final String ACCOUNT_ID = "accoundID";

	public static final int STATE_NEW = 0;
	public static final int STATE_DRAFT = 1;
	public static final int STATE_OUTBOX = 2;
	public static final int STATE_SENT = 3;
	public static final int STATE_PROBLEM = 4;

	// private String secid;
	private String nr;
	private String formname = "ruchy";
	private int logtype_mapped;
	private String data;
	private int godzina;
	private int minuta;
	private String comment;

	private String latlon;
	private String wpt;

	private int accoundID;

	private int id;
	private int state;
	private int problem;
	private String problemArg;

	// private String geoKretyLogin;

	public GeoKretLog() {
		// secid = "";
		nr = "";
		logtype_mapped = 0;

		setDateAndTime(new Date());
		comment = "";
		latlon = "";
		wpt = "";
		state = 0;
		problem = 0;
		problemArg = "";
	}

	public GeoKretLog( //
			int id, //
			int accoundID, //
			int state, //
			int problem, //
			String problemArg, //
			String nr, //
			String wpt, //
			String formname, //
			String latlon, //
			int logtype_mapped, //
			String data, //
			int godzina, //
			int minuta, //
			String comment //
	) {
		super();
		this.nr = nr;
		this.formname = formname;
		this.logtype_mapped = logtype_mapped;
		this.data = data;
		this.godzina = godzina;
		this.minuta = minuta;
		this.comment = comment;
		this.latlon = latlon;
		this.wpt = wpt;
		this.accoundID = accoundID;
		this.id = id;
		this.state = state;
		this.problem = problem;
		this.problemArg = problemArg;
	}

	public GeoKretLog(Bundle savedInstanceState) {
		this();
		if (savedInstanceState != null) {

			// secid = savedInstanceState.getString("secid");
			nr = savedInstanceState.getString(TRACKING_CODE);
			logtype_mapped = savedInstanceState.getInt(LOG_TYPE);
			data = savedInstanceState.getString(DATE);
			godzina = savedInstanceState.getInt(HOUR);
			minuta = savedInstanceState.getInt(MINUTE);
			comment = savedInstanceState.getString(COMMENT);

			latlon = savedInstanceState.getString(LATLON);
			wpt = savedInstanceState.getString(WAYPOINT);

			// geoKretyLogin = savedInstanceState.getString("geoKretyLogin");
			accoundID = savedInstanceState.getInt(ACCOUNT_ID);
		}
	}

	/*
	 * public String getSecid() { return secid; }
	 * 
	 * public void setSecid(String secid) { this.secid = secid; }
	 */

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

	/*
	 * public String getGeoKretyLogin() { return geoKretyLogin; }
	 * 
	 * public void setGeoKretyLogin(String geoKretyLogin) { this.geoKretyLogin =
	 * geoKretyLogin; }
	 */

	public int getAccoundID() {
		return accoundID;
	}

	public void setAccoundID(int accoundID) {
		this.accoundID = accoundID;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getProblem() {
		return problem;
	}

	public void setProblem(int problem) {
		this.problem = problem;
	}

	public String getProblemArg() {
		return problemArg;
	}

	public void setProblemArg(String problemArg) {
		this.problemArg = problemArg;
	}

	public String getFormatedTime() {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(2);
		return nf.format(getGodzina()) + ":" + nf.format(getMinuta());
	}

	public void setDateAndTime(Date date) {
		// data = DateFormat.format("yyyy-MM-dd", date).toString();
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
		// outState.putString("secid", secid);
		outState.putString(TRACKING_CODE, nr);
		outState.putInt(LOG_TYPE, logtype_mapped);
		outState.putString(DATE, data);
		outState.putInt(HOUR, godzina);
		outState.putInt(MINUTE, minuta);
		outState.putString(COMMENT, comment);

		outState.putString(LATLON, latlon);
		outState.putString(WAYPOINT, wpt);

		outState.putInt(ACCOUNT_ID, accoundID);
	}

	public boolean submitLog(Account account) throws MessagedException {
		return GeoKretyProvider.submitLog(account.getGeoKreySecredID(), this);
	}
}
