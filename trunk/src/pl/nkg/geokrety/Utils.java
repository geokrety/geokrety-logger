/*
 * Copyright (C) 2013 Michał Niedźwiecki
 * 
 * This file is part of GeoKrety Logger
 * http://geokretylog.sourceforge.net/
 * 
 * GeoKrety Logger is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * or see <http://www.gnu.org/licenses/>
 */
package pl.nkg.geokrety;

import java.io.IOException;
import java.io.StringReader;
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
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class Utils {
	public static GeoKretyApplication application;

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
		HttpClient httpclient = application.getHttpClient();// new
															// DefaultHttpClient();
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
		HttpClient httpclient = application.getHttpClient();

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
				data.length);
		for (String[] varible : data) {
			nameValuePairs.add(new BasicNameValuePair(varible[0], varible[1]));
		}

		String url2 = url + "?"
				+ URLEncodedUtils.format(nameValuePairs, "UTF-8");
		HttpGet httppost = new HttpGet(url2);

		// Execute HTTP Post Request
		return EntityUtils.toString(httpclient.execute(httppost).getEntity(),
				HTTP.UTF_8);
	}

	public static String getAppVer(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			return "";
		}
	}

	public static boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}
}
