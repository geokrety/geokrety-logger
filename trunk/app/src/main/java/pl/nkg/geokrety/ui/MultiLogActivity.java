package pl.nkg.geokrety.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.data.GeoKret;
import pl.nkg.geokrety.data.GeocacheLog;

public class MultiLogActivity extends AbstractActivity implements MultiLogFragment.OnFragmentInteractionListener {

    private MultiLogFragment mMultiLogFragment;
    private Menu mMenu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mMultiLogFragment =  MultiLogFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, mMultiLogFragment)
                    .commit();
        } else {
            mMultiLogFragment = (MultiLogFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_multilog, menu);
        mMenu = menu;
        onSelectionListUpdated(mMultiLogFragment.getSelectedGeoKretList(), mMultiLogFragment.getSelectedGeocacheLogList());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_postpone:
                return true;

            case R.id.action_send:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSelectionListUpdated(List<GeoKret> geoKretList, List<GeocacheLog> geocacheLogList) {
        boolean enabled = geoKretList.size() > 0 && geocacheLogList.size() > 0;
        mMenu.findItem(R.id.action_postpone).setVisible(enabled);
        mMenu.findItem(R.id.action_send).setVisible(enabled);
    }
}
