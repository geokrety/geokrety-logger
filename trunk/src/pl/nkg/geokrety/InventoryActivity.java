package pl.nkg.geokrety;

import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.data.Geokret;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.dialogs.RefreshProgressDialog;
import pl.nkg.geokrety.widgets.RefreshSuccessfulListener;
import pl.nkg.lib.dialogs.ManagedActivityDialog;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView;

public class InventoryActivity extends ManagedDialogsActivity implements
		AdapterView.OnItemSelectedListener, RefreshSuccessfulListener {

	private Account account;
	private RefreshProgressDialog refreshProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		refreshProgressDialog = new RefreshProgressDialog(this, this);
		super.onCreate(savedInstanceState);
		StateHolder holder = StateHolder.getInstance(this);
		setContentView(R.layout.activity_inventory);
		Spinner spin = (Spinner) findViewById(R.id.accountsSpiner);
		spin.setOnItemSelectedListener(this);
		ArrayAdapter<Account> aa = new ArrayAdapter<Account>(this,
				android.R.layout.simple_spinner_item, holder.getAccountList());

		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin.setAdapter(aa);
		spin.setSelection(holder.getDefaultAccount());

		if (holder.getDefaultAccount() != ListView.INVALID_POSITION) {
			account = holder.getAccountList().get(holder.getDefaultAccount());
			updateListView();
		}
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
		account.loadData(refreshProgressDialog, this);
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
		account.loadIfExpired(refreshProgressDialog, this);
	}

	@Override
	public void onRefreshSuccessful() {
		ArrayAdapter<Geokret> adapter = new ArrayAdapter<Geokret>(
				InventoryActivity.this, android.R.layout.simple_list_item_1,
				account.getInventory());
		ListView listView = (ListView) findViewById(R.id.inventoryListView);
		listView.setAdapter(adapter);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	protected void registerDialogs() {
		registerDialog(refreshProgressDialog);
	}

	@Override
	public void dialogFinished(ManagedActivityDialog dialog, int buttonId) {
	}
}
