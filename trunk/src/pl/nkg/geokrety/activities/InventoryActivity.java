package pl.nkg.geokrety.activities;

import java.io.Serializable;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.activities.listeners.RefreshListener;
import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.data.Geokret;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.dialogs.RefreshProgressDialog;
import pl.nkg.geokrety.threads.RefreshAccount;
import pl.nkg.lib.dialogs.AbstractDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;
import pl.nkg.lib.threads.AbstractForegroundTaskWrapper;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView;

public class InventoryActivity extends ManagedDialogsActivity implements
		AdapterView.OnItemSelectedListener {

	private Account account;
	private RefreshProgressDialog refreshProgressDialog;
	private GeoKretyApplication application;
	private RefreshAccount refreshAccount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StateHolder holder = StateHolder.getInstance(this);
		super.onCreate(savedInstanceState);
		refreshProgressDialog = new RefreshProgressDialog(this);

		application = (GeoKretyApplication) getApplication();
		refreshAccount = RefreshAccount.getFromHandler(application
				.getForegroundTaskHandler());

		setContentView(R.layout.activity_inventory);
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
		refreshAccount.attach(refreshProgressDialog, new RefreshListener(this) {
			@Override
			public void onFinish(
					AbstractForegroundTaskWrapper<Account, String, Boolean> sender,
					Account param, Boolean result) {
				super.onFinish(sender, param, result);
				refreshListView();
			}
		});
		StateHolder holder = StateHolder.getInstance(this);
		if (holder.getDefaultAccount() != ListView.INVALID_POSITION) {
			account = holder.getAccountList().get(holder.getDefaultAccount());
			updateListView();
		}
	}
	
	private void refreshListView() {
		ArrayAdapter<Geokret> adapter = new ArrayAdapter<Geokret>(
				InventoryActivity.this, android.R.layout.simple_list_item_1,
				account.getInventory());
		ListView listView = (ListView) findViewById(R.id.inventoryListView);
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
		getMenuInflater().inflate(R.menu.inventory, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_inventory_refresh:
			refreshAccout();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refreshAccout() {
		account.loadData((GeoKretyApplication) getApplication());
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		ListView listView = (ListView) findViewById(R.id.inventoryListView);
		listView.setAdapter(null);
		StateHolder holder = StateHolder.getInstance(this);
		account = holder.getAccountList().get(arg2);
		updateListView();
	}

	private void updateListView() {
		if (!account.loadIfExpired((GeoKretyApplication) getApplication())) {
			refreshListView();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public void dialogFinished(AbstractDialogWrapper<?> dialog, int buttonId,
			Serializable arg) {
	}
}
