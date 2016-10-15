/*
 * Copyright (C) 2013 Michał Niedźwiecki
 * 
 * This file is a part of GeoKrety Logger
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class Utils {
    public static GeoKretyApplication application;
    public static DecimalFormat latlonFormat = new DecimalFormat("#.######");

    public static String defaultIfNull(final String value, final String def) {
        return value == null ? def : value;
    }

    public static String extractBetween(final String src, final String startMarker,
            final String stopMarker) {
        int start = src.indexOf(startMarker);

        if (start == -1) {
            return null;
        }

        start += startMarker.length();
        final int stop = src.indexOf(stopMarker, start);

        if (stop == -1) {
            return null;
        }

        return src.substring(start, stop);
    }

    public static String formatException(final Throwable e) {
        String msg = e.getLocalizedMessage();

        if (msg == null) {
            msg = e.getMessage();
        }

        if (msg == null) {
            msg = e.toString();
        }

        return msg;
    }

    public static String getAppName() {
        return application.getText(R.string.app_name).toString();
    }

    public static String getAppVer() {
        try {
            return application.getPackageManager().getPackageInfo(
                    application.getPackageName(), 0).versionName;
        } catch (final NameNotFoundException e) {
            return "";
        }
    }

    public static String getDefaultLanguage() {
        return Locale.getDefault().getDisplayLanguage();
    }

    public static Document getDomElement(final String xml) {
        Document doc = null;
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {

            final DocumentBuilder db = dbf.newDocumentBuilder();

            final InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            doc = db.parse(is);

        } catch (final ParserConfigurationException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (final SAXException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (final IOException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        }
        // return DOM
        return doc;
    }

    public static final String getElementValue(final Node elem) {
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

    public static String httpGet(final String url, final String[][] data)
            throws ClientProtocolException, IOException {
        return httpGet(url, data, null);
    }

    public static String httpGet(final String url, final String[][] data,
            final HttpContext httpContext)
            throws ClientProtocolException, IOException {
        return responseToString(httpGetResponse(url, data, httpContext));
    }

    public static HttpResponse httpGetResponse(final String url, final String[][] data,
            final HttpContext httpContext)
            throws ClientProtocolException, IOException {
        final HttpClient httpclient = application.getHttpClient();

        final List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                data.length);
        for (final String[] varible : data) {
            nameValuePairs.add(new BasicNameValuePair(varible[0], varible[1]));
        }

        final String url2 = url + "?"
                + URLEncodedUtils.format(nameValuePairs, "UTF-8");
        final HttpGet httppost = new HttpGet(url2);

        return httpclient.execute(httppost, httpContext);
    }

    public static String httpPost(final String url, final String[][] data)
            throws ClientProtocolException,
            IOException {
        return httpPost(url, data, null);
    }

    public static String httpPost(final String url, final String[][] data,
            final HttpContext httpContext)
            throws ClientProtocolException, IOException {
        final HttpClient httpclient = application.getHttpClient();

        final HttpPost httppost = new HttpPost(url);

        // Add your data
        final List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                data.length);
        for (final String[] varible : data) {
            nameValuePairs.add(new BasicNameValuePair(varible[0], varible[1]));
        }
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

        // Execute HTTP Post Request
        return responseToString(httpclient.execute(httppost, httpContext));
    }

    public static boolean isEmpty(final String string) {
        return string == null || string.length() == 0;
    }

    @SuppressLint("ShowToast")
    public static Toast makeCenterToast(final Context context, final int resID) {
        final Toast toast = Toast.makeText(context, resID, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        return toast;
    }

    public static String responseToString(final HttpResponse response) throws ParseException,
            IOException {
        return EntityUtils.toString(response.getEntity(),
                HTTP.UTF_8);
    }
}
