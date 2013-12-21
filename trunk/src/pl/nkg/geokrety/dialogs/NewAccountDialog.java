package pl.nkg.geokrety.dialogs;

import java.io.Serializable;

import pl.nkg.geokrety.R;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;

public class NewAccountDialog extends AccountDialog {

	public NewAccountDialog(ManagedDialogsActivity a) {
		super(a, Dialogs.NEW_ACCOUNT_PROMPTDIALOG, R.string.accounts_add_title);
	}
	
	@Override
	public void show(Serializable arg) {
		setGKLogin("");
		setGKPassword("");
		setOCLogin("");
		super.show(arg);
	}
}
