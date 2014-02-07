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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.geokrety.threads.RefreshAccount;
import pl.nkg.lib.gkapi.GeoKretyProvider;
import pl.nkg.lib.okapi.OKAPIProvider;
import pl.nkg.lib.okapi.SupportedOKAPI;
import pl.nkg.lib.threads.ICancelable;
import android.os.Bundle;
import android.widget.AdapterView;

public class User {
    public static final String ACCOUNT_ID = "accountID";
    public static final String ACCOUNT_NAME = "accountName";
    public static final String SECID = "secid";
    public static final String OCUUIDS = "ocUUIDs";
    public static final String OCLOGINS = "ocLogins";
    public static final String HOME_LON = "homeCordLon";
    public static final String HOME_LAT = "homeCordLat";

    // private static final long EXPIRED = 24 * 60 * 60 * 1000; // TODO: turn
    // off during refresh in bacground

    private long id;
    private String name;

    private String geoKretySecredID;
    private String[] openCachingUUIDs;
    private String[] openCachingLogins;

    //@Deprecated
    //private List<GeocacheLog> openCachingLogs;

    //@Deprecated
    //private List<GeoKret> inventory;
    // private List<GeoKretLog> geoKretLogs;

    private Date lastDataLoaded;

    private String homeCordLon;
    private String homeCordLat;

    public User(final Bundle bundle) {
        unpack(bundle);
    }

    public User(final int id, final String name, final String geoKretySecredID,
            final String[] openCachingUUIDs, final String[] openCachingLogins) {
        this.id = id;
        this.name = name;
        this.geoKretySecredID = geoKretySecredID;
        this.openCachingUUIDs = openCachingUUIDs;
        this.openCachingLogins = openCachingLogins;
    }

    public String[] getOpenCachingLogins() {
        return openCachingLogins;
    }

    public boolean expired() {

        if (lastDataLoaded == null) {
            return true;
        }
        // return new Date().getTime() - lastDataLoaded.getTime() > EXPIRED;
        return false; // TODO: turn off during refresh in background
                      // implementation
    }

    /*public GeoKret getGeoKretByTrackingCode(final String trackingCode) {
        for (final GeoKret gk : getInventory()) {
            if (gk.getTrackingCode().equals(trackingCode)) {
                return gk;
            }
        }
        return null;
    }*/

    /*
     * public List<GeoKretLog> getGeoKretyLogs() { return geoKretLogs; }
     */

    public String getGeoKreySecredID() {
        return geoKretySecredID;
    }

    public String getHomeCordLat() {
        return homeCordLat;
    }

    public String getHomeCordLon() {
        return homeCordLon;
    }

    public long getID() {
        return id;
    }

    /*@Deprecated
    public List<GeoKret> getInventory() {
        if (inventory == null) {
            inventory = new ArrayList<GeoKret>();
        }
        return inventory;
    }*/

    public Date getLastDataLoaded() {
        return lastDataLoaded;
    }

    public String getName() {
        return name;
    }

    /*@Deprecated
    public List<GeocacheLog> getOpenCachingLogs() {
        if (openCachingLogs == null) {
            openCachingLogs = new ArrayList<GeocacheLog>();
        }
        return openCachingLogs;
    }*/

    public String[] getOpenCachingUUIDs() {
        return openCachingUUIDs;
    }

    /*public int getTrackingCodeIndex(final String trackingCode) {
        int pos = 0;
        for (final GeoKret g : getInventory()) {
            if (g.getTrackingCode().equalsIgnoreCase(trackingCode)) {
                return pos;
            }
            pos++;
        }
        return AdapterView.INVALID_POSITION;
    }*/

    /*private HashSet<String> getUnbufferedCacheCodes(final Collection<GeocacheLog> openCachingLogs) {
        final HashSet<String> caches = new HashSet<String>();
        for (final GeocacheLog log : new ArrayList<GeocacheLog>(openCachingLogs)) {
            if (!StateHolder.getGeoacheMap().containsKey(log.getCacheCode())) {
                caches.add(log.getCacheCode());
            }
        }
        return caches;
    }

    public int getWaypointIndex(final String waypoint) {
        int pos = 0;
        for (final GeocacheLog l : getOpenCachingLogs()) {
            if (l.getCacheCode().equalsIgnoreCase(waypoint)) {
                return pos;
            }
            pos++;
        }
        return AdapterView.INVALID_POSITION;
    }*/

    public boolean hasOpenCachingUUID(final int portal) {
        if (openCachingUUIDs == null || portal < 0 || portal >= openCachingUUIDs.length) {
            return false;
        }

        return !Utils.isEmpty(openCachingUUIDs[portal]);
    }

