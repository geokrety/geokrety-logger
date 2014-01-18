#!/bin/bash
# Copyright (C) 2014 Michał Niedźwiecki
# 
# This file is a part of GeoKrety Logger
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

# Render app icon
# Depends: inkscape, render.sh
#
# Example usage:
# ./render_ic_launcher.sh

SRC=app_icon.svg
DST=ic_launcher.png

./render.sh $SRC ../ic_launcher-web.png 512 512
./render.sh $SRC ../res/drawable-xxxhdpi/$DST 192 192
./render.sh $SRC ../res/drawable-xxhdpi/$DST 144 144
./render.sh $SRC ../res/drawable-xhdpi/$DST 96 96
./render.sh $SRC ../res/drawable-hdpi/$DST 72 72
./render.sh $SRC ../res/drawable-mdpi/$DST 48 48
./render.sh $SRC ../res/drawable-ldpi/$DST 36 36
