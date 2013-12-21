package pl.nkg.geokrety.activities.listeners;

import android.content.Context;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.data.Account;
import pl.nkg.lib.threads.GenericTaskListener;

public class RefreshListener extends
		GenericTaskListener<Account, String, Boolean> {

	public RefreshListener(Context context) {
		super(context);
		setFinishMessage(R.string.download_finish);
	}

}
