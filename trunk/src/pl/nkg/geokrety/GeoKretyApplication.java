/*
 * Copyright (C) 2013, 2014 Michał Niedźwiecki
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
package pl.nkg.geokrety;

import java.util.Date;

import org.acra.annotation.ReportsCrashes;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.services.LogSubmitterService;
import pl.nkg.geokrety.services.RefreshService;
import pl.nkg.geokrety.threads.GettingSecidThread;
import pl.nkg.geokrety.threads.GettingUuidThread;
import pl.nkg.lib.threads.ForegroundTaskHandler;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import org.acra.*;

import com.github.anrwatchdog.ANRWatchDog;

@ReportsCrashes(formKey = "",
    formUri = "http://geokretylog.sourceforge.net/reportbug.php",
    mode = ReportingInteractionMode.TOAST,
    resToastText = R.string.crash_toast_text)
public class GeoKretyApplication extends Application {
	private HttpClient httpClient;
	private ForegroundTaskHandler foregroundTaskHandler;
	private StateHolder stateHolder;
	private boolean noAccountHinted = false;
	public ANRWatchDog watchDog = new ANRWatchDog(30000);
	
	@SuppressWarnings("unused")
    @Override
	public void onCreate() {
		super.onCreate();
		
		if (isAcraEnabled()) {
    		ACRA.init(this);
    		if (BuildConfig.DEBUG == false) {
    		    watchDog.start();
    		}
		}
		
		Utils.application = this;
		stateHolder = new StateHolder(getApplicationContext());
		httpClient = createHttpClient();
		foregroundTaskHandler = new ForegroundTaskHandler();
		foregroundTaskHandler.registerTask(new GettingSecidThread(this));
		foregroundTaskHandler.registerTask(new GettingUuidThread(this));
		
		startService(new Intent(this, LogSubmitterService.class));
	}

    @Override
	public void onLowMemory() {
		super.onLowMemory();
		Utils.application = null;
		shutdownHttpClient();
		shutdownHandler();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Utils.application = null;
		shutdownHttpClient();
		shutdownHandler();
	}

	private void shutdownHandler() {
		if (foregroundTaskHandler != null) {
			foregroundTaskHandler.shutdown();
			foregroundTaskHandler = null;
		}
	}

	private HttpClient createHttpClient() {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
		HttpProtocolParams.setUseExpectContinue(params, true);
		HttpConnectionParams.setConnectionTimeout(params, getTimeOut());
		HttpConnectionParams.setSoTimeout(params, getTimeOut());

		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schReg.register(new Scheme("https",
				SSLSocketFactory.getSocketFactory(), 443));
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
				params, schReg);
		return new DefaultHttpClient(conMgr, params);
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public StateHolder getStateHolder() {
		return stateHolder;
	}

	public ForegroundTaskHandler getForegroundTaskHandler() {
		return foregroundTaskHandler;
	}

	public boolean isNoAccountHinted() {
		return noAccountHinted;
	}

	public void setNoAccountHinted(boolean noAccountHinted) {
		this.noAccountHinted = noAccountHinted;
	}

	private void shutdownHttpClient() {
		if (httpClient != null && httpClient.getConnectionManager() != null) {
			httpClient.getConnectionManager().shutdown();
		}
	}
	
	private long lastRefresh = 0;
	public void runRefreshService(boolean force) {
	    if (!force && !isAutoRefreshEnabled()) {
	        return;
	    }
	    
	    if (force || lastRefresh + getRefreshExpire() < new Date().getTime()) {
            Intent intent = new Intent(this, RefreshService.class);
            stopService(intent);
            if (isOnline()) {
                lastRefresh = new Date().getTime();
                startService(intent);
                Toast.makeText(this, R.string.toast_notify_refresh_start, Toast.LENGTH_LONG).show();
            } else {
                if (force) {
                    Toast.makeText(this, R.string.toast_notify_refresh_no_connection, Toast.LENGTH_LONG).show();
                }
            }
	    }
	}
	
	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}

    public int getRetryCount() {
        // TODO: use as app settings
        return 3;
    }
    
    public int getTimeOut() {
        // TODO: use as app settings
        return 60 * 1000;
    }
    
    public int getRefreshExpire() {
        // TODO: use as app settings
        return 5 * 60 * 1000;
    }
    
    public boolean isWaypointResolverEnabled() {
        // TODO: use as app settings
        return true;
    }

    public boolean isTrackingCodeVerifierEnabled() {
        // TODO: use as app settings
        return true;
    }
    
    public boolean isAutoRefreshEnabled() {
        // TODO: use as app settings
        return true;
    }

    public boolean isRetrySubmitEnabled() {
        // TODO: use as app settings
        return true;
    }

    public long getRetrySubmitDelay() {
        // TODO: use as app settings
        return 1000 * 60 * 5;
    }
    
    public boolean isAcraEnabled() {
        // TODO: use as app settings
        return true;
    }
    
    public boolean isExperimentalEnabled() {
     // TODO: use as app settings
        return false;
    }
}
