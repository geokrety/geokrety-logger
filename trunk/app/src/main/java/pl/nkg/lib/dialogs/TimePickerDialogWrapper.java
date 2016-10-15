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

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.widget.TimePicker;

public class TimePickerDialogWrapper extends
        AbstractAlertDialogWrapper<TimePickerDialog> implements
        TimePickerDialog.OnTimeSetListener {

    private int hourOfDay;
    private int minute;
    private boolean is24HourView;

    public TimePickerDialogWrapper(final ManagedDialogsActivity a, final int dialogId) {
        super(a, dialogId);
    }

    @Override
    public TimePickerDialog create() {
        final TimePickerDialog dialog = new TimePickerDialog(
                getManagedDialogsActivity(), this, hourOfDay, minute,
                is24HourView);
        if (hasTitle()) {
            dialog.setTitle(getTitle());
        }

        if (hasMessage()) {
            dialog.setMessage(getMessage());
        }

        return dialog;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public int getMinute() {
        return minute;
    }

    public boolean isIs24HourView() {
        return is24HourView;
    }

    @Override
    public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
        setHourOfDay(hourOfDay);
        setMinute(minute);
        getManagedDialogsActivity().dialogFinished(this,
                DialogInterface.BUTTON_POSITIVE, getArg());
    }

    @Override
    public void prepare(final TimePickerDialog dialog) {
        super.prepare(dialog);
        dialog.updateTime(hourOfDay, minute);
    }

    public void setHourOfDay(final int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public void setIs24HourView(final boolean is24HourView) {
        this.is24HourView = is24HourView;
    }

    public void setMinute(final int minute) {
        this.minute = minute;
    }

    public void show(final Serializable arg, final int hourOfDay, final int minute,
            final boolean is24HourView) {
        setHourOfDay(hourOfDay);
        setMinute(minute);
        setIs24HourView(is24HourView);
        show(arg);
    }
}
