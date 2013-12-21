package pl.nkg.geokrety.dialogs;

import java.io.Serializable;

import pl.nkg.geokrety.R;
import pl.nkg.lib.dialogs.AlertDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;

public class RemoveAccountDialog extends AlertDialogWrapper {

	private static final String POSITION = "position";

	public int getPosition() {
		return getBundle().getInt(POSITION);
	}

	public void setPosition(int position) {
		getBundle().putInt(POSITION, position);
	}

	public RemoveAccountDialog(ManagedDialogsActivity a) {
		super(a, Dialogs.REMOVE_ACCOUNT_ALERTDIALOG);
		setTitle(R.string.account_remove);
		setOkCancelButtons();
	}

	public void show(Serializable arg, String accountName, int position) {
		setPosition(position);
		setMessage(getManagedDialogsActivity().getText(
				R.string.accounts_remove_question)
				+ " " + accountName + "?");
		show(arg);
	}
}
