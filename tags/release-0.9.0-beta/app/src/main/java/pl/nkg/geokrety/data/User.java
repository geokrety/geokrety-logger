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

import pl.nkg.geokrety.Utils;
import android.os.Bundle;

public class User {
    public static final String ACCOUNT_ID = "accountID";
    public static final String ACCOUNT_NAME = "accountName";
    public static final String SECID = "secid";
    public static final String OCUUIDS = "ocUUIDs";
    public static final String OCLOGINS = "ocLogins";
    public static final String HOME_LON = "homeCordLon";
    public static final String HOME_LAT = "homeCordLat";
    public static final String GC_LOGIN = "gcLogin";
    public static final String GC_PASSWORD = "gcPassword";

    private long id;
    private String name;

    private String geoKretySecredID;
    private String[] openCachingUUIDs;
    private String[] openCachingLogins;

    private String homeCordLon;
    private String homeCordLat;

    private String gcLogin;
    private String gcPassword;

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

    public String getGeocachingLogin() {
        return gcLogin;
    }

    public String getGeocachingPassword() {
        return gcPassword;
    }

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

    public String getName() {
        return name;
    }

    public String[] getOpenCachingLogins() {
        return openCachingLogins;
    }

    public String[] getOpenCachingUUIDs() {
        return openCachingUUIDs;
    }

    public boolean hasOpenCachingUUID(final int portal) {
        if (openCachingUUIDs == null || portal < 0 || portal >= openCachingUUIDs.length) {
            return false;
        }

        return !Utils.isEmpty(openCachingUUIDs[portal]);
    }

    public Bundle pack(final Bundle bundle) {
        bundle.putStringArray(User.OCUUIDS, openCachingUUIDs);
        bundle.putStringArray(User.OCLOGINS, openCachingLogins);
        bundle.putLong(User.ACCOUNT_ID, id);
        bundle.putString(User.SECID, geoKretySecredID);
        bundle.putString(User.ACCOUNT_NAME, name);
        bundle.putString(User.HOME_LAT, homeCordLat);
        bundle.putString(User.HOME_LON, homeCordLon);
        bundle.putString(User.GC_LOGIN, gcLogin);
        bundle.putString(User.GC_PASSWORD, gcPassword);
        return bundle;
    }

    public void setGeocachingLogin(final String gcLogin) {
        this.gcLogin = gcLogin;
    }

    public void setGeocachingPassword(final String gcPassword) {
        this.gcPassword = gcPassword;
    }

    public void setHomeCordLat(final String homeCordLat) {
        this.homeCordLat = homeCordLat;
    }

    public void setHomeCordLon(final String homeCordLon) {
        this.homeCordLon = homeCordLon;
    }

    public void setID(final long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public Bundle unpack(final Bundle bundle) {
        geoKretySecredID = bundle.getString(User.SECID);
        id = bundle.getLong(User.ACCOUNT_ID);
        openCachingUUIDs = bundle.getStringArray(User.OCUUIDS);
        openCachingLogins = bundle.getStringArray(User.OCLOGINS);
        name = bundle.getString(User.ACCOUNT_NAME);
        homeCordLat = bundle.getString(User.HOME_LAT);
        homeCordLon = bundle.getString(User.HOME_LON);
        gcLogin = bundle.getString(User.GC_LOGIN);
        gcPassword = bundle.getString(User.GC_PASSWORD);
        return bundle;
    }


}
