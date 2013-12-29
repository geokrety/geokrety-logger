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
