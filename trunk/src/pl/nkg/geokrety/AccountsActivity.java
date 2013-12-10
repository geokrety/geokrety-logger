package pl.nkg.geokrety;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class AccountsActivity extends Activity {

	private ListView mainListView;
	private ArrayAdapter<String> listAdapter;
	private PreferencesDecorator preferences;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferences = new PreferencesDecorator(this);

		setContentView(R.layout.activity_accounts);

		mainListView = (ListView) findViewById(R.id.accountListView);

		// Create ArrayAdapter using the planet list.
		listAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice,
				preferences.getAccountList());

		// Set the ArrayAdapter as the ListView's adapter.
		mainListView.setAdapter(listAdapter);
		mainListView.setItemChecked(preferences.getCurrentAccount(), true);

		mainListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				preferences.setCurrentAccount(arg2);

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.accounts, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.account_new:
			onAddNewAccount();
			return true;

		case R.id.account_edit:
			onEditAccount();
			return true;

		case R.id.account_remove:
			onRemoveAccount();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void onAddNewAccount() {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.activity_account);
		dialog.setTitle(R.string.accounts_add_title);

		Button okButton = (Button) dialog.findViewById(R.id.okButton);
		Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
		// if button is clicked, close the custom dialog

		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String login = ((EditText) dialog
						.findViewById(R.id.loginEditText)).getText().toString();
				String password = ((EditText) dialog
						.findViewById(R.id.passwordEditText)).getText()
						.toString();

				if (login.length() == 0) {
					Toast t = Toast.makeText(AccountsActivity.this,
							R.string.error_login_null, Toast.LENGTH_SHORT);
					t.show();
					return;
				}

				if (password.length() == 0) {
					Toast t = Toast.makeText(AccountsActivity.this,
							R.string.error_password_null, Toast.LENGTH_SHORT);
					t.show();
					return;
				}

				preferences.addAccount(login, password);
				// listAdapter.add(login);
				listAdapter.notifyDataSetChanged();
				dialog.dismiss();
			}
		});

		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	private void onEditAccount() {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.activity_account);
		dialog.setTitle(R.string.accounts_edit_title);

		Button okButton = (Button) dialog.findViewById(R.id.okButton);
		Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);

		final EditText loginEditText = (EditText) dialog
				.findViewById(R.id.loginEditText);
		final EditText passwordEditText = (EditText) dialog
				.findViewById(R.id.passwordEditText);

		final int current = preferences.getCurrentAccount();// getCurrentAccount();
		if (current == ListView.INVALID_POSITION) {
			Toast t = Toast.makeText(AccountsActivity.this,
					R.string.error_current_null, Toast.LENGTH_SHORT);
			t.show();
			return;
		}

		loginEditText.setText(preferences.getAccountLogin(current));
		passwordEditText.setText(preferences.getAccountPassword(current));

		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String login = loginEditText.getText().toString();
				String password = passwordEditText.getText().toString();

				if (login.length() == 0) {
					Toast t = Toast.makeText(AccountsActivity.this,
							R.string.error_login_null, Toast.LENGTH_SHORT);
					t.show();
					return;
				}

				if (password.length() == 0) {
					Toast t = Toast.makeText(AccountsActivity.this,
							R.string.error_password_null, Toast.LENGTH_SHORT);
					t.show();
					return;
				}

				preferences.setAccountLogin(current, login);
				preferences.setAccountPassword(current, password);
				listAdapter.notifyDataSetChanged();
				dialog.dismiss();
			}
		});

		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	@Override
	protected void onPause() {
		preferences.flushAccount();
		super.onPause();
	}

	private void onRemoveAccount() {
		final int current = preferences.getCurrentAccount();// getCurrentAccount();
		if (current == ListView.INVALID_POSITION) {
			Toast t = Toast.makeText(AccountsActivity.this,
					R.string.error_current_null, Toast.LENGTH_SHORT);
			t.show();
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(R.string.account_remove);
		builder.setMessage(getResources().getText(
				R.string.accounts_remove_question)
				+ " " + preferences.getAccountLogin() + "?");

		builder.setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						mainListView.setItemChecked(
								preferences.getCurrentAccount(), false);
						preferences.removeAccount(preferences
								.getCurrentAccount());
						listAdapter.notifyDataSetChanged();
						preferences
								.setCurrentAccount(ListView.INVALID_POSITION);
						dialog.dismiss();
					}

				});

		builder.setNegativeButton(android.R.string.no,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		AlertDialog alert = builder.create();
		alert.show();
	}
}
