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
package pl.nkg.lib.dialogs;

import java.io.Serializable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;

public abstract class AbstractAlertDialogWrapper<D extends AlertDialog> extends
		AbstractDialogWrapper<D> {

	private static final String MESSAGE = "message";
	private CharSequence positiveButton = null;
	private CharSequence negativeButton = null;
	private CharSequence neutralButton = null;
	private boolean cancelable = true;

	private boolean clicked = false;

	public AbstractAlertDialogWrapper(ManagedDialogsActivity a, int dialogId) {
		super(a, dialogId);
	}

	public CharSequence getMessage() {
		return getBundle().getCharSequence(MESSAGE);
	}

	public boolean isCancelable() {
		return cancelable;
	}

	public void setCancelable(boolean cancelable) {
		this.cancelable = cancelable;
	}

	public void setMessage(CharSequence message) {
		getBundle().putCharSequence(MESSAGE, message);
		if (getInstance() != null) {
			getInstance().setMessage(message);
		}
	}

	public void setMessage(int messageID) {
		setMessage(getManagedDialogsActivity().getString(messageID));
	}
	
	public boolean hasMessage() {
		return getMessage() != null;
	}

	public CharSequence getPositiveButton() {
		return positiveButton;
	}

	public void setPositiveButton(CharSequence positiveButton) {
		this.positiveButton = positiveButton;
	}

	public CharSequence getNegativeButton() {
		return negativeButton;
	}

	public void setNegativeButton(CharSequence negativeButton) {
		this.negativeButton = negativeButton;
	}

	public CharSequence getNeutralButton() {
		return neutralButton;
	}

	public void setNeutralButton(CharSequence neutralButton) {
		this.neutralButton = neutralButton;
	}

	public boolean hasPositiveButtton() {
		return positiveButton != null;
	}

	public boolean hasNegativeButtton() {
		return negativeButton != null;
	}

	public boolean hasNeutralButtton() {
		return neutralButton != null;
	}

	protected boolean onValidate(Dialog dialog) {
		return true;
	}

	@Override
	public void prepare(final D dialog) {
		super.prepare(dialog);
		if (hasMessage()) {
			dialog.setMessage(getMessage());
		}
		dialog.setCancelable(isCancelable());
		if (hasPositiveButtton()) {
			((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE)
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							if (!clicked && onValidate(dialog)) {
								clicked = true;
								AbstractAlertDialogWrapper.this.onClick(dialog,
										DialogInterface.BUTTON_POSITIVE);
								dialog.dismiss();
							}
						}
					});
		}
	}

	@Override
	public void show(Serializable arg) {
		clicked = false;
		super.show(arg);
	}

	public void setOkCancelButtons() {
		setPositiveButton(getManagedDialogsActivity().getText(
				android.R.string.ok));
		setNegativeButton(getManagedDialogsActivity().getText(
				android.R.string.cancel));
	}
}
