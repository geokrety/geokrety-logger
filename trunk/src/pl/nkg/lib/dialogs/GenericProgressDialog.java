package pl.nkg.lib.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

public class GenericProgressDialog extends ManagedActivityDialog {

	private String mProgressMessage = null;
	private int mProgressTitleID;
	protected Context ctx = null;

	public GenericProgressDialog(ManagedDialogsActivity a, int dialogId,
			int title, String progressMessage) {
		super(a, dialogId);
		mProgressMessage = progressMessage;
		mProgressTitleID = title;
		ctx = a;
	}

	@Override
	public Dialog create() {
		ProgressDialog progress = new ProgressDialog(ctx);
		progress.setCancelable(false);
		progress.setIndeterminate(true);
		progress.setTitle(mProgressTitleID);
		progress.setMessage(mProgressMessage);
		return progress;
	}

	@Override
	public void prepare(final Dialog dialog) {
		((ProgressDialog) dialog).setMessage(mProgressMessage);
	}

	@Override
	public void onClickHook(int buttonId) {
	}

	protected boolean onValidate(Dialog dialog) {
		return true;
	}

	public String getProgressMessage() {
		return mProgressMessage;
	}

	public void setProgressMessage(String progressMessage) {
		this.mProgressMessage = progressMessage;
	}

	@Override
	public void saveInstanceState(Bundle outState) {
		outState.putString(this.getClass().getName() + "_message",
				getProgressMessage());
		super.saveInstanceState(outState);
	}

	@Override
	public void restoreInstanceState(Bundle savedInstanceState) {
		setProgressMessage(savedInstanceState.getString(this.getClass()
				.getName() + "_message"));
		super.restoreInstanceState(savedInstanceState);
	}
}
