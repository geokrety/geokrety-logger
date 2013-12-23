package pl.nkg.lib.threads;

import java.io.Serializable;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Pair;

import pl.nkg.lib.dialogs.AbstractProgressDialogWrapper;

public abstract class AbstractForegroundTaskWrapper<Param, Progress extends Serializable, Result> {
	private Param param;
	private ForegroundTaskHandler handler;
	private int id;

	private AbstractProgressDialogWrapper<Progress> progressDialogWrapper;
	private TaskListener<Param, Progress, Result> listener;
	private Throwable exception;

	private Thread thread;

	private Pair<Param, Result> nofiredFinish;
	private Pair<Param, Throwable> nofiredError;
	private int nofired;

	protected AbstractForegroundTaskWrapper(int id) {
		this.id = id;
	}

	void setHandler(ForegroundTaskHandler handler) {
		this.handler = handler;
	}

	public int getID() {
		return id;
	}

	public void setParam(Param param) {
		this.param = param;
	}

	public void execute(Param param) {
		setParam(param);
		cleanNofired();
		thread = new Thread();
		thread.execute(new Object[] { param });
	}

	private void cleanNofired() {
		nofired = 0;
		nofiredFinish = null;
		nofiredError = null;
	}

	public void attach(
			AbstractProgressDialogWrapper<Progress> progressDialogWrapper,
			TaskListener<Param, Progress, Result> listener) {
		this.progressDialogWrapper = progressDialogWrapper;
		this.listener = listener;
		progressDialogWrapper.setTask(this);
		if (thread == null) {
			progressDialogWrapper.dismiss();
		}

		if (nofired > 0) {
			switch (nofired) {
			case 1:
				listener.onFinish(this, nofiredFinish.first,
						nofiredFinish.second);
				break;

			case 2:
				listener.onBreak(this, nofiredFinish.first,
						nofiredFinish.second);
				break;

			case 3:
				listener.onError(this, nofiredError.first, nofiredError.second);
				break;
			}

			cleanNofired();
		}
	}

	public void detach() {
		progressDialogWrapper = null;
		listener = null;
	}

	private void fireFinish(Result result) {
		if (listener != null) {
			listener.onFinish(this, param, result);
		} else {
			nofired = 1;
			nofiredFinish = new Pair<Param, Result>(param, result);
		}
	}

	private void fireBreak(Result result) {
		if (listener != null) {
			listener.onBreak(this, param, result);
		} else {
			nofired = 2;
			nofiredFinish = new Pair<Param, Result>(param, result);
		}
	}

	private void fireError() {
		if (listener != null) {
			listener.onError(this, param, exception);
		} else {
			nofired = 3;
			nofiredError = new Pair<Param, Throwable>(param, exception);
		}
	}

	private void dismissProgressDialog() {
		if (progressDialogWrapper != null) {
			progressDialogWrapper.dismiss();
		}
	}

	private void setProgress(Progress progress) {
		if (progressDialogWrapper != null) {
			progressDialogWrapper.setProgress(progress);
			progressDialogWrapper.updateProgress();
		}
	}

	protected void publishProgress(Progress progress) {
		if (thread != null) {
			thread.publish(new Object[] { progress });
		}
	}

	protected abstract Result runInBackground(Param param) throws Throwable;

	public class Thread extends AsyncTask<Object, Object, Result> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (progressDialogWrapper != null) {
				progressDialogWrapper.show(null);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Result doInBackground(Object... params) {
			try {
				return runInBackground((Param) params[0]);
			} catch (Throwable t) {
				exception = t;
				return null;
			}
		}

		public void publish(Object... progress) {
			super.publishProgress(progress);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onProgressUpdate(Object... values) {
			super.onProgressUpdate(values);
			setProgress((Progress) values[0]);
		}

		@Override
		protected void onPostExecute(Result result) {
			super.onPostExecute(result);

			if (result == null) {
				fireError();
			} else {
				fireFinish(result);
			}

			dismissProgressDialog();
			thread = null;
			handler.terminateTask(AbstractForegroundTaskWrapper.this);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();

			fireBreak(null);

			dismissProgressDialog();
			thread = null;
			handler.terminateTask(AbstractForegroundTaskWrapper.this);
		}

		@SuppressLint("NewApi")
		@Override
		protected void onCancelled(Result result) {
			super.onCancelled(result);
			fireBreak(result);
			thread = null;
			handler.terminateTask(AbstractForegroundTaskWrapper.this);
		}

	}

	public void cancel(boolean b) {
		if (thread != null) {
			thread.cancel(b);
		}
	}

	public boolean isFinished() {
		return thread == null;
	}
}
