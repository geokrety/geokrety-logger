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

import java.util.ArrayList;
import java.util.List;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.activities.LogActivity;
import pl.nkg.geokrety.data.GeoKretLog;
import pl.nkg.geokrety.data.GeoKretLogDataSource;
import pl.nkg.geokrety.data.GeocacheLog;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.data.User;
import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.lib.gkapi.GeoKretyProvider;
import pl.nkg.lib.okapi.SupportedOKAPI;
import pl.nkg.lib.threads.AbstractForegroundTaskWrapper.Thread;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;

public class RefreshService extends IntentService {

    public static final String BROADCAST_START = "pl.nkg.geokrety.services.RefreshService.Submits.Start";
    public static final String BROADCAST_PROGRESS = "pl.nkg.geokrety.services.RefreshService.Progress";
    public static final String BROADCAST_ERROR = "pl.nkg.geokrety.services.RefreshService.Error";
    public static final String BROADCAST_CANCELED = "pl.nkg.geokrety.services.RefreshService.Canceled";
    public static final String BROADCAST_FINISH = "pl.nkg.geokrety.services.RefreshService.Submits.Finish";

    private static final String TAG = RefreshService.class.getName();
    private static final int RETRY_DELAY = 1000 * 60 * 5;

    private GeoKretyApplication application;
    private Handler handler;
    private NotificationManager notificationManager;
    private CharSequence dots;
    
    private static final int NOTIFY_ID = 2000000000;

    public RefreshService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dots = getText(R.string.dots).toString();
        application = (GeoKretyApplication) getApplication();
        handler = new Handler();
        notificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        try {
            sendBroadcast(new Intent(BROADCAST_START));
            runInBackground();
            notificationManager.cancel(NOTIFY_ID);
            sendBroadcast(new Intent(BROADCAST_FINISH));
        } catch (Throwable e) {
            showNotify(new Intent(), NOTIFY_ID, android.R.drawable.stat_notify_error, getText(R.string.message_submit_problem), e.getLocalizedMessage());
            sendBroadcast(new Intent(BROADCAST_ERROR));
        }
    }
    
    @SuppressWarnings("deprecation")
    private void showNotify(final Intent intent, int id, final int icon,
            CharSequence contentTitle, CharSequence contentMessage) {
        Notification notification = new Notification(icon, contentTitle + ": " + contentMessage,
                System.currentTimeMillis());
        ;
        notification.setLatestEventInfo(this, contentTitle, contentMessage,
                PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(id, notification);
    }

    protected String runInBackground()
            throws Throwable {
        StringBuilder report = new StringBuilder();
        StateHolder holder = application.getStateHolder();
        //User account = param;
        // TODO: refactor in 0.6.0
        for (User user : holder.getAccountList()) {
            if (!refreshProfile(report, user)) {
                return "";
            }
        }
        return report.toString();
    }

    private boolean refreshProfile(StringBuilder report, User account)
            throws MessagedException {
        StateHolder holder = ((GeoKretyApplication) getApplication())
                .getStateHolder();

        try {
            publishProgress(getProgressMessage(0));
            account.loadInventoryAndStore(null, holder.getInventoryDataSource(), holder.getGeoKretDataSource());

        } catch (MessagedException e) {
            report.append("\n");
            report.append(e.getFormatedMessage(getApplication()
                    .getApplicationContext()));
        }

        ArrayList<GeocacheLog> openCachingLogs = new ArrayList<GeocacheLog>();
        for (int i = 0; i < SupportedOKAPI.SUPPORTED.length; i++) {
            if (account.hasOpenCachingUUID(i)/* && !thread.isCancelled()*/) {
                publishProgress(getProgressMessage(1) + " "
                        + SupportedOKAPI.SUPPORTED[i].host + dots);
                try {
                    account.loadOpenCachingLogs(i, holder.getGeocacheLogDataSource(), holder.getGeocacheDataSource());
                } catch (MessagedException e) {
                    report.append("\n");
                    report.append(e.getFormatedMessage(getApplication()
                            .getApplicationContext()));
                }

                /*if (thread.isCancelled()) {
                    return false;
                }*/

                publishProgress(getProgressMessage(2) + " "
                        + SupportedOKAPI.SUPPORTED[i].host + dots);
                account.loadOCnamesToBuffer(null, openCachingLogs, i, holder.getGeocacheLogDataSource(), holder.getGeocacheDataSource());
            }
        }

        /*if (thread.isCancelled()) {
            return false;
        }*/

        account.touchLastLoadedDate(holder.getAccountDataSource());
        return true;
    }
    
    private static final int[] MESSAGES = {
        R.string.download_getting_gk,
        R.string.download_getting_ocs,
        R.string.download_getting_names
    };
    
    private CharSequence getProgressMessage(int step) {
        return getText(MESSAGES[step]);
    }
    
    private void publishProgress(CharSequence progress) {
        showNotify(new Intent(), NOTIFY_ID, android.R.drawable.stat_notify_sync, progress,
                getText(R.string.menu_ocs_refresh));
    }
}
