package pl.nkg.geokrety.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

import pl.nkg.geokrety.R;

public class MultiLogActivity extends AbstractActivity implements MultiLogFragment.OnFragmentInteractionListener {

    private MultiLogFragment mMultiLogFragment;

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
}
