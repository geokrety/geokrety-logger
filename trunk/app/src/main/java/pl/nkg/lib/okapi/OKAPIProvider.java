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

package pl.nkg.lib.okapi;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.data.Geocache;
import pl.nkg.geokrety.data.GeocacheLog;
import pl.nkg.geokrety.exceptions.MessagedException;
import pl.nkg.geokrety.exceptions.NoConnectionException;
import android.text.TextUtils;

public class OKAPIProvider {
    private static final String URL_BY_USERNAME = "/okapi/services/users/by_username";
    private static final String URL_USERLOGS = "/okapi/services/logs/userlogs";
    private static final String URL_GEOCACHES = "/okapi/services/caches/geocaches";

    public static String getServiceURL(final SupportedOKAPI okapi, final String service) {
        return TextUtils.join("", new String[] {
                "http://www.", okapi.host,
                service
        });
    }

    public static List<Geocache> loadOCnames(final Collection<String> waypoints,
            final SupportedOKAPI okapi) throws MessagedException {

        final String[][] getData = new String[][] {
                new String[] {
                        "consumer_key", okapi.consumerKey
                },
                new String[] {
                        "fields", "name|code|location|type|status"
                },
                new String[] {
                        "cache_codes", TextUtils.join("|", waypoints)
                },
                new String[] {
                        "lpc", "0"
                },
                new String[] {
                        "langpref", Utils.getDefaultLanguage()
                }
        };

        try {
            final String jsonString = Utils.httpGet(
                    getServiceURL(okapi, URL_GEOCACHES), getData);
            final JSONObject json = new JSONObject(jsonString);
            final List<Geocache> list = new LinkedList<Geocache>();
            for (final String waypoint : waypoints) {
                if (!json.isNull(waypoint)) {
                    final Geocache geocache = new Geocache(
                            json.getJSONObject(waypoint));
                    list.add(geocache);
                }
            }
            return list;
        } catch (final JSONException e) {
            throw new MessagedException(R.string.user_oc_error_login_invalid,
                    okapi.host);
        } catch (final IOException e) {
            throw new NoConnectionException(e);
            // } catch (IOException e) { // TODO: improve connection error
            // notification
            // throw new MessagedException(R.string.lastlogs_error_refresh);
        }
    }

    public static List<GeocacheLog> loadOpenCachingLogs(final SupportedOKAPI okapi,
            final String user_uuid) throws MessagedException {

        final String[][] getData = new String[][] {
                new String[] {
                        "user_uuid", user_uuid
                },
                new String[] {
                        "consumer_key", okapi.consumerKey
                }
        };
        try {
            final List<GeocacheLog> openCachingLogs = new LinkedList<GeocacheLog>();

            final String jsonString = Utils.httpGet(
                    OKAPIProvider.getServiceURL(okapi, URL_USERLOGS), getData);

            final JSONArray json = new JSONArray(jsonString);

            for (int i = 0; i < json.length(); i++) {
                openCachingLogs.add(new GeocacheLog(json.getJSONObject(i), okapi.nr));
            }

            return openCachingLogs;
        } catch (final JSONException e) {
            throw new MessagedException(R.string.user_oc_error_login_invalid,
                    okapi.host);
        } catch (final IOException e) {
            throw new NoConnectionException(e);
            // } catch (IOException e) { // TODO: better label
            // throw new MessagedException(R.string.lastlogs_error_refresh);
        } catch (final ParseException e) {
            throw new MessagedException(R.string.lastlogs_error_refresh);
        }
    }

    public static String loadOpenCachingUUID(final SupportedOKAPI okapi, final String login)
            throws MessagedException {
        final String[][] getData = new String[][] {
                new String[] {
                        "username", login
                },
                new String[] {
                        "fields", "uuid"
                },
                new String[] {
                        "consumer_key", okapi.consumerKey
                }
        };
        try {
            final String jsonString = Utils.httpGet(
                    getServiceURL(okapi, URL_BY_USERNAME), getData);
            final JSONObject json = new JSONObject(jsonString);
            return json.getString("uuid");
        } catch (final JSONException e) {
            throw new MessagedException(R.string.user_oc_error_login_invalid);
        } catch (final IOException e) { // TODO: improve conection error
                                        // notification
            throw new NoConnectionException(e);
        } catch (final Exception e) {
            throw new MessagedException(R.string.lastlogs_error_refresh);
        }
    }
}
