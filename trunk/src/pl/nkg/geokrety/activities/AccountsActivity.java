package pl.nkg.geokrety.activities;

import java.io.Serializable;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.dialogs.EditAccountDialog;
import pl.nkg.geokrety.dialogs.NewAccountDialog;
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
import android.widget.Toast;

public class AccountsActivity extends ManagedDialogsActivity {

	private ListView mainListView;
	private ArrayAdapter<Account> listAdapter;

	private NewAccountDialog newAccountDialog;
	private EditAccountDialog editAccountDialog;
	private RemoveAccountDialog removeAccountDialog;

	private static final int NEW_ACCOUNT = 1;
	private static final int EDIT_ACCOUNT = 2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final StateHolder holder = ((GeoKretyApplication) getApplication())
				.getStateHolder();

		newAccountDialog = new NewAccountDialog(this);
		editAccountDialog = new EditAccountDialog(this);
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
					.getGeoKretyLogin());
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

	public void onAddAccountButtonClicksV2(View view) {
		showNewAccountDialogV2();
	}

	private void showNewAccountDialog() {
		newAccountDialog.clearValues();
		newAccountDialog.show(null);
	}

	private void showNewAccountDialogV2() {
		Intent intent = new Intent(this, AccountActivity.class);
		startActivityForResult(intent, NEW_ACCOUNT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == NEW_ACCOUNT) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(
						this,
						"requestCode: "
								+ requestCode
								+ "\nresultCode: "
								+ resultCode
								+ "\nsecid: "
								+ data.getStringExtra(AccountActivity.SECID)
								+ "\nocuuid: "
								+ data.getStringArrayExtra(AccountActivity.OCUUIDS)[0]
								+ "\nid: "
								+ data.getLongExtra(AccountActivity.ACCOUNT_ID,
										-1), Toast.LENGTH_LONG).show();
				return;
			}
		}
		Toast.makeText(this,
				"requestCode: " + requestCode + "\nresultCode: " + resultCode,
				Toast.LENGTH_LONG).show();
	}

	private void showEditAccountDialog(int position) {

		Account account = ((GeoKretyApplication) getApplication())
				.getStateHolder().getAccountList().get(position);

		// editAccountDialog.setGKLogin(account.getGeoKretyLogin());
		// editAccountDialog.setGKPassword(account.getGeoKretyPassword());
		// editAccountDialog.setOCLogin(account.getOpenCachingLogin());
		// editAccountDialog.setPosition(position);
		editAccountDialog.show(null, account.getGeoKretyLogin(),
				account.getGeoKretyPassword(), account.getOpenCachingLogin(),
				position);
	}

	private void showRemoveAccountDialog(int position) {
		Account account = ((GeoKretyApplication) getApplication())
				.getStateHolder().getAccountList().get(position);
		// removeAccountDialog.setPosition(position);
		removeAccountDialog.show(account.getGeoKretyLogin(),
				account.getGeoKretyLogin(), position);
	}

	@Override
	protected void onPause() {
		((GeoKretyApplication) getApplication()).getStateHolder()
				.storeAccountList(this);
		super.onPause();
	}

	@Override
	public void dialogFinished(AbstractDialogWrapper<?> dialog, int buttonId,
			Serializable arg) {
		StateHolder holder = ((GeoKretyApplication) getApplication())
				.getStateHolder();
		if (dialog.getDialogId() == newAccountDialog.getDialogId()
				&& buttonId == Dialog.BUTTON_POSITIVE) {
			holder.getAccountList().add(
					new Account(newAccountDialog.getGKLogin(), newAccountDialog
							.getGKPassword(), newAccountDialog.getOCLogin()));
		} else if (dialog.getDialogId() == editAccountDialog.getDialogId()
				&& buttonId == Dialog.BUTTON_POSITIVE) {
			Account account = holder.getAccountList().get(
					(Integer) editAccountDialog.getPosition());
			account.setGeoKretyLogin(editAccountDialog.getGKLogin());
			account.setGeoKretyPassword(editAccountDialog.getGKPassword());
			account.setOpenCachingLogin(editAccountDialog.getOCLogin());
		} else if (dialog.getDialogId() == removeAccountDialog.getDialogId()
				&& buttonId == Dialog.BUTTON_POSITIVE) {
			mainListView.setItemChecked(holder.getDefaultAccount(), false);
			int pos = (Integer) removeAccountDialog.getPosition();
			holder.getAccountList().remove(pos);
			holder.setDefaultAccount(ListView.INVALID_POSITION);
		}
		listAdapter.notifyDataSetChanged();
	}
}
