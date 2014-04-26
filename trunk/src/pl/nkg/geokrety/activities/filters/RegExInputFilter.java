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

package pl.nkg.geokrety.activities.filters;

import java.util.regex.Pattern;

import android.text.InputFilter;
import android.text.Spanned;

public class RegExInputFilter implements InputFilter {

    private final Pattern mFilter;
    private final Pattern mPattern;

    public RegExInputFilter(final Pattern filter) {
        mFilter = filter;
        mPattern = filter;
    }

    public RegExInputFilter(final Pattern filter, final Pattern pattern) {
        mFilter = filter;
        mPattern = pattern;
    }

    @Override
    public CharSequence filter(final CharSequence source, final int start, final int end,
            final Spanned dest, final int dstart,
            final int dend) {
        if (mFilter.matcher(source).matches()) {
            return null;
        }
        return "";
    }

    public boolean validate(final CharSequence text) {
        return mPattern.matcher(text).matches();
    }
}
