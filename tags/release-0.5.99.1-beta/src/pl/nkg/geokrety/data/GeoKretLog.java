/*
 * Copyright (C) 2013, 2014 Michał Niedźwiecki
 * 
 * This file is a part of GeoKrety Logger
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

import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.lib.gkapi.GeoKretyProvider;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.Time;

public class GeoKretLog {
	private static final String[]	LOG_TYPE_MAP	= { "0", "1", "3", "5", "2" };

	public static final String		TRACKING_CODE	= "nr";
	public static final String		LOG_TYPE		= "logtype_mapped";
	public static final String		DATE			= "data";
	public static final String		HOUR			= "godzina";
	public static final String		MINUTE			= "minuta";
	public static final String		COMMENT			= "comment";
	public static final String		LATLON			= "latlon";
	public static final String		WAYPOINT		= "wpt";
	public static final String		ACCOUNT_ID		= "accoundID";

	public static final int			STATE_NEW		= 0;
	public static final int			STATE_DRAFT		= 1;
	public static final int			STATE_OUTBOX	= 2;
	public static final int			STATE_SENT		= 3;
	public static final int			STATE_PROBLEM	= 4;

	public static boolean checkIgnoreLocation(final int logtype_mapped) {
		return logtype_mapped == 1 || logtype_mapped == 4;
	}

	private String	nr;
	private String	formname	= "ruchy";
	private int		logtype_mapped;
	private String	data;
	private int		godzina;
	private int		minuta;

	private String	comment;
	private String	latlon;

	private String	wpt;
	private long	accoundID;

	private String	secid;
	private long	id;
	private int		state;
	private int		problem;

	private String	problemArg;
	
	private GeoKret geoKret;
	private Geocache geocache;

	public GeoKretLog() {
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

	public GeoKretLog(final Bundle savedInstanceState) {
		this();
		if (savedInstanceState != null) {

			nr = savedInstanceState.getString(TRACKING_CODE);
			logtype_mapped = savedInstanceState.getInt(LOG_TYPE);
			data = savedInstanceState.getString(DATE);
			godzina = savedInstanceState.getInt(HOUR);
			minuta = savedInstanceState.getInt(MINUTE);
			comment = savedInstanceState.getString(COMMENT);

			latlon = savedInstanceState.getString(LATLON);
			wpt = savedInstanceState.getString(WAYPOINT);

			accoundID = savedInstanceState.getLong(ACCOUNT_ID);
		}
	}

	public GeoKretLog( //
			final long id, //
			final long accoundID, //
			final int state, //
			final int problem, //
			final String problemArg, //
			final String nr, //
			final String wpt, //
			final String formname, //
			final String latlon, //
			final int logtype_mapped, //
			final String data, //
			final int godzina, //
			final int minuta, //
			final String comment, //
			final String secid //
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
		this.secid = secid;
	}

	public GeoKretLog(Cursor cursor, int i, boolean secidInQuery, boolean joinsInQuery) {
		this( //
				cursor.getInt(i + 0), //
				cursor.getInt(i + 1), //
				cursor.getInt(i + 2), //
				cursor.getInt(i + 3), //
				cursor.getString(i + 4), //
				cursor.getString(i + 5), //
				cursor.getString(i + 6), //
				cursor.getString(i + 7), //
				cursor.getString(i + 8), //
				cursor.getInt(i + 9), //
				cursor.getString(i + 10), //
				cursor.getInt(i + 11), //
				cursor.getInt(i + 12), //
				cursor.getString(i + 13), //
				secidInQuery ? cursor.getString(i + 14) : "" //
		);
		
		if (joinsInQuery) {
		    geocache = new Geocache(cursor, i + 14);
		    geoKret = new GeoKret(cursor, i + 19);
		}
	}

	public long getAccoundID() {
		return accoundID;
	}

	public String getComment() {
		return comment;
	}

	public String getData() {
		return data;
	}

	public String getFormatedTime() {
		final NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(2);
		return nf.format(getGodzina()) + ":" + nf.format(getMinuta());
	}

	public String getFormname() {
		return formname;
	}

	public int getGodzina() {
		return godzina;
	}

	public long getId() {
		return id;
	}

	public String getLatlon() {
		return latlon;
	}

	public String getLogType() {
		return LOG_TYPE_MAP[logtype_mapped];
	}

	public int getLogTypeMapped() {
		return logtype_mapped;
	}

	public int getMinuta() {
		return minuta;
	}

	public String getNr() {
		return nr;
	}

	public int getProblem() {
		return problem;
	}

	public String getProblemArg() {
		return problemArg;
	}

	public String getSecid() {
		return secid;
	}

	public int getState() {
		return state;
	}

	public String getWpt() {
		return wpt;
	}

	public void setAccoundID(final long accoundID) {
		this.accoundID = accoundID;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

	public void setData(final String data) {
		this.data = data;
	}

	public void setDate(final int year, final int monthOfYear, final int dayOfMonth) {
		final NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(2);
		data = Integer.toString(year) + "-" + nf.format(monthOfYear) + "-" + nf.format(dayOfMonth);
	}

	public void setDateAndTime(final Date date) {
		// data = DateFormat.format("yyyy-MM-dd", date).toString();
		final Time today = new Time(Time.getCurrentTimezone());
		today.set(date.getTime());
		data = today.format("%Y-%m-%d");
		godzina = today.hour;
		minuta = today.minute;
	}

	public void setFormname(final String formname) {
		this.formname = formname;
	}

	public void setGodzina(final int godzina) {
		this.godzina = godzina;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public void setLatlon(final String latlon) {
		this.latlon = latlon;
	}

	public void setLogTypeMapped(final int logtype) {
		logtype_mapped = logtype;
	}

	public void setMinuta(final int minuta) {
		this.minuta = minuta;
	}

	public void setNr(final String nr) {
		this.nr = nr;
	}

	public void setProblem(final int problem) {
		this.problem = problem;
	}

	public void setProblemArg(final String problemArg) {
		this.problemArg = problemArg;
	}

	public void setSecid(final String secid) {
		this.secid = secid;
	}

	public void setState(final int state) {
		this.state = state;
	}

	public void setWpt(final String wpt) {
		this.wpt = wpt;
	}

	public void storeToBundle(final Bundle outState) {
		// outState.putString("secid", secid);
		outState.putString(TRACKING_CODE, nr);
		outState.putInt(LOG_TYPE, logtype_mapped);
		outState.putString(DATE, data);
		outState.putInt(HOUR, godzina);
		outState.putInt(MINUTE, minuta);
		outState.putString(COMMENT, comment);

		outState.putString(LATLON, latlon);
		outState.putString(WAYPOINT, wpt);

		outState.putLong(ACCOUNT_ID, accoundID);
	}

	@Deprecated
	public boolean submitLog(final User account) throws MessagedException {
		return GeoKretyProvider.submitLog(account.getGeoKreySecredID(), this);
	}
	
	@Override
	public String toString() {
		return getNr() + " (" +getState() + ")";
	}

    public Geocache getGeoCache() {
        return geocache;
    }
    
    public GeoKret getGeoKret() {
        return geoKret;
    }
}
