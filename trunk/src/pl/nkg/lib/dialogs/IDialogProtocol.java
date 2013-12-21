package pl.nkg.lib.dialogs;

import java.io.Serializable;

import android.app.Dialog;
import android.os.Bundle;

/**
 * Na podstawie książki: Android 2 Tworzenie aplikacji<br/>
 * English title: Pro Android 2<br/>
 * Authors: Sayed Hashimi, Satya Komatineni, Dave MacLean<br/>
 * ISBN: 978-83-246-2754-7
 * 
 */
public interface IDialogProtocol<D extends Dialog> {
	public D create();

	public void prepare(D dialog);

	public int getDialogId();

	public void show(Serializable arg);

	public void saveInstanceState(Bundle outState);

	public void restoreInstanceState(Bundle savedInstanceState);
}
