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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.data.GeocacheLog;
import pl.nkg.geokrety.exceptions.MessagedException;
import android.text.Html;

public class GeocachingProvider {

    public static final String FORMAT_DATE_GEOCACHING = "MM/dd/yyyy";
    public static final int PORTAL = 100;
    private static final int LOGS_LIMIT = 20;
    
    private static final DateFormat dateFormat;

    static {
        dateFormat = new SimpleDateFormat(FORMAT_DATE_GEOCACHING, Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
    }
    
    public static List<GeocacheLog> loadOpenCachingLogs(String login, String password) throws MessagedException {

        String[][] postData = new String[][] {
                new String[] { "ctl00$tbUsername", login },
                new String[] { "ctl00$tbPassword", password } };
        try {
            List<GeocacheLog> openCachingLogs = new LinkedList<GeocacheLog>();

            HttpContext httpContext = new BasicHttpContext();
            Utils.httpPost("https://www.geocaching.com/login/default.aspx", postData, httpContext);
            String html = Utils.httpGet("http://www.geocaching.com/my/logs.aspx", new String[][] {new String[] {"s","1"}}); 
            String table = extractTable(html);
            
            if (table != null) {
                String[] rows = table.split("</tr>");
                int i = 0;
                for (String row : rows) {
                    GeocacheLog log = extractGeocache(row);
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
            e.printStackTrace();
            throw new MessagedException(R.string.oclogs_error_message);
        }
    }
    
    private static GeocacheLog extractGeocache(String row) throws ClientProtocolException, IOException {
        try {
            String[] cells = row.split("</td>");
            
            String logType = extractLogType(cells[0]);
            Date date = extractDate(cells[2]);
            
            date = new Date(date.getTime() + 12 * 60 * 60 * 1000);
            
            String name = extractName(cells[3]);
            String guid = extractGUID(cells[3]);
            
            // FIXME: extract to separated query
            String htmlCache = Utils.httpGet("http://www.geocaching.com/seek/cache_details.aspx", new String[][] {new String[] {"guid", guid}});
            String waypoint = extractWaypoint(htmlCache);

            // FIXME: name as comment? must be changed
            return new GeocacheLog(guid, waypoint, logType, date, name, PORTAL);
        } catch (NullPointerException e) {
            return null;
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private static String extractTable(String src) {
        return extractBetween(src, "<table class=\"Table\">", "</table>");
    }
    
    private static String extractLogType(String src) {
        return extractBetween(src, "title=\"", "\"");
    }
    
    private static Date extractDate(String src) {
        try {
            return dateFormat.parse(Html.fromHtml(src).toString().trim());
        } catch (ParseException e) {
            return null;
        }
    }
    
    private static String extractWaypoint(String str) {
        String title = extractBetween(str, "<title>", "</title>").trim();
        return title.substring(0, 7);
    }
    
    private static String extractGUID(String src) {
        return extractBetween(src, "guid=", "\"");
    }
    
    private static String extractName(String src) {
        return Html.fromHtml("<a" + extractBetween(src, "</a> <a", "</a>")).toString().trim();
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
