package pl.nkg.geokrety.activities;

import java.io.Serializable;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.activities.listeners.RefreshListener;
import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.data.GeocacheLog;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.dialogs.Dialogs;
import pl.nkg.geokrety.threads.RefreshAccount;
import pl.nkg.lib.dialogs.AbstractDialogWrapper;
import pl.nkg.lib.dialogs.GenericProgressDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;
import pl.nkg.lib.threads.AbstractForegroundTaskWrapper;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

public class LastOCsActivity extends ManagedDialogsActivity implements
		AdapterView.OnItemSelectedListener {

	private Account account;
	private GenericProgressDialogWrapper refreshProgressDialog;
	private GeoKretyApplication application;
	private RefreshAccount refreshAccount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		refreshProgressDialog = new GenericProgressDialogWrapper(this,
				Dialogs.REFRESH_ACCOUNT_PROGRESSDIALOG);

		application = (GeoKretyApplication) getApplication();
		refreshAccount = RefreshAccount.getFromHandler(application
				.getForegroundTaskHandler());

		StateHolder holder = StateHolder.getInstance(this);
		setContentView(R.layout.activity_last_ocs);
		Spinner spin = (Spinner) findViewById(R.id.accountsSpiner);
		spin.setOnItemSelectedListener(this);
		ArrayAdapter<Account> aa = new ArrayAdapter<Account>(this,
				android.R.layout.simple_spinner_item, holder.getAccountList());

		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin.setAdapter(aa);
		spin.setSelection(holder.getDefaultAccount());

	}

	@Override
	protected void onStart() {
		super.onStart();
		StateHolder holder = StateHolder.getInstance(this);
		refreshAccount.attach(refreshProgressDialog, new RefreshListener(this) {
			@Override
			public void onFinish(
					AbstractForegroundTaskWrapper<Account, String, Boolean> sender,
					Account param, Boolean result) {
				super.onFinish(sender, param, result);
				refreshListView();
			}
		});
		if (holder.getDefaultAccount() != ListView.INVALID_POSITION) {
			account = holder.getAccountList().get(holder.getDefaultAccount());
			updateListView();
		}
	}

	private void refreshListView() {
		ArrayAdapter<GeocacheLog> adapter = new ArrayAdapter<GeocacheLog>(this,
				android.R.layout.simple_list_item_1,
				account.getOpenCachingLogs());
		ListView listView = (ListView) findViewById(R.id.ocsListView);
		listView.setAdapter(adapter);
	}

	@Override
	protected void onStop() {
		refreshAccount.detach();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.last_ocs, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_ocs_refresh:
			refreshAccout();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refreshAccout() {
		account.loadData(application);
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		ListView listView = (ListView) findViewById(R.id.ocsListView);
		listView.setAdapter(null);
		StateHolder holder = StateHolder.getInstance(this);
		account = holder.getAccountList().get(arg2);
		updateListView();
	}

	private void updateListView() {
		if (!account.loadIfExpired(application)) {
			refreshListView();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public void dialogFinished(AbstractDialogWrapper<?> dialog, int buttonId,
			Serializable arg) {
		// TODO Auto-generated method stub

	}

}