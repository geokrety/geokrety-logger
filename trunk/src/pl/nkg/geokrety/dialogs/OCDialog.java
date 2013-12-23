package pl.nkg.geokrety.dialogs;

import java.io.Serializable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.widget.EditText;
import android.widget.Toast;
import pl.nkg.geokrety.R;
import pl.nkg.lib.dialogs.AlertDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;

public class OCDialog extends AlertDialogWrapper {

	private String mOCLogin = "";

	public OCDialog(ManagedDialogsActivity a) {
		super(a, Dialogs.OC_PROMPTDIALOG);
		setLayout(R.layout.dialog_oc);
		setOkCancelButtons();
	}

	@Override
	protected boolean onValidate(Dialog dialog) {
		mOCLogin = ((EditText) dialog.findViewById(R.id.ocEditText)).getText()
				.toString();

		if (mOCLogin.length() == 0) {
			showToast(R.string.error_oc_null);
			return false;
		}

		return true;
	}

	@Override
	public void prepare(AlertDialog dialog) {
		((EditText) dialog.findViewById(R.id.ocEditText)).setText(mOCLogin);
		super.prepare(dialog);
	}

	private void showToast(int stringID) {
		Toast.makeText(getManagedDialogsActivity(), stringID,
				Toast.LENGTH_SHORT).show();
	}

	public String getOCLogin() {
		return mOCLogin;
	}

	public void setOCLogin(String ocLogin) {
		this.mOCLogin = ocLogin;
	}

	public void clearValues() {
		mOCLogin = "";
	}

	public void show(Serializable arg, String title, String login) {
		setTitle(getManagedDialogsActivity().getText(R.string.title_dialog_gk)
				+ " " + title);
		setOCLogin(login);
		super.show(arg);
	}
}
