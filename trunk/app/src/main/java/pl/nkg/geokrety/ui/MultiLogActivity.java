package pl.nkg.geokrety.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;

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
}
