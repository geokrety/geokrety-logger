package pl.nkg.lib.dialogs;

import android.os.Bundle;
import pl.nkg.lib.dialogs.GenericLayoutDialog;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;

public class GenericAlertDialog extends GenericLayoutDialog {

	public GenericAlertDialog(ManagedDialogsActivity a, int dialogID,
			int title, String message) {
		super(a, dialogID, USE_DEFAULT_LAYOUT, title, message);
	}

	@Override
	public void saveInstanceState(Bundle outState) {
		outState.putString(this.getClass().getName() + "_message",
				getPromptMessage());
		super.saveInstanceState(outState);
	}

	@Override
	public void restoreInstanceState(Bundle savedInstanceState) {
		setPromptMessage(savedInstanceState.getString(this.getClass().getName()
				+ "_message"));
		super.restoreInstanceState(savedInstanceState);
	}
}
