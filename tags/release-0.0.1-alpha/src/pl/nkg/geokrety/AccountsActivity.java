package pl.nkg.geokrety;

import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.data.StateHolder;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class AccountsActivity extends Activity {

	private ListView mainListView;
	private ArrayAdapter<Account> listAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final StateHolder holder = StateHolder.getInstance(this);

		setContentView(R.layout.activity_accounts);

		mainListView = (ListView) findViewById(R.id.accountListView);

		// Create ArrayAdapter using the planet list.
		listAdapter = new ArrayAdapter<Account>(this,
				android.R.layout.simple_list_item_single_choice,
				holder.getAccountList());

		// Set the ArrayAdapter as the ListView's adapter.
		mainListView.setAdapter(listAdapter);
		mainListView.setItemChecked(holder.getDefaultAccount(), true);

		mainListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				holder.setDefaultAccount(arg2);
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
	public void onBackPressed() {
		setResult(StateHolder.getInstance(this).getDefaultAccount());
		finish();
		super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.account_new:
			prepareAccountDialog(false);
			return true;

		case R.id.account_edit:
			prepareAccountDialog(true);
			return true;

		case R.id.account_remove:
			onRemoveAccount();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private final DialogInterface.OnClickListener emptyListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
		}
	};

	private void prepareAccountDialog(final boolean isEdit) {
		final StateHolder holder = StateHolder.getInstance(this);
		final Account account = holder.getDefaultAccount() == ListView.INVALID_POSITION ? null
				: holder.getAccountList().get(holder.getDefaultAccount());

		if (isEdit && holder.getDefaultAccount() == ListView.INVALID_POSITION) {
			Toast t = Toast.makeText(AccountsActivity.this,
					R.string.error_current_null, Toast.LENGTH_SHORT);
			t.show();
			return;
		}

		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		View layout = inflater.inflate(R.layout.activity_account,
				(ViewGroup) findViewById(R.id.accuntMainLayout));

		final EditText loginEditText = (EditText) layout
				.findViewById(R.id.loginEditText);
		final EditText passwordEditText = (EditText) layout
				.findViewById(R.id.passwordEditText);
		final EditText ocEditText = (EditText) layout
				.findViewById(R.id.ocEditText);

		if (isEdit) {
			loginEditText.setText(account.getGeoKretyLogin());
			passwordEditText.setText(account.getGeoKretyPassword());
			ocEditText.setText(account.getOpenCachingLogin());
		}

		// Budowanie i pokazywanie
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		builder.setTitle(isEdit ? R.string.accounts_edit_title
				: R.string.accounts_add_title);
		builder.setNegativeButton(android.R.string.cancel, emptyListener);
		builder.setPositiveButton(android.R.string.ok, emptyListener);
		final AlertDialog alertDialog = builder.create();
		alertDialog.show();

		alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						String login = loginEditText.getText().toString();
						String password = passwordEditText.getText().toString();
						String oc = ocEditText.getText().toString();

						if (login.length() == 0) {
							Toast t = Toast.makeText(AccountsActivity.this,
									R.string.error_login_null,
									Toast.LENGTH_SHORT);
							t.show();
							return;
						}

						if (password.length() == 0) {
							Toast t = Toast.makeText(AccountsActivity.this,
									R.string.error_password_null,
									Toast.LENGTH_SHORT);
							t.show();
							return;
						}

						if (oc.length() == 0) {
							Toast t = Toast.makeText(AccountsActivity.this,
									R.string.error_oc_null, Toast.LENGTH_SHORT);
							t.show();
							return;
						}

						if (isEdit) {
							account.setGeoKretyLogin(login);
							account.setGeoKretyPassword(password);
							account.setOpenCachingLogin(oc);
						} else {
							holder.getAccountList().add(
									new Account(login, password, oc));
						}
						listAdapter.notifyDataSetChanged();
						alertDialog.dismiss();
					}
				});
	}

	@Override
	protected void onPause() {
		StateHolder.getInstance(this).storeAccountList(this);
		super.onPause();
	}

	private void onRemoveAccount() {
		final StateHolder holder = StateHolder.getInstance(this);
		final Account account = holder.getDefaultAccount() == ListView.INVALID_POSITION ? null
				: holder.getAccountList().get(holder.getDefaultAccount());

		if (account == null) {
			Toast t = Toast.makeText(AccountsActivity.this,
					R.string.error_current_null, Toast.LENGTH_SHORT);
			t.show();
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(R.string.account_remove);
		builder.setMessage(getResources().getText(
				R.string.accounts_remove_question)
				+ " " + account.getGeoKretyLogin() + "?");

		builder.setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						mainListView.setItemChecked(
								holder.getDefaultAccount(), false);
						holder.getAccountList().remove(holder.getDefaultAccount());
						listAdapter.notifyDataSetChanged();
						holder.setDefaultAccount(ListView.INVALID_POSITION);
					}

				});

		builder.setNegativeButton(android.R.string.no,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

		AlertDialog alert = builder.create();
		alert.show();
	}
}
