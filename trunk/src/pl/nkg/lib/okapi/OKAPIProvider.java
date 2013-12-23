package pl.nkg.lib.okapi;

import org.json.JSONException;
import org.json.JSONObject;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.exceptions.MessagedException;

public class OKAPIProvider {
	private static final String URL_BY_USERNAME = "/okapi/services/users/by_username";
	//private static final String URL_USERLOGS = "/okapi/services/logs/userlogs";
	
	//private static final String URL_GEOCACHES = "/okapi/services/caches/geocaches";

	public static String loadOpenCachingUUID(SupportedOKAPI okapi, String login)
			throws MessagedException {
		String[][] getData = new String[][] {
				new String[] { "username", login },
				new String[] { "fields", "uuid" },
				new String[] { "consumer_key", okapi.consumerKey } };
		try {
			String jsonString = Utils.httpGet("http://" + okapi.host
					+ URL_BY_USERNAME, getData);
			JSONObject json = new JSONObject(jsonString);
			return json.getString("uuid");
		} catch (JSONException e) {
			throw new MessagedException(R.string.invalid_oclogin_error_message);
		} catch (Exception e) {
			throw new MessagedException(R.string.oclogs_error_message);
		}
	}
}
