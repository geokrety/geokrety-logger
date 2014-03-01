/*
 * Copyright (C) 2014 Michał Niedźwiecki
 * 
 * This file is a part of GeoKrety Logger
 * http://geokretylog.sourceforge.net/
 * 
 * GeoKrety Logger is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * or see <http://www.gnu.org/licenses/>
 */
package pl.nkg.geokrety.dialogs;

import java.io.Serializable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.widget.EditText;
import android.widget.Toast;
import pl.nkg.geokrety.R;
import pl.nkg.lib.dialogs.AlertDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;

public class GCDialog extends AlertDialogWrapper {

	private String mLogin = "";
	private String mPassword = "";

	public GCDialog(ManagedDialogsActivity a) {
		super(a, Dialogs.GC_PROMPTDIALOG);
		setTitle(R.string.user_gc_title);
		setLayout(R.layout.dialog_gc);
		setOkCancelButtons();
	}

	@Override
	protected boolean onValidate(Dialog dialog) {
		mLogin = ((EditText) dialog.findViewById(R.id.loginEditText))
				.getText().toString();
		mPassword = ((EditText) dialog.findViewById(R.id.passwordEditText))
				.getText().toString();

		if (mLogin.length() == 0) {
			showToast(R.string.user_gc_error_login_null);
			return false;
		}

		if (mPassword.length() == 0) {
			showToast(R.string.user_gc_error_password_null);
			return false;
		}

		return true;
	}

	@Override
	public void prepare(AlertDialog dialog) {
		((EditText) dialog.findViewById(R.id.loginEditText)).setText(mLogin);
		((EditText) dialog.findViewById(R.id.passwordEditText))
				.setText(mPassword);
		super.prepare(dialog);
	}

	private void showToast(int stringID) {
		Toast.makeText(getManagedDialogsActivity(), stringID, Toast.LENGTH_SHORT).show();
	}

	public String getLogin() {
		return mLogin;
	}

	public void setLogin(String login) {
		this.mLogin = login;
	}

	public String getPassword() {
		return mPassword;
	}

	public void setGKPassword(String password) {
		this.mPassword = password;
	}

	public void clearValues() {
		mLogin = "";
		mPassword = "";
	}
	
	public void show(Serializable arg, String login) {
		setLogin(login);
		show(arg);
	}
}
