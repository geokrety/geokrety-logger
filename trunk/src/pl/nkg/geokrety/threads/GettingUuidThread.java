package pl.nkg.geokrety.threads;

import pl.nkg.lib.okapi.OKAPIProvider;
import pl.nkg.lib.okapi.SupportedOKAPI;
import pl.nkg.lib.threads.AbstractForegroundTaskWrapper;
import pl.nkg.lib.threads.ForegroundTaskHandler;
import android.util.Pair;

public class GettingUuidThread extends
		AbstractForegroundTaskWrapper<Pair<String, Integer>, String, String> {

	public static final int ID = 4;

	public GettingUuidThread() {
		super(ID);
	}

	@Override
	protected String runInBackground(Pair<String, Integer> param)
			throws Throwable {
		return OKAPIProvider.loadOpenCachingUUID(
				SupportedOKAPI.SUPPORTED[param.second], param.first);
	}

	public static GettingUuidThread getFromHandler(ForegroundTaskHandler handler) {
		AbstractForegroundTaskWrapper<?, ?, ?> a = handler.getTask(ID);
		GettingUuidThread b = (GettingUuidThread) a;
		return b;
	}
}
