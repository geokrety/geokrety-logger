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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

public class GeocacheLog {

	public static String FORMAT_DATE_ISO = "yyyy-MM-dd'T'HH:mm:ssZ";

	private final String uuid;
	private final Date date;
	private final String cache_code;
	private final String type;
	private final String comment;
	
	public GeocacheLog(JSONObject jsonObject) throws JSONException, ParseException {
		uuid = jsonObject.getString("uuid");
		date = fromISODateString(jsonObject.getString("date"));
		cache_code = jsonObject.getString("cache_code");
		type = jsonObject.getString("type");
		comment = jsonObject.getString("comment");
	}

	public String getUUID() {
		return uuid;
	}

	public Date getDate() {
		return date;
	}

	public String getCacheCode() {
		return cache_code;
	}

	public String getType() {
		return type;
	}

	public String getComment() {
		return comment;
	}
	
	@Override
	public String toString() {
		return StateHolder.getGeoacheMap().get(cache_code).getName() + " (" + cache_code + ")";
	}
	

	public static Date fromISODateString(String isoDateString) throws ParseException {
	    DateFormat f = new SimpleDateFormat(FORMAT_DATE_ISO, Locale.getDefault());
	    f.setTimeZone(TimeZone.getDefault());
	    return f.parse(isoDateString);
	}
}
