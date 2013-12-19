package pl.nkg.geokrety.threads;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.data.GeoKretLog;
import pl.nkg.geokrety.dialogs.LogProgressDialog;
import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.geokrety.widgets.LogSuccessfulListener;
import android.os.AsyncTask;
import android.widget.Toast;

public class LogGeoKret extends AsyncTask<String, Integer, Boolean> {

	private static LogGeoKret instance;

	public static LogGeoKret getInstance() {
		return instance;
	}

	private MessagedException excaption;
	private GeoKretLog log;
	private Account account;
	private LogProgressDialog logProgressDialog;
	private LogSuccessfulListener logSuccessfulListener;

	public LogGeoKret(GeoKretLog log, Account account,
			LogProgressDialog logProgressDialog,
			LogSuccessfulListener logSuccessfulListener) {
		this.log = log;
		this.account = account;
		this.logProgressDialog = logProgressDialog;
		this.logSuccessfulListener = logSuccessfulListener;
	}

	@Override
	protected void onPreExecute() {
		synchronized (instance) {
			try {
				logProgressDialog.show();
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
			return log.submitBackground(account);
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
							logProgressDialog.getManagedDialogsActivity(),
							R.string.submit_finish, Toast.LENGTH_LONG).show();
					logSuccessfulListener.onLogSuccessful();
				} else {
					Toast.makeText(
							logProgressDialog.getManagedDialogsActivity(),
							excaption.getFormatedMessage(logProgressDialog
									.getManagedDialogsActivity()),
							Toast.LENGTH_LONG).show();
				}
				logProgressDialog.dismiss();
			}
			instance = null;
		}
	}

	public void updateActivities(LogProgressDialog logProgressDialog,
			LogSuccessfulListener logSuccessfulListener) {
		synchronized (instance) {
			this.logProgressDialog = logProgressDialog;
			this.logSuccessfulListener = logSuccessfulListener;
		}
	}

	public static void logGeoKret(GeoKretLog log, Account account,
			LogProgressDialog logProgressDialog,
			LogSuccessfulListener listener, boolean force) {

		if (instance != null && !force) {
			return;
		}

		if (instance != null) {
			instance.cancel(true);
			instance = null;
		}

		instance = new LogGeoKret(log, account, logProgressDialog, listener);
		instance.execute();
	}
}
