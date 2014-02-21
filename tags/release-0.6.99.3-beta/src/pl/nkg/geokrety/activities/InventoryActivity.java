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

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.data.GeoKret;
import pl.nkg.geokrety.data.GeoKretDataSource;
import pl.nkg.geokrety.data.InventoryDataSource;
import pl.nkg.geokrety.data.User;
import pl.nkg.geokrety.services.RefreshService;
import pl.nkg.lib.adapters.ExtendedCursorAdapter;
import pl.nkg.lib.dialogs.AbstractDialogWrapper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class InventoryActivity extends AbstractGeoKretyActivity implements
        AdapterView.OnItemSelectedListener, OnItemClickListener {

    private class Adapter extends ExtendedCursorAdapter {

        public Adapter(final Context context, final Cursor c, final boolean autoRequery) {
            super(context, c, true, android.R.layout.simple_list_item_2);
        }

        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            final GeoKret gk = new GeoKret(cursor, 1);
            bindIcon(view, gk);
            bindTextView(view, android.R.id.text1,
                    gk.getFormatedCode() + " (" + gk.getTrackingCode() + ")");
            if (gk.getName() == null) {
                bindTextView(
                        view,
                        android.R.id.text2,
                        gk.getSynchroState() == 0 ? "..." : Utils.defaultIfNull(
                                gk.getSynchroError(), "..."));
            } else {
                bindTextView(view, android.R.id.text2, gk.getName() + " (" + gk.getDist() + "km)");
            }
        }

        private void bindIcon(final View view, final GeoKret gk) {
            if (gk.isSticky()) {
                final TextView tv = (TextView) view.findViewById(android.R.id.text1);
                tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_pine, 0);
            }
        }
    }

    public final static int ADD_GEOKRET = 1;

    public final static int EDIT_GEOKRET = 2;

    private User account;

    private Adapter adapter;

    private final BroadcastReceiver refreshBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final long userId = intent.getLongExtra(InventoryDataSource.COLUMN_USER_ID, -1);
            if (userId == account.getID()) {
                updateListView();
            }
        }
    };

    @Override
    public void dialogFinished(final AbstractDialogWrapper<?> dialog, final int buttonId,
            final Serializable arg) {
    }

    public void onAddButtonClicks(final View view) {
        final Intent intent = new Intent(this, GeoKretActivity.class);
        intent.putExtra(GeoKretActivity.USER_ID, account.getID());
        startActivityForResult(intent, ADD_GEOKRET);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.inventory, menu);
        return true;
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position,
            final long id) {
        final Intent intent = new Intent(this, GeoKretActivity.class);
        intent.putExtra(GeoKretActivity.USER_ID, account.getID());
        intent.putExtra(InventoryDataSource.COLUMN_ID, id);
        startActivityForResult(intent, EDIT_GEOKRET);
    }

    @Override
    public void onItemSelected(final AdapterView<?> arg0, final View arg1, final int arg2,
            final long arg3) {
        account = stateHolder.getAccountList().get(arg2);
        updateListView();
    }

    @Override
    public void onNothingSelected(final AdapterView<?> arg0) {
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_inventory_refresh:
                refreshAccout();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshAccout() {
        application.runRefreshService(true);
    }

    private void updateListView() {
        onRefreshDatabase();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        final Bundle ib = data.getExtras();
        final long userId = ib.getLong(GeoKretActivity.USER_ID);
        final String trackingCode = ib.getString(GeoKretActivity.TRACKING_CODE);
        final boolean sticky = ib.getBoolean(GeoKretActivity.STICKY);
        final GeoKret gk = new GeoKret(trackingCode,
                GeoKretDataSource.SYNCHRO_STATE_UNSYNCHRONIZED, null);
        gk.setSticky(sticky);
        final List<GeoKret> gkl = new ArrayList<GeoKret>(1);
        gkl.add(gk);
        final String oldTrackingCode = ib.getString(GeoKretActivity.TRACKING_CODE_OLD);
        stateHolder.getInventoryDataSource()
                .storeInventory(gkl, userId, false, oldTrackingCode);
        // application.runRefreshService(true);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        turnOnDatabaseUse();

        setContentView(R.layout.activity_inventory);
        final Spinner spin = (Spinner) findViewById(R.id.accountsSpiner);
        spin.setOnItemSelectedListener(this);
        final ArrayAdapter<User> aa = new ArrayAdapter<User>(this,
                android.R.layout.simple_spinner_item, stateHolder.getAccountList());

        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(aa);
        spin.setSelection(stateHolder.getDefaultAccountNr());

        ((ListView) findViewById(R.id.inventoryListView)).setOnItemClickListener(this);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(refreshBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onRefreshDatabase() {
        super.onRefreshDatabase();
        openCursor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stateHolder.getDefaultAccountNr() != AdapterView.INVALID_POSITION) {
            account = stateHolder.getAccountList().get(stateHolder.getDefaultAccountNr());
            updateListView();
        }
        registerReceiver(refreshBroadcastReceiver, new IntentFilter(
                RefreshService.BROADCAST_REFRESH_INVENTORY));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

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
}
