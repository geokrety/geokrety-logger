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
package pl.nkg.geokrety.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.services.LogSubmitterService;
import pl.nkg.geokrety.services.RefreshService;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;

public abstract class AbstractGeoKretyActivity extends ManagedDialogsActivity {

    protected GeoKretyApplication application;
    protected StateHolder stateHolder;
    protected SQLiteDatabase database;
    protected Cursor cursor;
    private boolean useDataBase = false;
    
    private final BroadcastReceiver refreshFinishBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Toast.makeText(AbstractGeoKretyActivity.this, R.string.refresh_message_refresh_finish, Toast.LENGTH_LONG).show();
            onRefreshDatabase();
        }
    };
    
    private final BroadcastReceiver refreshErrorBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String error = intent.getExtras().getString(RefreshService.INTENT_ERROR_MESSAGE);
            Toast.makeText(AbstractGeoKretyActivity.this, getText(R.string.refresh_message_refresh_error) + " " + error, Toast.LENGTH_LONG).show();
            onRefreshError();
        }
    };
    
    protected void onRefreshDatabase() {
    }
    
    protected void onRefreshError() {
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (GeoKretyApplication)getApplication();
        stateHolder = application.getStateHolder();
    }
    
    private boolean mPaused = true;
    public boolean isPaused() {
        return mPaused;
    }
    
    @Override
    protected void onPause() {
        mPaused = true;
        super.onPause();
        unregisterReceiver(refreshErrorBroadcastReceiver);
        unregisterReceiver(refreshFinishBroadcastReceiver);
        if (useDataBase) {
            database = null;
            closeCursorIfOpened();
            stateHolder.getDbHelper().closeDatabase();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (useDataBase) {
            database = stateHolder.getDbHelper().openDatabase();
            openCursor();
        }
        application.runRefreshService(false);
        registerReceiver(refreshFinishBroadcastReceiver, new IntentFilter(
                RefreshService.BROADCAST_FINISH));
        registerReceiver(refreshErrorBroadcastReceiver, new IntentFilter(
                RefreshService.BROADCAST_ERROR));
        mPaused = false;
    }
    
    protected Cursor openCursor() {
        closeCursorIfOpened();
        return null;
    }

    protected void closeCursorIfOpened() {
        if (useDataBase == false) {
            throw new RuntimeException("You forgot to use turnOnDatabaseUse() in onCreate()");
        }
        
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
    }
    
    protected void turnOnDatabaseUse() {
        if (useDataBase == false) {
            useDataBase = true;
        }
    }
}
