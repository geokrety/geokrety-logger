package pl.nkg.geokrety;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

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

	public static Document getDomElement(String xml) {
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			DocumentBuilder db = dbf.newDocumentBuilder();

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			doc = db.parse(is);

		} catch (ParserConfigurationException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (SAXException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (IOException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		}
		// return DOM
		return doc;
	}

	public static final String getElementValue(Node elem) {
		Node child;
		if (elem != null) {
			if (elem.hasChildNodes()) {
				for (child = elem.getFirstChild(); child != null; child = child
						.getNextSibling()) {
					if (child.getNodeType() == Node.TEXT_NODE) {
						return child.getNodeValue();
					}
				}
			}
		}
		return "";
	}

	public static String httpPost(String url, String[][] data)
			throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);

		// Add your data
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
				data.length);
		for (String[] varible : data) {
			nameValuePairs.add(new BasicNameValuePair(varible[0], varible[1]));
		}
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

		// Execute HTTP Post Request
		return EntityUtils.toString(httpclient.execute(httppost).getEntity(),
				HTTP.UTF_8);
	}

	public static String httpGet(String url, String[][] data)
			throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();

		StringBuilder sb = new StringBuilder();
		sb.append(url);
		boolean first = true;

		for (String[] varible : data) {
			sb.append(first ? "?" : "&");
			first = false;
			sb.append(varible[0]);
			sb.append("=");
			sb.append(Utils.encode(varible[1]));
		}

		String url2 = sb.toString();
		HttpGet httppost = new HttpGet(url2);

		// Execute HTTP Post Request
		return EntityUtils.toString(httpclient.execute(httppost).getEntity(),
				HTTP.UTF_8);
	}

}
