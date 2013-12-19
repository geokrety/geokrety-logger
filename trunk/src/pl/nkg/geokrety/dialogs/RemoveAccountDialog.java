package pl.nkg.geokrety.dialogs;

import pl.nkg.geokrety.R;
import pl.nkg.lib.dialogs.GenericAlertDialog;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;

public class RemoveAccountDialog extends GenericAlertDialog {

	public RemoveAccountDialog(ManagedDialogsActivity a) {
		super(a, Dialogs.REMOVE_ACCOUNT_ALERTDIALOG, R.string.account_remove,
				"");
	}

	public void show(String accountName) {
		setPromptMessage(getManagedDialogsActivity().getResources().getString(
				R.string.accounts_remove_question)
				+ " " + accountName + "?");
		show();
	}

}
