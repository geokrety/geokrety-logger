package pl.nkg.lib.threads;

import java.io.Serializable;

public interface TaskListener<Param, Progress extends Serializable, Result> {
	public void onBreak(
			AbstractForegroundTaskWrapper<Param, Progress, Result> sender,
			Param param, Result result);

	public void onError(
			AbstractForegroundTaskWrapper<Param, Progress, Result> sender,
			Param param, Throwable exception);

	public void onFinish(
			AbstractForegroundTaskWrapper<Param, Progress, Result> sender,
			Param param, Result result);
}
