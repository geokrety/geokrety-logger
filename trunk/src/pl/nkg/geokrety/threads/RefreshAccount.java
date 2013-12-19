package pl.nkg.geokrety.threads;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.dialogs.RefreshProgressDialog;
import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.geokrety.widgets.RefreshSuccessfulListener;
import android.os.AsyncTask;
import android.widget.Toast;

public class RefreshAccount extends AsyncTask<String, Integer, Boolean> {

	private static RefreshAccount instance;

	public static RefreshAccount getInstance() {
		return instance;
	}

	private MessagedException excaption;
	private Account account;
	private RefreshProgressDialog refreshProgressDialog;
	private RefreshSuccessfulListener refreshSuccessfulListener;

	public RefreshAccount(Account account,
			RefreshProgressDialog refreshProgressDialog,
			RefreshSuccessfulListener refreshSuccessfulListener) {
		this.account = account;
		this.refreshProgressDialog = refreshProgressDialog;
		this.refreshSuccessfulListener = refreshSuccessfulListener;
	}

	@Override
	protected void onPreExecute() {
		synchronized (instance) {
			try {
				refreshProgressDialog.show();
			} catch (Throwable t) {
				t.printStackTrace();
				cancel(true);
				instance = null;
			}
		}
	}

	@Override
	protected Boolean doInBackground(String... params) {
		try {
			publishProgress(0);
			account.loadSecureID(this);
			publishProgress(1);
			account.loadInventory(this);
			publishProgress(2);
			account.loadOpenCachingUUID(this);
			publishProgress(3);
			account.loadOpenCachingLogs(this);
			account.touchLastLoadedDate();
			return true;
		} catch (MessagedException e) {
			excaption = e;
			return false;
		}

	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		synchronized (instance) {
			if (result != null) {
				if (result) {
					Toast.makeText(
							refreshProgressDialog.getManagedDialogsActivity(),
							R.string.download_finish, Toast.LENGTH_LONG).show();
					refreshSuccessfulListener.onRefreshSuccessful();
				} else {
					Toast.makeText(
							refreshProgressDialog.getManagedDialogsActivity(),
							excaption.getFormatedMessage(refreshProgressDialog
									.getManagedDialogsActivity()),
							Toast.LENGTH_LONG).show();
				}
				refreshProgressDialog.dismiss();
			}
			instance = null;
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		refreshProgressDialog.setProgress(values[0]);
	}

	public void updateActivities(RefreshProgressDialog refreshProgressDialog,
			RefreshSuccessfulListener refreshSuccessfulListener) {
		synchronized (instance) {
			this.refreshProgressDialog = refreshProgressDialog;
			this.refreshSuccessfulListener = refreshSuccessfulListener;
		}
	}

	public static void refreshAccount(Account account,
			RefreshProgressDialog refreshProgressDialog,
			RefreshSuccessfulListener listener, boolean force) {

		if (instance != null && !force) {
			return;
		}

		if (instance != null) {
			instance.cancel(true);
			instance = null;
		}

		instance = new RefreshAccount(account, refreshProgressDialog, listener);
		instance.execute();
	}
}
