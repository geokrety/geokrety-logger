/*
 * Copyright (C) 2014 Michał Niedźwiecki
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

package pl.nkg.lib.location;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GPSAcquirer implements LocationListener {

    private static final String RUNNING = "running";
    private static final String MINTIME = "minTime";
    private static final String MINDISTANCE = "minDistance";

    public static boolean checkAndToast(final Context context) {
        if (!gpsExist(context)) {
            Utils.makeCenterToast(context, R.string.gps_error_not_exist).show();
            return false;
        }

        if (!gpsEnabled(context)) {
            Utils.makeCenterToast(context, R.string.gps_warning_not_enabled).show();
            return false;
        }

        Utils.makeCenterToast(context, R.string.gps_message_pleas_wait).show();

        return true;
    }

    public static boolean gpsEnabled(final Context context) {
        final LocationManager manager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @SuppressLint("InlinedApi")
    public static boolean gpsExist(final Context context) {
        if (android.os.Build.VERSION.SDK_INT < 8) {
            return true;
        }

        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    private final LocationManager locman;
    private boolean running;
    private long minTime;

    private float minDistance;

    private final String bundleName;

    private boolean paused;

    private final LocationListener listener;

    public GPSAcquirer(final Context ctx, final String bundleName, final LocationListener listener) {
        locman = (LocationManager) ctx
                .getSystemService(Context.LOCATION_SERVICE);
        running = false;
        this.bundleName = bundleName;
        this.listener = listener;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void onLocationChanged(final Location location) {
        if (!paused) {
            listener.onLocationChanged(location);
            running = false;
        }
        locman.removeUpdates(this);
    }

    @Override
    public void onProviderDisabled(final String provider) {
        if (!paused) {
            listener.onProviderDisabled(provider);
        }
    }

    @Override
    public void onProviderEnabled(final String provider) {
        if (!paused) {
            listener.onProviderEnabled(provider);
        }
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {
        if (!paused) {
            listener.onStatusChanged(provider, status, extras);
        }
    }

    public void pause(final Bundle outState) {
        paused = true;
        final Bundle bundle = new Bundle();
        bundle.putBoolean(RUNNING, running);
        bundle.putLong(MINTIME, minTime);
        bundle.putFloat(MINDISTANCE, minDistance);
        outState.putBundle(bundleName, bundle);
    }

    public void restore(final Bundle savedInstanceState) {
        final Bundle bundle = savedInstanceState.getBundle(bundleName);
        if (bundle != null) {
            running = bundle.getBoolean(RUNNING);
            minTime = bundle.getLong(MINTIME);
            minDistance = bundle.getFloat(MINDISTANCE);
        }

        start();
    }

    public void runRequest(final long minTime, final float minDistance) {
        locman.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime,
                minDistance, this);
        running = true;
        this.minDistance = minDistance;
        this.minTime = minTime;
        paused = false;
    }

    public void start() {
        if (running) {
            runRequest(minTime, minDistance);
        }
    }
}
