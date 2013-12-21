package pl.nkg.geokrety.threads;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.data.Account;
import pl.nkg.geokrety.data.GeoKretLog;
import pl.nkg.lib.dialogs.AbstractProgressDialogWrapper;
import pl.nkg.lib.threads.AbstractForegroundTaskWrapper;
import pl.nkg.lib.threads.ForegroundTaskHandler;
import pl.nkg.lib.threads.TaskListener;
import android.content.Context;
import android.util.Pair;

public class LogGeoKret extends
		AbstractForegroundTaskWrapper<Pair<GeoKretLog, Account>, String, Boolean> {

	public static final int ID = 2;
	private String message = "";

	public LogGeoKret() {
		super(ID);
	}

	@Override
	protected Boolean runInBackground(Pair<GeoKretLog, Account> param)
			throws Throwable {
		GeoKretLog log = param.first;
		Account account = param.second;
		return log.submitBackground(account);
	}

	public static LogGeoKret getFromHandler(ForegroundTaskHandler handler) {
		AbstractForegroundTaskWrapper<?, ?, ?> a = handler.getTask(ID);
		LogGeoKret b = (LogGeoKret) a;
		return b;
	}
	
	@Override
	public void attach(
			AbstractProgressDialogWrapper<String> progressDialogWrapper,
			TaskListener<Pair<GeoKretLog, Account>, String, Boolean> listener) {
		super.attach(progressDialogWrapper, listener);
		Context ctx = progressDialogWrapper.getManagedDialogsActivity();
		message = ctx.getText(R.string.submit_message).toString();
		progressDialogWrapper.setProgress(message);
	}
}
