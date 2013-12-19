package pl.nkg.geokrety.dialogs;

import pl.nkg.geokrety.R;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;

public class EditAccountDialog extends AccountDialog {

	public EditAccountDialog(ManagedDialogsActivity a) {
		super(a, Dialogs.EDIT_ACCOUNT_PROMPTDIALOG,
				R.string.accounts_edit_title);
	}
}
