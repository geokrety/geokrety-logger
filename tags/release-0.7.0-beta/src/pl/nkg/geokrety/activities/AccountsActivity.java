/*
 * Copyright (C) 2013, 2014 Michał Niedźwiecki
 * 
 * This file is a part of GeoKrety Logger
 * http://geokretylog.sourceforge.net/
 * 
 * GeoKrety Logger is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * or see <http://www.gnu.org/licenses/>
 */
package pl.nkg.geokrety.activities;

import java.io.Serializable;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.data.User;
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
import android.widget.Toast;

public class AccountsActivity extends ManagedDialogsActivity {

	private ListView mainListView;
	private ArrayAdapter<User> listAdapter;

	private RemoveAccountDialog removeAccountDialog;

	private static final int NEW_ACCOUNT = 1;
	private static final int EDIT_ACCOUNT = 2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final StateHolder holder = ((GeoKretyApplication) getApplication())
				.getStateHolder();

		removeAccountDialog = new RemoveAccountDialog(this);

		setContentView(R.layout.activity_accounts);

		mainListView = (ListView) findViewById(R.id.accountListView);

		listAdapter = new ArrayAdapter<User>(this,
				android.R.layout.simple_list_item_single_choice,
				holder.getAccountList());

		mainListView.setAdapter(listAdapter);
		mainListView.setItemChecked(holder.getDefaultAccountNr(), true);

		mainListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				holder.setDefaultAccount(arg2);
			}
		});

		registerForContextMenu(mainListView);
	}

	@Override
	protected void onStart() {
		super.onStart();
		GeoKretyApplication application = (GeoKretyApplication) getApplication();
		if (application.getStateHolder().getAccountList().size() == 0
				&& !application.isNoAccountHinted()) {
			application.setNoAccountHinted(true);
			Toast.makeText(this, R.string.main_error_no_account_configured,
					Toast.LENGTH_LONG).show();
			showNewAccountDialog();
		}
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
					R.array.profiles_contextmenu_profile);
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
				.getDefaultAccountNr());
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
			long id = data.getLongExtra(User.ACCOUNT_ID,
					ListView.INVALID_POSITION);
			if (id == ListView.INVALID_POSITION) {
				User account = new User(data.getExtras());
				holder.getUserDataSource().persist(account);
				holder.getAccountList().add(account);
			} else {
				User account = holder.getAccountByID(id);
				account.unpack(data.getExtras());
				holder.getUserDataSource().merge(account);
			}
			listAdapter.notifyDataSetChanged();
		}
	}

	private void showEditAccountDialog(int position) {

		User account = ((GeoKretyApplication) getApplication())
				.getStateHolder().getAccountList().get(position);

		Intent intent = new Intent(this, AccountActivity.class);
		intent.putExtras(account.pack(new Bundle()));
		startActivityForResult(intent, EDIT_ACCOUNT);
	}

	private void showRemoveAccountDialog(int position) {
		User account = ((GeoKretyApplication) getApplication())
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
			mainListView.setItemChecked(holder.getDefaultAccountNr(), false);
			int pos = (Integer) removeAccountDialog.getPosition();
			User account = holder.getAccountList().remove(pos);
			holder.setDefaultAccount(ListView.INVALID_POSITION);
			holder.getUserDataSource().remove(account.getID());
		}
		listAdapter.notifyDataSetChanged();
	}
}
