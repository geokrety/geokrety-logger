package pl.nkg.geokrety.activities;

import java.io.Serializable;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.dialogs.Dialogs;
import pl.nkg.geokrety.dialogs.GKDialog;
import pl.nkg.geokrety.dialogs.OCDialog;
import pl.nkg.geokrety.threads.GettingSecidThread;
import pl.nkg.geokrety.threads.GettingUuidThread;
import pl.nkg.lib.dialogs.AbstractDialogWrapper;
import pl.nkg.lib.dialogs.GenericProgressDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;
import pl.nkg.lib.okapi.SupportedOKAPI;
import pl.nkg.lib.threads.AbstractForegroundTaskWrapper;
import pl.nkg.lib.threads.GenericTaskListener;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AccountActivity extends ManagedDialogsActivity {

	public static final String ACCOUNT_ID = "accountID";
	public static final String ACCOUNT_NAME = "accountName";
	public static final String SECID = "secid";
	public static final String OCUUIDS = "ocUUIDs";

	private long accountID;
	private String accountName;
	private String secid;
	private String[] ocUUIDs = new String[SupportedOKAPI.SUPPORTED.length];

	private TextView accountNameEditText;
	private CheckBox gkCheckBox;
	private CheckBox[] ocCheckBox = new CheckBox[SupportedOKAPI.SUPPORTED.length];
	private Button saveButton;

	private GKDialog gkDialog;
	private OCDialog ocDialog;

	private GenericProgressDialogWrapper secidProgressDialog;
	private GenericProgressDialogWrapper uuidProgressDialog;

	private GettingSecidThread gettingSecidThread;
	private GettingUuidThread gettingUuidThread;

	private GeoKretyApplication application;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		application = (GeoKretyApplication) getApplication();

		gkDialog = new GKDialog(this);
		ocDialog = new OCDialog(this);

		secidProgressDialog = new GenericProgressDialogWrapper(this,
				Dialogs.SECID_PROGRESSDIALOG);
		secidProgressDialog.setProgress(getText(R.string.download_login_gk)
				.toString());

		uuidProgressDialog = new GenericProgressDialogWrapper(this,
				Dialogs.UUID_PROMPTDIALOG);
		// uuidProgressDialog.setProgress(getText(R.string.download_getting_ocs));

		gettingSecidThread = GettingSecidThread.getFromHandler(application
				.getForegroundTaskHandler());
		gettingUuidThread = GettingUuidThread.getFromHandler(application
				.getForegroundTaskHandler());

		setContentView(R.layout.activity_account);
		accountID = getIntent().getLongExtra(ACCOUNT_ID,
				ListView.INVALID_POSITION);
		secid = getIntent().getStringExtra(SECID);
		ocUUIDs = getIntent().getStringArrayExtra(OCUUIDS);
		accountName = getIntent().getStringExtra(ACCOUNT_NAME);

		if (ocUUIDs == null) {
			ocUUIDs = new String[SupportedOKAPI.SUPPORTED.length];
		}

		accountNameEditText = (TextView) findViewById(R.id.accountNameTextView);
		gkCheckBox = (CheckBox) findViewById(R.id.gkCheckBox);
		ocCheckBox[0] = (CheckBox) findViewById(R.id.ocCheckBox0);
		saveButton = (Button) findViewById(R.id.saveButton);

		findViewById(R.id.ocCheckBox1).setEnabled(false);
		findViewById(R.id.ocCheckBox2).setEnabled(false);
		findViewById(R.id.ocCheckBox3).setEnabled(false);
		findViewById(R.id.ocCheckBox4).setEnabled(false);

		accountNameEditText.setText(getIntent().getStringExtra(ACCOUNT_NAME));
		updateChecks();

		gkCheckBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (gkCheckBox.isChecked()) {
					gkDialog.show(null, accountName);
				} else {
					secid = null;
					saveButton.setEnabled(false);
				}

			}
		});

		for (int i = 0; i < ocCheckBox.length; i++) {
			final int nr = i;
			ocCheckBox[i].setOnClickListener(new OnClickListener() {

				final int portalNr = nr;

				@Override
				public void onClick(View v) {
					if (ocCheckBox[portalNr].isChecked()) {
						ocDialog.show(portalNr,
								SupportedOKAPI.SUPPORTED[portalNr].host,
								accountName);
					} else {
						ocUUIDs[portalNr] = null;
					}
				}
			});
		}
	}

	private void updateChecks() {
		gkCheckBox.setChecked(!Utils.isEmpty(secid));
		ocCheckBox[0].setChecked(!Utils.isEmpty(ocUUIDs[0]));
		accountNameEditText
				.setText(Utils.isEmpty(accountName) ? getText(R.string.account_account_name_hint)
						: accountName);
		saveButton.setEnabled(gkCheckBox.isChecked());
	}

	@Override
	protected void onStart() {
		super.onStart();
		gettingSecidThread.attach(secidProgressDialog,
				new GenericTaskListener<Pair<String, String>, String, String>(
						this) {

					@Override
					public void onFinish(
							AbstractForegroundTaskWrapper<Pair<String, String>, String, String> sender,
							Pair<String, String> param, String result) {
						secid = result;
						accountName = param.first;
						updateChecks();
						Toast.makeText(AccountActivity.this, "secid: " + secid,
								Toast.LENGTH_LONG).show();
					}

					@Override
					public void onError(
							AbstractForegroundTaskWrapper<Pair<String, String>, String, String> sender,
							Pair<String, String> param, Throwable exception) {
						super.onError(sender, param, exception);
						gkDialog.show(null);
					}
				});

		gettingUuidThread.attach(uuidProgressDialog,
				new GenericTaskListener<Pair<String, Integer>, String, String>(
						this) {

					@Override
					public void onFinish(
							AbstractForegroundTaskWrapper<Pair<String, Integer>, String, String> sender,
							Pair<String, Integer> param, String result) {
						ocUUIDs[param.second] = result;
						if (Utils.isEmpty(accountName)) {
							accountName = param.first;
						}
						updateChecks();
						Toast.makeText(
								AccountActivity.this,
								SupportedOKAPI.SUPPORTED[param.second].host
										+ " uuid: " + result, Toast.LENGTH_LONG)
								.show();
					}

					@Override
					public void onError(
							AbstractForegroundTaskWrapper<Pair<String, Integer>, String, String> sender,
							Pair<String, Integer> param, Throwable exception) {
						super.onError(sender, param, exception);
						ocDialog.show(param.second);
					}

				});

	}

	@Override
	public void dialogFinished(AbstractDialogWrapper<?> dialog, int buttonId,
			Serializable arg) {

		updateChecks();
		if (buttonId != Dialog.BUTTON_POSITIVE) {
			return;
		}

		if (dialog.getDialogId() == Dialogs.GK_PROMPTDIALOG) {
			gettingSecidThread.execute(new Pair<String, String>(gkDialog
					.getGKLogin(), gkDialog.getGKPassword()));
		} else if (dialog.getDialogId() == Dialogs.OC_PROMPTDIALOG) {
			int nr = (Integer) arg;
			uuidProgressDialog
					.setProgress(getText(R.string.download_getting_uuid) + " "
							+ SupportedOKAPI.SUPPORTED[nr].host
							+ getText(R.string.dots));
			gettingUuidThread.execute(new Pair<String, Integer>(ocDialog
					.getOCLogin(), nr));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArray(OCUUIDS, ocUUIDs);
		outState.putLong(ACCOUNT_ID, accountID);
		outState.putString(SECID, secid);
		outState.putString(ACCOUNT_NAME, accountName);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		secid = savedInstanceState.getString(SECID);
		accountID = savedInstanceState.getLong(ACCOUNT_ID);
		ocUUIDs = savedInstanceState.getStringArray(OCUUIDS);
		accountName = savedInstanceState.getString(ACCOUNT_NAME);
	}

	public void saveClick(View view) {
		Intent returnIntent = new Intent();
		returnIntent.putExtra(SECID, secid);
		returnIntent.putExtra(ACCOUNT_ID, accountID);
		returnIntent.putExtra(OCUUIDS, ocUUIDs);
		returnIntent.putExtra(ACCOUNT_NAME, accountName);
		setResult(RESULT_OK, returnIntent);
		finish();
	}
}
