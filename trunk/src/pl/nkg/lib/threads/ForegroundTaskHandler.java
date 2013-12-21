package pl.nkg.lib.threads;

import java.io.Serializable;

import android.util.SparseArray;

public class ForegroundTaskHandler {
	private AbstractForegroundTaskWrapper<?, ?, ?> currentTask;
	private SparseArray<AbstractForegroundTaskWrapper<?, ?, ?>> taskMap;

	public ForegroundTaskHandler() {
		taskMap = new SparseArray<AbstractForegroundTaskWrapper<?, ?, ?>>();
	}

	public void shutdown() {
		synchronized (this) {
			if (currentTask != null) {
				currentTask.cancel(true);
			}
		}
	}

	public boolean isBusy() {
		return currentTask != null;
	}

	public void registerTask(AbstractForegroundTaskWrapper<?, ?, ?> task) {
		taskMap.put(task.getID(), task);
		task.setHandler(this);
	}

	public void terminateTask(AbstractForegroundTaskWrapper<?, ?, ?> task) {
		synchronized (this) {
			if (currentTask != null && currentTask.getID() == task.getID()) {
				currentTask = null;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <Params> boolean runTask(int id, Params params) {
		synchronized (this) {
			if (isBusy()) {
				return false;
			}
			currentTask = taskMap.get(id);
			AbstractForegroundTaskWrapper<Params, ?, ?> task = (AbstractForegroundTaskWrapper<Params, ?, ?>) currentTask;
			task.execute(params);
			return true;
		}
	}

	@SuppressWarnings("unchecked")
	public <Params, Progress extends Serializable, Result> AbstractForegroundTaskWrapper<Params, Progress, Result> getTask(
			int id) {
		return (AbstractForegroundTaskWrapper<Params, Progress, Result>) taskMap
				.get(id);
	}
}
