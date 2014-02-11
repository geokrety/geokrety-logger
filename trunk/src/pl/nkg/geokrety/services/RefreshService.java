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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.acra.ACRA;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.data.GeoKret;
import pl.nkg.geokrety.data.GeoKretDataSource;
import pl.nkg.geokrety.data.Geocache;
import pl.nkg.geokrety.data.GeocacheLog;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.data.User;
import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.lib.gkapi.GeoKretyProvider;
import pl.nkg.lib.okapi.OKAPIProvider;
import pl.nkg.lib.okapi.SupportedOKAPI;
import pl.nkg.lib.threads.ICancelable;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

public class RefreshService extends IntentService {

    public static final String BROADCAST_START = "pl.nkg.geokrety.services.RefreshService.Submits.Start";
    public static final String BROADCAST_PROGRESS = "pl.nkg.geokrety.services.RefreshService.Progress";
    public static final String BROADCAST_ERROR = "pl.nkg.geokrety.services.RefreshService.Error";
    public static final String BROADCAST_CANCELED = "pl.nkg.geokrety.services.RefreshService.Canceled";
    public static final String BROADCAST_FINISH = "pl.nkg.geokrety.services.RefreshService.Submits.Finish";
    public static final String INTENT_ERROR_MESSAGE = "error";

    private static final String TAG = RefreshService.class.getSimpleName();

    private GeoKretyApplication application;
    private StateHolder stateHolder;
    private NotificationManager notificationManager;
    
    private static final int NOTIFY_ID = 2000000000;
    
    private class CancelHolder implements ICancelable {
        
        public boolean cancel;
        
        @Override
        public boolean isCancelled() {
            return cancel;
        }
    }
    private CancelHolder currentCancelHolder;

    public RefreshService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = (GeoKretyApplication) getApplication();
        stateHolder = application.getStateHolder();
        notificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        Log.println(Log.INFO, TAG, "Create");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (currentCancelHolder != null) {
            currentCancelHolder.cancel = true;
            currentCancelHolder = null;
        }
        
        CancelHolder cancelHolder = new CancelHolder();
        currentCancelHolder = cancelHolder;
        
        String error = null;
        Log.println(Log.INFO, TAG, "Run refresh service...");
        try {
            sendBroadcast(new Intent(BROADCAST_START));
            error = refreshBatch(cancelHolder);
            if (Utils.isEmpty(error)) {
                notificationManager.cancel(NOTIFY_ID);
                sendBroadcast(new Intent(cancelHolder.isCancelled() ? BROADCAST_CANCELED : BROADCAST_FINISH));
                error = null;
            }
        } catch (Throwable e) {
            Log.println(Log.ERROR, TAG, e.getLocalizedMessage());
            ACRA.getErrorReporter().handleSilentException(e);
            error = e.getLocalizedMessage();
            e.printStackTrace();
        }
        
        if (!Utils.isEmpty(error)) {
            showNotify(new Intent(), NOTIFY_ID, android.R.drawable.stat_notify_error, getText(R.string.message_submit_problem), error);
            Intent broadcast = new Intent(BROADCAST_ERROR);
            broadcast.putExtra(INTENT_ERROR_MESSAGE, error);
            sendBroadcast(broadcast);
        }
        
        Log.println(Log.INFO, TAG, "Finish refresh service");
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
    
    private String refreshBatch(CancelHolder cancelHolder) {
        List<User> users = stateHolder.getUserDataSource().getAll();
        StringBuilder sb = new StringBuilder();
        
        for (User user : users) {
            if (!canContinue(cancelHolder, sb)) {
                return sb.toString();
            }
            
            try {
                publishProgress(R.string.notify_refresh_inventory, user.getName() + getText(R.string.dots));
                refreshInventory(cancelHolder, user.getID(), user.getGeoKreySecredID());
            } catch (MessagedException e) {
                appendToStringBuilderWithNewLineIfNeed(sb, getText(R.string.notify_refresh_inventory));
                sb.append(": ");
                sb.append(user.getName());
                sb.append(" - ");
                sb.append(e.getFormatedMessage(this));
            }
        }
        
        for (int portal = 0; portal < SupportedOKAPI.SUPPORTED.length; portal++) {
            final SupportedOKAPI okapi = SupportedOKAPI.SUPPORTED[portal];
            
            for (User user : users) {
                if (!canContinue(cancelHolder, sb)) {
                    return sb.toString();
                }
                
                String uuid = user.getOpenCachingUUIDs()[portal];
                if (Utils.isEmpty(uuid)) {
                    continue;
                }
                
                try {
                    publishProgress(R.string.notify_refresh_last_logs, okapi.host + " " + user.getOpenCachingLogins()[portal] + getText(R.string.dots));
                    refreshLastLogs(cancelHolder, user.getID(), uuid, portal);
                } catch (MessagedException e) {
                    appendToStringBuilderWithNewLineIfNeed(sb, getText(R.string.notify_refresh_error_lost_connection));
                    sb.append(": ");
                    sb.append(okapi.host);
                    sb.append(" ");
                    sb.append(user.getOpenCachingLogins()[portal]);
                    sb.append(" - ");
                    sb.append(e.getFormatedMessage(this));
                }
            }
            
            if (!canContinue(cancelHolder, sb)) {
                return sb.toString();
            }
            
            try {
                publishProgress(R.string.notify_refresh_oc_names, okapi.host + getText(R.string.dots));
                refreshGeocaches(cancelHolder, portal);
            } catch (MessagedException e) {
                appendToStringBuilderWithNewLineIfNeed(sb, getText(R.string.notify_refresh_error_lost_connection));
                sb.append(": ");
                sb.append(okapi.host);
                sb.append(" - ");
                sb.append(e.getFormatedMessage(this));
            }
        }
        
        if (!canContinue(cancelHolder, sb)) {
            return sb.toString();
        }
        
        refreshGeoKrets(cancelHolder, sb);
        
        return sb.toString();
    }
    
