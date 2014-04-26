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

package pl.nkg.lib.threads;

import java.io.Serializable;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.exceptions.MessagedException;
import android.content.Context;
import android.widget.Toast;

public class GenericTaskListener<Param, Progress extends Serializable, Result>
        implements TaskListener<Param, Progress, Result> {
    protected final Context context;
    private Integer finishMessage = R.string.generic_finish;
    private Integer breakMessage = R.string.generic_broken;
    private Integer errorMessage = R.string.generic_error;

    public GenericTaskListener(final Context context) {
        this.context = context;
    }

    @Override
    public void onBreak(
            final AbstractForegroundTaskWrapper<Param, Progress, Result> sender,
            final Param param, final Result result) {
        if (breakMessage != null) {
            Toast.makeText(context, breakMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onError(
            final AbstractForegroundTaskWrapper<Param, Progress, Result> sender,
            final Param param, final Throwable exception) {
        if (exception instanceof MessagedException) {
            Toast.makeText(
                    context,
                    ((MessagedException) exception).getFormatedMessage(context),
                    Toast.LENGTH_LONG).show();
        } else if (errorMessage != null) {
            Toast.makeText(context,
                    errorMessage + " " + Utils.formatException(exception),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onFinish(
            final AbstractForegroundTaskWrapper<Param, Progress, Result> sender,
            final Param param, final Result result) {
        if (finishMessage != null) {
            Toast.makeText(context, finishMessage, Toast.LENGTH_LONG).show();
        }
    }

    public GenericTaskListener<Param, Progress, Result> setBreakMessage(
            final Integer breakMessage) {
        this.breakMessage = breakMessage;
        return this;
    }

    public GenericTaskListener<Param, Progress, Result> setErrorMessage(
            final Integer errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public GenericTaskListener<Param, Progress, Result> setFinishMessage(
            final Integer finishMessage) {
        this.finishMessage = finishMessage;
        return this;
    }
}
