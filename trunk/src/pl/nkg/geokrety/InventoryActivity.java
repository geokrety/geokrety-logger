package pl.nkg.geokrety;

import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.data.Geokret;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.exceptions.MessagedException;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.Toast;

public class InventoryActivity extends Activity implements
		AdapterView.OnItemSelectedListener {

	private Account account;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
		try {
			account.loadData();
			updateListView();
			Toast t = Toast.makeText(this, R.string.inventory_refresh_finish,
					Toast.LENGTH_SHORT);
			t.show();
		} catch (MessagedException e) {
			Toast t = Toast.makeText(this, e.getFormatedMessage(this),
					Toast.LENGTH_SHORT);
			t.show();
		}
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
		try {
			account.loadIfExpired();
			ArrayAdapter<Geokret> adapter = new ArrayAdapter<Geokret>(this,
					android.R.layout.simple_list_item_1, account.getInventory());
			ListView listView = (ListView) findViewById(R.id.inventoryListView);
			listView.setAdapter(adapter);
		} catch (MessagedException e) {
			Toast t = Toast.makeText(this, e.getFormatedMessage(this),
					Toast.LENGTH_SHORT);
			t.show();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
}
