package pl.nkg.geokrety;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
		startActivity(new Intent(this, InventoryActivity.class));
	}

	public void showLastOCsActivity(View view) {
		startActivity(new Intent(this, LastOCsActivity.class));
	}

	public void showLogGeoKretActivity(View view) {
		startActivity(new Intent(this, LogActivity.class));
	}
		
	public void showAboutActivity(View view) {
		startActivity(new Intent(this, AboutActivity.class));
	}
}
