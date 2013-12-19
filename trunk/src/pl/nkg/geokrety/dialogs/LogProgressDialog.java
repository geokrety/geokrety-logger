package pl.nkg.geokrety.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.threads.LogGeoKret;
import pl.nkg.geokrety.widgets.LogSuccessfulListener;
import pl.nkg.lib.dialogs.GenericProgressDialog;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;

public class LogProgressDialog extends GenericProgressDialog {

	private ProgressDialog instance;
	private LogSuccessfulListener listener;

	public LogProgressDialog(ManagedDialogsActivity a,
			LogSuccessfulListener listener) {
		super(a, Dialogs.LOG_PROGRESSDIALOG, R.string.submit_title,
				a.getResources().getString(R.string.submit_message));
		this.listener = listener;
	}

	@Override
	public Dialog create() {
		if (LogGeoKret.getInstance() == null) {
			return null;
		} else {
			instance = (ProgressDialog) super.create();

			return instance;
		}
	}

	@Override
	public void prepare(Dialog dialog) {
		super.prepare(dialog);
		if (LogGeoKret.getInstance() == null) {
			dialog.dismiss();
		} else {
			synchronized (LogGeoKret.getInstance()) {
				LogGeoKret.getInstance().updateActivities(this, listener);
			}
		}
	}

	public void dismiss() {
		try {
			instance.dismiss();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
