/*
 * Copyright (C) 2013 Michał Niedźwiecki
 * 
 * This file is a part of GeoKrety Logger
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
import android.widget.AdapterView;
import android.widget.ListAdapter;

/**
 * Na podstawie książki: Android 2 Tworzenie aplikacji<br/>
 * English title: Pro Android 2<br/>
 * Authors: Sayed Hashimi, Satya Komatineni, Dave MacLean<br/>
 * ISBN: 978-83-246-2754-7
 */
public class AlertDialogWrapper extends AbstractAlertDialogWrapper<AlertDialog> {

    private static final String CHECKED = "checked";

    private Integer layoutId = null;
    private ListAdapter adapter = null;

    public AlertDialogWrapper(final ManagedDialogsActivity a, final int dialogId) {
        super(a, dialogId);
    }

    @Override
    public AlertDialog create() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
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

    public ListAdapter getAdapter() {
        return adapter;
    }

    public int getCheckedItem() {
        return getBundle().getInt(CHECKED, AdapterView.INVALID_POSITION);
    }

    public Integer getLayout() {
        return layoutId;
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        setCheckedItem(which);
        super.onClick(dialog, which);
        if (adapter != null) {
            getInstance().dismiss();
        }
    }

    @Override
    public void prepare(final AlertDialog dialog) {
        super.prepare(dialog);
        if (adapter != null) {
            getInstance().getListView().setAdapter(adapter);
            getInstance().getListView().setItemChecked(getCheckedItem(), true);
        }
    }

    public void setAdapter(final ListAdapter adapter) {
        this.adapter = adapter;
        if (getInstance() != null && getInstance().getListView() != null) {
            getInstance().getListView().setAdapter(adapter);
        }
    }

    public void setCheckedItem(final int checkedItem) {
        getBundle().putInt(CHECKED, checkedItem);
        if (getInstance() != null && adapter != null) {
            getInstance().getListView().setItemChecked(getCheckedItem(), true);
        }
    }

    public void setLayout(final Integer layoutId) {
        this.layoutId = layoutId;
    }

    public void show(final Serializable arg, final int checkedItem) {
        setCheckedItem(checkedItem);
        super.show(arg);
    }

    protected void buildLayout(final AlertDialog.Builder builder) {
        if (layoutId != null) {
            final LayoutInflater li = LayoutInflater
                    .from(getManagedDialogsActivity());
            final View promptView = li.inflate(layoutId, null);
            builder.setView(promptView);
            builder.setInverseBackgroundForced(true);
        }
    }
}
