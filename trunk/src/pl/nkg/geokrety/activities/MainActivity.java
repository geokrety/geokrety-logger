/*
 * Copyright (C) 2013 Michał Niedźwiecki
 * 
 * This file is part of GeoKrety Logger
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
package pl.nkg.geokrety.activities;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
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
