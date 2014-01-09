/*
 * Copyright (C) 2013 Michał Niedźwiecki
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
import java.util.Date;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.activities.listeners.RefreshListener;
import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.data.GeoKretLog;
import pl.nkg.geokrety.data.Geocache;
import pl.nkg.geokrety.data.GeocacheLog;
import pl.nkg.geokrety.data.Geokret;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.dialogs.Dialogs;
import pl.nkg.geokrety.services.LogSubmitterService;
import pl.nkg.geokrety.threads.LogGeoKret;
import pl.nkg.geokrety.threads.RefreshAccount;
import pl.nkg.lib.dialogs.AbstractDialogWrapper;
import pl.nkg.lib.dialogs.AlertDialogWrapper;
import pl.nkg.lib.dialogs.DatePickerDialogWrapper;
import pl.nkg.lib.dialogs.GenericProgressDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;
import pl.nkg.lib.dialogs.TimePickerDialogWrapper;
import pl.nkg.lib.location.GPSAcquirer;
import pl.nkg.lib.threads.AbstractForegroundTaskWrapper;
import pl.nkg.lib.threads.GenericTaskListener;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class LogActivity extends ManagedDialogsActivity implements
		LocationListener {

	// private static final String LOGTYPE = "logtype";

	private GenericProgressDialogWrapper refreshProgressDialog;
	private GenericProgressDialogWrapper logProgressDialog;
	private TimePickerDialogWrapper timePickerDialog;
	private DatePickerDialogWrapper datePickerDialog;
	private AlertDialogWrapper inventorySpinnerDialog;
	private AlertDialogWrapper ocsSpinnerDialog;
	private AlertDialogWrapper logTypeSpinnerDialog;

	private GeoKretyApplication application;
	private RefreshAccount refreshAccount;
	private LogGeoKret logGeoKret;

	private GeoKretLog currentLog;
	private Account currentAccount;
	private int currentLogType = 0;

	// private Spinner logTypeSpinner;
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

	private GPSAcquirer gpsAcquirer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		refreshProgressDialog = new GenericProgressDialogWrapper(this,
				Dialogs.REFRESH_ACCOUNT_PROGRESSDIALOG);
		logProgressDialog = new GenericProgressDialogWrapper(this,
				Dialogs.LOG_PROGRESSDIALOG);
		timePickerDialog = new TimePickerDialogWrapper(this,
				Dialogs.TIME_PICKERDIALOG);
		datePickerDialog = new DatePickerDialogWrapper(this,
				Dialogs.DATE_PICKERDIALOG);

		inventorySpinnerDialog = new AlertDialogWrapper(this,
				Dialogs.INVENTORY_SPINNERDIALOG);
		inventorySpinnerDialog.setTitle(R.string.title_dialog_inventory);

		ocsSpinnerDialog = new AlertDialogWrapper(this,
				Dialogs.OCS_SPINNERDIALOG);
		ocsSpinnerDialog.setTitle(R.string.title_dialog_ocs);

		logTypeSpinnerDialog = new AlertDialogWrapper(this,
				Dialogs.TYPE_SPINNERDIALOG);

		logProgressDialog.setTitle(R.string.submit_title);

		application = (GeoKretyApplication) getApplication();
		refreshAccount = RefreshAccount.getFromHandler(application
				.getForegroundTaskHandler());
		logGeoKret = LogGeoKret.getFromHandler(application
				.getForegroundTaskHandler());

		currentLog = new GeoKretLog(savedInstanceState);

		setContentView(R.layout.activity_log);

		// logTypeSpinner = (Spinner) findViewById(R.id.logTypeSpinner);
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
		// logTypeSpinner.setOnItemSelectedListener(this);

		// currentLogType = savedInstanceState.getInt(LOGTYPE, 0);
		gpsAcquirer = new GPSAcquirer(this, "gpsAcquirer", this);

		Intent intent = getIntent();
		String action = intent.getAction();
		if (!Utils.isEmpty(action) && action.equals(Intent.ACTION_VIEW)) {
			Uri data = intent.getData();
			try {
				if (data.getHost().equals("geokrety.org")) {
					currentLog.setNr(data.getQueryParameter("nr"));
					currentLog.setWpt(data.getQueryParameter("wpt"));

					String date = data.getQueryParameter("data");
					String godzina = data.getQueryParameter("godzina");
					String minuta = data.getQueryParameter("minuta");

					if (!Utils.isEmpty(date)) {
						currentLog.setData(date);
					}

					if (!Utils.isEmpty(godzina)) {
						currentLog.setGodzina(Integer.parseInt(godzina));
					}

					if (!Utils.isEmpty(minuta)) {
						currentLog.setMinuta(Integer.parseInt(minuta));
					}
				} else if (data.getHost().equals("coord.info")) {
					String path = data.getPath();
					currentLog.setWpt(path.subSequence(1, path.length())
							.toString());
				} else {
					currentLog.setWpt(data.getQueryParameter("wp"));
				}
			} catch (Throwable e) {
				Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG)
						.show();
				finish();
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (application.getStateHolder().getAccountList().size() == 0) {
			Toast.makeText(this, R.string.no_account_configured,
					Toast.LENGTH_LONG).show();
			startActivity(new Intent(this, MainActivity.class));
			return;
		}

		refreshAccount.attach(refreshProgressDialog, new RefreshListener(this) {
			@Override
			public void onFinish(
					AbstractForegroundTaskWrapper<Account, String, String> sender,
					Account param, String result) {
				super.onFinish(sender, param, result);
				configAdapters();
			}
		});

		logGeoKret
				.attach(logProgressDialog,
						new GenericTaskListener<Pair<GeoKretLog, Account>, String, Boolean>(
								this) {
							public void onFinish(
									pl.nkg.lib.threads.AbstractForegroundTaskWrapper<android.util.Pair<GeoKretLog, Account>, String, Boolean> sender,
									android.util.Pair<GeoKretLog, Account> param,
									Boolean result) {
								super.onFinish(sender, param, result);
								// reset(null);
								finish();
							};
						}.setFinishMessage(R.string.submit_finish)
								.setBreakMessage(R.string.submit_broken));
		updateCurrentAccount(false, false);

		logTypeSpinnerDialog.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice, getResources()
						.getStringArray(R.array.log_array)));

		updateSpinners();
		loadFromGeoKretLog(currentLog);
		logTypeSpinnerDialog.setCheckedItem(currentLogType);
		gpsAcquirer.start();
	}

	@Override
	protected void onStop() {
		refreshAccount.detach();
		super.onStop();
	}

	private void updateCurrentAccount(boolean always, boolean force) {
		StateHolder holder = ((GeoKretyApplication) getApplication())
				.getStateHolder();
		int currentAccountNr = holder.getDefaultAccount();
		if (currentAccountNr != ListView.INVALID_POSITION) {
			currentAccount = holder.getAccountList().get(currentAccountNr);
			//currentLog.setGeoKretyLogin(currentAccount.getName());
			currentLog.setAccoundID(currentAccount.getID());
			if (always) {
				currentAccount.loadData(application, force);
			} else {
				currentAccount.loadIfExpired(application, force);
			}
		}
	}

	@Override
	protected void onPause() {
		storeToGeoKretLog(currentLog);
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		storeToGeoKretLog(currentLog);
		updateCurrentAccount(false, false);
		loadFromGeoKretLog(currentLog);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		gpsAcquirer.pause(outState);
		super.onSaveInstanceState(outState);
		storeToGeoKretLog(currentLog);
		currentLog.storeToBundle(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		currentLog = new GeoKretLog(savedInstanceState);
		loadFromGeoKretLog(currentLog);
		gpsAcquirer.restore(savedInstanceState);
	}

	private void loadFromGeoKretLog(GeoKretLog log) {
		currentLogType = (log.getLogTypeMapped());
		accountsButton.setText(application.getStateHolder().getAccountByID(log.getAccoundID()).getName());
		trackingCodeEditText.setText(log.getNr());
		datePicker.setText(log.getData());
		timePicker.setText(log.getFormatedTime());
		waypointEditText.setText(log.getWpt());
		coordinatesEditText.setText(log.getLatlon());
		commentEditText.setText(log.getComment());
		updateVisibles();
	}

	private void storeToGeoKretLog(GeoKretLog log) {
		log.setNr(trackingCodeEditText.getText().toString());
		log.setWpt(waypointEditText.getText().toString());
		log.setLatlon(coordinatesEditText.getText().toString());
		log.setComment(commentEditText.getText().toString());
		log.setLogTypeMapped(currentLogType);
	}

	private boolean updateSpinners() {
		if (!canShowUserData()) {
			return false;
		}

		if (currentAccount.loadIfExpired(application, false)) {
			return false;
		}

		configAdapters();
		return true;
	}

	private void configAdapters() {
		inventorySpinnerDialog.setAdapter(new ArrayAdapter<Geokret>(this,
				android.R.layout.simple_list_item_single_choice, currentAccount
						.getInventory()));

		ocsSpinnerDialog.setAdapter(new ArrayAdapter<GeocacheLog>(this,
				android.R.layout.simple_list_item_single_choice, currentAccount
						.getOpenCachingLogs()));
	}

	public void showLogType(View view) {
		logTypeSpinnerDialog.show(null, currentLogType);
	}

	public void showInventory(View view) {
		if (updateSpinners()) {
			inventorySpinnerDialog.show(null, currentAccount
					.getTrackingCodeIndex(trackingCodeEditText.getText()
							.toString()));
		}
	}

	private boolean canShowUserData() {
		if (currentAccount == null) {
			Toast.makeText(this, R.string.error_current_null,
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	public void showOcs(View view) {
		if (updateSpinners()) {
			ocsSpinnerDialog.show(null, currentAccount
					.getWaypointIndex(waypointEditText.getText().toString()));
		}
	}

	public void showAccountsActivity(View view) {
		startActivityForResult(new Intent(this, AccountsActivity.class), 0);
	}

	public void onClickSetCoordinatesFromGPS(View view) {
		if (GPSAcquirer.checkAndToast(this)) {
			gpsAcquirer.runRequest(1000, 30);
		}
	}

	public void onClickSetHomeCoordinates(View view) {
		coordinatesEditText.setText(currentAccount.getHomeCordLat() + " "
				+ currentAccount.getHomeCordLon());
	}

	public void checkDate(View view) {
		try {
			showDatePickerDialog();
		} catch (Exception e) {
			currentLog.setDateAndTime(new Date());
			showDatePickerDialog();
		}
	}

	private void showDatePickerDialog() {
		String[] date = currentLog.getData().split("-");
		int y = Integer.parseInt(date[0]);
		int m = Integer.parseInt(date[1]) - 1;
		int d = Integer.parseInt(date[2]);
		datePickerDialog.show(null, y, m, d);
	}

	public void checkTime(View view) {
		timePickerDialog.show(null, currentLog.getGodzina(),
				currentLog.getMinuta(), true);
	}

	public void submit(final View view) {
		storeToGeoKretLog(currentLog);
		/*application.getForegroundTaskHandler()
				.runTask(
						LogGeoKret.ID,
						new Pair<GeoKretLog, Account>(currentLog,
								currentAccount), true);*/
		currentLog.setState(GeoKretLog.STATE_OUTBOX);
		if (currentLog.getId() == 0) {
			application.getStateHolder().getGeoKretLogDataSource().persist(currentLog);
		} else {
			application.getStateHolder().getGeoKretLogDataSource().merge(currentLog);
		}
		startService(new Intent(this, LogSubmitterService.class));
		Toast.makeText(this, "Log submitting in background...", Toast.LENGTH_LONG).show();
		finish();
	}

	public void refreshButtonClick(final View view) {
		updateCurrentAccount(true, true);
	}

	public void reset(View view) {
		currentLog = new GeoKretLog();
		updateCurrentAccount(false, false);
		loadFromGeoKretLog(currentLog);
	}

	private void updateVisibles() {
		logTypeButton
				.setText(getResources().getStringArray(R.array.log_array)[currentLogType]);
		boolean locationVisible = !GeoKretLog
				.checkIgnoreLocation(currentLogType);
		waypointEditText.setEnabled(locationVisible);
		coordinatesEditText.setEnabled(locationVisible);
		gpsButton.setEnabled(locationVisible);
		ocsButton.setEnabled(locationVisible);
	}

	@Override
	public void dialogFinished(AbstractDialogWrapper<?> dialog, int buttonId,
			Serializable arg) {
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
					datePickerDialog.getMonthOfYear() + 1,
					datePickerDialog.getDayOfMonth());
			loadFromGeoKretLog(currentLog);
			break;

		case Dialogs.INVENTORY_SPINNERDIALOG:
			Geokret kret = (Geokret) inventorySpinnerDialog.getAdapter()
					.getItem(buttonId);
			trackingCodeEditText.setText(kret.getTackingCode());
			break;

		case Dialogs.OCS_SPINNERDIALOG:
			GeocacheLog log = (GeocacheLog) ocsSpinnerDialog.getAdapter()
					.getItem(buttonId);
			waypointEditText.setText(log.getCacheCode());

			Geocache gc = log.getGeoCache();
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
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		coordinatesEditText.setText(//
				Utils.latlonFormat.format(location.getLatitude()) + ' '
						+ Utils.latlonFormat.format(location.getLongitude()));
		Utils.makeCenterToast(this, R.string.gps_fixed).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}
}
