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

import org.json.JSONException;
import org.json.JSONObject;

import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.exceptions.LocationNotResolvedException;
import android.database.Cursor;

public class Geocache {

    public static Geocache fromWaypointResolver(final String waypoint, final JSONObject json)
            throws JSONException, LocationNotResolvedException {
        final String content = json.getString("tresc");
        final String lat = json.getString("lat");
        final String lon = json.getString("lon");

        if (Utils.isEmpty(lat) || Utils.isEmpty(lon)
                || content.contains("Please provide the coordinates")) {
            throw new LocationNotResolvedException(waypoint);
        }

        final String location = lat + " " + lon;
        final String name = android.text.Html.fromHtml(content.replace("</a>", " -")).toString()
                .replace('￼', ' ').replace('\n', ':').trim().replace("- :  ", "");

        return new Geocache(waypoint, name, location, null, null, null);
    }

    public static Geocache parseGeocachingCom(final String html) throws JSONException {
        final String title = Utils.extractBetween(html, "<title>", "</title>").trim();
        final String waypoint = title.substring(0, 7);
        final String type = Utils.extractBetween(title, "(", ")");
        final String jsonStr = "{" + Utils.extractBetween(html, "mapLatLng = {", ";");
        final JSONObject json = new JSONObject(jsonStr);
        final String name = json.getString("name");
        final String location = json.getString("lat") + " " + json.getString("lng");
        final String guid = Utils.extractBetween(html, ", guid='", "'");
        return new Geocache(waypoint, name, location, type, "", guid);
    }

    private final String name;
    private final String code;
    private final String guid;
    private final String location;

    private final String type;

    private final String status;

    public Geocache(final Cursor cursor, final int i) {
        code = cursor.getString(i + 0);
        name = cursor.getString(i + 1);
        location = cursor.getString(i + 2);
        type = cursor.getString(i + 3);
        status = cursor.getString(i + 4);
        guid = cursor.getString(i + 5);
    }

    public Geocache(final JSONObject jsonObject) throws JSONException {
        name = jsonObject.getString("name");
        code = jsonObject.getString("code");
        location = jsonObject.getString("location");
        type = jsonObject.getString("type");
        status = jsonObject.getString("status");
        guid = null;
    }

    @Deprecated
    public Geocache(final String content, final String lat, final String lon) {
        if (Utils.isEmpty(lat) || Utils.isEmpty(lon)) {
            location = "";
        } else {
            location = lat + " " + lon;
        }

        if (content.contains("Please provide the coordinates")) {
            name = null;
        } else {
            name = android.text.Html.fromHtml(content.replace("</a>", " -")).toString()
                    .replace('￼', ' ').replace('\n', ':').trim().replace("- :  ", "");
        }
        code = null;
        status = null;
        type = null;
        guid = null;
    }

    private Geocache(final String code, final String name, final String location,
            final String type,
            final String status, final String guid) {
        super();
        this.name = name;
        this.code = code;
        this.location = location;
        this.type = type;
        this.status = status;
        this.guid = guid;
    }

    public String getCode() {
        return code;
    }

    public String getFormattedLocation() {
        return getLocation().replace("|", " ");
    }

    public String getGUID() {
        return guid;
    }

    public String getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public boolean hasLocation() {
        return !Utils.isEmpty(getLocation());
    }
}
