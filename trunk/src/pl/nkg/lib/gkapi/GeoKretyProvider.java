package pl.nkg.lib.gkapi;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.exceptions.MessagedException;

public class GeoKretyProvider {
	private static final String URL_LOGIN = "http://geokrety.org/api-login2secid.php";

	public static String loadSecureID(String geoKretyLogin, String geoKretyPassword)
			throws MessagedException {
		String[][] postData = new String[][] {
				new String[] { "login", geoKretyLogin },
				new String[] { "password", geoKretyPassword } };

		String value;
		try {
			value = Utils.httpPost(URL_LOGIN, postData);
		} catch (Exception e) {
			throw new MessagedException(R.string.login_error_message,
					e.getLocalizedMessage());
		}

		if (value != null && !value.startsWith("error")) {
			return value.trim();
		} else {
			throw new MessagedException(R.string.login_error_password_message,
					String.valueOf(value));
		}
	}
}
