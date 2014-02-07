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

import android.database.Cursor;

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

	public Geocache(Cursor cursor) {
        this.code = cursor.getString(6);
	    this.name = cursor.getString(7);
        this.location =  cursor.getString(8);
        this.type =  cursor.getString(9);
        this.status =  cursor.getString(10);
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
