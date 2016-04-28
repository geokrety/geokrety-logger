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

package pl.nkg.geokrety.activities.listeners;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.data.User;
import pl.nkg.lib.threads.AbstractForegroundTaskWrapper;
import pl.nkg.lib.threads.GenericTaskListener;
import android.content.Context;
import android.widget.Toast;

public class RefreshListener extends
        GenericTaskListener<User, String, String> {

    public RefreshListener(final Context context) {
        super(context);
    }

    @Override
    public void onFinish(
            final AbstractForegroundTaskWrapper<User, String, String> sender,
            final User param, final String result) {
        if (Utils.isEmpty(result)) {
            Toast.makeText(context, R.string.refresh_download_finish, Toast.LENGTH_LONG)
                    .show();
        } else {
            Toast.makeText(
                    context,
                    context.getText(R.string.warning).toString() + ' ' + result,
                    Toast.LENGTH_LONG).show();
        }
    }
}
