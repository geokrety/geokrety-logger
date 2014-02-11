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

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.activities.controls.NotifyTextView;
import pl.nkg.geokrety.data.Geocache;
import pl.nkg.geokrety.exceptions.NoConnectionException;
import pl.nkg.lib.gkapi.GeoKretyProvider;

public class WaypointResolverService extends AbstractVerifyService {

    public static final String BROADCAST = "pl.nkg.geokrety.services.WaypointResolverService";

    private static final String TAG = WaypointResolverService.class.getSimpleName();

    public WaypointResolverService() {
        super(TAG, BROADCAST);
    }

    @Override
    protected void onHandleValue(final CharSequence value) throws Exception {
        final String wpt = value.toString();
        sendBroadcast(value, "", NotifyTextView.INFO,
                String.format(getText(R.string.resolve_wpt_message_info_waiting).toString(), wpt));

        Geocache gc = stateHolder.getGeocacheDataSource().loadByWaypoint(wpt);

        final TryDownload<Geocache> td = new TryDownload<Geocache>() {

            @Override
            protected Geocache run() throws NoConnectionException, Exception {
                return GeoKretyProvider.loadCoordinatesByWaypoint(wpt);
            }
        };

        if (gc == null) {
            gc = td.tryRun(application.getRetryCount());
        }

        if (gc == null) {
            sendBroadcast(value, "", NotifyTextView.WARNING,
                    String.format(getText(R.string.resolve_wpt_message_warning_no_connection)
                            .toString(), wpt));
        } else if (gc.getName() != null) {
            sendBroadcast(value, gc.getLocation(),
                    NotifyTextView.GOOD,

                    wpt + ": " + gc.getName());
        } else {
            sendBroadcast(value, "", NotifyTextView.ERROR,
                    String.format(getText(R.string.resolve_wpt_message_error_waypont_not_found)
                            .toString(), wpt));
        }
    }
}
