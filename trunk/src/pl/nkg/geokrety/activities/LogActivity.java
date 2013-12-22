package pl.nkg.geokrety.activities;

import java.io.Serializable;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.activities.listeners.RefreshListener;
import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.data.GeoKretLog;
import pl.nkg.geokrety.data.GeocacheLog;
import pl.nkg.geokrety.data.Geokret;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.dialogs.Dialogs;
import pl.nkg.geokrety.threads.LogGeoKret;
import pl.nkg.geokrety.threads.RefreshAccount;
import pl.nkg.lib.dialogs.AbstractDialogWrapper;
import pl.nkg.lib.dialogs.AlertDialogWrapper;
import pl.nkg.lib.dialogs.DatePickerDialogWrapper;
import pl.nkg.lib.dialogs.GenericProgressDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;
import pl.nkg.lib.dialogs.TimePickerDialogWrapper;
import pl.nkg.lib.threads.GenericTaskListener;
import android.os.Bundle;
import android.content.Intent;
import android.util.Pair;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class LogActivity extends ManagedDialogsActivity {

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
	// private Button gpsButton;
	private Button datePicker;
	private Button timePicker;
	private EditText waypointEditText;
	// private EditText coordinatesEditText;
	private EditText commentEditText;

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
		// gpsButton = (Button) findViewById(R.id.gpsButton);
		datePicker = (Button) findViewById(R.id.datePicker);
		timePicker = (Button) findViewById(R.id.timePicker);
		waypointEditText = (EditText) findViewById(R.id.waypointEditText);
		// coordinatesEditText = (EditText)
		// findViewById(R.id.coordinatesEditText);
		commentEditText = (EditText) findViewById(R.id.commentEditText);
		// logTypeSpinner.setOnItemSelectedListener(this);

		// currentLogType = savedInstanceState.getInt(LOGTYPE, 0);
	}

	@Override
	protected void onStart() {
		super.onStart();
		refreshAccount.attach(refreshProgressDialog, new RefreshListener(this));
		logGeoKret
				.attach(logProgressDialog,
						new GenericTaskListener<Pair<GeoKretLog, Account>, String, Boolean>(
								this) {
							public void onFinish(
									pl.nkg.lib.threads.AbstractForegroundTaskWrapper<android.util.Pair<GeoKretLog, Account>, String, Boolean> sender,
									android.util.Pair<GeoKretLog, Account> param,
									Boolean result) {
								super.onFinish(sender, param, result);
								reset(null);
							};
						}.setFinishMessage(R.string.submit_finish)
								.setBreakMessage(R.string.submit_broken));
		updateCurrentAccount();

		logTypeSpinnerDialog.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, getResources()
						.getStringArray(R.array.log_array)));

		configAdapters();
		loadFromGeoKretLog(currentLog);
		logTypeSpinnerDialog.setCheckedItem(currentLogType);
	}

	@Override
	protected void onStop() {
		refreshAccount.detach();
		super.onStop();
	}

	private void updateCurrentAccount() {
		StateHolder holder = ((GeoKretyApplication) getApplication())
				.getStateHolder();
		int currentAccountNr = holder.getDefaultAccount();
		if (currentAccountNr != ListView.INVALID_POSITION) {
			currentAccount = holder.getAccountList().get(currentAccountNr);
			currentLog.setGeoKretyLogin(currentAccount.getGeoKretyLogin());
			currentAccount.loadIfExpired(application);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.log, menu);
		return true;
	}

	@Override
	protected void onPause() {
		storeToGeoKretLog(currentLog);
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		storeToGeoKretLog(currentLog);
		updateCurrentAccount();
		loadFromGeoKretLog(currentLog);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		storeToGeoKretLog(currentLog);
		currentLog.storeToBundle(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		currentLog = new GeoKretLog(savedInstanceState);
		loadFromGeoKretLog(currentLog);
	}

	private void loadFromGeoKretLog(GeoKretLog log) {
		currentLogType = (log.getLogTypeMapped());
		accountsButton.setText(log.getGeoKretyLogin());
		trackingCodeEditText.setText(log.getNr());
		datePicker.setText(log.getData());
		timePicker.setText(log.getFormatedTime());
		waypointEditText.setText(log.getWpt());
		// coordinatesEditText.setText(log.getLatlon());
		commentEditText.setText(log.getComment());
		updateVisibles();
	}

	private void storeToGeoKretLog(GeoKretLog log) {
		log.setNr(trackingCodeEditText.getText().toString());
		log.setWpt(waypointEditText.getText().toString());
		// log.setLatlon(coordinatesEditText.getText().toString());
		log.setComment(commentEditText.getText().toString());
		log.setLogTypeMapped(currentLogType);
	}

	private boolean configAdapters() {
		if (!canShowUserData()) {
			return false;
		}

		if (currentAccount.loadIfExpired(application)) {
			return false;
		}

		inventorySpinnerDialog.setAdapter(new ArrayAdapter<Geokret>(this,
				android.R.layout.simple_spinner_dropdown_item, currentAccount
						.getInventory()));

		ocsSpinnerDialog.setAdapter(new ArrayAdapter<GeocacheLog>(this,
				android.R.layout.simple_spinner_dropdown_item, currentAccount
						.getOpenCachingLogs()));
		return true;
	}

	public void showLogType(View view) {
		logTypeSpinnerDialog.show(null, currentLogType);
	}

	public void showInventory(View view) {
		if (configAdapters()) {
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
		if (configAdapters()) {
			ocsSpinnerDialog.show(null, currentAccount
					.getWaypointIndex(waypointEditText.getText().toString()));
		}
	}

	public void showAccountsActivity(View view) {
		startActivityForResult(new Intent(this, AccountsActivity.class), 0);
	}

	public void setCoordinatesFromGPS(View view) {
		Toast.makeText(this, R.string.not_implemented_yet, Toast.LENGTH_SHORT)
				.show();
	}

	public void checkDate(View view) {
		String[] date = currentLog.getData().split("-");
		int y = Integer.parseInt(date[0]);
		int m = Integer.parseInt(date[1]) - 1;
		int d = Integer.parseInt(date[2]);
		try {
			datePickerDialog.show(null, y, m, d);
		} catch (Exception e) {
			System.out.print(e);
		}
	}

	public void checkTime(View view) {
		timePickerDialog.show(null, currentLog.getGodzina(),
				currentLog.getMinuta(), true);
	}

	public void submit(final View view) {
		storeToGeoKretLog(currentLog);
		currentLog.submit(application, currentAccount);
	}

	public void reset(View view) {
		currentLog = new GeoKretLog();
		updateCurrentAccount();
		loadFromGeoKretLog(currentLog);
	}

	private void updateVisibles() {
		logTypeButton
				.setText(getResources().getStringArray(R.array.log_array)[currentLogType]);
		boolean locationVisible = !GeoKretLog
				.checkIgnoreLocation(currentLogType);
		waypointEditText.setEnabled(locationVisible);
		// coordinatesEditText.setEnabled(locationVisible);
		// gpsButton.setEnabled(locationVisible);
		ocsButton.setEnabled(locationVisible);
	}

	@Override
	public void dialogFinished(AbstractDialogWrapper<?> dialog, int buttonId,
			Serializable arg) {
		switch (dialog.getDialogId()) {
		case Dialogs.TIME_PICKERDIALOG:
			currentLog.setGodzina(timePickerDialog.getHourOfDay());
			currentLog.setMinuta(timePickerDialog.getMinute());
			loadFromGeoKretLog(currentLog);
			break;

		case Dialogs.DATE_PICKERDIALOG:
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
}
