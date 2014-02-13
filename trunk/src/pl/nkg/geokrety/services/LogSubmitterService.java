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

package pl.nkg.geokrety.services;

import java.util.List;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.activities.LogActivity;
import pl.nkg.geokrety.data.GeoKretLog;
import pl.nkg.geokrety.data.GeoKretLogDataSource;
import pl.nkg.lib.gkapi.GeoKretyProvider;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Handler;

public class LogSubmitterService extends IntentService {

    public static final String BROADCAST_SUBMITS_START = "pl.nkg.geokrety.services.LogSubmitterService.Submits.Start";
    public static final String BROADCAST_SUBMIT_START = "pl.nkg.geokrety.services.LogSubmitterService.Submit.Start";
    public static final String BROADCAST_SUBMIT_DONE = "pl.nkg.geokrety.services.LogSubmitterService.Submit.Done";
    public static final String BROADCAST_SUBMITS_FINISH = "pl.nkg.geokrety.services.LogSubmitterService.Submits.Finish";

    private static final String TAG = LogSubmitterService.class.getSimpleName();

    private GeoKretyApplication application;
    private Handler handler;
    private NotificationManager notificationManager;

    public LogSubmitterService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = (GeoKretyApplication) getApplication();
        handler = new Handler();
        notificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final List<GeoKretLog> outbox = application.getStateHolder().getGeoKretLogDataSource()
                .loadOutbox();

        boolean connectionProblems = false;

        if (outbox.size() > 0) {
            sendBroadcast(new Intent(BROADCAST_SUBMITS_START));
        }

        for (final GeoKretLog oldlog : outbox) {
            final GeoKretLog log = application.getStateHolder().lockForLog(oldlog.getId());
            if (log == null) {
                continue;
            }
            log.setSecid(oldlog.getSecid());

            Intent broadcastStart = new Intent(BROADCAST_SUBMIT_START);
            Intent broadcastDone = new Intent(BROADCAST_SUBMIT_DONE);
            broadcastStart.putExtra(GeoKretLogDataSource.COLUMN_ID, log.getId());
            broadcastDone.putExtra(GeoKretLogDataSource.COLUMN_ID, log.getId());
            sendBroadcast(broadcastStart);

            String title = log.getNr();
            int notifyId = (int) log.getId();
            showNotify(intent, notifyId, R.drawable.ic_stat_notify_log_submitting, title,
                    getText(R.string.message_submitting));

            int icon = 0;
            String message = "";

            int ret = GeoKretyProvider.submitLog(log);
            switch (ret) {
                case GeoKretyProvider.LOG_NO_CONNECTION:
                    connectionProblems = true;
                    message = getText(R.string.message_submit_no_connection) + " ("
                            + log.getProblemArg() + ")";
                    icon = R.drawable.ic_stat_notify_log_no_connection;
                    break;

                case GeoKretyProvider.LOG_PROBLEM:
                    try {
                        message = getText(R.string.message_submit_problem) + ": "
                                + getText(log.getProblem()) + " " + log.getProblemArg();
                    } catch (NotFoundException e) {
                        message = getText(R.string.message_submit_problem) + ": "
                                + log.getProblemArg();
                    }
                    icon = R.drawable.ic_stat_notify_log_problem;
                    break;

                case GeoKretyProvider.LOG_SUCCESS:
                    message = getText(R.string.message_submit_success).toString();
                    icon = R.drawable.ic_stat_notify_log_success;
                    application.getStateHolder().getGeoKretLogDataSource().delete(log.getId());
                    break;

                case GeoKretyProvider.LOG_DOUBLE:
                    message = getText(log.getProblem()).toString();
                    icon = R.drawable.ic_stat_notify_log_double;
                    break;
            }

            if (ret == GeoKretyProvider.LOG_SUCCESS) {
                showNotify(intent, notifyId, icon, title, message);
            } else {
                Intent notificationIntent = new Intent(getApplicationContext(), LogActivity.class);
                notificationIntent.putExtra(GeoKretLogDataSource.COLUMN_ID, log.getId());
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                showNotify(notificationIntent, notifyId, icon, title, message);
            }

            application.getStateHolder().getGeoKretLogDataSource().merge(log);
            application.getStateHolder().releaseLockForLog(log.getId());
            sendBroadcast(broadcastDone);
        }

        if (connectionProblems && application.isRetrySubmitEnabled()) {

            // Retry after 5 minutes
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startService(new Intent(LogSubmitterService.this, LogSubmitterService.class));
                }
            }, application.getRetrySubmitDelay());
        }

        if (outbox.size() > 0) {
            sendBroadcast(new Intent(BROADCAST_SUBMITS_FINISH));
        }
    }

    @SuppressWarnings("deprecation")
    private void showNotify(final Intent intent, int id, final int icon,
            CharSequence contentTitle, CharSequence contentMessage) {
        Notification notification = new Notification(icon, contentTitle + ": " + contentMessage,
                System.currentTimeMillis());

        notification.setLatestEventInfo(this, contentTitle, contentMessage,
                PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(id, notification);
    }

}
