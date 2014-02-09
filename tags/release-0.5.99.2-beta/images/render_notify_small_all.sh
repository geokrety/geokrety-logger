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
# Depends: inkscape, render.sh, render_notify_small.sh
#
# Example usage:
# ./render_notify_small_all.sh

for src in ic_stat_notify_log_double ic_stat_notify_log_draft ic_stat_notify_log_no_connection ic_stat_notify_log_problem ic_stat_notify_log_submitting_anim_10 ic_stat_notify_log_submitting_anim_11 ic_stat_notify_log_submitting_anim_12 ic_stat_notify_log_submitting_anim_13 ic_stat_notify_log_submitting_anim_14 ic_stat_notify_log_submitting_anim_15 ic_stat_notify_log_submitting_anim_16 ic_stat_notify_log_submitting_anim_17 ic_stat_notify_log_submitting_anim_18 ic_stat_notify_log_submitting_anim_1 ic_stat_notify_log_submitting_anim_2 ic_stat_notify_log_submitting_anim_3 ic_stat_notify_log_submitting_anim_4 ic_stat_notify_log_submitting_anim_5 ic_stat_notify_log_submitting_anim_6 ic_stat_notify_log_submitting_anim_7 ic_stat_notify_log_submitting_anim_8 ic_stat_notify_log_submitting_anim_9 ic_stat_notify_log_success
do
  ./render_notify_small.sh $src
done
