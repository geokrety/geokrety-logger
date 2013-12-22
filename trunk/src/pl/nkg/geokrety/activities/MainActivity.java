package pl.nkg.geokrety.activities;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		TextView appName = (TextView) findViewById(R.id.appNameTextView);
		appName.setText(getResources().getString(R.string.version)
				+ Utils.getAppVer(this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void showAccountsActivity(View view) {
		startActivity(new Intent(this, AccountsActivity.class));
	}

	public void showInventoryActivity(View view) {
		if (accountExist()) {
			startActivity(new Intent(this, InventoryActivity.class));
		}
	}

	private boolean accountExist() {
		if (((GeoKretyApplication) getApplication()).getStateHolder()
				.getAccountList().size() == 0) {
			Toast.makeText(this, R.string.no_account_configured,
					Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	public void showLastOCsActivity(View view) {
		if (accountExist()) {
			startActivity(new Intent(this, LastOCsActivity.class));
		}
	}

	public void showLogGeoKretActivity(View view) {
		if (accountExist()) {
			startActivity(new Intent(this, LogActivity.class));
		}
	}

	public void showAboutActivity(View view) {
		startActivity(new Intent(this, AboutActivity.class));
	}
}
