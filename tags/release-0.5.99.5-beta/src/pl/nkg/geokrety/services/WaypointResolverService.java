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

import org.acra.ACRA;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.activities.GeoKretActivity;
import pl.nkg.geokrety.data.Geocache;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.exceptions.NoConnectionException;
import pl.nkg.lib.gkapi.GeoKretyProvider;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class WaypointResolverService extends IntentService {

    public static final String BROADCAST = "pl.nkg.geokrety.services.WaypointResolverService";
    public static final String INTENT_MESSAGE = "message";
    public static final String INTENT_MESSAGE_TYPE = "message_type";
    public static final String INTENT_WAYPOINT = "waypoint";
    public static final String INTENT_LATLON = "latlon";

    private static final String TAG = WaypointResolverService.class.getSimpleName();

    private GeoKretyApplication application;
    private StateHolder stateHolder;

    public WaypointResolverService() {
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
        final String wpt = intent.getExtras().getString(INTENT_WAYPOINT);
        Log.println(Log.INFO, TAG, "Run refresh service for " + wpt + "...");
        try {
            
            sendBroadcast(GeoKretActivity.INFO, wpt,
                    String.format(getText(R.string.resolve_wpt_message_info_waiting).toString(), wpt), "");

            Geocache gc = stateHolder.getGeocacheDataSource().loadByWaypoint(wpt);

            TryDownload<Geocache> td = new TryDownload<Geocache>() {

                @Override
                protected Geocache run() throws NoConnectionException, Exception {
                    return GeoKretyProvider.loadCoordinatesByWaypoint(wpt);
                }
            };

            if (gc == null) {
                gc = td.tryRun(application.getRetryCount());
            }

            if (gc == null) {
                sendBroadcast(GeoKretActivity.WARNING, wpt,
                        String.format(getText(R.string.resolve_wpt_message_warning_no_connection).toString(), wpt), "");
            } else if (gc.getName() != null) {
                sendBroadcast(
                        GeoKretActivity.GOOD,
                        wpt,
                        wpt + ": " + gc.getName(), gc.getLocation());
            } else {
                sendBroadcast(GeoKretActivity.ERROR, wpt,
                        String.format(getText(R.string.resolve_wpt_message_error_waypont_not_found).toString(), wpt), "");
            }

        } catch (Throwable e) {
            Log.println(Log.ERROR, TAG, e.getLocalizedMessage());
            ACRA.getErrorReporter().handleSilentException(e);
            e.printStackTrace();
            sendBroadcast(GeoKretActivity.ERROR, wpt, e.getLocalizedMessage(), "");
        }

        Log.println(Log.INFO, TAG, "Finish refresh service for " + wpt);
    }

    private void sendBroadcast(int type, CharSequence trackingCode,
            CharSequence message, CharSequence latlon) {
        Intent broadcast = new Intent(BROADCAST);
        broadcast.putExtra(INTENT_MESSAGE_TYPE, type);
        broadcast.putExtra(INTENT_WAYPOINT, trackingCode);
        broadcast.putExtra(INTENT_MESSAGE, message);
        broadcast.putExtra(INTENT_LATLON, latlon);
        sendBroadcast(broadcast);
    }
}
