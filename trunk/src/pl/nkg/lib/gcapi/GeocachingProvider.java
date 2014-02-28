/*
 * Copyright (C) 2014 Michał Niedźwiecki
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
package pl.nkg.lib.gcapi;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.data.Geocache;
import pl.nkg.geokrety.data.GeocacheLog;
import pl.nkg.geokrety.exceptions.MessagedException;

public class GeocachingProvider {

    public static final String FORMAT_DATE_GEOCACHING = "MM/dd/yyyy";
    public static final int PORTAL = 100;
    public static final String HOST = "geocaching.com";
    private static final int LOGS_LIMIT = 20;
    
    private static final DateFormat dateFormat;

    static {
        dateFormat = new SimpleDateFormat(FORMAT_DATE_GEOCACHING, Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
    }
    
    public static HttpContext login(String login, String password) throws MessagedException {
        String[][] postData = new String[][] {
                new String[] { "ctl00$tbUsername", login },
                new String[] { "ctl00$tbPassword", password } };
        try {
            HttpContext httpContext = new BasicHttpContext();
            String ret = Utils.httpPost("https://www.geocaching.com/login/default.aspx", postData, httpContext);
            if (ret.contains("Your username/password combination does not match. Make sure you entered your information correctly.")) {
                return null;
            }
            return httpContext;
        } catch (Throwable e) {
            throw new MessagedException(R.string.lastlogs_error_refresh, e.getLocalizedMessage());
        }
    }
    
    public static List<GeocacheLog> loadGeocachingComLogs(HttpContext httpContext) throws MessagedException {
        String[][] postData = new String[][] {new String[] {"s","1"}};
        try {
            List<GeocacheLog> openCachingLogs = new LinkedList<GeocacheLog>();

            String html = Utils.httpGet("http://www.geocaching.com/my/logs.aspx", postData); 
            String table = extractTable(html);
            
            if (table != null) {
                String[] rows = table.split("</tr>");
                int i = 0;
                for (String row : rows) {
                    GeocacheLog log = extractGeocacheLog(row);
                    if (log != null) {
                        i++;
                        openCachingLogs.add(log);
                        if (i >= LOGS_LIMIT) {
                            break;
                        }
                    }
                }
            }
            
            return openCachingLogs;
        } catch (Throwable e) {
            throw new MessagedException(R.string.lastlogs_error_refresh, e.getLocalizedMessage());
        }
    }
    
    public static Geocache loadGeocacheByGUID(HttpContext httpContext, String guid) throws MessagedException {

        String[][] postData = new String[][] {new String[] {"guid", guid}};
        try {
            String htmlCache = Utils.httpGet("http://www.geocaching.com/seek/cache_details.aspx", postData, httpContext);
            return Geocache.parseGeocachingCom(htmlCache);
        } catch (Throwable e) {
            throw new MessagedException(R.string.lastlogs_error_refresh, e.getLocalizedMessage());
        }
    }
    
    public static Geocache loadGeocacheByWaypoint(HttpContext httpContext, String waypoint) throws MessagedException {

        String[][] postData = new String[][] {new String[] {"wp", waypoint}};
        try {
            String htmlCache = Utils.httpGet("http://www.geocaching.com/seek/cache_details.aspx", postData, httpContext);
            return Geocache.parseGeocachingCom(htmlCache);
        } catch (Throwable e) {
            throw new MessagedException(R.string.lastlogs_error_refresh, e.getLocalizedMessage() + ": " + waypoint);
        }
    }
    
    private static GeocacheLog extractGeocacheLog(String row) throws ClientProtocolException, IOException {
        try {
            return GeocacheLog.fromGeocachingCom(row);//;/guid, logType, date);
        } catch (NullPointerException e) {
            return null;
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
    
    private static String extractTable(String src) {
        return extractBetween(src, "<table class=\"Table\">", "</table>");
    }
    
    private static String extractBetween(String src, String startMarker, String stopMarker) {
        int start = src.indexOf(startMarker);
        
        if (start == -1) {
            return null;
        }
        
        start += startMarker.length();
        int stop = src.indexOf(stopMarker, start);
        
        if (stop == -1) {
            return null;
        }
        
        return src.substring(start, stop);
    }
}
