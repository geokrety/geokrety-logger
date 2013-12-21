package pl.nkg.lib.dialogs;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListAdapter;

/**
 * Na podstawie książki: Android 2 Tworzenie aplikacji<br/>
 * English title: Pro Android 2<br/>
 * Authors: Sayed Hashimi, Satya Komatineni, Dave MacLean<br/>
 * ISBN: 978-83-246-2754-7
 * 
 */
public class AlertDialogWrapper extends
		AbstractAlertDialogWrapper<AlertDialog> {

	private Integer layoutId = null;
	private ListAdapter adapter = null;

	public AlertDialogWrapper(ManagedDialogsActivity a, int dialogId) {
		super(a, dialogId);
	}

	protected void buildLayout(AlertDialog.Builder builder) {
		if (layoutId != null) {
			LayoutInflater li = LayoutInflater
					.from(getManagedDialogsActivity());
			View promptView = li.inflate(layoutId, null);
			builder.setView(promptView);
		}
	}

	@Override
	public AlertDialog create() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				getManagedDialogsActivity());

		builder.setTitle(getTitle());
		builder.setMessage(getMessage());
		builder.setCancelable(isCancelable());

		if (hasPositiveButtton()) {
			builder.setPositiveButton(getPositiveButton(), this);
		}

		if (hasNegativeButtton()) {
			builder.setNegativeButton(getNegativeButton(), this);
		}

		if (hasNeutralButtton()) {
			builder.setNeutralButton(getNeutralButton(), this);
		}

		if (adapter != null) {
			builder.setAdapter(adapter, this);
		}

		buildLayout(builder);

		return builder.create();
	}

	public Integer getLayout() {
		return layoutId;
	}

	public void setLayout(Integer layoutId) {
		this.layoutId = layoutId;
	}

	public ListAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(ListAdapter adapter) {
		this.adapter = adapter;
	}
}
