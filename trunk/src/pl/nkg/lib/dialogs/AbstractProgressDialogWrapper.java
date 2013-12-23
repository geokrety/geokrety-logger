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
