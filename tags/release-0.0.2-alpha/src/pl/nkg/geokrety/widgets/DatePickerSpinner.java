package pl.nkg.geokrety.widgets;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DatePickerSpinner extends Spinner {

	// implement all the constructors

	public DatePickerSpinner(Context context, AttributeSet attrs, int defStyle,
			int mode) {
		super(context, attrs, defStyle, mode);
		// TODO Auto-generated constructor stub
	}

	public DatePickerSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public DatePickerSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public DatePickerSpinner(Context context, int mode) {
		super(context, mode);
		// TODO Auto-generated constructor stub
	}

	public DatePickerSpinner(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean performClick() {
		DatePickerDialog dDialog = new DatePickerDialog(getContext(),
				new OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						Toast.makeText(getContext(), "Something",
								Toast.LENGTH_SHORT).show();

					}
				}, 2012, 3, 3);
		dDialog.show();
		return false;
	}

}