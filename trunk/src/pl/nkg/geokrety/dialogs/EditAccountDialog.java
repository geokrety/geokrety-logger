package pl.nkg.geokrety.dialogs;

import java.io.Serializable;

import pl.nkg.geokrety.R;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;

public class EditAccountDialog extends AccountDialog {

	private static final String POSITION = "position";

	public int getPosition() {
		return getBundle().getInt(POSITION);
	}

	public void setPosition(int position) {
		getBundle().putInt(POSITION, position);
	}

	public EditAccountDialog(ManagedDialogsActivity a) {
		super(a, Dialogs.EDIT_ACCOUNT_PROMPTDIALOG,
				R.string.accounts_edit_title);
	}

	public void show(Serializable arg, String gkLogin, String gkPassword,
			String ocLogin, int position) {
		setPosition(position);
		setGKLogin(gkLogin);
		setGKPassword(gkPassword);
		setOCLogin(ocLogin);
		show(arg);
	}
}
