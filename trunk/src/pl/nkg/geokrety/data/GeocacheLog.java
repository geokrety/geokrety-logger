package pl.nkg.geokrety.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

public class GeocacheLog {

	public static String FORMAT_DATE_ISO = "yyyy-MM-dd'T'HH:mm:ssZ";

	private final String uuid;
	private final Date date;
	private final String cache_code;
	private final String type;
	private final String comment;
	
	public GeocacheLog(JSONObject jsonObject) throws JSONException, ParseException {
		uuid = jsonObject.getString("uuid");
		date = fromISODateString(jsonObject.getString("date"));
		cache_code = jsonObject.getString("cache_code");
		type = jsonObject.getString("type");
		comment = jsonObject.getString("comment");
	}

	public String getUUID() {
		return uuid;
	}

	public Date getDate() {
		return date;
	}

	public String getCacheCode() {
		return cache_code;
	}

	public String getType() {
		return type;
	}

	public String getComment() {
		return comment;
	}
	
	@Override
	public String toString() {
		return StateHolder.getGeoacheMap().get(cache_code).getName() + " (" + cache_code + ")";
	}
	

	public static Date fromISODateString(String isoDateString) throws ParseException {
	    DateFormat f = new SimpleDateFormat(FORMAT_DATE_ISO, Locale.getDefault());
	    f.setTimeZone(TimeZone.getDefault());
	    return f.parse(isoDateString);
	}
}
