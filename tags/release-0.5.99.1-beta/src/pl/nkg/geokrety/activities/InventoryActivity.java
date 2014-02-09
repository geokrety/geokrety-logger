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

package pl.nkg.geokrety.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.activities.listeners.RefreshListener;
import pl.nkg.geokrety.data.GeoKretDataSource;
import pl.nkg.geokrety.data.InventoryDataSource;
import pl.nkg.geokrety.data.User;
import pl.nkg.geokrety.data.GeoKret;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.services.LogSubmitterService;
import pl.nkg.geokrety.services.RefreshService;
import pl.nkg.lib.adapters.ExtendedCursorAdapter;
import pl.nkg.lib.dialogs.AbstractDialogWrapper;
import pl.nkg.lib.threads.AbstractForegroundTaskWrapper;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView;

public class InventoryActivity extends AbstractGeoKretyActivity implements
        AdapterView.OnItemSelectedListener, OnItemClickListener {

    public final static int ADD_GEOKRET = 1;
    public final static int EDIT_GEOKRET = 2;

    private User account;
    //private RefreshProgressDialog refreshProgressDialog;
    //private RefreshAccount refreshAccount;

    private class Adapter extends ExtendedCursorAdapter {

        public Adapter(final Context context, final Cursor c, final boolean autoRequery) {
            super(context, c, true, android.R.layout.simple_list_item_2);
        }

        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            final GeoKret gk = new GeoKret(cursor, 1);
            // bindIcon(view, log);
            bindTextView(view, android.R.id.text1,
                    gk.getFormatedCode() + " (" + gk.getTrackingCode() + ")");
            if (gk.getName() == null) {
                bindTextView(
                        view,
                        android.R.id.text2,
                        gk.getSynchroState() == 0 ? "..." : Utils.defaultIfNull(
                                gk.getSynchroError(), "..."));
            } else {
                bindTextView(view, android.R.id.text2, gk.getName() + " (" + gk.getType() + ")");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        turnOnDatabaseUse();
        //refreshProgressDialog = new RefreshProgressDialog(this);

        /*refreshAccount = RefreshAccount.getFromHandler(application
                .getForegroundTaskHandler());*/

        setContentView(R.layout.activity_inventory);
        Spinner spin = (Spinner) findViewById(R.id.accountsSpiner);
        spin.setOnItemSelectedListener(this);
        ArrayAdapter<User> aa = new ArrayAdapter<User>(this,
                android.R.layout.simple_spinner_item, stateHolder.getAccountList());

        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(aa);
        spin.setSelection(stateHolder.getDefaultAccountNr());

        ((ListView) findViewById(R.id.inventoryListView)).setOnItemClickListener(this);
    }

    /*
    @Override
    protected void onStart() {
        super.onStart();
        refreshAccount.attach(refreshProgressDialog, new RefreshListener(this) {
            @Override
            public void onFinish(
                    AbstractForegroundTaskWrapper<User, String, String> sender,
                    User param, String result) {
                super.onFinish(sender, param, result);
                refreshListView();
            }
        });
    }*/

    @Override
    protected void onRefreshDatabase() {
        super.onRefreshDatabase();
        openCursor();
    }
    
    private Adapter adapter;
    @Override
    protected Cursor openCursor() {
        super.openCursor();
        if (account != null && database != null) {
            cursor = InventoryDataSource.createLoadByUserIDCurosr(database, account.getID());
            if (adapter == null) {
                adapter = new Adapter(this, cursor, true);
                final ListView listView = (ListView) findViewById(R.id.inventoryListView);
                listView.setAdapter(adapter);
            } else {
                adapter.changeCursor(cursor);
            }
        }
        return cursor;
    }

    @Override
    protected void onStop() {
        //refreshAccount.detach();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.inventory, menu);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (stateHolder.getDefaultAccountNr() != ListView.INVALID_POSITION) {
            account = stateHolder.getAccountList().get(stateHolder.getDefaultAccountNr());
            updateListView();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_inventory_refresh:
                refreshAccout();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshAccout() {
        //account.loadData((GeoKretyApplication) getApplication(), true);
        //Intent intent = new Intent(this, RefreshService.class);
        //startService(intent);
        application.runRefreshService(true);
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
            long arg3) {
        //ListView listView = (ListView) findViewById(R.id.inventoryListView);
        //listView.setAdapter(null);
        //StateHolder holder = ((GeoKretyApplication) getApplication())
                //.getStateHolder();
        account = stateHolder.getAccountList().get(arg2);
        updateListView();
    }

    private void updateListView() {
        //if (!account.loadIfExpired((GeoKretyApplication) getApplication(), false)) {
            onRefreshDatabase();
        //}
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    @Override
    public void dialogFinished(AbstractDialogWrapper<?> dialog, int buttonId,
            Serializable arg) {
    }

    public void onAddButtonClicks(View view) {
        Intent intent = new Intent(this, GeoKretActivity.class);
        intent.putExtra(GeoKretActivity.USER_ID, account.getID());
        startActivityForResult(intent, ADD_GEOKRET);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Intent intent = new Intent(this, GeoKretActivity.class);
        intent.putExtra(GeoKretActivity.USER_ID, account.getID());
        intent.putExtra(InventoryDataSource.COLUMN_ID, id);
        startActivityForResult(intent, EDIT_GEOKRET);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        Bundle ib = data.getExtras();
        long userId = ib.getLong(GeoKretActivity.USER_ID);
        String trackingCode = ib.getString(GeoKretActivity.TRACKING_CODE);
        boolean sticky = ib.getBoolean(GeoKretActivity.STICKY);
        GeoKret gk = new GeoKret(trackingCode, GeoKretDataSource.SYNCHRO_STATE_UNSYNCHRONIZED, null);
        gk.setSticky(sticky);
        List<GeoKret> gkl = new ArrayList<GeoKret>(1);
        gkl.add(gk);
        String oldTrackingCode = ib.getString(GeoKretActivity.TRACKING_CODE_OLD);
        application.getStateHolder().getInventoryDataSource()
                .storeInventory(gkl, userId, false, oldTrackingCode);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
