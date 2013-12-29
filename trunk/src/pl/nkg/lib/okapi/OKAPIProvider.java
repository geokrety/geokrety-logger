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
package pl.nkg.lib.okapi;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.exceptions.MessagedException;

public class OKAPIProvider {
	private static final String URL_BY_USERNAME = "/okapi/services/users/by_username";

	// private static final String URL_USERLOGS =
	// "/okapi/services/logs/userlogs";

	// private static final String URL_GEOCACHES =
	// "/okapi/services/caches/geocaches";

	public static String loadOpenCachingUUID(SupportedOKAPI okapi, String login)
			throws MessagedException {
		String[][] getData = new String[][] {
				new String[] { "username", login },
				new String[] { "fields", "uuid" },
				new String[] { "consumer_key", okapi.consumerKey } };
		try {
			String jsonString = Utils.httpGet(
					getServiceURL(okapi, URL_BY_USERNAME), getData);
			JSONObject json = new JSONObject(jsonString);
			return json.getString("uuid");
		} catch (JSONException e) {
			throw new MessagedException(R.string.invalid_oclogin_error_message);
		} catch (Exception e) {
			throw new MessagedException(R.string.oclogs_error_message);
		}
	}

	public static String getServiceURL(SupportedOKAPI okapi, String service) {
		return TextUtils.join("", new String[] { "http://www.", okapi.host,
				service });
	}
}
