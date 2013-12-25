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

import pl.nkg.lib.threads.AbstractForegroundTaskWrapper;

import android.app.ProgressDialog;
import android.content.DialogInterface;

public abstract class AbstractProgressDialogWrapper<Progress extends Serializable> extends
		AbstractAlertDialogWrapper<ProgressDialog> {

	private static final String PROGRESS = "progress";
	
	private boolean indeterminate = true;
	private AbstractForegroundTaskWrapper<?, Progress, ?> task;;

	public AbstractProgressDialogWrapper(ManagedDialogsActivity a, int dialogId) {
		super(a, dialogId);
	}

	public boolean isIndeterminate() {
		return indeterminate;
	}

	public void setIndeterminate(boolean indeterminate) {
		this.indeterminate = indeterminate;
	}

	@Override
	public ProgressDialog create() {
		ProgressDialog progress = new ProgressDialog(
				getManagedDialogsActivity());
		progress.setCancelable(isCancelable());
		progress.setIndeterminate(isIndeterminate());
		progress.setTitle(getTitle());
		progress.setMessage(getMessage());
		
		return progress;
	}
	
	@Override
	public void prepare(ProgressDialog dialog) {
		super.prepare(dialog);
		updateProgress();
		if (task != null) {
			if (task.isFinished()) {
				dismiss();
			}
		}
	}

	public void dismiss() {
		try {
			if (getInstance() != null) {
				getInstance().dismiss();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public Progress getProgres() {
		return (Progress) getBundle().getSerializable(PROGRESS);
	}
	
	public void setProgress(Progress progress) {
		getBundle().putSerializable(PROGRESS, progress);
	}
	
	abstract public void updateProgress();
	
	public void setTask(AbstractForegroundTaskWrapper<?, Progress, ?> task) {
		this.task = task;
	}
	
	@Override
	protected void onCancel(DialogInterface dialog) {
		if (task != null) {
			task.cancel(true);
		}
		super.onCancel(dialog);
	}
}
