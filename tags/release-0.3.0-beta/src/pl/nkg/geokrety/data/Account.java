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
package pl.nkg.geokrety.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.widget.ListView;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.geokrety.threads.RefreshAccount;
import pl.nkg.lib.gkapi.GeoKretyProvider;
import pl.nkg.lib.okapi.OKAPIProvider;
import pl.nkg.lib.okapi.SupportedOKAPI;
import pl.nkg.lib.threads.ICancelable;

public class Account {
	public static final String ACCOUNT_ID = "accountID";
	public static final String ACCOUNT_NAME = "accountName";
	public static final String SECID = "secid";
	public static final String OCUUIDS = "ocUUIDs";

	private static final long EXPIRED = 24 * 60 * 60 * 1000;

	private int id;
	private String name;

	private String geoKretySecredID;
	private String[] openCachingUUIDs;

	private List<GeocacheLog> openCachingLogs;
	private List<Geokret> inventory;

	private Date lastDataLoaded;

	public Account(Bundle bundle) {
		unpack(bundle);
	}

	public Account(int id, String name, String geoKretySecredID,
			String[] openCachingUUIDs) {
		this.id = id;
		this.name = name;
		this.geoKretySecredID = geoKretySecredID;
		this.openCachingUUIDs = openCachingUUIDs;
	}

	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getLastDataLoaded() {
		return lastDataLoaded;
	}

	public void setLastDataLoaded(Date lastDataLoaded) {
		this.lastDataLoaded = lastDataLoaded;
	}

	public String getGeoKreySecredID() {
		return geoKretySecredID;
	}

	public List<GeocacheLog> getOpenCachingLogs() {
		if (openCachingLogs == null) {
			openCachingLogs = new ArrayList<GeocacheLog>();
		}
		return openCachingLogs;
	}

	public void setOpenCachingLogs(List<GeocacheLog> openCachingLogs) {
		this.openCachingLogs = openCachingLogs;
	}

	public List<Geokret> getInventory() {
		if (inventory == null) {
			inventory = new ArrayList<Geokret>();
		}
		return inventory;
	}

	public void setInventory(List<Geokret> gks) {
		this.inventory = gks;
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

	public boolean expired() {

		if (lastDataLoaded == null) {
			return true;
		}
		return new Date().getTime() - lastDataLoaded.getTime() > EXPIRED;
	}

	public Bundle pack(Bundle bundle) {
		bundle.putStringArray(Account.OCUUIDS, openCachingUUIDs);
		bundle.putInt(Account.ACCOUNT_ID, id);
		bundle.putString(Account.SECID, geoKretySecredID);
		bundle.putString(Account.ACCOUNT_NAME, name);
		return bundle;
	}

	public Bundle unpack(Bundle bundle) {
		geoKretySecredID = bundle.getString(Account.SECID);
		id = bundle.getInt(Account.ACCOUNT_ID);
		openCachingUUIDs = bundle.getStringArray(Account.OCUUIDS);
		name = bundle.getString(Account.ACCOUNT_NAME);
		return bundle;
	}

	public void loadInventoryAndStore(ICancelable cancelable, GeoKretDataSource dataSource)
			throws MessagedException {

		Map<String, Geokret> gkMap = GeoKretyProvider
				.loadInventory(geoKretySecredID);

		if (cancelable.isCancelled()) {
			return;
		}
		
		// merge with stickys
		for (Geokret geokret : new LinkedList<Geokret>(getInventory())) {
			if (geokret.isSticky()) {
				Geokret gk2 = gkMap.get(geokret.getTackingCode());
				if (gk2 == null) {
					gkMap.put(geokret.getTackingCode(), geokret);
				} else {
					gk2.setSticky(true);
				}
			}
		}

		ArrayList<Geokret> afterMerge = new ArrayList<Geokret>(gkMap.size());

		for (Geokret geokret : gkMap.values()) {
			afterMerge.add(geokret);
		}

		dataSource.store(afterMerge, getID());
		setInventory(dataSource.load(getID()));
	}

	public List<GeocacheLog> loadOpenCachingLogs(int portal)
			throws MessagedException {

		SupportedOKAPI okapi = SupportedOKAPI.SUPPORTED[portal];
		return OKAPIProvider.loadOpenCachingLogs(okapi,
				openCachingUUIDs[portal]);
	}

	public void touchLastLoadedDate(AccountDataSource accountDataSource) {
		lastDataLoaded = new Date();
		accountDataSource.storeLastLoadedDate(this);
	}

	public void loadOCnamesToBuffer(ICancelable cancelable, List<GeocacheLog> openCachingLogs,
			int portal) throws MessagedException {
		SupportedOKAPI okapi = SupportedOKAPI.SUPPORTED[portal];
		HashSet<String> codes = getUnbufferedCacheCodes(openCachingLogs);
		if (codes.size() == 0) {
			return;
		}
		
		if (cancelable.isCancelled()) {
			return;
		}

		for (Geocache geocache : OKAPIProvider.loadOCnames(codes, okapi)) {
			StateHolder.getGeoacheMap().put(geocache.getCode(), geocache);
		}
	}

	private HashSet<String> getUnbufferedCacheCodes(
			Collection<GeocacheLog> openCachingLogs) {
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

	public boolean loadIfExpired(GeoKretyApplication application, boolean force) {
		if (expired()) {
			loadData(application, force);
			return true;
		} else {
			return false;
		}
	}

	public void loadData(GeoKretyApplication application, boolean force) {
		application.getForegroundTaskHandler().runTask(RefreshAccount.ID, this,
				force);
	}

	public int getTrackingCodeIndex(String trackingCode) {
		int pos = 0;
		for (Geokret g : getInventory()) {
			if (g.getTackingCode().equalsIgnoreCase(trackingCode)) {
				return pos;
			}
			pos++;
		}
		return ListView.INVALID_POSITION;
	}

	public int getWaypointIndex(String waypoint) {
		int pos = 0;
		for (GeocacheLog l : getOpenCachingLogs()) {
			if (l.getCacheCode().equalsIgnoreCase(waypoint)) {
				return pos;
			}
			pos++;
		}
		return ListView.INVALID_POSITION;
	}

	public Geokret getGeoKretByTrackingCode(String trackingCode) {
		for (Geokret gk : getInventory()) {
			if (gk.getTackingCode().equals(trackingCode)) {
				return gk;
			}
		}
		return null;
	}

}
