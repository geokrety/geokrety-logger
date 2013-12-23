package pl.nkg.geokrety.threads;

import pl.nkg.lib.gkapi.GeoKretyProvider;
import pl.nkg.lib.threads.AbstractForegroundTaskWrapper;
import pl.nkg.lib.threads.ForegroundTaskHandler;
import android.util.Pair;

public class GettingSecidThread extends
		AbstractForegroundTaskWrapper<Pair<String, String>, String, String> {

	public static final int ID = 3;

	public GettingSecidThread() {
		super(ID);
	}

	@Override
	protected String runInBackground(Pair<String, String> param)
			throws Throwable {
		return GeoKretyProvider.loadSecureID(param.first, param.second);
	}

	public static GettingSecidThread getFromHandler(
			ForegroundTaskHandler handler) {
		AbstractForegroundTaskWrapper<?, ?, ?> a = handler.getTask(ID);
		GettingSecidThread b = (GettingSecidThread) a;
		return b;
	}
}