    public void loadData(final GeoKretyApplication application, final boolean force) {
        application.getForegroundTaskHandler().runTask(RefreshAccount.ID, this, force);
    }

    public boolean loadIfExpired(final GeoKretyApplication application, final boolean force) {
        if (expired()) {
            loadData(application, force);
            return true;
        } else {
            return false;
        }
    }

    // TODO: need refactor and move to background
    @Deprecated
    public void loadInventoryAndStore(final ICancelable cancelable,
            final InventoryDataSource dataSource, final GeoKretDataSource gkDataSource) throws MessagedException {

        final Map<String, GeoKret> gkMap = GeoKretyProvider.loadInventory(geoKretySecredID);

        if (cancelable.isCancelled()) {
            return;
        }
        
        HashSet<String> sticky = new HashSet<String>(dataSource.loadStickyList(id));
        for (GeoKret gk : gkMap.values()) {
            if (sticky.contains(gk.getTrackingCode())) {
                gk.setSticky(true);
            }
        }

        dataSource.storeInventory(gkMap.values(), getID(), true);
        gkDataSource.update(gkMap.values());
    }

    // TODO: need refactor and move to background
    @Deprecated
    public void loadOCnamesToBuffer(final ICancelable cancelable,
            final List<GeocacheLog> openCachingLogs, final int portal, final GeocacheLogDataSource gclDataSource, final GeocacheDataSource gcDataSource) throws MessagedException {
        final SupportedOKAPI okapi = SupportedOKAPI.SUPPORTED[portal];
        final HashSet<String> codes = new HashSet<String>(gclDataSource.loadNeedUpdateList(portal));

        if (codes.size() == 0) {
            return;
        }

        if (cancelable.isCancelled()) {
            return;
        }
        
        gcDataSource.update(OKAPIProvider.loadOCnames(codes, okapi));
    }

    // TODO: need refactor and move to background
    @Deprecated
    public void loadOpenCachingLogs(final int portal, final GeocacheLogDataSource gclDataSource, final GeocacheDataSource gcDataSource) throws MessagedException {

        final SupportedOKAPI okapi = SupportedOKAPI.SUPPORTED[portal];
        gclDataSource.store(OKAPIProvider.loadOpenCachingLogs(okapi, openCachingUUIDs[portal]), id, portal);
    }

    public Bundle pack(final Bundle bundle) {
        bundle.putStringArray(User.OCUUIDS, openCachingUUIDs);
        bundle.putStringArray(User.OCLOGINS, openCachingLogins);
        bundle.putLong(User.ACCOUNT_ID, id);
        bundle.putString(User.SECID, geoKretySecredID);
        bundle.putString(User.ACCOUNT_NAME, name);
        bundle.putString(User.HOME_LAT, homeCordLat);
        bundle.putString(User.HOME_LON, homeCordLon);
        return bundle;
    }

    /*
     * public void setGeoKretyLogs(final List<GeoKretLog> geoKretLogs) {
     * this.geoKretLogs = geoKretLogs; }
     */

    public void setHomeCordLat(final String homeCordLat) {
        this.homeCordLat = homeCordLat;
    }

    public void setHomeCordLon(final String homeCordLon) {
        this.homeCordLon = homeCordLon;
    }

    public void setID(final long id) {
        this.id = id;
    }

    /*@Deprecated
    public void setInventory(final List<GeoKret> gks) {
        inventory = gks;
    }*/

    public void setLastDataLoaded(final Date lastDataLoaded) {
        this.lastDataLoaded = lastDataLoaded;
    }

    public void setName(final String name) {
        this.name = name;
    }

    /*@Deprecated
    public void setOpenCachingLogs(final List<GeocacheLog> openCachingLogs) {
        this.openCachingLogs = openCachingLogs;
    }*/

    @Override
    public String toString() {
        return name;
    }

    public void touchLastLoadedDate(final UserDataSource accountDataSource) {
        lastDataLoaded = new Date();
        accountDataSource.storeLastLoadedDate(this);
    }

    public Bundle unpack(final Bundle bundle) {
        geoKretySecredID = bundle.getString(User.SECID);
        id = bundle.getLong(User.ACCOUNT_ID);
        openCachingUUIDs = bundle.getStringArray(User.OCUUIDS);
        openCachingLogins = bundle.getStringArray(User.OCLOGINS);
        name = bundle.getString(User.ACCOUNT_NAME);
        homeCordLat = bundle.getString(User.HOME_LAT);
        homeCordLon = bundle.getString(User.HOME_LON);
        return bundle;
    }

}
