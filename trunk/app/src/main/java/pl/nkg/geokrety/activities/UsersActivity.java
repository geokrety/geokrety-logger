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
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.data.User;
import pl.nkg.geokrety.dialogs.RemoveAccountDialog;
import pl.nkg.lib.dialogs.AbstractDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class UsersActivity extends ManagedDialogsActivity {

    private ListView mainListView;
    private ArrayAdapter<User> listAdapter;

    private RemoveAccountDialog removeAccountDialog;

    private static final int NEW_ACCOUNT = 1;
    private static final int EDIT_ACCOUNT = 2;

    @Override
    public void dialogFinished(final AbstractDialogWrapper<?> dialog, final int buttonId,
            final Serializable arg) {
        final StateHolder holder = ((GeoKretyApplication) getApplication())
                .getStateHolder();
        if (dialog.getDialogId() == removeAccountDialog.getDialogId()
                && buttonId == DialogInterface.BUTTON_POSITIVE) {
            mainListView.setItemChecked(holder.getDefaultAccountNr(), false);
            final int pos = removeAccountDialog.getPosition();
            final User account = holder.getAccountList().remove(pos);
            holder.setDefaultAccount(AdapterView.INVALID_POSITION);
            holder.getUserDataSource().remove(account.getID());
        }
        listAdapter.notifyDataSetChanged();
    }

    public void onAddAccountButtonClicks(final View view) {
        showNewAccountDialog();
    }

    @Override
    public void onBackPressed() {
        setResult(((GeoKretyApplication) getApplication()).getStateHolder()
                .getDefaultAccountNr());
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        final int menuItemIndex = item.getItemId();
        final StateHolder holder = ((GeoKretyApplication) getApplication())
                .getStateHolder();

        switch (menuItemIndex) {
            case 0:
                final int current = mainListView.getCheckedItemPosition();
                if (current == info.position) {
                    holder.setDefaultAccount(-1);
                } else {
                    holder.setDefaultAccount(info.position);
                }
                mainListView.setItemChecked(holder.getDefaultAccountNr(), true);
                return true;

            case 1:
                showRemoveAccountDialog(info.position);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
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

            @Override
            public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2,
                    final long arg3) {
                // holder.setDefaultAccount(arg2);
                mainListView.setItemChecked(holder.getDefaultAccountNr(), true);
                showEditAccountDialog(arg2);
            }
        });

        registerForContextMenu(mainListView);
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.accountListView) {
            final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            final User user = listAdapter.getItem(info.position);
            menu.setHeaderTitle(user.getName());
            final int current = mainListView.getCheckedItemPosition();
            final String[] menuItems = getResources()
                    .getStringArray(
                            current == info.position ? R.array.users_contextmenu_checked
                                    : R.array.users_contextmenu_unchecked);
            for (int i = 0; i < menuItems.length; i++) {
                final MenuItem item = menu.add(Menu.NONE, i, i, menuItems[i]);
                if (i == 0 && listAdapter.getCount() == 1) {
                    item.setEnabled(false);
                }
            }
        }
    }

    private void showEditAccountDialog(final int position) {

        final User account = ((GeoKretyApplication) getApplication())
                .getStateHolder().getAccountList().get(position);

        final Intent intent = new Intent(this, UserActivity.class);
        intent.putExtras(account.pack(new Bundle()));
        startActivityForResult(intent, EDIT_ACCOUNT);
    }

    private void showNewAccountDialog() {
        final Intent intent = new Intent(this, UserActivity.class);
        startActivityForResult(intent, NEW_ACCOUNT);
    }

    private void showRemoveAccountDialog(final int position) {
        final User account = ((GeoKretyApplication) getApplication())
                .getStateHolder().getAccountList().get(position);
        // removeAccountDialog.setPosition(position);
        removeAccountDialog
                .show(account.getName(), account.getName(), position);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final StateHolder holder = ((GeoKretyApplication) getApplication())
                .getStateHolder();

        if (resultCode == RESULT_OK) {
            final long id = data.getLongExtra(User.ACCOUNT_ID,
                    AdapterView.INVALID_POSITION);
            if (id == AdapterView.INVALID_POSITION) {
                final User account = new User(data.getExtras());
                holder.getUserDataSource().persist(account);
                holder.getAccountList().add(account);
            } else {
                final User account = holder.getAccountByID(id);
                account.unpack(data.getExtras());
                holder.getUserDataSource().merge(account);
            }
            listAdapter.notifyDataSetChanged();
        }
        mainListView.setItemChecked(holder.getDefaultAccountNr(), true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final GeoKretyApplication application = (GeoKretyApplication) getApplication();
        if (application.getStateHolder().getAccountList().size() == 0
                && !application.isNoAccountHinted()) {
            application.setNoAccountHinted(true);
            Toast.makeText(this, R.string.main_error_no_account_configured,
                    Toast.LENGTH_LONG).show();
            showNewAccountDialog();
        }
    }
}
