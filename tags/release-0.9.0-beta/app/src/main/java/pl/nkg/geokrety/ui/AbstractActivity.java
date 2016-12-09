package pl.nkg.geokrety.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import pl.nkg.geokrety.GeoKretyApplication;

public abstract class AbstractActivity extends AppCompatActivity {
    protected GeoKretyApplication mApplication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (GeoKretyApplication) getApplication();
    }
}
