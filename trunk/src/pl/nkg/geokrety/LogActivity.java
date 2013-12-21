package pl.nkg.geokrety;

import java.io.Serializable;

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
import pl.nkg.lib.dialogs.GenericProgressDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;
import pl.nkg.lib.threads.GenericTaskListener;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Pair;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class LogActivity extends ManagedDialogsActivity implements
		OnItemSelectedListener {

	private GenericProgressDialogWrapper refreshProgressDialog;
	private GenericProgressDialogWrapper logProgressDialog;
	private GeoKretyApplication application;
	private RefreshAccount refreshAccount;
	private LogGeoKret logGeoKret;

	private GeoKretLog currentLog;
	private Account currentAccount;

	private Spinner logTypeSpinner;
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

		logProgressDialog.setTitle(R.string.submit_title);

		application = (GeoKretyApplication) getApplication();
		refreshAccount = RefreshAccount.getFromHandler(application
				.getForegroundTaskHandler());
		logGeoKret = LogGeoKret.getFromHandler(application
				.getForegroundTaskHandler());

		currentLog = new GeoKretLog(savedInstanceState);


		setContentView(R.layout.activity_log);

		logTypeSpinner = (Spinner) findViewById(R.id.logTypeSpinner);
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
		logTypeSpinner.setOnItemSelectedListener(this);

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
						}.setFinishMessage(R.string.submit_finish).setBreakMessage(R.string.submit_broken));
		updateCurrentAccount();
		loadFromGeoKretLog(currentLog);
	}

	@Override
	protected void onStop() {
		refreshAccount.detach();
		super.onStop();
	}

	private void updateCurrentAccount() {
		StateHolder holder = StateHolder.getInstance(this);
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
		logTypeSpinner.setSelection(log.getLogTypeMapped());
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
		log.setLogTypeMapped(logTypeSpinner.getSelectedItemPosition());
	}

	public void showInventory(View view) {
		if (!canShowUserData()) {
			return;
		}

		if (currentAccount.loadIfExpired(application)) {
			return;
		}

		final ArrayAdapter<Geokret> adapter = new ArrayAdapter<Geokret>(this,
				android.R.layout.simple_spinner_dropdown_item,
				currentAccount.getInventory());

		new AlertDialog.Builder(this).setTitle(R.string.title_dialog_inventory)
				.setAdapter(adapter, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						trackingCodeEditText.setText(adapter.getItem(which)
								.getTackingCode());

						dialog.dismiss();
					}
				}).create().show();
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
		if (!canShowUserData()) {
			return;
		}

		if (currentAccount.loadIfExpired(application)) {
			return;
		}

		final ArrayAdapter<GeocacheLog> adapter = new ArrayAdapter<GeocacheLog>(
				this, android.R.layout.simple_spinner_dropdown_item,
				currentAccount.getOpenCachingLogs());

		new AlertDialog.Builder(this).setTitle(R.string.title_dialog_ocs)
				.setAdapter(adapter, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						GeocacheLog log = adapter.getItem(which);
						waypointEditText.setText(log.getCacheCode());
						storeToGeoKretLog(currentLog);
						currentLog.setDateAndTime(log.getDate());
						loadFromGeoKretLog(currentLog);

						dialog.dismiss();
					}
				}).create().show();
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
			new DatePickerDialog(this,
					new DatePickerDialog.OnDateSetListener() {

						@Override
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							currentLog.setDate(year, monthOfYear + 1,
									dayOfMonth);
							loadFromGeoKretLog(currentLog);
						}
					}, y, m, d).show();
		} catch (Exception e) {
			System.out.print(e);
		}
	}

	public void checkTime(View view) {
		new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				currentLog.setGodzina(hourOfDay);
				currentLog.setMinuta(minute);
				loadFromGeoKretLog(currentLog);
			}
		}, currentLog.getGodzina(), currentLog.getMinuta(), true).show();
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

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		updateVisibles();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	private void updateVisibles() {
		int s = logTypeSpinner.getSelectedItemPosition();
		boolean locationVisible = !GeoKretLog.checkIgnoreLocation(s);
		waypointEditText.setEnabled(locationVisible);
		// coordinatesEditText.setEnabled(locationVisible);
		// gpsButton.setEnabled(locationVisible);
		ocsButton.setEnabled(locationVisible);
	}

	@Override
	public void dialogFinished(AbstractDialogWrapper<?> dialog, int buttonId,
			Serializable arg) {
		// TODO Auto-generated method stub

	}
}
