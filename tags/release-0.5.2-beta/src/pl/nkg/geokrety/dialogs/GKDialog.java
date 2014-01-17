/*
 * Copyright (C) 2013 Michał Niedźwiecki
 * 
 * This file is part of GeoKrety Logger
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

public class GKDialog extends AlertDialogWrapper {

	private String mGKLogin = "";
	private String mGKPassword = "";

	public GKDialog(ManagedDialogsActivity a) {
		super(a, Dialogs.GK_PROMPTDIALOG);
		setTitle(R.string.title_dialog_gk);
		setLayout(R.layout.dialog_gk);
		setOkCancelButtons();
	}

	@Override
	protected boolean onValidate(Dialog dialog) {
		mGKLogin = ((EditText) dialog.findViewById(R.id.loginEditText))
				.getText().toString();
		mGKPassword = ((EditText) dialog.findViewById(R.id.passwordEditText))
				.getText().toString();

		if (mGKLogin.length() == 0) {
			showToast(R.string.error_login_null);
			return false;
		}

		if (mGKPassword.length() == 0) {
			showToast(R.string.error_password_null);
			return false;
		}

		return true;
	}

	@Override
	public void prepare(AlertDialog dialog) {
		((EditText) dialog.findViewById(R.id.loginEditText)).setText(mGKLogin);
		((EditText) dialog.findViewById(R.id.passwordEditText))
				.setText(mGKPassword);
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

	public void clearValues() {
		mGKLogin = "";
		mGKPassword = "";
	}
	
	public void show(Serializable arg, String login) {
		setGKLogin(login);
		show(arg);
	}
}
