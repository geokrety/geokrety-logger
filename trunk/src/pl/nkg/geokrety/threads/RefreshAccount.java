package pl.nkg.geokrety.threads;

import java.util.ArrayList;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.data.GeocacheLog;
import pl.nkg.lib.dialogs.AbstractProgressDialogWrapper;
import pl.nkg.lib.okapi.SupportedOKAPI;
import pl.nkg.lib.threads.AbstractForegroundTaskWrapper;
import pl.nkg.lib.threads.ForegroundTaskHandler;
import pl.nkg.lib.threads.TaskListener;
import android.content.Context;

public class RefreshAccount extends
		AbstractForegroundTaskWrapper<Account, String, Boolean> {

	public static final int ID = 1;
	private String[] messages;

	public RefreshAccount() {
		super(ID);
	}

	@Override
	protected Boolean runInBackground(Account param) throws Throwable {
		Account account = param;

		publishProgress(getProgressMessage(0));
		account.loadInventory(this);
		ArrayList<GeocacheLog> openCachingLogs = new ArrayList<GeocacheLog>();
		for (int i = 0; i < SupportedOKAPI.SUPPORTED.length; i++) {
			if (account.hasOpenCachingUUID(i)) {
				publishProgress(getProgressMessage(1) + " "
						+ SupportedOKAPI.SUPPORTED[i].host + messages[2]);
				account.loadOpenCachingLogs(this, openCachingLogs, i);
			}
		}
		account.setOpenCachingLogs(openCachingLogs);
		account.touchLastLoadedDate();
		return true;
	}

	private String getProgressMessage(int step) {
		synchronized (this) {
			if (messages == null) {
				return "";
			}
			return messages[step];
		}
	}

	@Override
	public void attach(
			AbstractProgressDialogWrapper<String> progressDialogWrapper,
			TaskListener<Account, String, Boolean> listener) {
		super.attach(progressDialogWrapper, listener);

		synchronized (this) {
			Context ctx = progressDialogWrapper.getManagedDialogsActivity();
			messages = new String[3];
			messages[0] = ctx.getText(R.string.download_getting_gk).toString();
			messages[1] = ctx.getText(R.string.download_getting_ocs)
					.toString();
			messages[2] = ctx.getText(R.string.dots).toString();
		}
	}

	public static RefreshAccount getFromHandler(ForegroundTaskHandler handler) {
		AbstractForegroundTaskWrapper<?, ?, ?> a = handler.getTask(ID);
		RefreshAccount b = (RefreshAccount) a;
		return b;
	}
}
