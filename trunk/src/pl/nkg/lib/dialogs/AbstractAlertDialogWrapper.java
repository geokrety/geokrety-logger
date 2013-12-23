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
