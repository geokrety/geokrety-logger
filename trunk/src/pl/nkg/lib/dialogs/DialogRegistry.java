package pl.nkg.lib.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.util.SparseArray;

/**
 * Na podstawie książki: Android 2 Tworzenie aplikacji<br/>
 * English title: Pro Android 2<br/>
 * Authors: Sayed Hashimi, Satya Komatineni, Dave MacLean<br/>
 * ISBN: 978-83-246-2754-7
 * 
 */
public class DialogRegistry {
	SparseArray<IDialogProtocol<? extends Dialog>> idsToDialogs = new SparseArray<IDialogProtocol<?>>();

	public void registerDialog(IDialogProtocol<?> dialog) {
		idsToDialogs.put(dialog.getDialogId(), dialog);
	}

	public Dialog create(int id) {
		IDialogProtocol<?> dp = idsToDialogs.get(id);
		if (dp == null) {
			return null;
		}
		return dp.create();
	}

	
	@SuppressWarnings("unchecked")
	public void prepare(Dialog dialog, int id) {
		@SuppressWarnings("rawtypes")
		IDialogProtocol dp = idsToDialogs.get(id);
		if (dp == null) {
			throw new RuntimeException("Dialog id is not registered: " + id);
		}
		dp.prepare(dialog);
	}

	public void saveInstanceState(Bundle outState) {
		for (int i = 0; i < idsToDialogs.size(); i++) {
			idsToDialogs.get(idsToDialogs.keyAt(i)).saveInstanceState(outState);
		}
	}

	public void restoreInstanceState(Bundle savedInstanceState) {
		for (int i = 0; i < idsToDialogs.size(); i++) {
			idsToDialogs.get(idsToDialogs.keyAt(i)).restoreInstanceState(
					savedInstanceState);
		}
	}
}
