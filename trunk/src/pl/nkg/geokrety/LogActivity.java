package pl.nkg.geokrety;

import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.data.GeoKretLog;
import pl.nkg.geokrety.data.GeocacheLog;
import pl.nkg.geokrety.data.Geokret;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.exceptions.MessagedException;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class LogActivity extends Activity {

	private GeoKretLog currentLog;
	private Account currentAccount;

	private Spinner logTypeSpinner;
	private Button accountsButton;
	private EditText trackingCodeEditText;
	private Button datePicker;
	private Button timePicker;
	private EditText waypointEditText;
	private EditText coordinatesEditText;
	private EditText commentEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentLog = new GeoKretLog();

		updateCurrentAccount();

		setContentView(R.layout.activity_log);

		logTypeSpinner = (Spinner) findViewById(R.id.logTypeSpinner);
		accountsButton = (Button) findViewById(R.id.accountsButton);
		trackingCodeEditText = (EditText) findViewById(R.id.trackingCodeEditText);
		datePicker = (Button) findViewById(R.id.datePicker);
		timePicker = (Button) findViewById(R.id.timePicker);
		waypointEditText = (EditText) findViewById(R.id.waypointEditText);
		coordinatesEditText = (EditText) findViewById(R.id.coordinatesEditText);
		commentEditText = (EditText) findViewById(R.id.commentEditText);
		loadFromGeoKretLog(currentLog);
	}

	private void updateCurrentAccount() {
		StateHolder holder = StateHolder.getInstance(this);
		int currentAccountNr = holder.getDefaultAccount();
		if (currentAccountNr != ListView.INVALID_POSITION) {
			currentAccount = holder.getAccountList().get(currentAccountNr);
			currentLog.setGeoKretyLogin(currentAccount.getGeoKretyLogin());
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

	private void loadFromGeoKretLog(GeoKretLog log) {
		logTypeSpinner.setSelection(log.getLogTypeMapped());
		accountsButton.setText(log.getGeoKretyLogin());
		trackingCodeEditText.setText(log.getNr());
		datePicker.setText(log.getData());
		timePicker.setText(log.getFormatedTime());
		waypointEditText.setText(log.getWpt());
		coordinatesEditText.setText(log.getLatlon());
		commentEditText.setText(log.getComment());
	}

	private void storeToGeoKretLog(GeoKretLog log) {
		log.setNr(trackingCodeEditText.getText().toString());
		log.setWpt(waypointEditText.getText().toString());
		log.setLatlon(coordinatesEditText.getText().toString());
		log.setComment(commentEditText.getText().toString());
		log.setLogTypeMapped(logTypeSpinner.getSelectedItemPosition());
	}

	public void showInventory(View view) {
		if (!canShowUserData()) {
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
		try {
			currentAccount.loadIfExpired();
			return true;
		} catch (MessagedException e) {
			Toast.makeText(this, e.getFormatedMessage(this), Toast.LENGTH_SHORT)
					.show();
		} catch (Exception e) {
			Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT)
					.show();
		}

		return false;
	}

	public void showOcs(View view) {
		if (!canShowUserData()) {
			return;
		}

		final ArrayAdapter<GeocacheLog> adapter = new ArrayAdapter<GeocacheLog>(
				this, android.R.layout.simple_spinner_dropdown_item,
				currentAccount.getOpenCachingLogs());

		new AlertDialog.Builder(this).setTitle(R.string.title_dialog_inventory)
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
		new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				currentLog.setDate(year, monthOfYear + 1, dayOfMonth);
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

	public void submit(View view) {
		try {
			storeToGeoKretLog(currentLog);
			String ret = currentLog.submit(currentAccount);
			reset(view);
			Toast.makeText(this, ret, Toast.LENGTH_SHORT).show();
		} catch (MessagedException e) {
			Toast.makeText(this, e.getFormatedMessage(this), Toast.LENGTH_SHORT)
					.show();
		} catch (Exception e) {
			Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void reset(View view) {
		currentLog = new GeoKretLog();
		updateCurrentAccount();
		loadFromGeoKretLog(currentLog);
	}

}
