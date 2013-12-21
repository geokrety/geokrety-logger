package pl.nkg.lib.dialogs;

import java.io.Serializable;

/**
 * Na podstawie książki: Android 2 Tworzenie aplikacji<br/>
 * English title: Pro Android 2<br/>
 * Authors: Sayed Hashimi, Satya Komatineni, Dave MacLean<br/>
 * ISBN: 978-83-246-2754-7
 * 
 */
public interface IDialogFinishedCallBack {
	public void dialogFinished(AbstractDialogWrapper<?> dialog, int buttonId, Serializable arg);
}
