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

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;

public abstract class AbstractDialogWrapper<D extends Dialog> implements
        IDialogProtocol<D>, DialogInterface.OnClickListener {

    public interface OnClickListener<D extends Dialog> {
        void onClicks(AbstractDialogWrapper<D> sender, int buttonID);
    }

    private static final String TITLE = "title";

    private static final String ARG = "arg";
    private final ManagedDialogsActivity mActivity;
    private final int mDialogId;
    private Bundle bundle;

    private final String bundleName;

    private D instance;

    public AbstractDialogWrapper(final ManagedDialogsActivity a, final int dialogId) {
        mActivity = a;
        mDialogId = dialogId;
        bundleName = getClass().getName() + "_" + mDialogId + "_arg";
        bundle = new Bundle();
        a.registerDialog(this);
    }

    public Bundle getBundle() {
        return bundle;
    }

    @Override
    public int getDialogId() {
        return mDialogId;
    }

    public D getInstance() {
        return instance;
    }

    public ManagedDialogsActivity getManagedDialogsActivity() {
        return mActivity;
    }

    public CharSequence getTitle() {
        return getBundle().getCharSequence(TITLE);
    }

    public boolean hasTitle() {
        return getTitle() != null;
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        mActivity.dialogFinished(this, which, getArg());
    }

    @Override
    public void prepare(final D dialog) {
        dialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(final DialogInterface dialog) {
                AbstractDialogWrapper.this.onDismiss(dialog);
            }
        });
        dialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(final DialogInterface dialog) {
                AbstractDialogWrapper.this.onCancel(dialog);
            }
        });
        instance = dialog;
        if (hasTitle()) {
            dialog.setTitle(getTitle());
        }
    }

    @Override
    public void restoreInstanceState(final Bundle savedInstanceState) {
        bundle = savedInstanceState.getBundle(bundleName);
    }

    @Override
    public void saveInstanceState(final Bundle outState) {
        outState.putBundle(bundleName, bundle);
    }

    public void setTitle(final CharSequence title) {
        getBundle().putCharSequence(TITLE, title);
        if (instance != null) {
            instance.setTitle(title);
        }
    }

    public void setTitle(final int titleID) {
        setTitle(mActivity.getText(titleID));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void show(final Serializable arg) {
        setArg(arg);
        mActivity.showDialog(mDialogId);
    }

    protected Serializable getArg() {
        return getBundle().getSerializable(ARG);
    }

    protected void onCancel(final DialogInterface dialog) {
    }

    protected void onDismiss(final DialogInterface dialog) {
        instance = null;
    }

    protected void setArg(final Serializable arg) {
        getBundle().putSerializable(ARG, arg);
    }
}
