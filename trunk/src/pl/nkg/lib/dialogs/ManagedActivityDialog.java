package pl.nkg.lib.dialogs;

import java.io.Serializable;

import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Na podstawie ksi¹¿ki: Android 2 Tworzenie aplikacji<br/>
 * English title: Pro Android 2<br/>
 * Authors: Sayed Hashimi, Satya Komatineni, Dave MacLean<br/>
 * ISBN: 978-83-246-2754-7
 * 
 */
public abstract class ManagedActivityDialog implements IDialogProtocol,
		DialogInterface.OnClickListener {
	private ManagedDialogsActivity mActivity;
	private int mDialogId;
	private Serializable arg;

	public ManagedActivityDialog(ManagedDialogsActivity a, int dialogId) {
		mActivity = a;
		mDialogId = dialogId;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		onClickHook(which);
		mActivity.dialogFinished(this, which);
	}

	@Override
	public int getDialogId() {
		return mDialogId;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void show() {
		mActivity.showDialog(mDialogId);
	}

	public ManagedDialogsActivity getManagedDialogsActivity() {
		return mActivity;
	}

	public Serializable getArg() {
		return arg;
	}

	public void setArg(Serializable arg) {
		this.arg = arg;
	}

	@Override
	public void saveInstanceState(Bundle outState) {
		outState.putSerializable(getClass().getName() + "_arg", arg);
	}

	@Override
	public void restoreInstanceState(Bundle savedInstanceState) {
		arg = savedInstanceState.getSerializable(getClass().getName() + "_arg");
	}
}
