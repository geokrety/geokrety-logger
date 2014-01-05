/*
 * Copyright (C) 2013 Michał Niedźwiecki
 * 
 * This file is part of GeoKrety Logger
 * http://geokretylog.sourceforge.net/
 * 
 * GeoKrety Logger is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * or see <http://www.gnu.org/licenses/>
 */
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
public class AlertDialogWrapper extends AbstractAlertDialogWrapper<AlertDialog> {

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

		if (hasTitle()) {
			builder.setTitle(getTitle());
		}

		if (hasMessage()) {
			builder.setMessage(getMessage());
		}

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
		if (getInstance() != null && getInstance().getListView() != null) {
			getInstance().getListView().setAdapter(adapter);
		}
	}

	public int getCheckedItem() {
		return getBundle().getInt(CHECKED, ListView.INVALID_POSITION);
	}

	public void setCheckedItem(int checkedItem) {
		getBundle().putInt(CHECKED, checkedItem);
		if (getInstance() != null && adapter != null) {
			getInstance().getListView().setItemChecked(getCheckedItem(), true);
		}
	}

	@Override
	public void prepare(AlertDialog dialog) {
		super.prepare(dialog);
		if (adapter != null) {
			getInstance().getListView().setAdapter(adapter);
			getInstance().getListView().setItemChecked(getCheckedItem(), true);
		}
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
