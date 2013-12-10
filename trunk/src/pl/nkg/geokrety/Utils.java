package pl.nkg.geokrety;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Utils {
	public static String encode(String decoded) {
		try {
			return URLEncoder.encode(decoded, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String decode(String encoded) {
		try {
			return URLDecoder.decode(encoded, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
