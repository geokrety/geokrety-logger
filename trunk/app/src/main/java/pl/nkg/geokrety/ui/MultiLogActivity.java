package pl.nkg.geokrety.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.List;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.data.GeoKret;
import pl.nkg.geokrety.data.GeocacheLog;
import pl.nkg.geokrety.data.User;

public class MultiLogActivity extends AbstractActivity implements MultiLogFragment.OnFragmentInteractionListener {

    private static final String STATE_USER = "user";
    private MultiLogFragment mMultiLogFragment;
    private Menu mMenu;
    private int mCurrentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (savedInstanceState == null) {
            mCurrentUser = mApplication.getStateHolder().getAccountList().indexOf(mApplication.getStateHolder().getDefaultAccount());
            mMultiLogFragment =  MultiLogFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, mMultiLogFragment)
                    .commit();
        } else {
            mCurrentUser = savedInstanceState.getInt(STATE_USER);
            mMultiLogFragment = (MultiLogFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_multilog, menu);
        mMenu = menu;
        updateMenuIconsVisible();

        MenuItem users = menu.findItem( R.id.action_user);
        View view = MenuItemCompat.getActionView(users);
        if (view instanceof Spinner)
        {
            final Spinner spinner = (Spinner) view;
            final List<User> userList = mApplication.getStateHolder().getAccountList();
            spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, userList.toArray(new User[userList.size()])));
            spinner.setSelection(mCurrentUser);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                                           int arg2, long arg3) {
                    mCurrentUser = arg2;
                    mMultiLogFragment.setUser(userList.get(arg2));
                    updateMenuIconsVisible();
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });

        }

        return true;
    }

    private void updateMenuIconsVisible() {
        onSelectionListUpdated(mMultiLogFragment.getSelectedGeoKretList(), mMultiLogFragment.getSelectedGeocacheLogList());
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_USER, mCurrentUser);
    }

    @Override
    public void onSelectionListUpdated(List<GeoKret> geoKretList, List<GeocacheLog> geocacheLogList) {
        boolean enabled = geoKretList.size() > 0 && geocacheLogList.size() > 0;
        mMenu.findItem(R.id.action_postpone).setVisible(enabled);
        mMenu.findItem(R.id.action_send).setVisible(enabled);
    }
}
