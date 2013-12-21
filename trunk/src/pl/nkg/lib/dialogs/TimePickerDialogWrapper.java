package pl.nkg.lib.dialogs;

import java.io.Serializable;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.widget.TimePicker;

public class TimePickerDialogWrapper extends
		AbstractAlertDialogWrapper<TimePickerDialog> implements
		TimePickerDialog.OnTimeSetListener {

	private int hourOfDay;
	private int minute;
	private boolean is24HourView;

	public TimePickerDialogWrapper(ManagedDialogsActivity a, int dialogId) {
		super(a, dialogId);
	}

	@Override
	public TimePickerDialog create() {
		TimePickerDialog dialog = new TimePickerDialog(
				getManagedDialogsActivity(), this, hourOfDay, minute,
				is24HourView);
		dialog.setTitle(getTitle());
		dialog.setMessage(getMessage());
		return dialog;
	}
	
	@Override
	public void prepare(TimePickerDialog dialog) {
		super.prepare(dialog);
		dialog.updateTime(hourOfDay, minute);
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		setHourOfDay(hourOfDay);
		setMinute(minute);
		getManagedDialogsActivity().dialogFinished(this,
				Dialog.BUTTON_POSITIVE, getArg());
	}

	public int getHourOfDay() {
		return hourOfDay;
	}

	public void setHourOfDay(int hourOfDay) {
		this.hourOfDay = hourOfDay;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public boolean isIs24HourView() {
		return is24HourView;
	}

	public void setIs24HourView(boolean is24HourView) {
		this.is24HourView = is24HourView;
	}

	public void show(Serializable arg, int hourOfDay, int minute,
			boolean is24HourView) {
		setHourOfDay(hourOfDay);
		setMinute(minute);
		setIs24HourView(is24HourView);
		show(arg);
	}
}
