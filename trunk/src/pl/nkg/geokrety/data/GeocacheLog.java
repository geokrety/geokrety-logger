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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import pl.nkg.geokrety.Utils;
import pl.nkg.lib.gcapi.GeocachingProvider;

import android.database.Cursor;
import android.text.Html;

public class GeocacheLog {

	public static String FORMAT_DATE_ISO = "yyyy-MM-dd'T'HH:mm:ssZ";
	public static String FORMAT_DATE_READABLE = "yyyy-MM-dd HH:mm";

	private final String uuid;
	private final Date date;
	private final String cache_code;
	private final String type;
	private final String comment;
	private final int portal;
	private Geocache geocache;

	public GeocacheLog(JSONObject jsonObject, int portal) throws JSONException,
			ParseException {
		uuid = jsonObject.getString("uuid");
		date = fromISODateString(jsonObject.getString("date"));
		cache_code = jsonObject.getString("cache_code");
		type = jsonObject.getString("type");
		comment = jsonObject.getString("comment");
		this.portal = portal;
	}

	@Deprecated
	public GeocacheLog(String uuid, String cache_code, String type, Date date,
			String comment, int portal) {
		super();
		this.uuid = uuid;
		this.date = date;
		this.cache_code = cache_code;
		this.type = type;
		this.comment = comment;
		this.portal = portal;
	}

	public GeocacheLog(Cursor cursor, int i) {
	    this.uuid = cursor.getString(i + 0);
        this.type = cursor.getString(i + 1);
        this.date = new Date(cursor.getLong(i + 2));
        this.comment = cursor.getString(i + 3);
        this.portal = cursor.getInt(i + 4);
        this.cache_code = cursor.getString(i + 5);
        if (!cursor.isNull(i + 6)) {
            this.geocache = new Geocache(cursor, i + 6);
        }
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

	public int getPortal() {
		return portal;
	}

	public Geocache getGeoCache() {
		return geocache;
	}

	@Override
	public String toString() {
	    // FIXME: comment = name of cache - fixed?
	    /*if (portal == GeocachingProvider.PORTAL) {
	        return comment + " (" + cache_code + ")";
	    }*/
	    
		if (getGeoCache() != null && getGeoCache().getName() != null) {
			return getGeoCache().getName() + " (" + cache_code + ")";
		} else {
			return cache_code;
		}
	}

	private static DateFormat dateFormat;
	private static DateFormat readableDateFormat;
	private static final DateFormat geocachingComDateFormat;

	static {
		dateFormat = new SimpleDateFormat(FORMAT_DATE_ISO, Locale.getDefault());
		dateFormat.setTimeZone(TimeZone.getDefault());
		readableDateFormat = new SimpleDateFormat(FORMAT_DATE_READABLE, Locale.getDefault());
		readableDateFormat.setTimeZone(TimeZone.getDefault());
		geocachingComDateFormat = new SimpleDateFormat(GeocachingProvider.FORMAT_DATE_GEOCACHING, Locale.getDefault());
		geocachingComDateFormat.setTimeZone(TimeZone.getDefault());
	}

	public static Date fromISODateString(String isoDateString)
			throws ParseException {
		return dateFormat.parse(isoDateString);
	}
	
	public static String toReadableDateString(Date date) {
	    return readableDateFormat.format(date);
	}

    public static GeocacheLog fromGeocachingCom(String row) {
        String[] cells = row.split("</td>");
        
        String logType = extractLogType(cells[0]);
        Date date = extractDate(cells[2]);
        
        date = new Date(date.getTime() + 12 * 60 * 60 * 1000);
        
        String guid = extractGUID(cells[3]);
        return new GeocacheLog(guid, "", logType, date, "", GeocachingProvider.PORTAL);
    }
    

    private static String extractLogType(String src) {
        return Utils.extractBetween(src, "title=\"", "\"");
    }
    
    private static Date extractDate(String src) {
        try {
            return geocachingComDateFormat.parse(Html.fromHtml(src).toString().trim());
        } catch (ParseException e) {
            return null;
        }
    }
    
    private static String extractGUID(String src) {
        return Utils.extractBetween(src, "guid=", "\"");
    }
}
