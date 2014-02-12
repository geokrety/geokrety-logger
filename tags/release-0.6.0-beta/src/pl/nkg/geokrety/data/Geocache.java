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

import android.database.Cursor;
import android.text.Html;

public class Geocache {

	private final String name;
	private final String code;
	private final String location;
	private final String type;
	private final String status;

	public Geocache(JSONObject jsonObject) throws JSONException {
		name = jsonObject.getString("name");
		code = jsonObject.getString("code");
		location = jsonObject.getString("location");
		type = jsonObject.getString("type");
		status = jsonObject.getString("status");
	}

	@Deprecated
	public Geocache(String code, String name, String location, String type,
			String status) {
		super();
		this.name = name;
		this.code = code;
		this.location = location;
		this.type = type;
		this.status = status;
	}

	public Geocache(Cursor cursor, int i) {
        this.code = cursor.getString(i + 0);
	    this.name = cursor.getString(i + 1);
        this.location =  cursor.getString(i + 2);
        this.type =  cursor.getString(i + 3);
        this.status =  cursor.getString(i + 4);
    }

    public Geocache(String content, String lat, String lon) {
        if (Utils.isEmpty(lat) || Utils.isEmpty(lon)) {
            location = "";
        } else {
            location = lat + " " + lon;
        }
        
        if (content.contains("Please provide the coordinates")) {
            name = null;
        } else {
            name = android.text.Html.fromHtml(content.replace("</a>", " -")).toString().replace('￼', ' ').replace('\n', ':').trim().replace("- :  ", "");
        }
        code = null;
        status = null;
        type = null;
    }

    public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public String getLocation() {
		return location;
	}

	public String getType() {
		return type;
	}

	public String getStatus() {
		return status;
	}
}
