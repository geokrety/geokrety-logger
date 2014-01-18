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
