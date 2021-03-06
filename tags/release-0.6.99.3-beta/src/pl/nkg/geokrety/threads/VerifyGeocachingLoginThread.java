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
package pl.nkg.geokrety.threads;

import pl.nkg.lib.gcapi.GeocachingProvider;
import pl.nkg.lib.threads.AbstractForegroundTaskWrapper;
import pl.nkg.lib.threads.ForegroundTaskHandler;
import pl.nkg.geokrety.GeoKretyApplication;
import android.util.Pair;

public class VerifyGeocachingLoginThread extends
		AbstractForegroundTaskWrapper<Pair<String, String>, String, Boolean> {

	public static final int ID = 5;

	public VerifyGeocachingLoginThread(GeoKretyApplication application) {
		super(application, ID);
	}

	public static VerifyGeocachingLoginThread getFromHandler(
			ForegroundTaskHandler handler) {
		AbstractForegroundTaskWrapper<?, ?, ?> a = handler.getTask(ID);
		VerifyGeocachingLoginThread b = (VerifyGeocachingLoginThread) a;
		return b;
	}

	@Override
	protected Boolean runInBackground(Thread thread,
			Pair<String, String> param) throws Throwable {
		return GeocachingProvider.login(param.first, param.second) != null;
	}
}
