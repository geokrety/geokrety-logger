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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.data.Geocache;
import pl.nkg.geokrety.data.GeocacheLog;
import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.geokrety.exceptions.NoConnectionException;
import pl.nkg.geokrety.exceptions.WaypointNotFoundException;

public class GeocachingProvider {

    public static final String FORMAT_DATE_GEOCACHING = "MM/dd/yyyy";
    public static final int PORTAL = 100;
    public static final String HOST = "geocaching.com";
    private static final int LOGS_LIMIT = 20;

    public static DateFormat detectDateFormat(final HttpContext httpContext)
            throws MessagedException {
        try {
            final String html = Utils.httpGet(
                    "https://www.geocaching.com/account/settings/preferences", new String[][] {},
                    httpContext);

            final String dateFormatSelect = extractBetween(html,
                    "SelectedDateFormat", "</select>");
            final String pattern = extractBetween(dateFormatSelect,
                    "<option selected=\"selected\" value=\"", "\"");
            return new SimpleDateFormat(pattern, Locale.US);
        } catch (final IOException e) {
            throw new NoConnectionException(e);
        } catch (final Throwable e) {
            throw new MessagedException(R.string.lastlogs_error_refresh, e.getLocalizedMessage());
        }
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

    public static Geocache loadGeocacheByGUID(final HttpContext httpContext, final String guid)
            throws MessagedException {

        final String[][] postData = new String[][] {
                new String[] {
                        "guid", guid
                }
        };
        try {
            final String htmlCache = Utils.httpGet(
                    "https://www.geocaching.com/seek/cache_details.aspx", postData, httpContext);
            return Geocache.parseGeocachingCom(htmlCache);
        } catch (final IOException e) {
            throw new NoConnectionException(e);
        } catch (final Throwable e) {
            throw new MessagedException(R.string.lastlogs_error_refresh, e.getLocalizedMessage());
        }
    }

    public static Geocache loadGeocacheByWaypoint(final HttpContext httpContext,
            final String waypoint) throws MessagedException {

        final String[][] postData = new String[][] {
                new String[] {
                        "wp", waypoint
                }
        };
        try {
            final HttpResponse response = Utils.httpGetResponse(
                    "https://www.geocaching.com/seek/cache_details.aspx", postData, httpContext);

            if (response.getStatusLine().getStatusCode() == 404) {
                throw new WaypointNotFoundException(waypoint);
            }

            final String htmlCache = Utils.responseToString(response);

            if (htmlCache.contains("File Not Found")) {
                throw new WaypointNotFoundException(waypoint);
            }

            return Geocache.parseGeocachingCom(htmlCache);
        } catch (final WaypointNotFoundException e) {
            throw e;
        } catch (final IOException e) {
            throw new NoConnectionException(e);
        } catch (final Throwable e) {
            throw new MessagedException(R.string.lastlogs_error_refresh, e.getLocalizedMessage()
                    + ": " + waypoint);
        }
    }

    public static List<GeocacheLog> loadGeocachingComLogs(final HttpContext httpContext)
            throws MessagedException {
        final String[][] postData = new String[][] {
                new String[] {
                        "s", "1"
                }
        };
        try {
            final List<GeocacheLog> openCachingLogs = new LinkedList<GeocacheLog>();

            final DateFormat dateFormat = detectDateFormat(httpContext);
            final String html = Utils.httpGet("https://www.geocaching.com/my/logs.aspx", postData,
                    httpContext);
            final String table = extractTable(html);

            if (table != null) {
                final String[] rows = table.split("</tr>");
                int i = 0;
                for (final String row : rows) {
                    final GeocacheLog log = extractGeocacheLog(row, dateFormat);
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
        } catch (final IOException e) {
            throw new NoConnectionException(e);
        } catch (final Throwable e) {
            throw new MessagedException(R.string.lastlogs_error_refresh, e.getLocalizedMessage());
        }
    }

    public static HttpContext login(final String login, final String password)
            throws MessagedException {
        final String[][] postData = new String[][] {
	        	new String[] {
	                    "__EVENTTARGET", ""
	            },    
	        	new String[] {
	                    "__EVENTARGUMENT", ""
	            },    
	        	new String[] {
                        "ctl00$ContentBody$tbUsername", login
                },
                new String[] {
                        "ctl00$ContentBody$tbPassword", password
                },
	        	new String[] {
	                    "ctl00$ContentBody$cbRememberMe", "on"
	            },    
	        	new String[] {
	                    "ctl00$ContentBody$btnSignIn", "Login"
	            }
        };
        try {
            final HttpContext httpContext = new BasicHttpContext();
            final String ret = Utils.httpPost("https://www.geocaching.com/login/default.aspx",
                    postData, httpContext);
            if (ret.contains("You are signed in as")) {
                return httpContext;
            }
            return null;
        } catch (final IOException e) {
            throw new NoConnectionException(e);
        } catch (final Throwable e) {
            throw new MessagedException(R.string.lastlogs_error_refresh, e.getLocalizedMessage());
        }
    }

    private static GeocacheLog extractGeocacheLog(final String row, final DateFormat dateFormat)
            throws ClientProtocolException,
            IOException {
        try {
            return GeocacheLog.fromGeocachingCom(row, dateFormat);
        } catch (final NullPointerException e) {
            return null;
        } catch (final ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private static String extractTable(final String src) {
        return extractBetween(src, "<table class=\"Table\">", "</table>");
    }
}
