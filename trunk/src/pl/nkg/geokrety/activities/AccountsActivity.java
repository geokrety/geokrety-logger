package pl.nkg.geokrety.activities;

import java.io.Serializable;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.dialogs.RemoveAccountDialog;
import pl.nkg.lib.dialogs.AbstractDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;
import android.os.Bundle;
import android.app.Dialog;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AccountsActivity extends ManagedDialogsActivity {

	private ListView mainListView;
	private ArrayAdapter<Account> listAdapter;

	//private NewAccountDialog newAccountDialog;
	//private EditAccountDialog editAccountDialog;
	private RemoveAccountDialog removeAccountDialog;

	private static final int NEW_ACCOUNT = 1;
	private static final int EDIT_ACCOUNT = 2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final StateHolder holder = ((GeoKretyApplication) getApplication())
				.getStateHolder();

		//newAccountDialog = new NewAccountDialog(this);
		//editAccountDialog = new EditAccountDialog(this);
		removeAccountDialog = new RemoveAccountDialog(this);

		setContentView(R.layout.activity_accounts);

		mainListView = (ListView) findViewById(R.id.accountListView);

		listAdapter = new ArrayAdapter<Account>(this,
				android.R.layout.simple_list_item_single_choice,
				holder.getAccountList());

		mainListView.setAdapter(listAdapter);
		mainListView.setItemChecked(holder.getDefaultAccount(), true);

		mainListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				holder.setDefaultAccount(arg2);
			}
		});

		registerForContextMenu(mainListView);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		StateHolder holder = ((GeoKretyApplication) getApplication())
				.getStateHolder();

		if (v.getId() == R.id.accountListView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(holder.getAccountList().get(info.position)
					.getName());
			String[] menuItems = getResources().getStringArray(
					R.array.context_menu_account);
			for (int i = 0; i < menuItems.length; i++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		int menuItemIndex = item.getItemId();

		switch (menuItemIndex) {
		case 0:
			showEditAccountDialog(info.position);
			return true;

		case 1:
			showRemoveAccountDialog(info.position);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		setResult(((GeoKretyApplication) getApplication()).getStateHolder()
				.getDefaultAccount());
		finish();
		super.onBackPressed();
	}

	public void onAddAccountButtonClicks(View view) {
		showNewAccountDialog();
	}

	private void showNewAccountDialog() {
		Intent intent = new Intent(this, AccountActivity.class);
		startActivityForResult(intent, NEW_ACCOUNT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		StateHolder holder = ((GeoKretyApplication) getApplication())
				.getStateHolder();

		if (resultCode == RESULT_OK) {
			long id = data.getLongExtra(Account.ACCOUNT_ID,
					ListView.INVALID_POSITION);
			if (id == ListView.INVALID_POSITION) {
				Account account = new Account(data.getExtras());
				holder.getAccountDataSource().persistAccount(account);
				holder.getAccountList().add(account);
			} else {
				Account account = holder.getAccountByID(id);
				account.unpack(data.getExtras());
				holder.getAccountDataSource().mergeAccount(account);
			}
			listAdapter.notifyDataSetChanged();
		}
	}

	private void showEditAccountDialog(int position) {

		Account account = ((GeoKretyApplication) getApplication())
				.getStateHolder().getAccountList().get(position);

		Intent intent = new Intent(this, AccountActivity.class);
		intent.putExtras(account.pack(new Bundle()));
		startActivityForResult(intent, EDIT_ACCOUNT);
	}

	private void showRemoveAccountDialog(int position) {
		Account account = ((GeoKretyApplication) getApplication())
				.getStateHolder().getAccountList().get(position);
		// removeAccountDialog.setPosition(position);
		removeAccountDialog
				.show(account.getName(), account.getName(), position);
	}
	
	@Override
	public void dialogFinished(AbstractDialogWrapper<?> dialog, int buttonId,
			Serializable arg) {
		StateHolder holder = ((GeoKretyApplication) getApplication())
				.getStateHolder();
		if (dialog.getDialogId() == removeAccountDialog.getDialogId()
				&& buttonId == Dialog.BUTTON_POSITIVE) {
			mainListView.setItemChecked(holder.getDefaultAccount(), false);
			int pos = (Integer) removeAccountDialog.getPosition();
			Account account = holder.getAccountList().remove(pos);
			holder.setDefaultAccount(ListView.INVALID_POSITION);
			holder.getAccountDataSource().removeAccount(account.getID());
		}
		listAdapter.notifyDataSetChanged();
	}
}
