package pl.nkg.geokrety.dialogs;

import pl.nkg.geokrety.R;
import pl.nkg.lib.dialogs.GenericProgressDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;

public class RefreshProgressDialog extends GenericProgressDialogWrapper {

	public RefreshProgressDialog(ManagedDialogsActivity a) {
		super(a, Dialogs.REFRESH_ACCOUNT_PROGRESSDIALOG);
		setTitle(R.string.download_title);
		setMessage("");
	}
}
