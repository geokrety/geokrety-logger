/*
 * Copyright (C) 2013 Michał Niedźwiecki
 * 
 * This file is part of GeoKrety Logger
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

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.lib.okapi.OKAPIProvider;
import pl.nkg.lib.okapi.SupportedOKAPI;
import pl.nkg.lib.threads.AbstractForegroundTaskWrapper;
import pl.nkg.lib.threads.ForegroundTaskHandler;
import android.util.Pair;

public class GettingUuidThread extends
		AbstractForegroundTaskWrapper<Pair<String, Integer>, String, String> {

	public static final int ID = 4;

	public GettingUuidThread(GeoKretyApplication application) {
		super(application, ID);
	}

	@Override
	protected String runInBackground(Pair<String, Integer> param)
			throws Throwable {
		return OKAPIProvider.loadOpenCachingUUID(
				SupportedOKAPI.SUPPORTED[param.second], param.first);
	}

	public static GettingUuidThread getFromHandler(ForegroundTaskHandler handler) {
		AbstractForegroundTaskWrapper<?, ?, ?> a = handler.getTask(ID);
		GettingUuidThread b = (GettingUuidThread) a;
		return b;
	}
}
