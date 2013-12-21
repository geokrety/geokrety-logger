package pl.nkg.lib.threads;

import java.io.Serializable;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.exceptions.MessagedException;

import android.content.Context;
import android.widget.Toast;

public class GenericTaskListener<Param, Progress extends Serializable, Result>
		implements TaskListener<Param, Progress, Result> {
	private final Context context;
	private Integer finishMessage = R.string.generic_finish;
	private Integer breakMessage = R.string.generic_broken;
	private Integer errorMessage = R.string.generic_error;

	public GenericTaskListener(Context context) {
		this.context = context;
	}

	public GenericTaskListener<Param, Progress, Result> setFinishMessage(
			Integer finishMessage) {
		this.finishMessage = finishMessage;
		return this;
	}

	public GenericTaskListener<Param, Progress, Result> setBreakMessage(
			Integer breakMessage) {
		this.breakMessage = breakMessage;
		return this;
	}

	public GenericTaskListener<Param, Progress, Result> setErrorMessage(
			Integer errorMessage) {
		this.errorMessage = errorMessage;
		return this;
	}

	public void onBreak(
			AbstractForegroundTaskWrapper<Param, Progress, Result> sender,
			Param param, Result result) {
		if (breakMessage != null) {
			Toast.makeText(context, breakMessage, Toast.LENGTH_LONG).show();
		}
	}

	public void onError(
			AbstractForegroundTaskWrapper<Param, Progress, Result> sender,
			Param param, Throwable exception) {
		if (exception instanceof MessagedException) {
			Toast.makeText(
					context,
					((MessagedException) exception).getFormatedMessage(context),
					Toast.LENGTH_LONG).show();
		} else if (errorMessage != null) {
			Toast.makeText(context,
					errorMessage + " " + exception.getLocalizedMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	public void onFinish(
			AbstractForegroundTaskWrapper<Param, Progress, Result> sender,
			Param param, Result result) {
		if (finishMessage != null) {
			Toast.makeText(context, finishMessage, Toast.LENGTH_LONG).show();
		}
	}
}
