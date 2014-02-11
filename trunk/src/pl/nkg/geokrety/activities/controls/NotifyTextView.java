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
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class NotifyTextView extends TextView {

    // TODO: make a my NotifyTextView control
    public static final int GOOD = 0;
    public static final int INFO = 1;
    public static final int WARNING = 2;
    public static final int ERROR = 3;
    
    private static final int[] COLORS = new int[] {
        R.color.valid_color, R.color.info_color, R.color.warning_color, R.color.error_color
};

    public NotifyTextView(Context context) {
        super(context);
    }

    public NotifyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotifyTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setLabel(CharSequence content, int color) {
        setTextColor(getResources().getColor(COLORS[color]));
        setText(content);
    }
}
