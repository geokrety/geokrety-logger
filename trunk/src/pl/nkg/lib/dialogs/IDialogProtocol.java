package pl.nkg.lib.dialogs;

import android.app.Dialog;
import android.os.Bundle;

/**
 * Na podstawie ksi¹¿ki: Android 2 Tworzenie aplikacji<br/>
 * English title: Pro Android 2<br/>
 * Authors: Sayed Hashimi, Satya Komatineni, Dave MacLean<br/>
 * ISBN: 978-83-246-2754-7
 * 
 */
public interface IDialogProtocol {
	public Dialog create();

	public void prepare(Dialog dialog);

	public int getDialogId();

	public void show();

	public void onClickHook(int buttonId);

	public void saveInstanceState(Bundle outState);

	public void restoreInstanceState(Bundle savedInstanceState);
}
