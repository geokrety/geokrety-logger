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

import java.util.Date;
import java.util.Locale;

import org.apache.http.protocol.HttpContext;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.activities.controls.NotifyTextView;
import pl.nkg.geokrety.data.Geocache;
import pl.nkg.geokrety.data.User;
import pl.nkg.geokrety.exceptions.WaypointNotFoundException;
import pl.nkg.geokrety.exceptions.LocationNotResolvedException;
import pl.nkg.geokrety.exceptions.NoConnectionException;
import pl.nkg.lib.gcapi.GeocachingProvider;
import pl.nkg.lib.gkapi.GeoKretyProvider;

public class WaypointResolverService extends AbstractVerifyService {

    public static final String BROADCAST = "pl.nkg.geokrety.services.WaypointResolverService";

    private static final String TAG = WaypointResolverService.class.getSimpleName();

    private long lastLogin = 0;
    private HttpContext session;
    private static final long SESSION_EXPIRED = 60 * 60 * 1000;

    public WaypointResolverService() {
        super(TAG, BROADCAST);
    }

    @Override
    protected void onHandleValue(final CharSequence value) throws Exception {
        final String wpt = value.toString();
        sendBroadcast(value, "", NotifyTextView.INFO,
                String.format(getText(R.string.resolve_wpt_message_waiting).toString(), wpt));

        Geocache gc = stateHolder.getGeocacheDataSource().loadByWaypoint(wpt);

        final TryDownload<Geocache> td = new TryDownload<Geocache>() {

            @Override
            protected Geocache run() throws NoConnectionException, Exception {
                try {
                    return GeoKretyProvider.loadCoordinatesByWaypoint(wpt);
                } catch (LocationNotResolvedException e) {
                    if (!wpt.toUpperCase(Locale.ENGLISH).startsWith("GC")) {
                        throw e;
                    }
                    
                    if (session == null || new Date().getTime() > lastLogin + SESSION_EXPIRED) {
                        session = null;
                        for (User user : stateHolder.getAccountList()) {
                            if (!Utils.isEmpty(user.getGeocachingLogin())) {
                                session = GeocachingProvider.login(user.getGeocachingLogin(),
                                        user.getGeocachingPassword());
                                if (session != null) {
                                    break;
                                }
                            }
                        }
                    }

                    if (session != null) {
                        Geocache gc = GeocachingProvider.loadGeocacheByWaypoint(session, wpt);
                        stateHolder.getGeocacheDataSource().updateGeocachingCom(gc);
                        return gc;
                    } else {
                        throw e;
                    }
                }
            }
        };

        try {
            if (gc == null) {
                gc = td.tryRun(application.getRetryCount());
            }
            sendBroadcast(value, gc.getLocation(), NotifyTextView.GOOD, wpt + ": " + gc.getName());
        } catch (LocationNotResolvedException e) {
            sendBroadcast(value, "", NotifyTextView.ERROR,
                    String.format(getText(R.string.resolve_wpt_error_location_can_not_be_resolved)
                            .toString(), wpt));
        } catch (WaypointNotFoundException e) {
            sendBroadcast(value, "", NotifyTextView.ERROR,
                    String.format(getText(R.string.resolve_wpt_error_waypont_not_found)
                            .toString(), wpt));
        } catch (NoConnectionException e) {
            sendBroadcast(value, "", NotifyTextView.WARNING,
                    String.format(getText(R.string.resolve_wpt_warning_no_connection)
                            .toString(), wpt));            
        }
    }
}
