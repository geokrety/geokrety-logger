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

package pl.nkg.geokrety.activities;

import java.io.Serializable;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.data.GeoKret;
import pl.nkg.geokrety.data.GeoKretDataSource;
import pl.nkg.geokrety.data.GeoKretLog;
import pl.nkg.geokrety.data.GeoKretLogDataSource;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.data.User;
import pl.nkg.geokrety.dialogs.Dialogs;
import pl.nkg.geokrety.services.LogSubmitterService;
import pl.nkg.lib.adapters.ExtendedCursorAdapter;
import pl.nkg.lib.dialogs.AbstractDialogWrapper;
import pl.nkg.lib.dialogs.AlertDialogWrapper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class GeoKretLogsActivity extends AbstractGeoKretyActivity implements
        AdapterView.OnItemSelectedListener, OnItemClickListener {

    private class Adapter extends ExtendedCursorAdapter {

        public Adapter(final Context context, final Cursor c, final boolean autoRequery) {
            super(context, c, true, R.layout.row_log);
        }

        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            final GeoKretLog log = new GeoKretLog(cursor, 0, false, true);
            bindIcon(view, log);
            bindTextView(view, R.id.gkCodeTextView, formatGeoKretCode(log));
            bindTextView(view, R.id.gkNameTextView, formatGeoKretName(log));
            // bindTextView(view, R.id.commentTextView, log.getComment());
            if (log.getState() == GeoKretLog.STATE_PROBLEM) {
                view.findViewById(R.id.errorTextView).setVisibility(View.VISIBLE);
                view.findViewById(R.id.logDescriptionLinearLayout).setVisibility(View.GONE);
                bindTextView(view, R.id.errorTextView, formatErrorMessage(log));
            } else {
                view.findViewById(R.id.errorTextView).setVisibility(View.GONE);
                view.findViewById(R.id.logDescriptionLinearLayout).setVisibility(View.VISIBLE);
                bindCacheCode(view, log);
                bindTextView(view, R.id.cacheNameTextView, formatCacheName(log));
                bindTextView(view, R.id.profileContentTextView, formatProfileName(log));
                bindTextView(view, R.id.statusContentTextView, formatStatus(log));
            }
        }

        private int adjustCacheDrawable(final GeoKretLog log) {
            if (log.getGeoCache() != null && log.getGeoCache().getType() != null) {
                final String type = log.getGeoCache().getType();

                if (type.equals("Traditional")) {
                    return R.drawable.ic_cache_traditional_small;
                } else if (type.equals("Multi")) {
                    return R.drawable.ic_cache_multi_small;
                } else if (type.equals("Quiz")) {
                    return R.drawable.ic_cache_quiz_small;
                } else if (type.equals("Virtual")) {
                    return R.drawable.ic_cache_virtual_small;
                } else if (type.equals("Event")) {
                    return R.drawable.ic_cache_event_small;
                } else {
                    return R.drawable.ic_cache_unknown_small;
                }
            }
            return 0;
        }

        private void bindCacheCode(final View view, final GeoKretLog log) {
            final TextView cacheCodeTextView = (TextView) view.findViewById(R.id.cacheCodeTextView);
            cacheCodeTextView.setText(Utils.isEmpty(log.getWpt()) ? log.getLatlon() : log.getWpt());

            cacheCodeTextView.setCompoundDrawablesWithIntrinsicBounds(adjustCacheDrawable(log), 0,
                    0, 0);
        }

        private void bindIcon(final View view, final GeoKretLog log) {
            if (log.getLogTypeMapped() > 0) {
                final int drawable = checkHumanGeokret(log) ? LOG_TYPE_ICON_MAP_HUMAN[log
                        .getLogTypeMapped()] : LOG_TYPE_ICON_MAP_GK[log.getLogTypeMapped()];
                ImageView im = ((ImageView) view.findViewById(android.R.id.icon));
                Drawable image = getResources().getDrawable(drawable);
                im.setImageDrawable(image);
            }
        }

        private boolean checkHumanGeokret(final GeoKretLog log) {
            if (log.getGeoKret() != null && log.getGeoKret().getType() != null) {
                return log.getGeoKret().getType() == GeoKret.TYPE_HUMAN;
            }
            return false;
        }

        private CharSequence formatCacheName(final GeoKretLog log) {
            if (log.getGeoCache() != null) {
                return log.getGeoCache().getName();
            } else {
                return "";
            }
        }

        private CharSequence formatErrorMessage(final GeoKretLog log) {
            if (log.getProblem() == R.string.warning_already_logged) {
                return getText(R.string.warning_already_logged);
            } else {
                try {
                    return getText(log.getProblem()) + " " + log.getProblemArg();
                } catch (NotFoundException e) {
                    return log.getProblemArg();
                }
            }
        }

        private CharSequence formatGeoKretCode(final GeoKretLog log) {
            if (log.getGeoKret() != null) {
                return log.getGeoKret().getFormatedCode() + " (" + log.getNr() + ")";
            } else {
                return log.getNr();
            }
        }

        private CharSequence formatGeoKretName(final GeoKretLog log) {
            if (log.getGeoKret() == null || log.getGeoKret().getSynchroState() == GeoKretDataSource.SYNCHRO_STATE_UNSYNCHRONIZED) {
                return "...";
            }
            if (log.getGeoKret().getSynchroState() == GeoKretDataSource.SYNCHRO_STATE_ERROR) {
                return log.getGeoKret().getSynchroError();
            }
            return log.getGeoKret().getName();
        }

        private CharSequence formatProfileName(final GeoKretLog log) {
            // TODO Auto-generated method stub
            return stateHolder.getAccountByID(log.getAccoundID()).getName();
        }

        private CharSequence formatStatus(final GeoKretLog log) {
            switch (log.getState()) {
                case GeoKretLog.STATE_DRAFT:
                    return getText(R.string.log_status_draft);

                case GeoKretLog.STATE_NEW:
                    // TODO: probably newer used
                    return getText(R.string.log_status_new);

                case GeoKretLog.STATE_PROBLEM:
                    return log.getProblem() == R.string.warning_already_logged ? getText(R.string.log_status_double)
                            : getText(R.string.log_status_problem);

                case GeoKretLog.STATE_SENT:
                    return getText(R.string.log_status_success);

                case GeoKretLog.STATE_OUTBOX:
                    if (stateHolder.isLocked(log.getId())) {
                        return getText(R.string.dots); // TODO: label
                    } else {
                        return getText(R.string.log_status_queue);
                    }

                default:
                    return getText(R.string.log_status_unidentified);
            }
        }

    }

    private final BroadcastReceiver submitDoneBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            onRefreshDatabase();
        }
    };

    private static final int[] LOG_TYPE_ICON_MAP_GK = {
            R.drawable.ic_log_gk_drop_gk,
            R.drawable.ic_log_gk_grab_gk,
            R.drawable.ic_log_gk_met_gk,
            R.drawable.ic_log_gk_dipped_gk,
            R.drawable.ic_log_gk_comment_gk
    };

    private static final int[] LOG_TYPE_ICON_MAP_HUMAN = {
            R.drawable.ic_log_gk_drop_human,
            R.drawable.ic_log_gk_grab_human,
            R.drawable.ic_log_gk_met_human,
            R.drawable.ic_log_gk_dipped_human,
            R.drawable.ic_log_gk_comment_gk
    };

    private User account;
    private ListView listView;

    private Spinner accountsSpinner;

    private AlertDialogWrapper removeLogDialog;
    private Adapter adapter;

    @Override
    public void dialogFinished(final AbstractDialogWrapper<?> dialog, final int buttonId,
            final Serializable arg) {
        if (dialog.getDialogId() == removeLogDialog.getDialogId()) {
            if (buttonId == DialogInterface.BUTTON_POSITIVE) {
                application.getStateHolder().getGeoKretLogDataSource()
                        .removeAllLogs(account.getID(), stateHolder.getLocked());
                updateListView();
            }
        }
    }

    public void onClickRemoveAll(final View view) {
        removeLogDialog.show(null);
    }

    public void onClickSubmitAll(final View view) {
        application.getStateHolder().getGeoKretLogDataSource()
                .moveAllDraftsToOutbox(account.getID());
        updateListView();
        startService(new Intent(this, LogSubmitterService.class));
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position,
            final long id) {
        if (!stateHolder.isLocked(id)) {
            final Intent intent = new Intent(this, LogActivity.class);
            intent.putExtra(GeoKretLogDataSource.COLUMN_ID, id);
            startActivity(intent);
        }
    }

    @Override
    public void onItemSelected(final AdapterView<?> arg0, final View arg1, final int arg2,
            final long arg3) {
        final StateHolder holder = ((GeoKretyApplication) getApplication()).getStateHolder();
        account = holder.getAccountList().get(arg2);
        updateListView();
    }

    @Override
    public void onNothingSelected(final AdapterView<?> arg0) {
    }

    private void updateListView() {
        onRefreshDatabase();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        turnOnDatabaseUse();
        removeLogDialog = new AlertDialogWrapper(this, Dialogs.REMOVE_ALL_LOGS_ALERTDIALOG);
        removeLogDialog.setMessage(R.string.form_confirm_delete_all_msg);
        removeLogDialog.setOkCancelButtons();

        setContentView(R.layout.activity_geokretlogs);
        accountsSpinner = (Spinner) findViewById(R.id.accountsSpiner);
        accountsSpinner.setOnItemSelectedListener(this);
        final ArrayAdapter<User> usersAdapter = new ArrayAdapter<User>(this,
                android.R.layout.simple_spinner_item, stateHolder.getAccountList());

        usersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountsSpinner.setAdapter(usersAdapter);
        accountsSpinner.setSelection(stateHolder.getDefaultAccountNr());

        listView = (ListView) findViewById(R.id.gklListView);
        listView.setOnItemClickListener(this);
    }
    
    @Override
    protected Cursor openCursor() {
        super.openCursor();
        if (account != null && database != null) {
            cursor = GeoKretLogDataSource.createLoadByUserIDCurosr(database, account.getID());
            if (adapter == null) {
                adapter = new Adapter(this, cursor, true);
                final ListView listView = (ListView) findViewById(R.id.gklListView);
                listView.setAdapter(adapter);
            } else {
                adapter.changeCursor(cursor);
            } 
        }
        return cursor;
    }
    
    @Override
    protected void onRefreshDatabase() {
        super.onRefreshDatabase();
        openCursor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(submitDoneBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final StateHolder holder = ((GeoKretyApplication) getApplication()).getStateHolder();
        final int nr = accountsSpinner.getSelectedItemPosition();
        if (nr != AdapterView.INVALID_POSITION) {
            account = holder.getAccountList().get(nr);
            updateListView();
        }
        registerReceiver(submitDoneBroadcastReceiver, new IntentFilter(
                LogSubmitterService.BROADCAST_SUBMIT_START));
        registerReceiver(submitDoneBroadcastReceiver, new IntentFilter(
                LogSubmitterService.BROADCAST_SUBMIT_DONE));
    }
}
