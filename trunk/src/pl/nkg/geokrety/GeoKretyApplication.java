package pl.nkg.geokrety;

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
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.threads.LogGeoKret;
import pl.nkg.geokrety.threads.RefreshAccount;
import pl.nkg.lib.threads.ForegroundTaskHandler;

import android.app.Application;

public class GeoKretyApplication extends Application {
	private HttpClient httpClient;
	private ForegroundTaskHandler foregroundTaskHandler;
	private StateHolder stateHolder;

	@Override
	public void onCreate() {
		super.onCreate();
		Utils.application = this;
		stateHolder = new StateHolder(getApplicationContext());
		httpClient = createHttpClient();
		foregroundTaskHandler = new ForegroundTaskHandler();
		foregroundTaskHandler.registerTask(new LogGeoKret());
		foregroundTaskHandler.registerTask(new RefreshAccount());
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

	private void shutdownHttpClient() {
		if (httpClient != null && httpClient.getConnectionManager() != null) {
			httpClient.getConnectionManager().shutdown();
		}
	}
}
