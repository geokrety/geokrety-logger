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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.activities.listeners.RefreshListener;
import pl.nkg.geokrety.data.User;
import pl.nkg.geokrety.data.GeoKretLog;
import pl.nkg.geokrety.data.GeoKretLogDataSource;
import pl.nkg.geokrety.data.Geocache;
import pl.nkg.geokrety.data.GeocacheLog;
import pl.nkg.geokrety.data.GeoKret;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.dialogs.Dialogs;
import pl.nkg.geokrety.dialogs.RemoveLogDialog;
import pl.nkg.geokrety.services.LogSubmitterService;
import pl.nkg.geokrety.services.VerifyGeoKretService;
import pl.nkg.geokrety.services.WaypointResolverService;
import pl.nkg.lib.dialogs.AbstractDialogWrapper;
import pl.nkg.lib.dialogs.AlertDialogWrapper;
import pl.nkg.lib.dialogs.DatePickerDialogWrapper;
import pl.nkg.lib.dialogs.GenericProgressDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;
import pl.nkg.lib.dialogs.TimePickerDialogWrapper;
import pl.nkg.lib.location.GPSAcquirer;
import pl.nkg.lib.threads.AbstractForegroundTaskWrapper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class LogActivity extends AbstractGeoKretyActivity implements LocationListener, TextWatcher {

    //private GenericProgressDialogWrapper refreshProgressDialog;
    // private GenericProgressDialogWrapper logProgressDialog;
    private RemoveLogDialog removeLogDialog;
    private TimePickerDialogWrapper timePickerDialog;
    private DatePickerDialogWrapper datePickerDialog;
    private AlertDialogWrapper inventorySpinnerDialog;
    private AlertDialogWrapper ocsSpinnerDialog;
    private AlertDialogWrapper logTypeSpinnerDialog;

    //private RefreshAccount refreshAccount;

    private GeoKretLog currentLog;
    private User currentAccount;
    private int currentLogType = 0;

    private Button logTypeButton;
    private Button accountsButton;
    private EditText trackingCodeEditText;
    private Button ocsButton;
    private ImageButton gpsButton;
    private Button datePicker;
    private Button timePicker;
    private EditText waypointEditText;
    private EditText coordinatesEditText;
    private EditText commentEditText;
    
    private TextView tcNotfyTextView;
    private TextView wptNotfyTextView;

    private GPSAcquirer gpsAcquirer;

    public void checkDate(final View view) {
        try {
            showDatePickerDialog();
        } catch (final Exception e) {
            currentLog.setDateAndTime(new Date());
            showDatePickerDialog();
        }
    }

    public void checkTime(final View view) {
        timePickerDialog.show(null, currentLog.getGodzina(), currentLog.getMinuta(), true);
    }

    @Override
    public void dialogFinished(final AbstractDialogWrapper<?> dialog, final int buttonId,
            final Serializable arg) {
        switch (dialog.getDialogId()) {
            case Dialogs.TIME_PICKERDIALOG:
                storeToGeoKretLog(currentLog);
                currentLog.setGodzina(timePickerDialog.getHourOfDay());
                currentLog.setMinuta(timePickerDialog.getMinute());
                loadFromGeoKretLog(currentLog);
                break;

            case Dialogs.DATE_PICKERDIALOG:
                storeToGeoKretLog(currentLog);
                currentLog.setDate(datePickerDialog.getYear(),
                        datePickerDialog.getMonthOfYear() + 1, datePickerDialog.getDayOfMonth());
                loadFromGeoKretLog(currentLog);
                break;

            case Dialogs.INVENTORY_SPINNERDIALOG:
                final GeoKret kret = (GeoKret) inventorySpinnerDialog.getAdapter()
                        .getItem(buttonId);
                trackingCodeEditText.setText(kret.getTrackingCode());
                break;

            case Dialogs.OCS_SPINNERDIALOG:
                final GeocacheLog log = (GeocacheLog) ocsSpinnerDialog.getAdapter().getItem(
                        buttonId);
                waypointEditText.setText(log.getCacheCode());

                final Geocache gc = log.getGeoCache();
                if (gc != null) {
                    coordinatesEditText.setText(gc.getLocation().replace("|", " "));
                }

                storeToGeoKretLog(currentLog);
                currentLog.setDateAndTime(log.getDate());
                loadFromGeoKretLog(currentLog);
                break;

            case Dialogs.TYPE_SPINNERDIALOG:
                currentLogType = buttonId;
                updateVisibles();
                break;

            case Dialogs.REMOVE_LOG_ALERTDIALOG:
                if (buttonId == DialogInterface.BUTTON_POSITIVE) {
                    if (currentLog.getId() > 0) {
                        stateHolder.getGeoKretLogDataSource().delete(currentLog.getId());
                    }
                    finish();
                }
                break;
        }
    }

    public void onClickDelete(final View view) {
        removeLogDialog.show(null);
    }

    public void onClickDraft(final View view) {
        saveLog(GeoKretLog.STATE_DRAFT);
        Toast.makeText(this, R.string.message_draft_saved, Toast.LENGTH_LONG).show();
        finish();
    }

    public void onClickSetCoordinatesFromGPS(final View view) {
        if (GPSAcquirer.checkAndToast(this)) {
            gpsAcquirer.runRequest(1000, 30);
        }
    }

    public void onClickSetHomeCoordinates(final View view) {
        coordinatesEditText.setText(currentAccount.getHomeCordLat() + " "
                + currentAccount.getHomeCordLon());
    }

    public void onClickSubmit(final View view) {
        saveLog(GeoKretLog.STATE_OUTBOX);
        startService(new Intent(this, LogSubmitterService.class));
        Toast.makeText(this, R.string.message_do_submitting, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onLocationChanged(final Location location) {
        coordinatesEditText.setText(//
                Utils.latlonFormat.format(location.getLatitude()) + ' '
                        + Utils.latlonFormat.format(location.getLongitude()));
        Utils.makeCenterToast(this, R.string.gps_fixed).show();
    }

    @Override
    public void onProviderDisabled(final String provider) {
    }

    @Override
    public void onProviderEnabled(final String provider) {
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {
    }

    public void refreshButtonClick(final View view) {
        application.runRefreshService(true);
        /*if (currentAccount != null) {
            currentAccount.loadData(application, true);
        }*/
    }

    public void showAccountsActivity(final View view) {
        startActivityForResult(new Intent(this, AccountsActivity.class), 0);
    }

    public void showInventory(final View view) {
        if (updateSpinners()) {
            inventorySpinnerDialog.show(null, inventoryAdapter.indexOf(trackingCodeEditText.getText().toString()));
        }
    }

    public void showLogType(final View view) {
        logTypeSpinnerDialog.show(null, currentLogType);
    }

    public void showOcs(final View view) {
        if (updateSpinners()) {
            ocsSpinnerDialog.show(null,
                    lastLogsAdapter.indexOf(waypointEditText.getText().toString()));
        }
    }

    private boolean canShowUserData() {
        if (currentAccount == null) {
            Toast.makeText(this, R.string.error_current_null, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    
    private InventoryAdapter inventoryAdapter;
    private class InventoryAdapter extends ArrayAdapter<GeoKret> {

        public InventoryAdapter() {
            super(LogActivity.this, android.R.layout.simple_list_item_single_choice, stateHolder.getInventoryDataSource().loadInventory(currentAccount.getID()));
        }
        
        public int indexOf(String trackingCode) {
            for (int i = 0; i < getCount(); i++) {
                if (trackingCode.toUpperCase(Locale.ENGLISH).contains(getItem(i).getTrackingCode().toUpperCase(Locale.ENGLISH))) {
                    return i;
                }
            }
            return -1;
        }
    }
    
    private LastLogsAdapter lastLogsAdapter;
    private class LastLogsAdapter extends ArrayAdapter<GeocacheLog> {

        public LastLogsAdapter() {
            super(LogActivity.this, android.R.layout.simple_list_item_single_choice, stateHolder.getGeocacheLogDataSource().loadLastLogs(currentAccount.getID()));
        }
        
        public int indexOf(String waypoint) {
            for (int i = 0; i < getCount(); i++) {
                if (waypoint.toUpperCase(Locale.ENGLISH).contains(getItem(i).getCacheCode().toUpperCase(Locale.ENGLISH))) {
                    return i;
                }
            }
            return -1;
        }
    }
    
    @Override
    protected void onRefreshDatabase() {
        super.onRefreshDatabase();
        configAdapters();
    }

    private void configAdapters() {
        inventoryAdapter = new InventoryAdapter();
        inventorySpinnerDialog.setAdapter(inventoryAdapter);

        lastLogsAdapter = new LastLogsAdapter();
        ocsSpinnerDialog.setAdapter(lastLogsAdapter);
    }

    private void loadFromGeoKretLog(final GeoKretLog log) {
        currentAccount = stateHolder.getAccountByID(log.getAccoundID());
        currentLogType = log.getLogTypeMapped();
        // accountsButton.setText(application.getStateHolder().getAccountByID(log.getAccoundID()).getName());
        accountsButton.setText(currentAccount == null ? getText(R.string.form_profile)
                : currentAccount.getName());
        trackingCodeEditText.setText(log.getNr());
        datePicker.setText(log.getData());
        timePicker.setText(log.getFormatedTime());
        waypointEditText.setText(log.getWpt());
        coordinatesEditText.setText(log.getLatlon());
        commentEditText.setText(log.getComment());
        updateVisibles();
    }

    private void saveLog(final int state) {
        storeToGeoKretLog(currentLog);
        currentLog.setState(state);
        if (currentLog.getId() == 0) {
            stateHolder.getGeoKretLogDataSource().persist(currentLog);
        } else {
            stateHolder.getGeoKretLogDataSource().merge(currentLog);
        }
    }

    private void showDatePickerDialog() {
        final String[] date = currentLog.getData().split("-");
        final int y = Integer.parseInt(date[0]);
        final int m = Integer.parseInt(date[1]) - 1;
        final int d = Integer.parseInt(date[2]);
        datePickerDialog.show(null, y, m, d);
    }

    private void storeToGeoKretLog(final GeoKretLog log) {
        log.setNr(trackingCodeEditText.getText().toString());
        log.setWpt(waypointEditText.getText().toString());
        log.setLatlon(coordinatesEditText.getText().toString());
        log.setComment(commentEditText.getText().toString());
        log.setLogTypeMapped(currentLogType);
        log.setAccoundID(currentAccount == null ? 0 : currentAccount.getID());
    }

    private boolean updateSpinners() {
        if (!canShowUserData()) {
            return false;
        }

        /*if (currentAccount.loadIfExpired(application, false)) {
            return false;
        }*/

        configAdapters();
        return true;
    }

    private void updateVisibles() {
        logTypeButton.setText(getResources().getStringArray(R.array.log_array)[currentLogType]);
        final boolean locationVisible = !GeoKretLog.checkIgnoreLocation(currentLogType);
        waypointEditText.setEnabled(locationVisible);
        coordinatesEditText.setEnabled(locationVisible);
        gpsButton.setEnabled(locationVisible);
        ocsButton.setEnabled(locationVisible);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        currentAccount = stateHolder.getDefaultAccount();
        storeToGeoKretLog(currentLog);
        loadFromGeoKretLog(currentLog);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        final Intent intent = getIntent();

        //refreshProgressDialog = new GenericProgressDialogWrapper(this,
        //        Dialogs.REFRESH_ACCOUNT_PROGRESSDIALOG);
        // logProgressDialog = new GenericProgressDialogWrapper(this,
        // Dialogs.LOG_PROGRESSDIALOG);
        removeLogDialog = new RemoveLogDialog(this);
        timePickerDialog = new TimePickerDialogWrapper(this, Dialogs.TIME_PICKERDIALOG);
        datePickerDialog = new DatePickerDialogWrapper(this, Dialogs.DATE_PICKERDIALOG);

        inventorySpinnerDialog = new AlertDialogWrapper(this, Dialogs.INVENTORY_SPINNERDIALOG);
        inventorySpinnerDialog.setTitle(R.string.title_dialog_inventory);

        ocsSpinnerDialog = new AlertDialogWrapper(this, Dialogs.OCS_SPINNERDIALOG);
        ocsSpinnerDialog.setTitle(R.string.title_dialog_ocs);

        logTypeSpinnerDialog = new AlertDialogWrapper(this, Dialogs.TYPE_SPINNERDIALOG);
        
        tcNotfyTextView = (TextView) findViewById(R.id.tcNotfyTextView);
        wptNotfyTextView = (TextView) findViewById(R.id.wptNotfyTextView);

        // logProgressDialog.setTitle(R.string.submit_title);

        //refreshAccount = RefreshAccount.getFromHandler(application.getForegroundTaskHandler());

        final long logID = intent.getLongExtra(GeoKretLogDataSource.COLUMN_ID,
                AdapterView.INVALID_ROW_ID);
        if (logID != AdapterView.INVALID_ROW_ID) {
            currentLog = stateHolder.getGeoKretLogDataSource().loadByID(logID);
        }

        
        if (currentLog == null) {
            currentLog = new GeoKretLog(savedInstanceState);
        } else {
            currentAccount = stateHolder.getAccountByID(currentLog.getAccoundID());
            if (currentLog.getState() == GeoKretLog.STATE_PROBLEM) {
                TextView errorTextView = (TextView)findViewById(R.id.errorTextView);
                errorTextView.setText(getText(currentLog.getProblem()) + " " + currentLog.getProblemArg());
                errorTextView.setVisibility(View.VISIBLE);
            }
        }


        logTypeButton = (Button) findViewById(R.id.logTypeButton);
        accountsButton = (Button) findViewById(R.id.accountsButton);
        trackingCodeEditText = (EditText) findViewById(R.id.trackingCodeEditText);
        ocsButton = (Button) findViewById(R.id.ocsButton);
        gpsButton = (ImageButton) findViewById(R.id.gpsButton);
        datePicker = (Button) findViewById(R.id.datePicker);
        timePicker = (Button) findViewById(R.id.timePicker);
        waypointEditText = (EditText) findViewById(R.id.waypointEditText);
        coordinatesEditText = (EditText) findViewById(R.id.coordinatesEditText);
        commentEditText = (EditText) findViewById(R.id.commentEditText);

        gpsAcquirer = new GPSAcquirer(this, "gpsAcquirer", this);
        
        trackingCodeEditText.addTextChangedListener(this);
        trackingCodeEditText.setFilters(new InputFilter[] {
                GeoKretActivity.TRACKING_CODE_FILTER, new InputFilter.LengthFilter(6)
        });
        
        waypointEditText.addTextChangedListener(this);
        trackingCodeEditText.setFilters(new InputFilter[] {
                GeoKretActivity.WAYPOINT_FILTER
        });

        final String action = intent.getAction();

        if (!Utils.isEmpty(action) && action.equals(Intent.ACTION_VIEW)) {
            final Uri data = intent.getData();
            try {
                if (data.getHost().equals("geokrety.org")) {
                    currentLog.setNr(data.getQueryParameter("nr"));
                    currentLog.setWpt(data.getQueryParameter("wpt"));

                    final String date = data.getQueryParameter("data");
                    final String godzina = data.getQueryParameter("godzina");
                    final String minuta = data.getQueryParameter("minuta");
                    final String username = data.getQueryParameter("username");
                    String rrUsername = data.getQueryParameter("net.rygielski.roadrunner:profile_name");

                    if (!Utils.isEmpty(date)) {
                        currentLog.setData(date);
                    }

                    if (!Utils.isEmpty(godzina)) {
                        currentLog.setGodzina(Integer.parseInt(godzina));
                    }

                    if (!Utils.isEmpty(minuta)) {
                        currentLog.setMinuta(Integer.parseInt(minuta));
                    }

                    if (!Utils.isEmpty(username)) {
                        currentAccount = stateHolder.matchAccount(username);
                    } else if (!Utils.isEmpty(rrUsername)) {
                        currentAccount = stateHolder.matchAccount(rrUsername);
                    }
                } else if (data.getHost().equals("coord.info")) {
                    final String path = data.getPath();
                    currentLog.setWpt(path.subSequence(1, path.length()).toString());
                } else {
                    currentLog.setWpt(data.getQueryParameter("wp"));
                }
            } catch (final Throwable e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        unregisterReceiver(verifierBroadcastReceiver);
        unregisterReceiver(resolverBroadcastReceiver);
        storeToGeoKretLog(currentLog);
        super.onPause();
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentLog = new GeoKretLog(savedInstanceState);
        loadFromGeoKretLog(currentLog);
        gpsAcquirer.restore(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        gpsAcquirer.pause(outState);
        super.onSaveInstanceState(outState);
        storeToGeoKretLog(currentLog);
        currentLog.storeToBundle(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (stateHolder.getAccountList().size() == 0) {
            Toast.makeText(this, R.string.main_message_error_no_account_configured, Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
            return;
        }

        /*refreshAccount.attach(refreshProgressDialog, new RefreshListener(this) {
            @Override
            public void onFinish(final AbstractForegroundTaskWrapper<User, String, String> sender,
                    final User param, final String result) {
                super.onFinish(sender, param, result);
                configAdapters();
            }
        });*/

        // updateCurrentAccount(false, false);
        if (currentAccount == null) {
            currentAccount = stateHolder.getDefaultAccount();
        }
        
        if (currentAccount != null) {
            currentLog.setAccoundID(currentAccount.getID());
        }

        logTypeSpinnerDialog.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, getResources().getStringArray(
                        R.array.log_array)));

        updateSpinners();
        loadFromGeoKretLog(currentLog);
        logTypeSpinnerDialog.setCheckedItem(currentLogType);
        gpsAcquirer.start();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void afterTextChanged(Editable s) {
        GeoKretActivity.validate(trackingCodeEditText, tcNotfyTextView);
        GeoKretActivity.resolve(waypointEditText, wptNotfyTextView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(verifierBroadcastReceiver,
                new IntentFilter(VerifyGeoKretService.BROADCAST));
        registerReceiver(resolverBroadcastReceiver,
                new IntentFilter(WaypointResolverService.BROADCAST));
        GeoKretActivity.validate(trackingCodeEditText, tcNotfyTextView);
        GeoKretActivity.resolve(waypointEditText, wptNotfyTextView);
    }
    
 // TODO: make a my NotifyTextView control
    private final BroadcastReceiver verifierBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (trackingCodeEditText
                    .getText()
                    .toString()
                    .equals(intent.getExtras().getString(VerifyGeoKretService.INTENT_TRACKING_CODE))) {
                int type = intent
                        .getExtras().getInt(VerifyGeoKretService.INTENT_MESSAGE_TYPE);
                GeoKretActivity.setLabel(tcNotfyTextView,
                        intent.getExtras().getString(VerifyGeoKretService.INTENT_MESSAGE), type);
                //saveButton.setEnabled(type != ERROR);  // TODO: is need?
            }
        }
    };
    
    private final BroadcastReceiver resolverBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (waypointEditText
                    .getText()
                    .toString()
                    .equals(intent.getExtras().getString(WaypointResolverService.INTENT_WAYPOINT))) {
                Bundle bundle = intent.getExtras();
                int type = bundle.getInt(WaypointResolverService.INTENT_MESSAGE_TYPE);
                GeoKretActivity.setLabel(wptNotfyTextView, bundle.getString(WaypointResolverService.INTENT_MESSAGE), type);

                coordinatesEditText.setEnabled(type != GeoKretActivity.GOOD);
                // TODO: gps and home buttons too
                
                if (type == GeoKretActivity.GOOD) {
                    coordinatesEditText.setText(bundle.getString(WaypointResolverService.INTENT_LATLON));
                }
            }
        }
    };
}
