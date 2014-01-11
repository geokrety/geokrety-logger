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

	@SuppressWarnings("deprecation")
	@Override
	protected void onHandleIntent(final Intent intent) {
		final List<GeoKretLog> outbox = application.getStateHolder().getGeoKretLogDataSource().loadOutbox();

		boolean connectionProblems = false;

		for (final GeoKretLog log : outbox) {
			if (!application.getStateHolder().lockForLog(log.getId())) {
				continue;
			}
			
			// TODO: need refactor and clean
			String title = log.getNr();
			Notification notification = new Notification(R.drawable.writing_log_submitting, title + ": " + getText(R.string.message_submitting), System.currentTimeMillis());;
			notification.setLatestEventInfo(this, title, getText(R.string.message_submitting),
		            PendingIntent.getActivity(this, 1, intent, 0));
			notificationManager.notify((int)log.getId(), notification);
			
			int icon = 0;
			String message = "";
			
			int ret = GeoKretyProvider.submitLog(log);
			switch (ret) {
				case GeoKretyProvider.LOG_NO_CONNECTION:
					connectionProblems = true;
					message = getText(R.string.message_submit_no_connection) + " (" + log.getProblemArg() + ")";
					icon = R.drawable.writing_log_no_connection;
					break;
					
				case GeoKretyProvider.LOG_PROBLEM:
					message = getText(R.string.message_submit_problem) + ": " +  getText(log.getProblem()) + " " + log.getProblemArg();
					icon = R.drawable.writing_log_problem;
					break;
					
				case GeoKretyProvider.LOG_SUCCESS:
					message = getText(R.string.message_submit_success).toString();
					icon = R.drawable.writing_log_success;
					break;
					
				case GeoKretyProvider.LOG_DOUBLE:
					message = getText(log.getProblem()).toString();
					icon = R.drawable.writing_log_double;
					break;
			}
			
			notification = new Notification(icon, title + ": " + message, System.currentTimeMillis());;
			notification.setLatestEventInfo(this, title, message,
		            PendingIntent.getActivity(this, 1, intent, 0));
			notificationManager.notify((int)log.getId(), notification);
			application.getStateHolder().getGeoKretLogDataSource().merge(log);
			application.getStateHolder().releaseLockForLog(log.getId());
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
