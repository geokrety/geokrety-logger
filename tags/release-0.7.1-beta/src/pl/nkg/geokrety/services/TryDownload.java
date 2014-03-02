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

import pl.nkg.geokrety.exceptions.NoConnectionException;

public abstract class TryDownload<O> {
    // FIXME: implement cancellation

    private int mCount = 0;
    private int mRetry = 0;
    
    public O tryRun(int count) throws Exception {
        if (mCount != 0) {
            throw new RuntimeException("tryRun method not finished");
        }
        mCount = count;
        
        Throwable t = null;
        
        for (mRetry = 0; mRetry < count; mRetry++) {
            try {
                O o = run();
                mCount = 0;
                return o;
            } catch (NoConnectionException e) {
                t = e;
                if (!doRetry(mRetry)) {
                    mCount = 0;
                    throw e;
                }
            }
        }
        throw new NoConnectionException(t);
    }

    protected boolean doRetry(int retry) {
        return true;
    }
    
    protected abstract O run() throws NoConnectionException, Exception;
}
