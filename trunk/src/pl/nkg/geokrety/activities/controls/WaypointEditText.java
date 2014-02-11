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

package pl.nkg.geokrety.activities.controls;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.activities.filters.WaypointInputFilter;
import pl.nkg.geokrety.services.WaypointResolverService;
import android.content.Context;
import android.text.InputFilter;
import android.util.AttributeSet;

public class WaypointEditText extends AbstractNotifiedEditText {

    public WaypointEditText(final Context context) {
        super(context, new WaypointInputFilter());
        init();
    }

    public WaypointEditText(final Context context, final AttributeSet attrs) {
        super(context, attrs, new WaypointInputFilter());
        init();
    }

    public WaypointEditText(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle, new WaypointInputFilter());
        init();
    }

    private void init() {
        setFilters(new InputFilter[] {
                regExInputFilter
        });
    }

    @Override
    protected CharSequence getInvalidateMessage() {
        return "";
    }

    @Override
    protected String getServiceBroadcast() {
        return WaypointResolverService.BROADCAST;
    }

    @Override
    protected Class<?> getServiceClass() {
        return WaypointResolverService.class;
    }

    @Override
    protected CharSequence getWaitMessage() {
        return getContext().getText(R.string.resolve_wpt_message_info_waiting);
    }
}
