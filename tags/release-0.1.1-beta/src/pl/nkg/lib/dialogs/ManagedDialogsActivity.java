/*
 * Copyright (C) 2013 Michał Niedźwiecki
 * 
 * This file is part of GeoKrety Logger
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

import java.io.Serializable;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

/**
 * Na podstawie książki: Android 2 Tworzenie aplikacji<br/>
 * English title: Pro Android 2<br/>
 * Authors: Sayed Hashimi, Satya Komatineni, Dave MacLean<br/>
 * ISBN: 978-83-246-2754-7
 * 
 */
public abstract class ManagedDialogsActivity extends Activity implements
		IDialogFinishedCallBack {

	private DialogRegistry dr = new DialogRegistry();

	public void registerDialog(IDialogProtocol<?> dialog) {
		dr.registerDialog(dialog);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return dr.create(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		dr.prepare(dialog, id);
	}

	@Override
	abstract public void dialogFinished(AbstractDialogWrapper<?> dialog,
			int buttonId, Serializable arg);

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		dr.saveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		dr.restoreInstanceState(savedInstanceState);
	}
}
