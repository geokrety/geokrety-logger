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

package pl.nkg.geokrety.activities;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.ui.MultiLogActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private GeoKretyApplication application;

    public void showAboutActivity(final View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }

    public void showAccountsActivity(final View view) {
        startActivity(new Intent(this, UsersActivity.class));
    }

    public void showGeoKretLogsActivity(final View view) {
        if (accountExistAndToast()) {
            startActivity(new Intent(this, GeoKretLogsActivity.class));
        }
    }

    public void showInventoryActivity(final View view) {
        if (accountExistAndToast()) {
            startActivity(new Intent(this, InventoryActivity.class));
        }
    }

    public void showLastOCsActivity(final View view) {
        if (accountExistAndToast()) {
            startActivity(new Intent(this, LastOCsActivity.class));
        }
    }

    public void showLogGeoKretActivity(final View view) {
        if (accountExistAndToast()) {
            startActivity(new Intent(this, LogActivity.class));
        }
    }

    public void showMultiLogActivity(final View view) {
        if (accountExistAndToast()) {
            startActivityForResult(new Intent(this, MultiLogActivity.class), 1000);
        }
    }

    private boolean accountExist() {
        return application.getStateHolder().getAccountList().size() == 0;
    }

    private boolean accountExistAndToast() {
        if (accountExist()) {
            Toast.makeText(this, R.string.main_error_no_account_configured,
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView appName = (TextView) findViewById(R.id.appNameTextView);
        appName.setText(getResources().getString(R.string.main_label_version_prefix)
                + Utils.getAppVer());
        application = (GeoKretyApplication) getApplication();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (accountExist() && !application.isNoAccountHinted()) {
            showAccountsActivity(null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1000 && resultCode == 1) {
            showGeoKretLogsActivity(null);
        }
    }
}
