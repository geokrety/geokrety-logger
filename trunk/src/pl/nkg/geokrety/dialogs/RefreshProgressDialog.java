package pl.nkg.geokrety.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.threads.RefreshAccount;
import pl.nkg.geokrety.widgets.RefreshSuccessfulListener;
import pl.nkg.lib.dialogs.GenericProgressDialog;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;

public class RefreshProgressDialog extends GenericProgressDialog {

	private ProgressDialog instance;
	private RefreshSuccessfulListener listener;

	public RefreshProgressDialog(ManagedDialogsActivity a,
			RefreshSuccessfulListener listener) {
		super(a, Dialogs.REFRESH_ACCOUNT_PROGRESSDIALOG,
				R.string.download_title, "");
		this.listener = listener;
	}

	@Override
	public Dialog create() {
		if (RefreshAccount.getInstance() == null) {
			return null;
		} else {
			instance = (ProgressDialog) super.create();

			return instance;
		}
	}

	@Override
	public void prepare(Dialog dialog) {
		super.prepare(dialog);
		if (RefreshAccount.getInstance() == null) {
			dialog.dismiss();
		} else {
			synchronized (RefreshAccount.getInstance()) {
				RefreshAccount.getInstance().updateActivities(this, listener);
			}
		}
	}

	public void setProgress(int value) {
		try {
			String message = "";
			switch (value) {
			case 0:
				message = getManagedDialogsActivity().getResources().getString(
						R.string.download_login_gk);
				break;

			case 1:
				message = getManagedDialogsActivity().getResources().getString(
						R.string.download_getting_gk);
				break;

			case 2:
				message = getManagedDialogsActivity().getResources().getString(
						R.string.download_getting_ocs);
				break;

			case 3:
				message = getManagedDialogsActivity().getResources().getString(
						R.string.download_getting_names);
				break;
			}
			instance.setMessage(message);
			setProgressMessage(message);
		} catch (Throwable t) {
			t.printStackTrace();
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
