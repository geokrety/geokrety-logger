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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ListView;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.geokrety.threads.RefreshAccount;

public class Account {
	public static final String ACCOUNT_ID = "accountID";
	public static final String ACCOUNT_NAME = "accountName";
	public static final String SECID = "secid";
	public static final String OCUUIDS = "ocUUIDs";

	private static final String URL_EXPORT2 = "http://geokrety.org/export2.php";
	private static final String URL_USERLOGS = "http://opencaching.pl/okapi/services/logs/userlogs";
	private static final String URL_GEOCACHES = "http://opencaching.pl/okapi/services/caches/geocaches";
	private static final String CONSUMER_KEY = "DajjA4r3QZNRHAef7XZD";
	private static final long EXPIRED = 24 * 60 * 60 * 1000;

	private long id;
	private String name;

	private String geoKretySecredID;
	private String[] openCachingUUIDs;

	private List<GeocacheLog> openCachingLogs;
	private List<Geokret> inventory;

	private Date lastDataLoaded;

	public Account(Bundle bundle) {
		unpack(bundle);
	}

	public Account(long id, String name, String geoKretySecredID,
			String[] openCachingUUIDs) {
		this.id = id;
		this.name = name;
		this.geoKretySecredID = geoKretySecredID;
		this.openCachingUUIDs = openCachingUUIDs;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Bundle pack(Bundle bundle) {
		bundle.putStringArray(Account.OCUUIDS, openCachingUUIDs);
		bundle.putLong(Account.ACCOUNT_ID, id);
		bundle.putString(Account.SECID, geoKretySecredID);
		bundle.putString(Account.ACCOUNT_NAME, name);
		return bundle;
	}

	public Bundle unpack(Bundle bundle) {
		geoKretySecredID = bundle.getString(Account.SECID);
		id = bundle.getLong(Account.ACCOUNT_ID);
		openCachingUUIDs = bundle.getStringArray(Account.OCUUIDS);
		name = bundle.getString(Account.ACCOUNT_NAME);
		return bundle;
	}

	public String getGeoKreySecredID() {
		return geoKretySecredID;
	}

	public List<GeocacheLog> getOpenCachingLogs() {
		return openCachingLogs;
	}

	public List<Geokret> getInventory() {
		return inventory;
	}

	public boolean expired() {
		if (lastDataLoaded == null) {
			return true;
		}
		return new Date().getTime() - lastDataLoaded.getTime() > EXPIRED;
	}

	public void loadInventory(RefreshAccount asyncTask)
			throws MessagedException {
		ArrayList<Geokret> inventory = new ArrayList<Geokret>();

		String[][] getData = new String[][] {
				new String[] { "secid", geoKretySecredID },
				new String[] { "inventory", "1" } };
		try {
			String xml = Utils.httpGet(URL_EXPORT2, getData);
			Document doc = Utils.getDomElement(xml);

			NodeList nl = doc.getElementsByTagName("geokret");

			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				inventory.add(new Geokret(node));
			}
			this.inventory = Collections.synchronizedList(inventory);
		} catch (Exception e) {
			throw new MessagedException(R.string.inventory_error_message);
		}
	}

	public void setOpenCachingLogs(ArrayList<GeocacheLog> openCachingLogs) {
		this.openCachingLogs = Collections.synchronizedList(openCachingLogs);
	}

	public void loadOpenCachingLogs(RefreshAccount asyncTask,
			ArrayList<GeocacheLog> openCachingLogs, int portal)
			throws MessagedException {
		String[][] getData = new String[][] {
				new String[] { "user_uuid", openCachingUUIDs[portal] },
				new String[] { "consumer_key", CONSUMER_KEY } };
		try {
			String jsonString = Utils.httpGet(URL_USERLOGS, getData);

			JSONArray json = new JSONArray(jsonString);

			for (int i = 0; i < json.length(); i++) {
				openCachingLogs.add(new GeocacheLog(json.getJSONObject(i)));
			}

			if (openCachingLogs.size() > 0) {
				loadOCnames(openCachingLogs, portal);
			}
		} catch (JSONException e) {
			throw new MessagedException(R.string.invalid_oclogin_error_message);
		} catch (IOException e) {
			throw new MessagedException(R.string.oclogs_error_message);
		} catch (ParseException e) {
			throw new MessagedException(R.string.oclogs_error_message);
		}
	}

	public void touchLastLoadedDate() {
		lastDataLoaded = new Date();
	}

	private void loadOCnames(ArrayList<GeocacheLog> openCachingLogs, int portal)
			throws ClientProtocolException, IOException, JSONException {
		HashSet<String> codes = getCacheCodes(openCachingLogs);
		if (codes.size() == 0) {
			return;
		}

		String[][] getData = new String[][] {
				new String[] { "user_uuid", openCachingUUIDs[portal] },
				new String[] { "consumer_key", CONSUMER_KEY },
				new String[] { "fields", "name|code" },
				new String[] { "cache_codes", TextUtils.join("|", codes) },
				new String[] { "lpc", "0" } };

		String jsonString = Utils.httpGet(URL_GEOCACHES, getData);
		JSONObject json = new JSONObject(jsonString);
		for (String code : codes) {
			Geocache geocache = new Geocache(json.getJSONObject(code));
			StateHolder.getGeoacheMap().put(geocache.getCode(), geocache);
		}
	}

	private HashSet<String> getCacheCodes(ArrayList<GeocacheLog> openCachingLogs) {
		HashSet<String> caches = new HashSet<String>();
		for (GeocacheLog log : new ArrayList<GeocacheLog>(openCachingLogs)) {
			if (!StateHolder.getGeoacheMap().containsKey(log.getCacheCode())) {
				caches.add(log.getCacheCode());
			}
		}
		return caches;
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean loadIfExpired(GeoKretyApplication application) {
		if (expired()) {
			loadData(application);
			return true;
		} else {
			return false;
		}
	}

	public void loadData(GeoKretyApplication application) {
		application.getForegroundTaskHandler().runTask(RefreshAccount.ID, this);
	}

	public int getTrackingCodeIndex(String trackingCode) {
		int pos = 0;
		for (Geokret g : inventory) {
			if (g.getTackingCode().equalsIgnoreCase(trackingCode)) {
				return pos;
			}
			pos++;
		}
		return ListView.INVALID_POSITION;
	}

	public int getWaypointIndex(String waypoint) {
		int pos = 0;
		for (GeocacheLog l : openCachingLogs) {
			if (l.getCacheCode().equalsIgnoreCase(waypoint)) {
				return pos;
			}
			pos++;
		}
		return ListView.INVALID_POSITION;
	}

	public long getID() {
		return id;
	}

	public void setID(long id) {
		this.id = id;
	}

	public String[] getOpenCachingUUIDs() {
		return openCachingUUIDs;
	}

	public boolean hasOpenCachingUUID(int portal) {
		if (openCachingUUIDs == null || portal < 0
				|| portal >= openCachingUUIDs.length) {
			return false;
		}

		return !Utils.isEmpty(openCachingUUIDs[portal]);
	}
}
