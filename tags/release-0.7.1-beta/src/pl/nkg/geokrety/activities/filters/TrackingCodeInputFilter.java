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

public class TrackingCodeInputFilter extends RegExInputFilter {

    public static final Pattern CAPS_LETTERS_AND_DIGITS_PATTERN = Pattern.compile("[a-zA-Z0-9]*");
    public static final Pattern TRACKING_CODE_PATTERN = Pattern.compile("^[a-zA-Z0-9]{6}$");

    public TrackingCodeInputFilter() {
        super(CAPS_LETTERS_AND_DIGITS_PATTERN, TRACKING_CODE_PATTERN);
    } 
}
