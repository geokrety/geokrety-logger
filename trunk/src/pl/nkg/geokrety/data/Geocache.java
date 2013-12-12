package pl.nkg.geokrety.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Geocache {

	private final String name;
	private final String code;
	
	public Geocache(JSONObject jsonObject) throws JSONException {
		name = jsonObject.getString("name");
		code = jsonObject.getString("code");
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}
}
