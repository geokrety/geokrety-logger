/*
 * Copyright (C) 2013, 2014 Michał Niedźwiecki
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

package pl.nkg.geokrety.exceptions;

import pl.nkg.geokrety.Utils;
import android.content.Context;

public class MessagedException extends Exception {
    private static final long serialVersionUID = 6866743560825681444L;

    private static String getFormatedMessage(final int messageID, final String arg) {
        try {
            return Utils.application.getApplicationContext().getResources().getString(messageID)
                    + " " + arg;
        } catch (final Throwable e) {
            return "MessagedException: " + messageID;
        }
    }

    private final int messageID;

    private final String arg;

    public MessagedException(final int messageID) {
        super(getFormatedMessage(messageID, ""));
        this.messageID = messageID;
        arg = "";
    }

    public MessagedException(final int message, final String arg) {
        super(getFormatedMessage(message, arg));
        messageID = message;
        this.arg = arg;
    }

    public String getFormatedMessage(final Context context) {
        final String message = context.getResources().getString(messageID) + " " + arg;
        return message;
    }

    public int getMessageID() {
        return messageID;
    }
}
