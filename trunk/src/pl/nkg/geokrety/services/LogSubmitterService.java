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
package pl.nkg.geokrety.services;

import java.util.List;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.data.GeoKretLog;
import pl.nkg.lib.gkapi.GeoKretyProvider;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;

public class LogSubmitterService extends IntentService {

	private static final String			TAG			= LogSubmitterService.class.getName();
	private static final int			RETRY_DELAY	= 1000 * 60 * 5;

	private GeoKretyApplication	application;
	private Handler				handler;
	private NotificationManager notificationManager; 

	public LogSubmitterService() {
		super(TAG);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		application = (GeoKretyApplication) getApplication();
		handler = new Handler();
		notificationManager = ((NotificationManager)getSystemService(NOTIFICATION_SERVICE));
	}

	@Override
	protected void onHandleIntent(final Intent intent) {
		final List<GeoKretLog> outbox = application.getStateHolder().getGeoKretLogDataSource().loadOutbox();

		boolean connectionProblems = false;

		for (final GeoKretLog log : outbox) {
			// TODO: need refactor and clean
			String title = log.getNr();
			Notification notification = new Notification(android.R.drawable.stat_sys_upload, title + ": sbmitting...", System.currentTimeMillis());;
			notification.setLatestEventInfo(this, title, "submitting...",
		            PendingIntent.getActivity(this, 1, intent, 0));
			notificationManager.notify(log.getId(), notification);
			
			int icon = android.R.drawable.stat_sys_upload_done;
			String message = "";
			
			int ret = GeoKretyProvider.submitLog(log);
			switch (ret) {
				case GeoKretyProvider.LOG_NO_CONNECTION:
					connectionProblems = true;
					message = log.getProblemArg();
					break;
					
				case GeoKretyProvider.LOG_PROBLEM:
					message = getText(log.getProblem()) + " " + log.getProblemArg();
					break;
					
				case GeoKretyProvider.LOG_SUCCESS:
					message = getText(R.string.submit_finish).toString();
					break;
			}
			
			notification = new Notification(icon, title + ": " + message, System.currentTimeMillis());;
			notification.setLatestEventInfo(this, title, message,
		            PendingIntent.getActivity(this, 1, intent, 0));
			notificationManager.notify(log.getId(), notification);
			application.getStateHolder().getGeoKretLogDataSource().merge(log);
		}

		if (connectionProblems) {

			// Retry after 5 minutes
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					startService(new Intent(LogSubmitterService.this, LogSubmitterService.class));
				}
			}, RETRY_DELAY);
		}
	}

}
