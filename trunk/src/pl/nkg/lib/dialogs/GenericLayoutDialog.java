package pl.nkg.lib.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Na podstawie ksi¹¿ki: Android 2 Tworzenie aplikacji<br/>
 * English title: Pro Android 2<br/>
 * Authors: Sayed Hashimi, Satya Komatineni, Dave MacLean<br/>
 * ISBN: 978-83-246-2754-7
 * 
 */
public class GenericLayoutDialog extends ManagedActivityDialog {

	public static final int USE_DEFAULT_LAYOUT = -1;
	public static final String NO_USE_MESSAGE = null;

	private String mPromptMessage = null;
	private int mPromptTitleID;
	private int mLayoutId;
	protected Context ctx = null;
	private boolean clicked = false;

	public GenericLayoutDialog(ManagedDialogsActivity a, int dialogId,
			int layoutId, int title, String promptMessage) {
		super(a, dialogId);
		mLayoutId = layoutId;
		mPromptMessage = promptMessage;
		mPromptTitleID = title;
		ctx = a;
	}

	@Override
	public Dialog create() {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(mPromptTitleID);

		if (mLayoutId != USE_DEFAULT_LAYOUT) {
			LayoutInflater li = LayoutInflater.from(ctx);
			View promptView = li.inflate(mLayoutId, null);
			builder.setView(promptView);
		}

		if (mPromptMessage != NO_USE_MESSAGE) {
			builder.setMessage(mPromptMessage);
		}

		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, this);
		AlertDialog ad = builder.create();
		return ad;
	}

	@Override
	public void prepare(final Dialog dialog) {
		((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE)
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (!clicked && onValidate(dialog)) {
							clicked = true;
							GenericLayoutDialog.this.onClick(dialog,
									DialogInterface.BUTTON_POSITIVE);
							dialog.dismiss();
						}
					}
				});

		if (mPromptMessage != NO_USE_MESSAGE) {
			((AlertDialog) dialog).setMessage(mPromptMessage);
		}
	}

	@Override
	public void onClickHook(int buttonId) {
	}

	protected boolean onValidate(Dialog dialog) {
		return true;
	}

	public String getPromptMessage() {
		return mPromptMessage;
	}

	public void setPromptMessage(String promptMessage) {
		this.mPromptMessage = promptMessage;
	}

	@Override
	public void show() {
		clicked = false;
		super.show();
	}
}
