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
import java.util.regex.Pattern;

import pl.nkg.geokrety.GeoKretyApplication;
//import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.data.GeoKret;
import pl.nkg.geokrety.data.InventoryDataSource;
import pl.nkg.geokrety.dialogs.Dialogs;
import pl.nkg.geokrety.services.VerifyGeoKretService;
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

    private EditText trackingCodeEditText;
    private CheckBox stickyCheckBox;
    private Button saveButton;
    private TextView notfyTextView;

    private static final Pattern CAPS_LETTERS_AND_DIGITS_PATTERN = Pattern.compile("[A-Z0-9]*");
    private static final Pattern TRACKING_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{6}$");
    private static final InputFilter TRACKING_CODE_FILTER = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                int dstart,
                int dend) {
            if (CAPS_LETTERS_AND_DIGITS_PATTERN.matcher(source).matches()) {
                return null;
            }
            return "";
        }
    };

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

        trackingCodeEditText = (EditText) findViewById(R.id.trackingCodeEditText);
        saveButton = (Button) findViewById(R.id.saveButton);
        trackingCodeEditText.setFilters(new InputFilter[] {
                TRACKING_CODE_FILTER, new InputFilter.LengthFilter(6)
        });
        // nameEditText = (EditText) findViewById(R.id.nameEditText);
        stickyCheckBox = (CheckBox) findViewById(R.id.stickyCheckBox);
        notfyTextView = (TextView) findViewById(R.id.notfyTextView);

        Bundle ib = getIntent().getExtras();
        userId = ib.getLong(USER_ID);
        long id = ib.getLong(InventoryDataSource.COLUMN_ID);

        if (id > 0) {
            GeoKret geoKret = application.getStateHolder().getInventoryDataSource().loadByID(id);
            oldTrackingCode = geoKret.getTrackingCode();
            trackingCodeEditText.setText(geoKret.getTrackingCode());
            // nameEditText.setText(ib.getString(NAME));
            stickyCheckBox.setChecked(geoKret.isSticky());
        } else {
            stickyCheckBox.setChecked(true);
            //saveButton.setEnabled(false); // TODO: is need?
        }

        // nameEditText.addTextChangedListener(this);
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
        // returnIntent.putExtra(NAME, nameEditText.getText().toString());
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
        validate();
    }

    private void validate() {
        if (TRACKING_CODE_PATTERN.matcher(trackingCodeEditText.getText().toString()).matches()) {
            notfyTextView.setText("");
            setLabel(notfyTextView, getText(R.string.verify_tc_message_info_waiting), INFO);
            runVerifyService();
        } else {
            //saveButton.setEnabled(false);  // TODO: is need?
            setLabel(notfyTextView, getText(R.string.geokret_message_error_invalid_trackingcode),
                    ERROR);
        }
    }

    private final BroadcastReceiver verifierBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (trackingCodeEditText
                    .getText()
                    .toString()
                    .equals(intent.getExtras().getString(VerifyGeoKretService.INTENT_TRACKING_CODE))) {
                int type = intent
                        .getExtras().getInt(VerifyGeoKretService.INTENT_MESSAGE_TYPE);
                setLabel(notfyTextView,
                        intent.getExtras().getString(VerifyGeoKretService.INTENT_MESSAGE), type);
                //saveButton.setEnabled(type != ERROR);  // TODO: is need?
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(verifierBroadcastReceiver,
                new IntentFilter(VerifyGeoKretService.BROADCAST));
        validate();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(verifierBroadcastReceiver);
        super.onPause();
    }

    private void runVerifyService() {
        Intent intent = new Intent(this, VerifyGeoKretService.class);
        intent.putExtra(VerifyGeoKretService.INTENT_TRACKING_CODE, trackingCodeEditText.getText()
                .toString());
        startService(intent);
    }

    // TODO: make a my NotifyTextView control
    public static final int GOOD = 0;
    public static final int INFO = 1;
    public static final int WARNING = 2;
    public static final int ERROR = 3;
    private static final int[] COLORS = new int[] {
            R.color.valid_color, R.color.info_color, R.color.warning_color, R.color.error_color
    };

    private static void setLabel(TextView textView, CharSequence content, int color) {
        textView.setTextColor(textView.getResources().getColor(COLORS[color]));
        textView.setText(content);
    }
}
