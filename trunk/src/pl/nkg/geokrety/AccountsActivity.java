package pl.nkg.geokrety;

import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.dialogs.EditAccountDialog;
import pl.nkg.geokrety.dialogs.NewAccountDialog;
import pl.nkg.geokrety.dialogs.RemoveAccountDialog;
import pl.nkg.lib.dialogs.ManagedActivityDialog;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;
import android.os.Bundle;
import android.app.Dialog;
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

	private NewAccountDialog newAccountDialog;
	private EditAccountDialog editAccountDialog;
	private RemoveAccountDialog removeAccountDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		newAccountDialog = new NewAccountDialog(this);
		editAccountDialog = new EditAccountDialog(this);
		removeAccountDialog = new RemoveAccountDialog(this);

		super.onCreate(savedInstanceState);
		final StateHolder holder = StateHolder.getInstance(this);

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
		StateHolder holder = StateHolder.getInstance(this);

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
		setResult(StateHolder.getInstance(this).getDefaultAccount());
		finish();
		super.onBackPressed();
	}

	public void onAddAccountButtonClicks(View view) {
		showNewAccountDialog();
	}

	private void showNewAccountDialog() {
		newAccountDialog.clearValues();
		newAccountDialog.show();
	}

	private void showEditAccountDialog(int position) {

		Account account = StateHolder.getInstance(this).getAccountList()
				.get(position);

		editAccountDialog.setGKLogin(account.getGeoKretyLogin());
		editAccountDialog.setGKPassword(account.getGeoKretyPassword());
		editAccountDialog.setOCLogin(account.getOpenCachingLogin());
		editAccountDialog.setArg(position);
		editAccountDialog.show();
	}

	private void showRemoveAccountDialog(int position) {
		Account account = StateHolder.getInstance(this).getAccountList()
				.get(position);
		removeAccountDialog.setArg(position);
		removeAccountDialog.show(account.getGeoKretyLogin());
	}

	@Override
	protected void onPause() {
		StateHolder.getInstance(this).storeAccountList(this);
		super.onPause();
	}

	@Override
	public void dialogFinished(ManagedActivityDialog dialog, int buttonId) {
		StateHolder holder = StateHolder.getInstance(this);
		if (dialog.getDialogId() == newAccountDialog.getDialogId()
				&& buttonId == Dialog.BUTTON_POSITIVE) {
			holder.getAccountList().add(
					new Account(newAccountDialog.getGKLogin(), newAccountDialog
							.getGKPassword(), newAccountDialog.getOCLogin()));
		} else if (dialog.getDialogId() == editAccountDialog.getDialogId()
				&& buttonId == Dialog.BUTTON_POSITIVE) {
			Account account = holder.getAccountList().get(
					(Integer) editAccountDialog.getArg());
			account.setGeoKretyLogin(editAccountDialog.getGKLogin());
			account.setGeoKretyPassword(editAccountDialog.getGKPassword());
			account.setOpenCachingLogin(editAccountDialog.getOCLogin());
		} else if (dialog.getDialogId() == removeAccountDialog.getDialogId()
				&& buttonId == Dialog.BUTTON_POSITIVE) {
			mainListView.setItemChecked(holder.getDefaultAccount(), false);
			int pos = (Integer) removeAccountDialog.getArg();
			holder.getAccountList().remove(pos);
			holder.setDefaultAccount(ListView.INVALID_POSITION);
		}
		listAdapter.notifyDataSetChanged();
	}

	@Override
	protected void registerDialogs() {
		registerDialog(newAccountDialog);
		registerDialog(editAccountDialog);
		registerDialog(removeAccountDialog);
	}

}
