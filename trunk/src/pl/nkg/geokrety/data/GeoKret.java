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

import java.util.Locale;

import org.w3c.dom.Node;

import android.database.Cursor;

public class GeoKret {
    private Integer mGeoKretId;
    private Integer mDist;
    private Integer mOwnerId;
    private Integer mState;
    private Integer mType;
    private String mName;
    private String mTrackingCode;
    private boolean mSticky = false;
    private int mSynchroState;
    private String mSynchroError;


    public GeoKret(final Node node) {
        mGeoKretId = Integer.parseInt(node.getAttributes().getNamedItem("id")
                .getNodeValue());
        mDist = Integer.parseInt(node.getAttributes().getNamedItem("dist")
                .getNodeValue());
        mOwnerId = Integer.parseInt(node.getAttributes()
                .getNamedItem("owner_id").getNodeValue());
        mType = Integer.parseInt(node.getAttributes().getNamedItem("type")
                .getNodeValue());
        mTrackingCode = node.getAttributes().getNamedItem("nr").getNodeValue();
        mName = node.getChildNodes().item(0).getNodeValue();

        final Node stateNode = node.getAttributes().getNamedItem("state");
        if (stateNode == null) {
            mState = null;
        } else {
            mState = Integer.parseInt(stateNode.getNodeValue());
        }
        mSynchroState = GeoKretDataSource.SYNCHRO_STATE_SYNCHRONIZED;
    }

    public GeoKret(final String trackingCode, final int synchroState, final String synchroError) {
        mTrackingCode = trackingCode;
        mSynchroState = synchroState;
        mSynchroError = synchroError;
    }

    public GeoKret(Cursor cursor) {
        mTrackingCode = cursor.getString(1);
        mSticky = cursor.getInt(2) != 0;
        mGeoKretId = cursor.isNull(3) ? null : cursor.getInt(3);
        mDist = cursor.isNull(4) ? null : cursor.getInt(4);
        mOwnerId = cursor.isNull(5) ? null : cursor.getInt(5);
        mState = cursor.isNull(6) ? null : cursor.getInt(6);
        mType = cursor.isNull(7) ? null : cursor.getInt(7);
        mName = cursor.isNull(8) ? null : cursor.getString(8);
        mSynchroState = cursor.isNull(9) ? GeoKretDataSource.SYNCHRO_STATE_UNSYNCHRONIZED : cursor.getInt(9);
        mSynchroError = cursor.isNull(10) ? null : cursor.getString(10);
    }

    public int getDist() {
        return mDist;
    }

    public int getGeoKretId() {
        return mGeoKretId;
    }

    public String getName() {
        return mName;
    }

    public int getOwnerId() {
        return mOwnerId;
    }

    public Integer getState() {
        return mState;
    }

    public String getSynchroError() {
        return mSynchroError;
    }

    public int getSynchroState() {
        return mSynchroState;
    }

    public String getTrackingCode() {
        return mTrackingCode;
    }

    public Integer getType() {
        return mType;
    }

    public boolean isSticky() {
        return mSticky;
    }

    public void setDist(final Integer dist) {
        mDist = dist;
    }

    public void setGeoKretId(final Integer geoKretId) {
        mGeoKretId = geoKretId;
    }

    public void setName(final String name) {
        mName = name;
    }

    public void setOwnerId(final Integer ownerId) {
        mOwnerId = ownerId;
    }

    public void setState(final Integer state) {
        mState = state;
    }

    public void setSticky(final boolean sticky) {
        mSticky = sticky;
    }

    public void setSynchroError(final String synchroError) {
        mSynchroError = synchroError;
    }

    public void setSynchroState(final int synchroState) {
        mSynchroState = synchroState;
    }

    public void setTrackingCode(final String nr) {
        mTrackingCode = nr;
    }

    public void setType(final Integer type) {
        mType = type;
    }

    @Override
    public String toString() {
        return mName + " (" + mTrackingCode + ")";
    }

    public String getFormatedCode() {
        if (mGeoKretId == null /*|| mGeoKretId == 0*/) {
            return "...";
        } else {
            return "GK" + Integer.toHexString(mGeoKretId).toUpperCase(Locale.ENGLISH);
        }
    }
}
