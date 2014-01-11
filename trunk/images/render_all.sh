#!/bin/bash
# Copyright (C) 2014 Michał Niedźwiecki
# 
# This file is part of GeoKrety Logger
# http://geokretylog.sourceforge.net/
# 
# GeoKrety Logger is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 3 of the License, or
# (at your option) any later version.
# 
# This source code is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this source code; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
# or see <http://www.gnu.org/licenses/>

# Render png image from svg
# Depends: inkscape, ./render_ic_launcher.sh ./render_notify_small_all.sh ./render_notify_small.sh ./render.sh
#
# Example usage:
# ./render.sh app_icon.svg app_icon.png 512 512

./render_ic_launcher.sh
./render_notify_small_all.sh
