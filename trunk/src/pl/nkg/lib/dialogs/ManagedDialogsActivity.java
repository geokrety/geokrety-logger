package pl.nkg.lib.dialogs;

import java.io.Serializable;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

/**
 * Na podstawie książki: Android 2 Tworzenie aplikacji<br/>
 * English title: Pro Android 2<br/>
 * Authors: Sayed Hashimi, Satya Komatineni, Dave MacLean<br/>
 * ISBN: 978-83-246-2754-7
 * 
 */
public abstract class ManagedDialogsActivity extends Activity implements
		IDialogFinishedCallBack {

	private DialogRegistry dr = new DialogRegistry();

	public void registerDialog(IDialogProtocol<?> dialog) {
		dr.registerDialog(dialog);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return dr.create(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		dr.prepare(dialog, id);
	}

	@Override
	abstract public void dialogFinished(AbstractDialogWrapper<?> dialog,
			int buttonId, Serializable arg);

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		dr.saveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		dr.restoreInstanceState(savedInstanceState);
	}
}
