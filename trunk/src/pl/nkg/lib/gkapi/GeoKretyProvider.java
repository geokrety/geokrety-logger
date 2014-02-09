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
package pl.nkg.lib.gkapi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.data.GeoKretLog;
import pl.nkg.geokrety.data.GeoKret;
import pl.nkg.geokrety.exceptions.MessagedException;
import android.text.TextUtils;

public class GeoKretyProvider {

	public static final int		LOG_SUCCESS			= 1;
	public static final int		LOG_PROBLEM			= 2;
	public static final int		LOG_NO_CONNECTION	= 3;
	public static final int		LOG_DOUBLE	= 4;

	private static final String	URL_LOGIN			= "http://geokrety.org/api-login2secid.php";
	private static final String	URL_EXPORT2			= "http://geokrety.org/export2.php";
	private static final String	URL_RUCHY			= "http://geokrety.org/ruchy.php";
	private static final String URL_AJAX            = "http://geokrety.org/szukaj-ajax.php";
	
	
	private static final String URL_EXPORT_GLID     = "http://geokrety.org/export2.php?gkid=";
	private static final String URL_SZUKAJ_GK       = "http://geokrety.org/szukaj-ajax.php?skad=ajax&nr=";
	private static final String URL_SZUKAJ_GC       = "http://geokrety.org/szukaj-ajax.php?skad=ajax&wpt=";

	public static boolean checkIgnoreLocation(final int logtype_mapped) {
		return logtype_mapped == 1 || logtype_mapped == 4;
	}

	public static Map<String, GeoKret> loadInventory(final String geoKretySecredID) throws MessagedException {
		final HashMap<String, GeoKret> inventory = new HashMap<String, GeoKret>();

		final String[][] getData = new String[][] { //
				new String[] { "secid", geoKretySecredID }, //
				new String[] { "inventory", "1" } };
		try {
			final String xml = Utils.httpGet(URL_EXPORT2, getData);
			final Document doc = Utils.getDomElement(xml);

			final NodeList nl = doc.getElementsByTagName("geokret");

			for (int i = 0; i < nl.getLength(); i++) {
				final Node node = nl.item(i);
				final GeoKret geokret = new GeoKret(node);
				inventory.put(geokret.getTrackingCode(), geokret);
			}
			return inventory;
		} catch (final Exception e) {
			throw new MessagedException(R.string.inventory_error_message);
		}
	}
	
	public static GeoKret loadSingleGeoKretByID(int id) throws MessagedException {
        final String[][] getData = new String[][] { new String[] { "gkid", Integer.toString(id) } };
        try {
            final String xml = Utils.httpGet(URL_EXPORT2, getData);
            final Document doc = Utils.getDomElement(xml);

            final NodeList nl = doc.getElementsByTagName("geokret");

            if (nl.getLength() > 0) {
                return new GeoKret(nl.item(0));
            } else {
                return null;
            }
        } catch (final Exception e) {
            throw new MessagedException(R.string.inventory_error_message); // TODO: need refactor before 0.6.0
        }	    
	}
	
	private final static String KONKRET = "konkret.php?id=";
	public static int loadIDByTranckingCode(String trackingCode) throws MessagedException {
	    final String[][] getData = new String[][] { //
                new String[] { "skad", "ajax" }, //
                new String[] { "nr", trackingCode } };
        try {
            final String xml = Utils.httpGet(URL_AJAX, getData);
            
            int pos = xml.indexOf(KONKRET);
            if (pos == -1) {
                return -1;
            }
            
            int pos2 = xml.indexOf("\'", pos + KONKRET.length());
            if (pos2 == -1) {
                return -1;
            }
            
            String id = xml.substring(pos + KONKRET.length(), pos2);
            return Integer.parseInt(id);
        } catch (final Exception e) {
            throw new MessagedException(R.string.inventory_error_message); // TODO: need refactor before 0.6.0
        }
	}

	public static String loadSecureID(final String geoKretyLogin, final String geoKretyPassword) throws MessagedException {
		final String[][] postData = new String[][] { //
				new String[] { "login", geoKretyLogin }, //
				new String[] { "password", geoKretyPassword } };

		String value;
		try {
			value = Utils.httpPost(URL_LOGIN, postData);
		} catch (final Exception e) {
			throw new MessagedException(R.string.login_error_message, e.getLocalizedMessage());
		}

		if (value != null && !value.startsWith("error")) {
			return value.trim();
		} else {
			throw new MessagedException(R.string.login_error_password_message, String.valueOf(value));
		}
	}

