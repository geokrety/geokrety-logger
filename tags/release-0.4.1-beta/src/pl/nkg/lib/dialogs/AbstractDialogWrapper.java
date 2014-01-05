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

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;

public abstract class AbstractDialogWrapper<D extends Dialog> implements
		IDialogProtocol<D>, DialogInterface.OnClickListener {

	private static final String TITLE = "title";
	private static final String ARG = "arg";

	private ManagedDialogsActivity mActivity;
	private int mDialogId;
	private Bundle bundle;
	private final String bundleName;

	private D instance;

	public AbstractDialogWrapper(ManagedDialogsActivity a, int dialogId) {
		mActivity = a;
		mDialogId = dialogId;
		bundleName = getClass().getName() + "_" + mDialogId + "_arg";
		bundle = new Bundle();
		a.registerDialog(this);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		mActivity.dialogFinished(this, which, getArg());
	}

	@Override
	public int getDialogId() {
		return mDialogId;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void show(Serializable arg) {
		setArg(arg);
		mActivity.showDialog(mDialogId);
	}

	public ManagedDialogsActivity getManagedDialogsActivity() {
		return mActivity;
	}

	public Bundle getBundle() {
		return bundle;
	}

	@Override
	public void saveInstanceState(Bundle outState) {
		outState.putBundle(bundleName, bundle);
	}

	@Override
	public void restoreInstanceState(Bundle savedInstanceState) {
		bundle = savedInstanceState.getBundle(bundleName);
	}

	@Override
	public void prepare(D dialog) {
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				AbstractDialogWrapper.this.onDismiss(dialog);
			}
		});
		dialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				AbstractDialogWrapper.this.onCancel(dialog);
			}
		});
		instance = dialog;
		if (hasTitle()) {
			dialog.setTitle(getTitle());
		}
	}

	protected void onDismiss(DialogInterface dialog) {
		instance = null;
	}

	protected void onCancel(DialogInterface dialog) {
	}

	public CharSequence getTitle() {
		return getBundle().getCharSequence(TITLE);
	}
	
	public boolean hasTitle() {
		return getTitle() != null;
	}

	public void setTitle(int titleID) {
		setTitle(mActivity.getText(titleID));
	}

	public void setTitle(CharSequence title) {
		getBundle().putCharSequence(TITLE, title);
		if (instance != null) {
			instance.setTitle(title);
		}
	}

	public interface OnClickListener<D extends Dialog> {
		void onClicks(AbstractDialogWrapper<D> sender, int buttonID);
	}

	protected Serializable getArg() {
		return getBundle().getSerializable(ARG);
	}

	protected void setArg(Serializable arg) {
		getBundle().putSerializable(ARG, arg);
	}

	public D getInstance() {
		return instance;
	}
}
