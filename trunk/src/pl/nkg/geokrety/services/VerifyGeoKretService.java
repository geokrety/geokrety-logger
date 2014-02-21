/*
 * Copyright (C) 2014 Michał Niedźwiecki
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

package pl.nkg.geokrety.services;

import java.util.LinkedList;
import java.util.List;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.activities.controls.NotifyTextView;
import pl.nkg.geokrety.data.GeoKret;
import pl.nkg.geokrety.data.GeoKretDataSource;
import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.geokrety.exceptions.NoConnectionException;
import pl.nkg.lib.gkapi.GeoKretyProvider;

public class VerifyGeoKretService extends AbstractVerifyService {

    public static final String BROADCAST = "pl.nkg.geokrety.services.VerifyGeoKretService";
    private static final String TAG = VerifyGeoKretService.class.getSimpleName();

    public VerifyGeoKretService() {
        super(TAG, BROADCAST);
    }

    private GeoKret retriveGeoKret(final CharSequence tc) throws MessagedException {
        final int id = GeoKretyProvider.loadIDByTranckingCode(tc);
        if (id == -1) {
            return new GeoKret(tc.toString(), GeoKretDataSource.SYNCHRO_STATE_ERROR, getText(
                    R.string.validation_error_no_such_geokret).toString());
        } else {
            final GeoKret gk = GeoKretyProvider.loadSingleGeoKretByID(id);
            gk.setTrackingCode(tc.toString());
            final List<GeoKret> gks = new LinkedList<GeoKret>();
            gks.add(gk);
            stateHolder.getGeoKretDataSource().update(gks);
            return gk;
        }
    }

    @Override
    protected void onHandleValue(final CharSequence value) throws Exception {
        sendBroadcast(value, "", NotifyTextView.INFO,
                getText(R.string.verify_tc_message_waiting));

        GeoKret gk = stateHolder.getGeoKretDataSource().loadByTrackingCode(value);

        final TryDownload<GeoKret> td = new TryDownload<GeoKret>() {

            @Override
            protected GeoKret run() throws NoConnectionException, Exception {
                return retriveGeoKret(value);
            }
        };

        if (gk == null) {
            gk = td.tryRun(application.getRetryCount());
        }

        if (gk == null) {
            sendBroadcast(value, "", NotifyTextView.WARNING,
                    getText(R.string.verify_tc_warning_no_connection));
        } else if (gk.getSynchroState() == GeoKretDataSource.SYNCHRO_STATE_SYNCHRONIZED) {
            sendBroadcast(value, "",
                    NotifyTextView.GOOD,
                    getText(R.string.verify_tc_message_good_verified) + " "
                            + gk.getFormatedCodeAndName());
        } else {
            sendBroadcast(value, "", NotifyTextView.ERROR,
                    getText(R.string.verify_tc_error_geokret_not_found));
        }
    }
}