	public static int submitLog(final GeoKretLog log) {
		final boolean ignoreLocation = checkIgnoreLocation(log.getLogTypeMapped());

		final String[][] postData = new String[][] { //
				new String[] { "secid", log.getSecid() }, //
				new String[] { "nr", log.getNr().toUpperCase() }, //
				new String[] { "formname", log.getFormname() }, //
				new String[] { "logtype", log.getLogType() }, //
				new String[] { "data", log.getData() }, //
				new String[] { "godzina", Integer.toString(log.getGodzina()) }, //
				new String[] { "minuta", Integer.toString(log.getMinuta()) }, //
				new String[] { "comment", log.getComment() }, //
				new String[] { "app", Utils.getAppName() }, //
				new String[] { "app_ver", Utils.getAppVer() }, //
				new String[] { "mobile_lang", Utils.getDefaultLanguage() }, //
				new String[] { "latlon", ignoreLocation ? "" : log.getLatlon() }, //
				new String[] { "wpt", ignoreLocation ? "" : log.getWpt() }, };

		log.setProblem(0);
		log.setProblemArg("");

		try {
			final String value = Utils.httpPost(URL_RUCHY, postData);
			final String[] adsFix = value.split("<script");
			final Document doc = Utils.getDomElement(adsFix[0]);
			final NodeList nl = doc.getElementsByTagName("error").item(0).getChildNodes();

			final LinkedList<String> errors = new LinkedList<String>();
			for (int i = 0; i < nl.getLength(); i++) {
				errors.add(nl.item(0).getNodeValue());
			}

			if (errors.size() > 0) {

				log.setState(GeoKretLog.STATE_PROBLEM);
				if (errors.get(0).equals("There is an entry with this date. Correct the date or the hour.")) {
					log.setProblem(R.string.warning_already_logged);
					return LOG_DOUBLE;
				} else if (errors.get(0).equals("Identical log has been submited.")) {
				    // login just submitted
				    return LOG_SUCCESS;
                } else {
					log.setProblem(R.string.submit_fail);
					log.setProblemArg(TextUtils.join("\n", errors));
					return LOG_PROBLEM;
				}
			} else {
				log.setState(GeoKretLog.STATE_SENT);
				return LOG_SUCCESS;
			}
		} catch (final Exception e) {
			log.setProblemArg(e.getLocalizedMessage());
			return LOG_NO_CONNECTION;
		}
	}

	@Deprecated
	public static boolean submitLog(final String secid, final GeoKretLog log) throws MessagedException {
		final boolean ignoreLocation = checkIgnoreLocation(log.getLogTypeMapped());

		final String[][] postData = new String[][] { //
				new String[] { "secid", secid }, //
				new String[] { "nr", log.getNr() }, //
				new String[] { "formname", log.getFormname() }, //
				new String[] { "logtype", log.getLogType() }, //
				new String[] { "data", log.getData() }, //
				new String[] { "godzina", Integer.toString(log.getGodzina()) }, //
				new String[] { "minuta", Integer.toString(log.getMinuta()) }, //
				new String[] { "comment", log.getComment() }, //
				new String[] { "app", Utils.getAppName() }, //
				new String[] { "app_ver", Utils.getAppVer() }, //
				new String[] { "mobile_lang", Utils.getDefaultLanguage() }, //
				new String[] { "latlon", ignoreLocation ? "" : log.getLatlon() }, //
				new String[] { "wpt", ignoreLocation ? "" : log.getWpt() }, };

		try {
			final String value = Utils.httpPost(URL_RUCHY, postData);
			final String[] adsFix = value.split("<script");
			final Document doc = Utils.getDomElement(adsFix[0]);
			final NodeList nl = doc.getElementsByTagName("error").item(0).getChildNodes();

			final LinkedList<String> errors = new LinkedList<String>();
			for (int i = 0; i < nl.getLength(); i++) {
				errors.add(nl.item(0).getNodeValue());
			}

			if (errors.size() > 0) {

				if (errors.get(0).equals("There is an entry with this date. Correct the date or the hour.")) {
					throw new MessagedException(R.string.warning_already_logged);
				} else {
					throw new MessagedException(R.string.submit_fail, "\n" + TextUtils.join("\n", errors));
				}
			}
			return true;
		} catch (final MessagedException e) {
			throw e;
		} catch (final Exception e) {
			throw new MessagedException(R.string.submit_fail, e.getLocalizedMessage());
		}
	}
}
