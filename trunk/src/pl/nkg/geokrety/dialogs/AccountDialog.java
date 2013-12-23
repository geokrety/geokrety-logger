package pl.nkg.geokrety.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.widget.EditText;
import android.widget.Toast;
import pl.nkg.geokrety.R;
import pl.nkg.lib.dialogs.AlertDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;

public abstract class AccountDialog extends AlertDialogWrapper {

	private String mGKLogin = "";
	private String mGKPassword = "";
	private String mOCLogin = "";

	public AccountDialog(ManagedDialogsActivity a, int dialogId, int title) {
		super(a, dialogId);
		setTitle(title);
		setLayout(R.layout.dialog_account);
		setOkCancelButtons();
	}

	@Override
	protected boolean onValidate(Dialog dialog) {
		mGKLogin = ((EditText) dialog.findViewById(R.id.loginEditText))
				.getText().toString();
		mGKPassword = ((EditText) dialog.findViewById(R.id.passwordEditText))
				.getText().toString();
		mOCLogin = ((EditText) dialog.findViewById(R.id.ocEditText)).getText()
				.toString();

		if (mGKLogin.length() == 0) {
			showToast(R.string.error_login_null);
			return false;
		}

		if (mGKPassword.length() == 0) {
			showToast(R.string.error_password_null);
			return false;
		}

		if (mOCLogin.length() == 0) {
			showToast(R.string.error_oc_null);
			return false;
		}

		return true;
	}

	@Override
	public void prepare(AlertDialog dialog) {
		((EditText) dialog.findViewById(R.id.loginEditText)).setText(mGKLogin);
		((EditText) dialog.findViewById(R.id.passwordEditText))
				.setText(mGKPassword);
		((EditText) dialog.findViewById(R.id.ocEditText)).setText(mOCLogin);
		super.prepare(dialog);
	}

	private void showToast(int stringID) {
		Toast.makeText(getManagedDialogsActivity(), stringID, Toast.LENGTH_SHORT).show();
	}

	public String getGKLogin() {
		return mGKLogin;
	}

	public void setGKLogin(String gkLogin) {
		this.mGKLogin = gkLogin;
	}

	public String getGKPassword() {
		return mGKPassword;
	}

	public void setGKPassword(String gkPassword) {
		this.mGKPassword = gkPassword;
	}

	public String getOCLogin() {
		return mOCLogin;
	}

	public void setOCLogin(String ocLogin) {
		this.mOCLogin = ocLogin;
	}

	public void clearValues() {
		mGKLogin = "";
		mGKPassword = "";
		mOCLogin = "";
	}
}
