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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public static GeocacheLog fromGeocachingCom(final String row, final DateFormat dateFormat) {
        final String[] cells = row.split("</td>");

        final String logType = extractLogType(cells[0]);
        Date date = extractDate(cells[2], dateFormat);

        date = new Date(date.getTime() + 12 * 60 * 60 * 1000);

        final String guid = extractGUID(cells[3]);
        return new GeocacheLog(guid, "", logType, date, "", GeocachingProvider.PORTAL);
    }

    private final String uuid;
    private final Date date;
    private final String cache_code;
    private final String type;
    private final String comment;
    private final int portal;

    private Geocache geocache;

    private static DateFormat dateFormat;

    private static DateFormat readableDateFormat;

    private static final DateFormat geocachingComDateFormat;

    static {
        dateFormat = new SimpleDateFormat(FORMAT_DATE_ISO, Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        readableDateFormat = new SimpleDateFormat(FORMAT_DATE_READABLE, Locale.getDefault());
        readableDateFormat.setTimeZone(TimeZone.getDefault());
        geocachingComDateFormat = new SimpleDateFormat(GeocachingProvider.FORMAT_DATE_GEOCACHING,
                Locale.getDefault());
        geocachingComDateFormat.setTimeZone(TimeZone.getDefault());
    }

    public static Date fromISODateString(final String isoDateString)
            throws ParseException {
        return dateFormat.parse(isoDateString);
    }

    public static String toReadableDateString(final Date date) {
        return readableDateFormat.format(date);
    }

    private static Date extractDate(final String src, final DateFormat dateFormat) {
        try {
            return dateFormat.parse(Html.fromHtml(src).toString().trim());
        } catch (final ParseException e) {
            return null;
        }
    }

    private static String extractGUID(final String src) {
        return Utils.extractBetween(src, "guid=", "\"");
    }

    private static String extractLogType(final String src) {
        return Utils.extractBetween(src, "title=\"", "\"");
    }

    public GeocacheLog(final Cursor cursor, final int i) {
        uuid = cursor.getString(i + 0);
        type = cursor.getString(i + 1);
        date = new Date(cursor.getLong(i + 2));
        comment = cursor.getString(i + 3);
        portal = cursor.getInt(i + 4);
        cache_code = cursor.getString(i + 5);
        if (!cursor.isNull(i + 6)) {
            geocache = new Geocache(cursor, i + 6);
        }
    }

    public GeocacheLog(final JSONObject jsonObject, final int portal) throws JSONException,
            ParseException {
        uuid = jsonObject.getString("uuid");
        date = fromISODateString(jsonObject.getString("date"));
        cache_code = jsonObject.getString("cache_code");
        type = jsonObject.getString("type");
        comment = jsonObject.getString("comment");
        this.portal = portal;
    }

    @Deprecated
    public GeocacheLog(final String uuid, final String cache_code, final String type,
            final Date date,
            final String comment, final int portal) {
        super();
        this.uuid = uuid;
        this.date = date;
        this.cache_code = cache_code;
        this.type = type;
        this.comment = comment;
        this.portal = portal;
    }

    public String getCacheCode() {
        return cache_code;
    }

    public String getComment() {
        return comment;
    }

    public Date getDate() {
        return date;
    }

    public Geocache getGeoCache() {
        return geocache;
    }

    public int getPortal() {
        return portal;
    }

    public String getType() {
        return type;
    }

    public String getUUID() {
        return uuid;
    }

    @Override
    public String toString() {
        if (getGeoCache() != null && getGeoCache().getName() != null) {
            return getGeoCache().getName() + " (" + cache_code + ")";
        } else {
            return cache_code;
        }
    }
}