    private void refreshGeoKrets(CancelHolder cancelHolder, StringBuilder sb) {
        List<String> list = stateHolder.getInventoryDataSource().loadNeedUpdateList();
        
        List<GeoKret> gks = new LinkedList<GeoKret>();
        for (String tc : list) {
            if (!canContinue(cancelHolder, sb)) {
                return;
            }
            
            try {
                publishProgress(R.string.notify_refresh_own_gk, "TrackingCode: " + tc);
                int id = GeoKretyProvider.loadIDByTranckingCode(tc);
                
                if (!canContinue(cancelHolder, sb)) {
                    return;
                }
                
                GeoKret gk;
                if (id != -1) {
                    gk = GeoKretyProvider.loadSingleGeoKretByID(id);
                    gk.setTrackingCode(tc);
                    
                } else {
                    gk = new GeoKret(tc, GeoKretDataSource.SYNCHRO_STATE_ERROR, getText(R.string.error_description_no_such_geokret).toString());
                }
                gks.add(gk);
            } catch (MessagedException e) {
                appendToStringBuilderWithNewLineIfNeed(sb, getText(R.string.notify_refresh_error_lost_connection));
                sb.append(": ");
                sb.append(tc);
                sb.append(" - ");
                sb.append(e.getFormatedMessage(this));
            }
        }
        
        if (!canContinue(cancelHolder, sb)) {
            return;
        }
        
        stateHolder.getGeoKretDataSource().update(gks);
    }

    private static void appendToStringBuilderWithNewLineIfNeed(StringBuilder sb, CharSequence text) {
        if (sb.length() > 0 ) {
            sb.append("\n");
        }
        sb.append(text);
    }
    
    private boolean canContinue(CancelHolder cancelHolder, StringBuilder sb) {
        if (cancelHolder.cancel) {
            appendToStringBuilderWithNewLineIfNeed(sb, getText(R.string.notify_refresh_error_broken));
            return false;
        }
        
        if (!application.isOnline()) {
            appendToStringBuilderWithNewLineIfNeed(sb, getText(R.string.notify_refresh_error_lost_connection));
            return false;
        }
        
        return true;
    }
    
    private void refreshInventory(CancelHolder cancelHolder, long userId, String secId) throws MessagedException {
        final Map<String, GeoKret> gkMap = GeoKretyProvider.loadInventory(secId);

        if (cancelHolder.isCancelled()) {
            return;
        }
        
        HashSet<String> sticky = new HashSet<String>(stateHolder.getInventoryDataSource().loadStickyList(userId));
        for (GeoKret gk : gkMap.values()) {
            if (sticky.contains(gk.getTrackingCode())) {
                gk.setSticky(true);
            }
        }

        stateHolder.getInventoryDataSource().storeInventory(gkMap.values(), userId, true);
        stateHolder.getGeoKretDataSource().update(gkMap.values());
    }
    
    private void refreshLastLogs(CancelHolder cancelHolder, long userId, String uuid, int portal) throws MessagedException {
        SupportedOKAPI okapi = SupportedOKAPI.SUPPORTED[portal];
        List<GeocacheLog> logs = OKAPIProvider.loadOpenCachingLogs(okapi, uuid);
        
        if (cancelHolder.isCancelled()) {
            return;
        }
        
        stateHolder.getGeocacheLogDataSource().store(logs, userId, portal);
    }
    
    private void refreshGeocaches(CancelHolder cancelHolder, int portal) throws MessagedException {
        final SupportedOKAPI okapi = SupportedOKAPI.SUPPORTED[portal];
        final HashSet<String> codes = new HashSet<String>(stateHolder.getGeocacheLogDataSource().loadNeedUpdateList(portal));

        if (codes.size() == 0) {
            return;
        }
        
        List<Geocache> gcs = OKAPIProvider.loadOCnames(codes, okapi);

        if (cancelHolder.isCancelled()) {
            return;
        }
        
        stateHolder.getGeocacheDataSource().update(gcs);
    }
    
    private void publishProgress(int title, CharSequence progress) {
        showNotify(new Intent(), NOTIFY_ID, android.R.drawable.stat_notify_sync, getText(title), progress);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (currentCancelHolder != null) {
            currentCancelHolder.cancel = true;
            currentCancelHolder = null;
        }
        notificationManager.cancel(NOTIFY_ID);
        Log.println(Log.INFO, TAG, "Destroy");
    }
}
