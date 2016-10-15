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

import android.util.SparseArray;

public class ForegroundTaskHandler {
    private AbstractForegroundTaskWrapper<?, ?, ?> currentTask;
    private final SparseArray<AbstractForegroundTaskWrapper<?, ?, ?>> taskMap;

    public ForegroundTaskHandler() {
        taskMap = new SparseArray<AbstractForegroundTaskWrapper<?, ?, ?>>();
    }

    @SuppressWarnings("unchecked")
    public <Params, Progress extends Serializable, Result> AbstractForegroundTaskWrapper<Params, Progress, Result> getTask(
            final int id) {
        return (AbstractForegroundTaskWrapper<Params, Progress, Result>) taskMap
                .get(id);
    }

    public boolean isBusy() {
        return currentTask != null;
    }

    public void registerTask(final AbstractForegroundTaskWrapper<?, ?, ?> task) {
        taskMap.put(task.getID(), task);
        task.setHandler(this);
    }

    @SuppressWarnings("unchecked")
    public <Params> boolean runTask(final int id, final Params params, final boolean force) {
        synchronized (this) {
            if (isBusy()) {
                if (force) {
                    currentTask.cancel(true);
                } else {
                    return false;
                }
            }
            currentTask = taskMap.get(id);
            final AbstractForegroundTaskWrapper<Params, ?, ?> task = (AbstractForegroundTaskWrapper<Params, ?, ?>) currentTask;
            task.execute(params);
            return true;
        }
    }

    public void shutdown() {
        synchronized (this) {
            if (currentTask != null) {
                currentTask.cancel(true);
            }
        }
    }

    public void terminateTask(final AbstractForegroundTaskWrapper<?, ?, ?> task) {
        synchronized (this) {
            if (currentTask != null && currentTask.getID() == task.getID()) {
                currentTask = null;
            }
        }
    }
}
