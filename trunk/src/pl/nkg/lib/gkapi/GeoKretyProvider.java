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
package pl.nkg.lib.gkapi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.text.TextUtils;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.data.GeoKretLog;
import pl.nkg.geokrety.data.Geokret;
import pl.nkg.geokrety.exceptions.MessagedException;

public class GeoKretyProvider {
	private static final String URL_LOGIN = "http://geokrety.org/api-login2secid.php";
	private static final String URL_EXPORT2 = "http://geokrety.org/export2.php";
	private static final String URL_RUCHY = "http://geokrety.org/ruchy.php";

	public static String loadSecureID(String geoKretyLogin,
			String geoKretyPassword) throws MessagedException {
		String[][] postData = new String[][] {
				new String[] { "login", geoKretyLogin },
				new String[] { "password", geoKretyPassword } };

		String value;
		try {
			value = Utils.httpPost(URL_LOGIN, postData);
		} catch (Exception e) {
			throw new MessagedException(R.string.login_error_message,
					e.getLocalizedMessage());
		}

		if (value != null && !value.startsWith("error")) {
			return value.trim();
		} else {
			throw new MessagedException(R.string.login_error_password_message,
					String.valueOf(value));
		}
	}

	public static Map<String, Geokret> loadInventory(String geoKretySecredID)
			throws MessagedException {
		HashMap<String, Geokret> inventory = new HashMap<String, Geokret>();

		String[][] getData = new String[][] {
				new String[] { "secid", geoKretySecredID },
				new String[] { "inventory", "1" } };
		try {
			String xml = Utils.httpGet(URL_EXPORT2, getData);
			Document doc = Utils.getDomElement(xml);

			NodeList nl = doc.getElementsByTagName("geokret");

			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				Geokret geokret = new Geokret(node);
				inventory.put(geokret.getTackingCode(), geokret);
			}
			return inventory;
		} catch (Exception e) {
			throw new MessagedException(R.string.inventory_error_message);
		}
	}

	public static boolean submitLog(String secid, GeoKretLog log)
			throws MessagedException {
		boolean ignoreLocation = checkIgnoreLocation(log.getLogTypeMapped());

		String[][] postData = new String[][] {
				new String[] { "secid", secid },
				new String[] { "nr", log.getNr() },
				new String[] { "formname", log.getFormname() },
				new String[] { "logtype", log.getLogType() },
				new String[] { "data", log.getData() },
				new String[] { "godzina", Integer.toString(log.getGodzina()) },
				new String[] { "minuta", Integer.toString(log.getMinuta()) },
				new String[] { "comment", log.getComment() },
				new String[] { "app", Utils.getAppName() },
				new String[] { "app_ver", Utils.getAppVer() },
				new String[] { "mobile_lang", Utils.getDefaultLanguage() },
				new String[] { "latlon", ignoreLocation ? "" : log.getLatlon() },
				new String[] { "wpt", ignoreLocation ? "" : log.getWpt() }, };

		try {
			String value = Utils.httpPost(URL_RUCHY, postData);
			String[] adsFix = value.split("<script");
			Document doc = Utils.getDomElement(adsFix[0]);
			NodeList nl = doc.getElementsByTagName("error").item(0)
					.getChildNodes();

			LinkedList<String> errors = new LinkedList<String>();
			for (int i = 0; i < nl.getLength(); i++) {
				errors.add(nl.item(0).getNodeValue());
			}

			if (errors.size() > 0) {

				if (errors
						.get(0)
						.equals("There is an entry with this date. Correct the date or the hour.")) {
					return true;
				} else {
					throw new MessagedException(R.string.submit_fail, "\n"
							+ TextUtils.join("\n", errors));
				}
			}
			return true;
		} catch (MessagedException e) {
			throw e;
		} catch (Exception e) {
			throw new MessagedException(R.string.submit_fail,
					e.getLocalizedMessage());
		}
	}

	public static boolean checkIgnoreLocation(int logtype_mapped) {
		return logtype_mapped == 1 || logtype_mapped == 4;
	}
}
