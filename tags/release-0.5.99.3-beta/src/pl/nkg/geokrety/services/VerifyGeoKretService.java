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

import org.acra.ACRA;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.activities.GeoKretActivity;
import pl.nkg.geokrety.data.GeoKret;
import pl.nkg.geokrety.data.GeoKretDataSource;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.geokrety.exceptions.NoConnectionException;
import pl.nkg.lib.gkapi.GeoKretyProvider;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class VerifyGeoKretService extends IntentService {

    public static final String BROADCAST = "pl.nkg.geokrety.services.VerifyGeoKretService";
    public static final String INTENT_MESSAGE = "message";
    public static final String INTENT_TRACKING_CODE = "tracking_code";
    public static final String INTENT_MESSAGE_TYPE = "message_type";

    private static final String TAG = VerifyGeoKretService.class.getSimpleName();

    private GeoKretyApplication application;
    private StateHolder stateHolder;

    public VerifyGeoKretService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = (GeoKretyApplication) getApplication();
        stateHolder = application.getStateHolder();
        Log.println(Log.INFO, TAG, "Create");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final String tc = intent.getExtras().getString(INTENT_TRACKING_CODE);
        Log.println(Log.INFO, TAG, "Run refresh service for " + tc + "...");
        try {
            sendBroadcast(GeoKretActivity.INFO, tc,
                    getText(R.string.verify_tc_message_info_waiting));

            GeoKret gk = stateHolder.getGeoKretDataSource().loadByTrackingCode(tc);

            TryDownload<GeoKret> td = new TryDownload<GeoKret>() {

                @Override
                protected GeoKret run() throws NoConnectionException, Exception {
                    return retriveGeoKret(tc);
                }
            };

            if (gk == null) {
                gk = td.tryRun(application.getRetryCount());
            }

            if (gk == null) {
                sendBroadcast(GeoKretActivity.WARNING, tc,
                        getText(R.string.verify_tc_message_warning_no_connection));
            } else if (gk.getSynchroState() == GeoKretDataSource.SYNCHRO_STATE_SYNCHRONIZED) {
                sendBroadcast(
                        GeoKretActivity.GOOD,
                        tc,
                        getText(R.string.verify_tc_message_good_verified) + " "
                                + gk.getFormatedCodeAndName());
            } else {
                sendBroadcast(GeoKretActivity.ERROR, tc,
                        getText(R.string.verify_tc_message_error_geokret_not_found));
            }

        } catch (Throwable e) {
            Log.println(Log.ERROR, TAG, e.getLocalizedMessage());
            ACRA.getErrorReporter().handleSilentException(e);
            e.printStackTrace();
            sendBroadcast(GeoKretActivity.ERROR, tc, e.getLocalizedMessage());
        }

        Log.println(Log.INFO, TAG, "Finish refresh service for " + tc);
    }

    private GeoKret retriveGeoKret(String tc) throws MessagedException {
        int id = GeoKretyProvider.loadIDByTranckingCode(tc);
        if (id == -1) {
            return new GeoKret(tc, GeoKretDataSource.SYNCHRO_STATE_ERROR, getText(
                    R.string.error_description_no_such_geokret).toString());
        } else {
            GeoKret gk = GeoKretyProvider.loadSingleGeoKretByID(id);
            gk.setTrackingCode(tc);
            List<GeoKret> gks = new LinkedList<GeoKret>();
            gks.add(gk);
            stateHolder.getGeoKretDataSource().update(gks);
            return gk;
        }
    }

    private void sendBroadcast(int type, CharSequence trackingCode,
            CharSequence error) {
        Intent broadcast = new Intent(BROADCAST);
        broadcast.putExtra(INTENT_MESSAGE_TYPE, type);
        broadcast.putExtra(INTENT_TRACKING_CODE, trackingCode);
        broadcast.putExtra(INTENT_MESSAGE, error);
        sendBroadcast(broadcast);
    }
}
