/*
 * Copyright (C) 2013 Michał Niedźwiecki
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
			showToast(R.string.user_oc_error_login_null);
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
		setTitle(getManagedDialogsActivity().getText(R.string.user_oc_title)
				+ " " + title);
		setOCLogin(login);
		super.show(arg);
	}
}
