package pl.nkg.geokrety.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import java.util.List;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.data.GeoKret;
import pl.nkg.geokrety.data.GeocacheLog;
import pl.nkg.geokrety.data.User;

public class MultiLogActivity extends AbstractActivity implements MultiLogFragment.OnFragmentInteractionListener, ActionBar.OnNavigationListener {

    private static final String STATE_USER = "user";
    private MultiLogFragment mMultiLogFragment;
    private Menu mMenu;
    private int mCurrentUser;
    List<User> mUserList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        mUserList = mApplication.getStateHolder().getAccountList();
        actionBar.setListNavigationCallbacks(new ArrayAdapter<>(this, R.layout.action_menu_user_item, mUserList), this);


        if (savedInstanceState == null) {
            mCurrentUser = mApplication.getStateHolder().getAccountList().indexOf(mApplication.getStateHolder().getDefaultAccount());
            mMultiLogFragment =  MultiLogFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, mMultiLogFragment)
                    .commit();
        } else {
            mCurrentUser = savedInstanceState.getInt(STATE_USER);
            mMultiLogFragment = (MultiLogFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
            actionBar.setSelectedNavigationItem(mCurrentUser);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_multilog, menu);
        mMenu = menu;
        updateMenuIconsVisible();
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

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        mCurrentUser = itemPosition;
        mMultiLogFragment.setUser(mUserList.get(itemPosition));
        updateMenuIconsVisible();
        return true;
    }
}
