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
import java.util.Locale;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.activities.controls.NotifyTextView;
import pl.nkg.geokrety.activities.controls.TrackingCodeEditText;
import pl.nkg.geokrety.activities.controls.WaypointEditText;
import pl.nkg.geokrety.activities.listeners.VerifyResponseListener;
import pl.nkg.geokrety.data.InventoryDataSource;
import pl.nkg.geokrety.data.User;
import pl.nkg.geokrety.data.GeoKretLog;
import pl.nkg.geokrety.data.GeoKretLogDataSource;
import pl.nkg.geokrety.data.Geocache;
import pl.nkg.geokrety.data.GeocacheLog;
import pl.nkg.geokrety.data.GeoKret;
import pl.nkg.geokrety.dialogs.Dialogs;
import pl.nkg.geokrety.dialogs.RemoveLogDialog;
import pl.nkg.geokrety.services.LogSubmitterService;
import pl.nkg.geokrety.services.RefreshService;
import pl.nkg.lib.dialogs.AbstractDialogWrapper;
import pl.nkg.lib.dialogs.AlertDialogWrapper;
import pl.nkg.lib.dialogs.DatePickerDialogWrapper;
import pl.nkg.lib.dialogs.TimePickerDialogWrapper;
import pl.nkg.lib.gcapi.GeocachingProvider;
import pl.nkg.lib.location.GPSAcquirer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class LogActivity extends AbstractGeoKretyActivity implements LocationListener, VerifyResponseListener {

    private RemoveLogDialog removeLogDialog;
    private TimePickerDialogWrapper timePickerDialog;
    private DatePickerDialogWrapper datePickerDialog;
    private AlertDialogWrapper inventorySpinnerDialog;
    private AlertDialogWrapper ocsSpinnerDialog;
    private AlertDialogWrapper logTypeSpinnerDialog;
    private AlertDialogWrapper userSpinnerDialog;

    private GeoKretLog currentLog;
    private User currentAccount;
    private int currentLogType = -1;
    private boolean savedLog = false;

    private Button logTypeButton;
    private Button accountsButton;
    private TrackingCodeEditText trackingCodeEditText;
    private Button ocsButton;
    private ImageButton gpsButton;
    private Button datePicker;
    private Button timePicker;
    private WaypointEditText waypointEditText;
    private EditText coordinatesEditText;
    private EditText commentEditText;
    
    private NotifyTextView tcNotifyTextView;
    private NotifyTextView wptNotifyTextView;

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
                    if (!Utils.isEmpty(gc.getLocation())) {
                        coordinatesEditText.setText(gc.getLocation().replace("|", " "));
                    }
                }

                storeToGeoKretLog(currentLog);
                if (log.getPortal() == GeocachingProvider.PORTAL) {
                    currentLog.setDate(log.getDate());
                } else {
                    currentLog.setDateAndTime(log.getDate());
                }
                loadFromGeoKretLog(currentLog);
                break;
                
            case Dialogs.USER_SPINNERDIALOG:
                currentAccount = (User) userSpinnerDialog.getAdapter().getItem(buttonId);
                storeToGeoKretLog(currentLog);
                loadFromGeoKretLog(currentLog);
                configAdapters();
                break;

            case Dialogs.TYPE_SPINNERDIALOG:
                currentLogType = buttonId - 1;
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
        if (canShowUserData()) {
            saveLog(GeoKretLog.STATE_DRAFT);
            Toast.makeText(this, R.string.submit_message_draft_saved, Toast.LENGTH_LONG).show();
            finish();
        }
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
        if (currentLogType == -1) {
            Toast.makeText(this, R.string.validation_error_no_log_type, Toast.LENGTH_LONG).show();
        } else if (!canShowUserData()) {
        } else if (Utils.isEmpty(trackingCodeEditText.getText().toString())) {
            Toast.makeText(this, R.string.validation_error_no_traking_code, Toast.LENGTH_LONG).show();
        } else if (coordinatesEditText.isEnabled() && Utils.isEmpty(coordinatesEditText.getText().toString())) {
            Toast.makeText(this, R.string.validation_error_no_location, Toast.LENGTH_LONG).show();
        } else {
            saveLog(GeoKretLog.STATE_OUTBOX);
            startService(new Intent(this, LogSubmitterService.class));
            Toast.makeText(this, R.string.submit_message_do_submitting, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        coordinatesEditText.setText(//
                Utils.latlonFormat.format(location.getLatitude()) + ' '
                        + Utils.latlonFormat.format(location.getLongitude()));
        Utils.makeCenterToast(this, R.string.gps_message_fixed).show();
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
    }

    public void showAccountsActivity(final View view) {
        //startActivityForResult(new Intent(this, AccountsActivity.class), 0);
        userSpinnerDialog.show(null, currentAccount == null ? -1 : usersAdapter.indexOf(currentAccount.getID()));
    }

    public void showInventory(final View view) {
        if (updateSpinners()) {
            inventorySpinnerDialog.show(null, inventoryAdapter.indexOf(trackingCodeEditText.getText().toString()));
        }
    }

    public void showLogType(final View view) {
        logTypeSpinnerDialog.show(null, currentLogType + 1);
    }

    public void showOcs(final View view) {
        if (updateSpinners()) {
            ocsSpinnerDialog.show(null,
                    lastLogsAdapter.indexOf(waypointEditText.getText().toString()));
        }
    }
    
    private boolean canShowUserData() {
        if (currentAccount == null) {
            Toast.makeText(this, R.string.error_no_profile_selected, Toast.LENGTH_SHORT).show();
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
    
    private UsersAdapter usersAdapter;
    private class UsersAdapter extends ArrayAdapter<User> {

        public UsersAdapter() {
            super(LogActivity.this, android.R.layout.simple_list_item_single_choice, stateHolder.getUserDataSource().getAll());
        }
        
        public int indexOf(long id) {
            for (int i = 0; i < getCount(); i++) {
                if (id == getItem(i).getID()) {
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
        if (canShowUserData()) {
            inventoryAdapter = new InventoryAdapter();
            inventorySpinnerDialog.setAdapter(inventoryAdapter);
    
            lastLogsAdapter = new LastLogsAdapter();
            ocsSpinnerDialog.setAdapter(lastLogsAdapter);
        }
    }

    private void loadFromGeoKretLog(final GeoKretLog log) {
        currentAccount = stateHolder.getAccountByID(log.getAccoundID());
        currentLogType = log.getLogTypeMapped();
        accountsButton.setText(currentAccount == null ? getText(R.string.log_button_profile)
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
        savedLog = true;
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

        configAdapters();
        return true;
    }

    private void updateVisibles() {
        logTypeButton.setText(getResources().getStringArray(R.array.log_array)[currentLogType + 1]);
        final boolean locationVisible = !GeoKretLog.checkIgnoreLocation(currentLogType);
        waypointEditText.setEnabled(locationVisible);
        coordinatesEditText.setEnabled(locationVisible);
        gpsButton.setEnabled(locationVisible);
        ocsButton.setEnabled(locationVisible);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        savedLog = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        final Intent intent = getIntent();

        removeLogDialog = new RemoveLogDialog(this);
        timePickerDialog = new TimePickerDialogWrapper(this, Dialogs.TIME_PICKERDIALOG);
        datePickerDialog = new DatePickerDialogWrapper(this, Dialogs.DATE_PICKERDIALOG);

        inventorySpinnerDialog = new AlertDialogWrapper(this, Dialogs.INVENTORY_SPINNERDIALOG);
        inventorySpinnerDialog.setTitle(R.string.log_inventory_title);

        ocsSpinnerDialog = new AlertDialogWrapper(this, Dialogs.OCS_SPINNERDIALOG);
        ocsSpinnerDialog.setTitle(R.string.log_lastlogs_title);
        
        userSpinnerDialog = new AlertDialogWrapper(this, Dialogs.USER_SPINNERDIALOG);
        usersAdapter = new UsersAdapter();
        userSpinnerDialog.setAdapter(usersAdapter);

        logTypeSpinnerDialog = new AlertDialogWrapper(this, Dialogs.TYPE_SPINNERDIALOG);
        
        tcNotifyTextView = (NotifyTextView) findViewById(R.id.tcNotfiyTextView);
        wptNotifyTextView = (NotifyTextView) findViewById(R.id.wptNotifyTextView);

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
                errorTextView.setText(currentLog.formatProblem(this));
                errorTextView.setVisibility(View.VISIBLE);
            }
        }


        logTypeButton = (Button) findViewById(R.id.logTypeButton);
        accountsButton = (Button) findViewById(R.id.accountsButton);
        trackingCodeEditText = (TrackingCodeEditText) findViewById(R.id.trackingCodeEditText);
        ocsButton = (Button) findViewById(R.id.ocsButton);
        gpsButton = (ImageButton) findViewById(R.id.gpsButton);
        datePicker = (Button) findViewById(R.id.datePicker);
        timePicker = (Button) findViewById(R.id.timePicker);
        waypointEditText = (WaypointEditText) findViewById(R.id.waypointEditText);
        coordinatesEditText = (EditText) findViewById(R.id.coordinatesEditText);
        commentEditText = (EditText) findViewById(R.id.commentEditText);

        gpsAcquirer = new GPSAcquirer(this, "gpsAcquirer", this);
        
        trackingCodeEditText.bindWithNotifyTextView(tcNotifyTextView);
        
        waypointEditText.bindWithNotifyTextView(wptNotifyTextView);
        waypointEditText.setVerifyResponseListener(this);

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
                Toast.makeText(this, Utils.formatException(e), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        unregisterReceiver(refreshBroadcastReceiver);
        trackingCodeEditText.unregisterReceiver();
        waypointEditText.unregisterReceiver();
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
        if (currentAccount != null && !savedLog && !isEmpty()) {
            saveLog(GeoKretLog.STATE_DRAFT);
        }
        currentLog.storeToBundle(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (stateHolder.getAccountList().size() == 0) {
            Toast.makeText(this, R.string.main_error_no_account_configured, Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
            return;
        }

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
        logTypeSpinnerDialog.setCheckedItem(currentLogType + 1);
        gpsAcquirer.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        trackingCodeEditText.registerReceiver();
        waypointEditText.registerReceiver();
        registerReceiver(refreshBroadcastReceiver, new IntentFilter(
                RefreshService.BROADCAST_REFRESH_INVENTORY));
    }
    
    @Override
    public void onChangeValue() {
        coordinatesEditText.setEnabled(true);
    }

    @Override
    public void onVerifyResponse(CharSequence response, boolean valid) {
        coordinatesEditText.setEnabled(!valid);
        if (valid) {
            coordinatesEditText.setText(response);
        }
    }
    
    private final BroadcastReceiver refreshBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final long userId = intent.getLongExtra(InventoryDataSource.COLUMN_USER_ID, -1);
            if (currentAccount != null && userId == currentAccount.getID()) {
                configAdapters();
            }
        }
    };
    
    @Override
    public void onBackPressed() {
        if (!isEmpty()) {
            saveLog(GeoKretLog.STATE_DRAFT);
            Toast.makeText(this, R.string.submit_message_draft_saved, Toast.LENGTH_LONG).show();
        }
        super.onBackPressed();
    };
    
    private boolean isEmpty() {
        return Utils.isEmpty(trackingCodeEditText.getText().toString()) && Utils.isEmpty(coordinatesEditText.getText().toString()) && Utils.isEmpty(waypointEditText.getText().toString()) && Utils.isEmpty(commentEditText.getText().toString());
    }
}
