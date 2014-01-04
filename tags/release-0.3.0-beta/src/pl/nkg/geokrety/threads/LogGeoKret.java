/*
 * Copyright (C) 2013, 2014 Michał Niedźwiecki
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
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.data.GeoKretLog;
import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.lib.dialogs.AbstractProgressDialogWrapper;
import pl.nkg.lib.threads.AbstractForegroundTaskWrapper;
import pl.nkg.lib.threads.ForegroundTaskHandler;
import pl.nkg.lib.threads.TaskListener;
import android.content.Context;
import android.util.Pair;

public class LogGeoKret
		extends
		AbstractForegroundTaskWrapper<Pair<GeoKretLog, Account>, String, Boolean> {

	public static final int ID = 2;
	private String message = "";

	public LogGeoKret(GeoKretyApplication application) {
		super(application, ID);
		Context ctx = application.getApplicationContext();
		message = ctx.getText(R.string.submit_message).toString();
	}

	public static LogGeoKret getFromHandler(ForegroundTaskHandler handler) {
		AbstractForegroundTaskWrapper<?, ?, ?> a = handler.getTask(ID);
		LogGeoKret b = (LogGeoKret) a;
		return b;
	}

	@Override
	public void attach(
			AbstractProgressDialogWrapper<String> progressDialogWrapper,
			TaskListener<Pair<GeoKretLog, Account>, String, Boolean> listener) {
		super.attach(progressDialogWrapper, listener);
		progressDialogWrapper.setProgress(message);
	}

	@Override
	protected Boolean runInBackground(Thread thread,
			Pair<GeoKretLog, Account> param) throws Throwable {
		GeoKretLog log = param.first;
		Account account = param.second;
		if (log.submitLog(account) && !thread.isCancelled()) {
			try {
				account.loadInventoryAndStore(thread,
						((GeoKretyApplication) getApplication())
								.getStateHolder().getGeoKretDataSource());
			} catch (MessagedException e) {
				// only refresh
			}
			return true;
		}
		return false;
	}
}
