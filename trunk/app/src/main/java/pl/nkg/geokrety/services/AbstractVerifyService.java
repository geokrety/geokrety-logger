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

package pl.nkg.geokrety.services;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.activities.controls.NotifyTextView;
import pl.nkg.geokrety.data.StateHolder;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

abstract public class AbstractVerifyService extends IntentService {

    public static final String INTENT_VALUE = "value";
    public static final String INTENT_MESSAGE = "message";
    public static final String INTENT_MESSAGE_TYPE = "message_type";
    public static final String INTENT_RESPONSE = "response";

    protected GeoKretyApplication application;
    protected StateHolder stateHolder;
    protected String logTag;
    private final String mBroadcast;

    public AbstractVerifyService(final String name, final String broadcast) {
        super(name);
        logTag = name;
        mBroadcast = broadcast;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = (GeoKretyApplication) getApplication();
        stateHolder = application.getStateHolder();
        Log.println(Log.INFO, logTag, "Create");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final String value = intent.getExtras().getString(INTENT_VALUE);
        Log.println(Log.INFO, logTag, "Run verify service for " + value + "...");
        try {
            onHandleValue(value);

        } catch (final Throwable e) {
            e.printStackTrace();
            final String msg = Utils.formatException(e);
            Log.println(Log.ERROR, logTag, msg);
            sendBroadcast(value, "", NotifyTextView.ERROR, msg);
        }

        Log.println(Log.INFO, logTag, "Finish verify service for " + value);
    }

    abstract protected void onHandleValue(CharSequence value) throws Exception;

    protected void sendBroadcast(final CharSequence value, final CharSequence response,
            final int type, final CharSequence message) {
        final Intent broadcast = new Intent(mBroadcast);
        broadcast.putExtra(INTENT_MESSAGE_TYPE, type);
        broadcast.putExtra(INTENT_VALUE, value);
        broadcast.putExtra(INTENT_MESSAGE, message);
        broadcast.putExtra(INTENT_RESPONSE, response);
        sendBroadcast(broadcast);
    }
}
