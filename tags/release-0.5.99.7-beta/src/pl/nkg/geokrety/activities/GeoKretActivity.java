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

import pl.nkg.geokrety.GeoKretyApplication;
//import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.activities.controls.NotifyTextView;
import pl.nkg.geokrety.activities.controls.TrackingCodeEditText;
import pl.nkg.geokrety.activities.filters.TrackingCodeInputFilter;
import pl.nkg.geokrety.activities.filters.WaypointInputFilter;
import pl.nkg.geokrety.data.GeoKret;
import pl.nkg.geokrety.data.InventoryDataSource;
import pl.nkg.geokrety.dialogs.Dialogs;
import pl.nkg.geokrety.services.VerifyGeoKretService;
import pl.nkg.geokrety.services.WaypointResolverService;
import pl.nkg.lib.dialogs.AbstractDialogWrapper;
import pl.nkg.lib.dialogs.AlertDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class GeoKretActivity extends ManagedDialogsActivity implements TextWatcher,
        OnCheckedChangeListener {

    public static final String USER_ID = "user_id";
    public static final String TRACKING_CODE = "tracking_code";
    public static final String TRACKING_CODE_OLD = "tracking_code_old";
    public static final String NAME = "name";
    public static final String STICKY = "sticky";

    private GeoKretyApplication application;
    private AlertDialogWrapper saveModifiedsDialog;
    private boolean modified;

    private long userId;
    private String oldTrackingCode = "";

    private TrackingCodeEditText trackingCodeEditText;
    private CheckBox stickyCheckBox;
    //private Button saveButton;
    private NotifyTextView notifyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_kret);

        application = (GeoKretyApplication) getApplication();

        saveModifiedsDialog = new AlertDialogWrapper(this,
                Dialogs.SAVE_MODIFIEDSDIALOG);
        saveModifiedsDialog.setTitle(R.string.geokret_save_title);
        saveModifiedsDialog.setMessage(R.string.geokret_save_message);
        saveModifiedsDialog.setPositiveButton(getText(R.string.yes));
        saveModifiedsDialog.setNegativeButton(getText(R.string.no));
        saveModifiedsDialog.setNeutralButton(getText(android.R.string.cancel));

        trackingCodeEditText = (TrackingCodeEditText) findViewById(R.id.trackingCodeEditText);
        //saveButton = (Button) findViewById(R.id.saveButton);
        
        stickyCheckBox = (CheckBox) findViewById(R.id.stickyCheckBox);
        notifyTextView = (NotifyTextView) findViewById(R.id.notifyTextView);

        Bundle ib = getIntent().getExtras();
        userId = ib.getLong(USER_ID);
        long id = ib.getLong(InventoryDataSource.COLUMN_ID);

        if (id > 0) {
            GeoKret geoKret = application.getStateHolder().getInventoryDataSource().loadByID(id);
            oldTrackingCode = geoKret.getTrackingCode();
            trackingCodeEditText.setText(geoKret.getTrackingCode());
            stickyCheckBox.setChecked(geoKret.isSticky());
        } else {
            stickyCheckBox.setChecked(true);
            //saveButton.setEnabled(false); // TODO: is need?
        }

        trackingCodeEditText.bindWithNotifyTextView(notifyTextView);
        trackingCodeEditText.addTextChangedListener(this);
        stickyCheckBox.setOnCheckedChangeListener(this);
        modified = false;
    }

    @Override
    public void dialogFinished(AbstractDialogWrapper<?> dialog, int buttonId,
            Serializable arg) {
        if (dialog.getDialogId() == Dialogs.SAVE_MODIFIEDSDIALOG) {
            switch (buttonId) {
                case Dialog.BUTTON_POSITIVE:
                    saveClick(null);
                    return;

                case Dialog.BUTTON_NEUTRAL:
                    return;

                case Dialog.BUTTON_NEGATIVE:
                    modified = false;
                    onBackPressed();
                    return;
            }
        }
    }

    public void saveClick(View view) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(USER_ID, userId);
        returnIntent.putExtra(TRACKING_CODE, trackingCodeEditText.getText()
                .toString());
        returnIntent.putExtra(STICKY, stickyCheckBox.isChecked());
        returnIntent.putExtra(TRACKING_CODE_OLD, oldTrackingCode);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(USER_ID, userId);
        outState.putString(TRACKING_CODE_OLD, oldTrackingCode);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        userId = savedInstanceState.getLong(USER_ID);
        oldTrackingCode = savedInstanceState.getString(TRACKING_CODE_OLD);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (modified) {
            saveModifiedsDialog.show(null);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        modified = true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        modified = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        trackingCodeEditText.registerReceiver();
    }

    @Override
    protected void onPause() {
        trackingCodeEditText.unregisterReceiver();
        super.onPause();
    }
}
