package pl.nkg.lib.dialogs;

import java.io.Serializable;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.widget.DatePicker;

public class DatePickerDialogWrapper extends
		AbstractAlertDialogWrapper<DatePickerDialog> implements
		DatePickerDialog.OnDateSetListener {

	private int year = 2013;
	private int monthOfYear = 11;
	private int dayOfMonth = 12;

	public DatePickerDialogWrapper(ManagedDialogsActivity a, int dialogId) {
		super(a, dialogId);
	}

	@Override
	public DatePickerDialog create() {
		DatePickerDialog dialog = new DatePickerDialog(
				getManagedDialogsActivity(), this, year, monthOfYear,
				dayOfMonth);
		if (hasTitle()) {
			dialog.setTitle(getTitle());
		}

		if (hasMessage()) {
			dialog.setMessage(getMessage());
		}

		return dialog;
	}

	@Override
	public void prepare(DatePickerDialog dialog) {
		super.prepare(dialog);
		dialog.updateDate(year, monthOfYear, dayOfMonth);
	}

	public void show(Serializable arg, int year, int monthOfYear, int dayOfMonth) {
		this.year = year;
		this.monthOfYear = monthOfYear;
		this.dayOfMonth = dayOfMonth;
		show(arg);
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonthOfYear() {
		return monthOfYear;
	}

	public void setMonthOfYear(int monthOfYear) {
		this.monthOfYear = monthOfYear;
	}

	public int getDayOfMonth() {
		return dayOfMonth;
	}

	public void setDayOfMonth(int dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {

		setDayOfMonth(dayOfMonth);
		setMonthOfYear(monthOfYear);
		setYear(year);
		getManagedDialogsActivity().dialogFinished(this,
				Dialog.BUTTON_POSITIVE, getArg());
	}
}
