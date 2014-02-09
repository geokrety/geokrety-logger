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
package pl.nkg.geokrety.receivers;

import pl.nkg.geokrety.services.LogSubmitterService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;

public class NetworkStateReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context paramContext, final Intent paramIntent) {
		if (paramIntent.getExtras() != null) {
			final NetworkInfo localNetworkInfo = (NetworkInfo) paramIntent.getExtras().get("networkInfo");
			if (localNetworkInfo != null && localNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
				paramContext.startService(new Intent(paramContext, LogSubmitterService.class));
			}
		}
	}
}
