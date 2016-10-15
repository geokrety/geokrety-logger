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

package pl.nkg.lib.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.util.SparseArray;

/**
 * Na podstawie książki: Android 2 Tworzenie aplikacji<br/>
 * English title: Pro Android 2<br/>
 * Authors: Sayed Hashimi, Satya Komatineni, Dave MacLean<br/>
 * ISBN: 978-83-246-2754-7
 */
public class DialogRegistry {
    SparseArray<IDialogProtocol<? extends Dialog>> idsToDialogs = new SparseArray<IDialogProtocol<?>>();

    public Dialog create(final int id) {
        final IDialogProtocol<?> dp = idsToDialogs.get(id);
        if (dp == null) {
            return null;
        }
        return dp.create();
    }

    @SuppressWarnings("unchecked")
    public void prepare(final Dialog dialog, final int id) {
        @SuppressWarnings("rawtypes")
        final IDialogProtocol dp = idsToDialogs.get(id);
        if (dp == null) {
            throw new RuntimeException("Dialog id is not registered: " + id);
        }
        dp.prepare(dialog);
    }

    public void registerDialog(final IDialogProtocol<?> dialog) {
        idsToDialogs.put(dialog.getDialogId(), dialog);
    }

    public void restoreInstanceState(final Bundle savedInstanceState) {
        for (int i = 0; i < idsToDialogs.size(); i++) {
            idsToDialogs.get(idsToDialogs.keyAt(i)).restoreInstanceState(
                    savedInstanceState);
        }
    }

    public void saveInstanceState(final Bundle outState) {
        for (int i = 0; i < idsToDialogs.size(); i++) {
            idsToDialogs.get(idsToDialogs.keyAt(i)).saveInstanceState(outState);
        }
    }
}
