package pl.nkg.lib.dialogs;

import java.io.Serializable;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Na podstawie książki: Android 2 Tworzenie aplikacji<br/>
 * English title: Pro Android 2<br/>
 * Authors: Sayed Hashimi, Satya Komatineni, Dave MacLean<br/>
 * ISBN: 978-83-246-2754-7
 * 
 */
public class AlertDialogWrapper extends
		AbstractAlertDialogWrapper<AlertDialog> {

	private static final String CHECKED = "checked";
	
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
			builder.setSingleChoiceItems(adapter, getCheckedItem(), this);
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

	public int getCheckedItem() {
		return getBundle().getInt(CHECKED, ListView.INVALID_POSITION);
	}

	public void setCheckedItem(int checkedItem) {
		getBundle().putInt(CHECKED, checkedItem);
		if (getInstance() != null) {
			getInstance().getListView().setItemChecked(getCheckedItem(), true);
		}
	}
	
	@Override
	public void prepare(AlertDialog dialog) {
		super.prepare(dialog);
		getInstance().getListView().setItemChecked(getCheckedItem(), true);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		setCheckedItem(which);
		super.onClick(dialog, which);
		if (adapter != null) {
			getInstance().dismiss();
		}
	}
	
	public void show(Serializable arg, int checkedItem) {
		setCheckedItem(checkedItem);
		super.show(arg);
	}
}
