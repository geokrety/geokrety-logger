/*
 * Copyright (C) 2014 Michał Niedźwiecki
 * 
 * This file is part of GeoKrety Logger
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
import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.data.GeoKretLog;
import pl.nkg.geokrety.data.GeoKretLogDataSource;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.lib.dialogs.AbstractDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class GeoKretLogsActivity extends ManagedDialogsActivity implements AdapterView.OnItemSelectedListener, OnItemClickListener {

	private class Adapter extends CursorAdapter {

		private final int				layout;
		private final LayoutInflater	inflater;

		public Adapter(final Context context, final Cursor c, final boolean autoRequery) {
			super(context, c, true);
			layout = android.R.layout.simple_list_item_1;
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public void bindView(final View view, final Context context, final Cursor cursor) {
			final TextView textView = (TextView) view.findViewById(android.R.id.text1);

			final GeoKretLog log = new GeoKretLog(cursor, 0, false);
			int leftDrawable = 0;
			switch (log.getState()) {
				case GeoKretLog.STATE_DRAFT:
					leftDrawable = R.drawable.writing_log_submitting;
					// TODO: create another icon for this
					break;

				case GeoKretLog.STATE_NEW:
					// TODO: probably newer used
					break;

				case GeoKretLog.STATE_PROBLEM:
					leftDrawable = log.getProblem() == R.string.warning_already_logged ? R.drawable.writing_log_double : R.drawable.writing_log_problem;
					break;

				case GeoKretLog.STATE_SENT:
					leftDrawable = R.drawable.writing_log_success;
					break;

				case GeoKretLog.STATE_OUTBOX:
					leftDrawable = R.drawable.writing_log_no_connection;
					// TODO: create another icon for this
					break;
			}
			textView.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, 0, 0, 0);
			textView.setText(log.toString());
		}

		@Override
		public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
			return inflater.inflate(layout, parent, false);
		}

	}

	private Account					account;
	private SQLiteDatabase			database;

	private Cursor					geoKretLogsCursor;
	private ListView listView;

	@Override
	public void dialogFinished(final AbstractDialogWrapper<?> dialog, final int buttonId, final Serializable arg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		// TODO Auto-generated method stub
		Toast.makeText(this, String.valueOf(id) + ", pos: " + position, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onItemSelected(final AdapterView<?> arg0, final View arg1, final int arg2, final long arg3) {
		listView.setAdapter(null);
		final StateHolder holder = ((GeoKretyApplication) getApplication()).getStateHolder();
		account = holder.getAccountList().get(arg2);
		updateListView();
	}

	@Override
	public void onNothingSelected(final AdapterView<?> arg0) {}

	private void closeCursorIfOpened() {
		if (geoKretLogsCursor != null) {
			geoKretLogsCursor.close();
			geoKretLogsCursor = null;
		}
	}

	private void refreshListView() {
		closeCursorIfOpened();
		geoKretLogsCursor = GeoKretLogDataSource.createLoadByUserIDCurosr(database, account.getID());

		final Adapter adapter = new Adapter(this, geoKretLogsCursor, true);
		final ListView listView = (ListView) findViewById(R.id.gklListView);
		listView.setAdapter(adapter);
	}

	private void updateListView() {
		refreshListView();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final StateHolder holder = ((GeoKretyApplication) getApplication()).getStateHolder();
		setContentView(R.layout.activity_geokretlogs);
		final Spinner spin = (Spinner) findViewById(R.id.accountsSpiner);
		spin.setOnItemSelectedListener(this);
		final ArrayAdapter<Account> usersAdapter = new ArrayAdapter<Account>(this, android.R.layout.simple_spinner_item, holder.getAccountList());

		usersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin.setAdapter(usersAdapter);
		spin.setSelection(holder.getDefaultAccount());

		listView = (ListView) findViewById(R.id.gklListView);
		listView.setOnItemClickListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		closeCursorIfOpened();
		database.close();
	}

	@Override
	protected void onStart() {
		super.onStart();
		final StateHolder holder = ((GeoKretyApplication) getApplication()).getStateHolder();
		database = holder.getDbHelper().getReadableDatabase();
		if (holder.getDefaultAccount() != AdapterView.INVALID_POSITION) {
			account = holder.getAccountList().get(holder.getDefaultAccount());
			updateListView();
		}
	}
}
