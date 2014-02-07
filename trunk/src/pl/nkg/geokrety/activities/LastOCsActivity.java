/*
 * Copyright (C) 2013 Michał Niedźwiecki
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
import pl.nkg.geokrety.activities.listeners.RefreshListener;
import pl.nkg.geokrety.data.User;
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

public class LastOCsActivity extends AbstractGeoKretyActivity implements
		AdapterView.OnItemSelectedListener {

	private User account;
	private GenericProgressDialogWrapper refreshProgressDialog;
	private RefreshAccount refreshAccount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		refreshProgressDialog = new GenericProgressDialogWrapper(this,
				Dialogs.REFRESH_ACCOUNT_PROGRESSDIALOG);

		refreshAccount = RefreshAccount.getFromHandler(application
				.getForegroundTaskHandler());

		StateHolder holder = ((GeoKretyApplication) getApplication())
				.getStateHolder();
		setContentView(R.layout.activity_last_ocs);
		Spinner spin = (Spinner) findViewById(R.id.accountsSpiner);
		spin.setOnItemSelectedListener(this);
		ArrayAdapter<User> aa = new ArrayAdapter<User>(this,
				android.R.layout.simple_spinner_item, holder.getAccountList());

		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin.setAdapter(aa);
		spin.setSelection(holder.getDefaultAccountNr());

	}

	@Override
	protected void onStart() {
		super.onStart();
		StateHolder holder = ((GeoKretyApplication) getApplication())
				.getStateHolder();
		refreshAccount.attach(refreshProgressDialog, new RefreshListener(this) {
			@Override
			public void onFinish(
					AbstractForegroundTaskWrapper<User, String, String> sender,
					User param, String result) {
				super.onFinish(sender, param, result);
				refreshListView();
			}
		});
		if (holder.getDefaultAccountNr() != ListView.INVALID_POSITION) {
			account = holder.getAccountList().get(holder.getDefaultAccountNr());
			updateListView();
		}
	}

	private void refreshListView() {
	    // TODO: refactor to CursorAdapter
		ArrayAdapter<GeocacheLog> adapter = new ArrayAdapter<GeocacheLog>(this,
				android.R.layout.simple_list_item_1,
				stateHolder.getGeocacheLogDataSource().loadLastLogs(account.getID()));
		
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
		account.loadData(application, true);
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		ListView listView = (ListView) findViewById(R.id.ocsListView);
		listView.setAdapter(null);
		StateHolder holder = ((GeoKretyApplication) getApplication())
				.getStateHolder();
		account = holder.getAccountList().get(arg2);
		updateListView();
	}

	private void updateListView() {
		if (!account.loadIfExpired(application, false)) {
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
