/*
 * Copyright (C) 2013, 2014 Michał Niedźwiecki
 * 
 * This file is part of GeoKrety Logger
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

//import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.dialogs.Dialogs;
import pl.nkg.lib.dialogs.AbstractDialogWrapper;
import pl.nkg.lib.dialogs.AlertDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class GeoKretActivity extends ManagedDialogsActivity implements TextWatcher, OnCheckedChangeListener {

	public static final String USER_ID = "user_id";
	public static final String TRACKING_CODE = "tracking_code";
	public static final String TRACKING_CODE_OLD = "tracking_code_old";
	public static final String NAME = "name";
	public static final String STICKY = "sticky";

	//private GeoKretyApplication application;
	private AlertDialogWrapper saveModifiedsDialog;
	private boolean modified;

	private int userId;
	private String oldTrackingCode;

	private EditText trackingCodeEditText;
	private EditText nameEditText;
	private CheckBox stickyCheckBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geo_kret);

		//application = (GeoKretyApplication) getApplication();

		saveModifiedsDialog = new AlertDialogWrapper(this,
				Dialogs.SAVE_MODIFIEDSDIALOG);
		saveModifiedsDialog.setTitle(R.string.account_save_title);
		saveModifiedsDialog.setMessage(R.string.account_save_message);
		saveModifiedsDialog.setPositiveButton(getText(R.string.yes));
		saveModifiedsDialog.setNegativeButton(getText(R.string.no));
		saveModifiedsDialog.setNeutralButton(getText(android.R.string.cancel));

		trackingCodeEditText = (EditText) findViewById(R.id.trackingCodeEditText);
		nameEditText = (EditText) findViewById(R.id.nameEditText);
		stickyCheckBox = (CheckBox) findViewById(R.id.stickyCheckBox);

		Bundle ib = getIntent().getExtras();

		userId = ib.getInt(USER_ID);
		oldTrackingCode = ib.getString(TRACKING_CODE);
		trackingCodeEditText.setText(ib.getString(TRACKING_CODE));
		nameEditText.setText(ib.getString(NAME));
		stickyCheckBox.setChecked(ib.getBoolean(STICKY, true));
		
		nameEditText.addTextChangedListener(this);
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
		returnIntent.putExtra(NAME, nameEditText.getText().toString());
		returnIntent.putExtra(STICKY, stickyCheckBox.isChecked());
		returnIntent.putExtra(TRACKING_CODE_OLD, oldTrackingCode);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(USER_ID, userId);
		outState.putString(TRACKING_CODE_OLD, oldTrackingCode);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		userId = savedInstanceState.getInt(USER_ID);
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
}
