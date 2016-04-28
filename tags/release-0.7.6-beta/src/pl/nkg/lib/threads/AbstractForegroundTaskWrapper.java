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

package pl.nkg.lib.threads;

import java.io.Serializable;

import pl.nkg.lib.dialogs.AbstractProgressDialogWrapper;
import android.annotation.SuppressLint;
import android.app.Application;
import android.os.AsyncTask;
import android.util.Pair;

public abstract class AbstractForegroundTaskWrapper<Param, Progress extends Serializable, Result> {
    public class Thread extends AsyncTask<Object, Object, Result> implements ICancelable {

        public void publish(final Object... progress) {
            super.publishProgress(progress);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Result doInBackground(final Object... params) {
            try {
                return runInBackground(this, (Param) params[0]);
            } catch (final Throwable t) {
                exception = t;
                return null;
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            fireBreak(null);

            dismissProgressDialog();
            thread = null;
            handler.terminateTask(AbstractForegroundTaskWrapper.this);
        }

        @SuppressLint("NewApi")
        @Override
        protected void onCancelled(final Result result) {
            super.onCancelled(result);
            fireBreak(result);
            thread = null;
            handler.terminateTask(AbstractForegroundTaskWrapper.this);
        }

        @Override
        protected void onPostExecute(final Result result) {
            super.onPostExecute(result);

            if (result == null) {
                fireError();
            } else {
                fireFinish(result);
            }

            dismissProgressDialog();
            thread = null;
            handler.terminateTask(AbstractForegroundTaskWrapper.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressDialogWrapper != null) {
                progressDialogWrapper.show(null);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onProgressUpdate(final Object... values) {
            super.onProgressUpdate(values);
            setProgress((Progress) values[0]);
        }

    }

    private Param param;
    private ForegroundTaskHandler handler;

    private final int id;
    private AbstractProgressDialogWrapper<Progress> progressDialogWrapper;
    private TaskListener<Param, Progress, Result> listener;

    private Throwable exception;

    private Thread thread;
    private Pair<Param, Result> nofiredFinish;
    private Pair<Param, Throwable> nofiredError;

    private int nofired;

    private final Application application;

    protected AbstractForegroundTaskWrapper(final Application application,
            final int id) {
        this.id = id;
        this.application = application;
    }

    public void attach(
            final AbstractProgressDialogWrapper<Progress> progressDialogWrapper,
            final TaskListener<Param, Progress, Result> listener) {
        this.progressDialogWrapper = progressDialogWrapper;
        this.listener = listener;
        progressDialogWrapper.setTask(this);
        if (thread == null) {
            progressDialogWrapper.dismiss();
        }

        if (nofired > 0) {
            switch (nofired) {
                case 1:
                    listener.onFinish(this, nofiredFinish.first,
                            nofiredFinish.second);
                    break;

                case 2:
                    listener.onBreak(this, nofiredFinish.first,
                            nofiredFinish.second);
                    break;

                case 3:
                    listener.onError(this, nofiredError.first, nofiredError.second);
                    break;
            }

            cleanNofired();
        }
    }

    public void cancel(final boolean force) {
        if (thread != null) {
            thread.cancel(force);
        }
    }

    public void detach() {
        progressDialogWrapper = null;
        listener = null;
    }

    public void execute(final Param param) {
        setParam(param);
        cleanNofired();
        thread = new Thread();
        thread.execute(new Object[] {
                param
        });
    }

    public Application getApplication() {
        return application;
    }

    public int getID() {
        return id;
    }

    public boolean isFinished() {
        return thread == null;
    }

    public void setParam(final Param param) {
        this.param = param;
    }

    private void cleanNofired() {
        nofired = 0;
        nofiredFinish = null;
        nofiredError = null;
    }

    private void dismissProgressDialog() {
        if (progressDialogWrapper != null) {
            progressDialogWrapper.dismiss();
        }
    }

    private void fireBreak(final Result result) {
        if (listener != null) {
            listener.onBreak(this, param, result);
        } else {
            nofired = 2;
            nofiredFinish = new Pair<Param, Result>(param, result);
        }
    }

    private void fireError() {
        if (listener != null) {
            listener.onError(this, param, exception);
        } else {
            nofired = 3;
            nofiredError = new Pair<Param, Throwable>(param, exception);
        }
    }

    private void fireFinish(final Result result) {
        if (listener != null) {
            listener.onFinish(this, param, result);
        } else {
            nofired = 1;
            nofiredFinish = new Pair<Param, Result>(param, result);
        }
    }

    private void setProgress(final Progress progress) {
        if (progressDialogWrapper != null) {
            progressDialogWrapper.setProgress(progress);
            progressDialogWrapper.updateProgress();
        }
    }

    protected void publishProgress(final Progress progress) {
        if (thread != null) {
            thread.publish(new Object[] {
                    progress
            });
        }
    }

    protected abstract Result runInBackground(Thread thread, Param param) throws Throwable;

    void setHandler(final ForegroundTaskHandler handler) {
        this.handler = handler;
    }
}
