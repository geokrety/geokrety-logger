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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.data.GeocacheLog;
import pl.nkg.lib.dialogs.AbstractProgressDialogWrapper;
import pl.nkg.lib.okapi.SupportedOKAPI;
import pl.nkg.lib.threads.AbstractForegroundTaskWrapper;
import pl.nkg.lib.threads.ForegroundTaskHandler;
import pl.nkg.lib.threads.TaskListener;
import android.content.Context;

public class RefreshAccount extends
		AbstractForegroundTaskWrapper<Account, String, Boolean> {

	public static final int ID = 1;
	private String[] messages;
	private String dots;

	public RefreshAccount(GeoKretyApplication application) {
		super(application, ID);
	}

	@Override
	protected Boolean runInBackground(Account param) throws Throwable {
		Account account = param;

		publishProgress(getProgressMessage(0));
		account.loadInventory();
		
		ArrayList<GeocacheLog> openCachingLogs = new ArrayList<GeocacheLog>();
		for (int i = 0; i < SupportedOKAPI.SUPPORTED.length; i++) {
			if (account.hasOpenCachingUUID(i)) {
				publishProgress(getProgressMessage(1) + " "
						+ SupportedOKAPI.SUPPORTED[i].host + dots);
				openCachingLogs.addAll(account.loadOpenCachingLogs(i));
				publishProgress(getProgressMessage(2) + " "
						+ SupportedOKAPI.SUPPORTED[i].host + dots);
				account.loadOCnamesToBuffer(openCachingLogs, i);
			}
		}
		Collections.sort(openCachingLogs, new Comparator<GeocacheLog>() {
			@Override
			public int compare(GeocacheLog lhs, GeocacheLog rhs) {
				return rhs.getDate().compareTo(lhs.getDate());
			}
		});
		account.setOpenCachingLogs(openCachingLogs);
		account.touchLastLoadedDate();
		return true;
	}

	private String getProgressMessage(int step) {
		synchronized (this) {
			if (messages == null) {
				return "";
			}
			return messages[step];
		}
	}

	@Override
	public void attach(
			AbstractProgressDialogWrapper<String> progressDialogWrapper,
			TaskListener<Account, String, Boolean> listener) {
		super.attach(progressDialogWrapper, listener);

		synchronized (this) {
			Context ctx = progressDialogWrapper.getManagedDialogsActivity();
			messages = new String[3];
			messages[0] = ctx.getText(R.string.download_getting_gk).toString();
			messages[1] = ctx.getText(R.string.download_getting_ocs).toString();
			messages[2] = ctx.getText(R.string.download_getting_names).toString();
			dots = ctx.getText(R.string.dots).toString();
		}
	}

	public static RefreshAccount getFromHandler(ForegroundTaskHandler handler) {
		AbstractForegroundTaskWrapper<?, ?, ?> a = handler.getTask(ID);
		RefreshAccount b = (RefreshAccount) a;
		return b;
	}
}
