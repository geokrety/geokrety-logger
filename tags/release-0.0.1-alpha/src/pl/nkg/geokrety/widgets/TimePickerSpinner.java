package pl.nkg.geokrety.widgets;

import android.annotation.TargetApi;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TimePickerSpinner extends Spinner {

	// implement all the constructors

	public TimePickerSpinner(Context context, AttributeSet attrs, int defStyle,
			int mode) {
		super(context, attrs, defStyle, mode);
		// TODO Auto-generated constructor stub
	}

	public TimePickerSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public TimePickerSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public TimePickerSpinner(Context context, int mode) {
		super(context, mode);
		// TODO Auto-generated constructor stub
	}

	public TimePickerSpinner(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean performClick() {
		TimePickerDialog dDialog = new TimePickerDialog(getContext(),
				new OnTimeSetListener() {

					@Override
					public void onTimeSet(TimePicker arg0, int arg1, int arg2) {
						Toast.makeText(getContext(), "Something",
								Toast.LENGTH_SHORT).show();
					}

				}, 3, 4, false);
		dDialog.show();
		return false;
	}

}
