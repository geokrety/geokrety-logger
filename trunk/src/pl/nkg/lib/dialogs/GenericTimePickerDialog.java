package pl.nkg.lib.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;

// TODO: to jest tylko zaczête
public class GenericTimePickerDialog extends ManagedActivityDialog {

	private String mMessage = null;
	private int mTitleID;
	protected Context ctx = null;
	private int hourOfDay;
	private int minute;
	private boolean is24HourView;
	
	private TimePickerDialog.OnTimeSetListener listener;

	public GenericTimePickerDialog(ManagedDialogsActivity a, int dialogId,
			int title, String Message) {
		super(a, dialogId);
		mMessage = Message;
		mTitleID = title;
		ctx = a;
	}

	@Override
	public Dialog create() {
		TimePickerDialog dialog = new TimePickerDialog(ctx, listener, hourOfDay, minute, is24HourView);
		dialog.setTitle(mTitleID);
		dialog.setMessage(mMessage);
		return dialog;
	}

	@Override
	public void prepare(final Dialog dialog) {
		((TimePickerDialog) dialog).setMessage(mMessage);
	}

	@Override
	public void onClickHook(int buttonId) {
	}

	protected boolean onValidate(Dialog dialog) {
		return true;
	}

	public String getMessage() {
		return mMessage;
	}

	public void setMessage(String Message) {
		this.mMessage = Message;
	}

	@Override
	public void saveInstanceState(Bundle outState) {
		outState.putString(this.getClass().getName() + "_message",
				getMessage());
		super.saveInstanceState(outState);
	}

	@Override
	public void restoreInstanceState(Bundle savedInstanceState) {
		setMessage(savedInstanceState.getString(this.getClass()
				.getName() + "_message"));
		super.restoreInstanceState(savedInstanceState);
	}
}
