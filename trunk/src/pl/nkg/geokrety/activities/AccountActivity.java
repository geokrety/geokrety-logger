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

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.dialogs.Dialogs;
import pl.nkg.geokrety.dialogs.GKDialog;
import pl.nkg.geokrety.dialogs.OCDialog;
import pl.nkg.geokrety.threads.GettingSecidThread;
import pl.nkg.geokrety.threads.GettingUuidThread;
import pl.nkg.lib.dialogs.AbstractDialogWrapper;
import pl.nkg.lib.dialogs.AlertDialogWrapper;
import pl.nkg.lib.dialogs.GenericProgressDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;
import pl.nkg.lib.location.GPSAcquirer;
import pl.nkg.lib.okapi.SupportedOKAPI;
import pl.nkg.lib.threads.AbstractForegroundTaskWrapper;
import pl.nkg.lib.threads.GenericTaskListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class AccountActivity extends ManagedDialogsActivity implements LocationListener, TextWatcher {

	// TODO: to moze byc razem z klasa Account
	private int								accountID;
	private String							accountName;
	private String							secid;
	private String[]						ocUUIDs		= new String[SupportedOKAPI.SUPPORTED.length];
	private boolean							modified;

	// private TextView accountNameEditText;
	private CheckBox						gkCheckBox;
	private final CheckBox[]				ocCheckBox	= new CheckBox[SupportedOKAPI.SUPPORTED.length];
	private Button							saveButton;
	private EditText						latEditText;
	private EditText						lonEditText;

	private AlertDialogWrapper				saveModifiedsDialog;

	private GKDialog						gkDialog;
	private OCDialog						ocDialog;

	private GenericProgressDialogWrapper	secidProgressDialog;
	private GenericProgressDialogWrapper	uuidProgressDialog;

	private GettingSecidThread				gettingSecidThread;
	private GettingUuidThread				gettingUuidThread;

	private GeoKretyApplication				application;
	private GPSAcquirer						gpsAcquirer;

	@Override
	public void afterTextChanged(final Editable s) {
		modified = true;
	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}

	@Override
	public void dialogFinished(final AbstractDialogWrapper<?> dialog, final int buttonId, final Serializable arg) {

		if (dialog.getDialogId() == Dialogs.SAVE_MODIFIEDSDIALOG) {
			switch (buttonId) {
				case DialogInterface.BUTTON_POSITIVE:
					saveClick(null);
					return;

				case DialogInterface.BUTTON_NEUTRAL:
					return;

				case DialogInterface.BUTTON_NEGATIVE:
					modified = false;
					onBackPressed();
					return;
			}
		}

		updateChecks();
		if (buttonId != DialogInterface.BUTTON_POSITIVE) {
			return;
		}

		if (dialog.getDialogId() == Dialogs.GK_PROMPTDIALOG) {
			gettingSecidThread.execute(new Pair<String, String>(gkDialog.getGKLogin(), gkDialog.getGKPassword()));
		} else if (dialog.getDialogId() == Dialogs.OC_PROMPTDIALOG) {
			final int nr = (Integer) arg;
			uuidProgressDialog.setProgress(getText(R.string.download_getting_uuid) + " " + SupportedOKAPI.SUPPORTED[nr].host + getText(R.string.dots));
			gettingUuidThread.execute(new Pair<String, Integer>(ocDialog.getOCLogin(), nr));
		}
	}

	@Override
	public void onBackPressed() {
		if (modified) {
			saveModifiedsDialog.show(null);
		} else {
			super.onBackPressed();
		}
	}

	public void onClickSetCoordinatesFromGPS(final View view) {
		if (GPSAcquirer.checkAndToast(this)) {
			gpsAcquirer.runRequest(1000, 30);
		}
	}

	@Override
	public void onLocationChanged(final Location location) {
		lonEditText.setText(Utils.latlonFormat.format(location.getLongitude()));
		latEditText.setText(Utils.latlonFormat.format(location.getLatitude()));
		Utils.makeCenterToast(this, R.string.gps_fixed).show();
	}

	@Override
	public void onProviderDisabled(final String provider) {}

	@Override
	public void onProviderEnabled(final String provider) {}

	@Override
	public void onStatusChanged(final String provider, final int status, final Bundle extras) {}

	@Override
	public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {}

	public void saveClick(final View view) {
		if (Utils.isEmpty(Account.SECID)) {
			Toast.makeText(this, R.string.error_login_null, Toast.LENGTH_LONG).show();
			return;
		}

		final Intent returnIntent = new Intent();
		returnIntent.putExtra(Account.SECID, secid);
		returnIntent.putExtra(Account.ACCOUNT_ID, accountID);
		returnIntent.putExtra(Account.OCUUIDS, ocUUIDs);
		returnIntent.putExtra(Account.ACCOUNT_NAME, accountName);
		returnIntent.putExtra(Account.HOME_LAT, latEditText.getText().toString());
		returnIntent.putExtra(Account.HOME_LON, lonEditText.getText().toString());
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	private void updateChecks() {
		gkCheckBox.setChecked(!Utils.isEmpty(secid));
		for (int i = 0; i < ocCheckBox.length; i++) {
			ocCheckBox[i].setChecked(!Utils.isEmpty(ocUUIDs[i]));
		}
		setTitle(getText(R.string.title_activity_account) + ": " + (Utils.isEmpty(accountName) ? getText(R.string.account_account_name_hint) : accountName));
		saveButton.setEnabled(gkCheckBox.isChecked());
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		application = (GeoKretyApplication) getApplication();

		saveModifiedsDialog = new AlertDialogWrapper(this, Dialogs.SAVE_MODIFIEDSDIALOG);
		saveModifiedsDialog.setTitle(R.string.account_save_title);
		saveModifiedsDialog.setMessage(R.string.account_save_message);
		saveModifiedsDialog.setPositiveButton(getText(R.string.yes));
		saveModifiedsDialog.setNegativeButton(getText(R.string.no));
		saveModifiedsDialog.setNeutralButton(getText(android.R.string.cancel));

		gkDialog = new GKDialog(this);
		ocDialog = new OCDialog(this);

		secidProgressDialog = new GenericProgressDialogWrapper(this, Dialogs.SECID_PROGRESSDIALOG);
		secidProgressDialog.setProgress(getText(R.string.download_login_gk).toString());

		uuidProgressDialog = new GenericProgressDialogWrapper(this, Dialogs.UUID_PROMPTDIALOG);
		// uuidProgressDialog.setProgress(getText(R.string.download_getting_ocs));

		gettingSecidThread = GettingSecidThread.getFromHandler(application.getForegroundTaskHandler());
		gettingUuidThread = GettingUuidThread.getFromHandler(application.getForegroundTaskHandler());

		setContentView(R.layout.activity_account);
		latEditText = (EditText) findViewById(R.id.latEditText);
		lonEditText = (EditText) findViewById(R.id.lonEditText);

		accountID = getIntent().getIntExtra(Account.ACCOUNT_ID, AdapterView.INVALID_POSITION);
		secid = getIntent().getStringExtra(Account.SECID);
		ocUUIDs = getIntent().getStringArrayExtra(Account.OCUUIDS);
		accountName = getIntent().getStringExtra(Account.ACCOUNT_NAME);
		lonEditText.setText(Utils.defaultIfNull(getIntent().getStringExtra(Account.HOME_LON), ""));
		latEditText.setText(Utils.defaultIfNull(getIntent().getStringExtra(Account.HOME_LAT), ""));

		if (ocUUIDs == null) {
			ocUUIDs = new String[SupportedOKAPI.SUPPORTED.length];
		}

		// accountNameEditText = (TextView)
		// findViewById(R.id.accountNameTextView);
		gkCheckBox = (CheckBox) findViewById(R.id.gkCheckBox);
		ocCheckBox[0] = (CheckBox) findViewById(R.id.ocCheckBox0);
		ocCheckBox[1] = (CheckBox) findViewById(R.id.ocCheckBox1);
		ocCheckBox[2] = (CheckBox) findViewById(R.id.ocCheckBox2);
		ocCheckBox[3] = (CheckBox) findViewById(R.id.ocCheckBox3);
		ocCheckBox[4] = (CheckBox) findViewById(R.id.ocCheckBox4);
		saveButton = (Button) findViewById(R.id.saveButton);

		// accountNameEditText.setText(getIntent().getStringExtra(Account.ACCOUNT_NAME));
		updateChecks();
		modified = false;
		gpsAcquirer = new GPSAcquirer(this, "gpsAcquirer", this);

		gkCheckBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				if (gkCheckBox.isChecked()) {
					gkDialog.show(null, accountName);
				} else {
					modified = true;
					secid = null;
					saveButton.setEnabled(false);
				}

			}
		});

		for (int i = 0; i < ocCheckBox.length; i++) {
			final int nr = i;
			ocCheckBox[i].setOnClickListener(new OnClickListener() {

				final int	portalNr	= nr;

				@Override
				public void onClick(final View v) {
					if (ocCheckBox[portalNr].isChecked()) {
						ocDialog.show(portalNr, SupportedOKAPI.SUPPORTED[portalNr].host, accountName);
					} else {
						modified = true;
						ocUUIDs[portalNr] = null;
					}
				}
			});
		}

		lonEditText.addTextChangedListener(this);
		latEditText.addTextChangedListener(this);
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		secid = savedInstanceState.getString(Account.SECID);
		accountID = savedInstanceState.getInt(Account.ACCOUNT_ID);
		ocUUIDs = savedInstanceState.getStringArray(Account.OCUUIDS);
		accountName = savedInstanceState.getString(Account.ACCOUNT_NAME);
		gpsAcquirer.restore(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		gpsAcquirer.pause(outState);
		outState.putStringArray(Account.OCUUIDS, ocUUIDs);
		outState.putInt(Account.ACCOUNT_ID, accountID);
		outState.putString(Account.SECID, secid);
		outState.putString(Account.ACCOUNT_NAME, accountName);
	}

	@Override
	protected void onStart() {
		super.onStart();
		gpsAcquirer.start();
		gettingSecidThread.attach(secidProgressDialog, new GenericTaskListener<Pair<String, String>, String, String>(this) {

			@Override
			public void onError(final AbstractForegroundTaskWrapper<Pair<String, String>, String, String> sender, final Pair<String, String> param,
					final Throwable exception) {
				super.onError(sender, param, exception);
				gkDialog.show(null);
			}

			@Override
			public void onFinish(final AbstractForegroundTaskWrapper<Pair<String, String>, String, String> sender, final Pair<String, String> param,
					final String result) {
				modified = true;
				secid = result;
				accountName = param.first;
				updateChecks();
				Toast.makeText(AccountActivity.this, "secid: " + secid, Toast.LENGTH_LONG).show();
			}
		});

		gettingUuidThread.attach(uuidProgressDialog, new GenericTaskListener<Pair<String, Integer>, String, String>(this) {

			@Override
			public void onError(final AbstractForegroundTaskWrapper<Pair<String, Integer>, String, String> sender, final Pair<String, Integer> param,
					final Throwable exception) {
				super.onError(sender, param, exception);
				ocDialog.show(param.second);
			}

			@Override
			public void onFinish(final AbstractForegroundTaskWrapper<Pair<String, Integer>, String, String> sender, final Pair<String, Integer> param,
					final String result) {
				modified = true;
				ocUUIDs[param.second] = result;
				if (Utils.isEmpty(accountName)) {
					accountName = param.first;
				}
				updateChecks();
				Toast.makeText(AccountActivity.this, SupportedOKAPI.SUPPORTED[param.second].host + " uuid: " + result, Toast.LENGTH_LONG).show();
			}

		});

	}
}
