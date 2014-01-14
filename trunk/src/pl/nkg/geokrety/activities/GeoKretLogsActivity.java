/*
 * Copyright (C) 2014 Michał Niedźwiecki
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

package pl.nkg.geokrety.activities;

import java.io.Serializable;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.data.GeoKretLog;
import pl.nkg.geokrety.data.GeoKretLogDataSource;
import pl.nkg.geokrety.data.StateHolder;
import pl.nkg.geokrety.data.User;
import pl.nkg.lib.dialogs.AbstractDialogWrapper;
import pl.nkg.lib.dialogs.ManagedDialogsActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class GeoKretLogsActivity extends ManagedDialogsActivity implements
        AdapterView.OnItemSelectedListener, OnItemClickListener {

    private class Adapter extends CursorAdapter {

        private final int layout;
        private final LayoutInflater inflater;

        public Adapter(final Context context, final Cursor c, final boolean autoRequery) {
            super(context, c, true);
            layout = R.layout.row_log;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            final GeoKretLog log = new GeoKretLog(cursor, 0, false);
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

        private CharSequence formatProfileName(GeoKretLog log) {
            // TODO Auto-generated method stub
            return holder.getAccountByID(log.getAccoundID()).getName();
        }

        private CharSequence formatCacheName(GeoKretLog log) {
            // TODO Auto-generated method stub
            if (StateHolder.getGeoacheMap().containsKey(log.getWpt())) {
                return StateHolder.getGeoacheMap().get(log.getWpt()).getName();
            } else {
                return "";
            }
        }

        private void bindCacheCode(View view, GeoKretLog log) {
            TextView cacheCodeTextView = (TextView)view.findViewById(R.id.cacheCodeTextView);
            cacheCodeTextView.setText(Utils.isEmpty(log.getWpt()) ? log.getLatlon() : log.getWpt());
            
            cacheCodeTextView.setCompoundDrawablesWithIntrinsicBounds(adjustCacheDrawable(log), 0, 0, 0);
        }

        private int adjustCacheDrawable(GeoKretLog log) {
            // TODO Auto-generated method stub
            if (StateHolder.getGeoacheMap().containsKey(log.getWpt())) {
                String type = StateHolder.getGeoacheMap().get(log.getWpt()).getType();
                
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

        private CharSequence formatGeoKretName(GeoKretLog log) {
            // TODO Auto-generated method stub
            return "";
        }

        private CharSequence formatGeoKretCode(GeoKretLog log) {
            // TODO Auto-generated method stub
            return log.getNr();
        }

        @Override
        public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
            return inflater.inflate(layout, parent, false);
        }

        private void bindIcon(final View view, final GeoKretLog log) {
            final int drawable = checkHumanGeokret(log) ? LOG_TYPE_ICON_MAP_HUMAN[log.getLogTypeMapped()] : LOG_TYPE_ICON_MAP_GK[log.getLogTypeMapped()];
            ((ImageView) view.findViewById(android.R.id.icon)).setImageDrawable(getResources()
                    .getDrawable(drawable));
        }

        private boolean checkHumanGeokret(GeoKretLog log) {
            // TODO Auto-generated method stub
            return false;
        }

        private void bindTextView(final View view, final int id, final CharSequence content) {
            ((TextView) view.findViewById(id)).setText(content);
        }

        private CharSequence formatErrorMessage(final GeoKretLog log) {
            if (log.getProblem() == R.string.warning_already_logged) {
                return getText(R.string.warning_already_logged);
            } else {
                return getText(log.getProblem()) + " " + log.getProblemArg();
            }
        }

        private CharSequence formatStatus(final GeoKretLog log) {
            // TODO: use values
            switch (log.getState()) {
                case GeoKretLog.STATE_DRAFT:
                    return "draft";

                case GeoKretLog.STATE_NEW:
                    // TODO: probably newer used
                    return "new";

                case GeoKretLog.STATE_PROBLEM:
                    return log.getProblem() == R.string.warning_already_logged ? "double"
                            : "problem";

                case GeoKretLog.STATE_SENT:
                    return "success";

                case GeoKretLog.STATE_OUTBOX:
                    return "queue";

                default:
                    return "";
            }
        }

    }

    private static final int[] LOG_TYPE_ICON_MAP_GK = {
            R.drawable.ic_log_gk_drop_gk,
            R.drawable.ic_log_gk_grab_gk,
            R.drawable.ic_log_gk_met_gk,
            R.drawable.ic_log_gk_dipped_gk,
            R.drawable.ic_log_gk_comment_gk
    };

    // TODO: in future
    private static final int[] LOG_TYPE_ICON_MAP_HUMAN = {
            R.drawable.ic_log_gk_drop_human,
            R.drawable.ic_log_gk_grab_human,
            R.drawable.ic_log_gk_met_human,
            R.drawable.ic_log_gk_dipped_human,
            R.drawable.ic_log_gk_comment_gk
    };

    private User account;
    private SQLiteDatabase database;

    private Cursor geoKretLogsCursor;
    private ListView listView;
    
    private GeoKretyApplication application;
    private StateHolder holder;

    @Override
    public void dialogFinished(final AbstractDialogWrapper<?> dialog, final int buttonId,
            final Serializable arg) {
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position,
            final long id) {
        final Intent intent = new Intent(this, LogActivity.class);
        intent.putExtra(GeoKretLogDataSource.COLUMN_ID, id);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(final AdapterView<?> arg0, final View arg1, final int arg2,
            final long arg3) {
        listView.setAdapter(null);
        final StateHolder holder = ((GeoKretyApplication) getApplication()).getStateHolder();
        account = holder.getAccountList().get(arg2);
        updateListView();
    }

    @Override
    public void onNothingSelected(final AdapterView<?> arg0) {
    }

    private void closeCursorIfOpened() {
        if (geoKretLogsCursor != null) {
            geoKretLogsCursor.close();
            geoKretLogsCursor = null;
        }
    }

    private void refreshListView() {
        closeCursorIfOpened();
        geoKretLogsCursor = GeoKretLogDataSource
                .createLoadByUserIDCurosr(database, account.getID());

        final Adapter adapter = new Adapter(this, geoKretLogsCursor, true);
        final ListView listView = (ListView) findViewById(R.id.gklListView);
        listView.setAdapter(adapter);
    }

    private void updateListView() {
        refreshListView();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = ((GeoKretyApplication) getApplication());

        holder = application.getStateHolder();
        setContentView(R.layout.activity_geokretlogs);
        final Spinner spin = (Spinner) findViewById(R.id.accountsSpiner);
        spin.setOnItemSelectedListener(this);
        final ArrayAdapter<User> usersAdapter = new ArrayAdapter<User>(this,
                android.R.layout.simple_spinner_item, holder.getAccountList());

        usersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(usersAdapter);
        spin.setSelection(holder.getDefaultAccountNr());

        listView = (ListView) findViewById(R.id.gklListView);
        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCursorIfOpened();
        ((GeoKretyApplication) getApplication()).getStateHolder().getDbHelper().closeDatabase();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        final StateHolder holder = ((GeoKretyApplication) getApplication()).getStateHolder();
        database = holder.getDbHelper().openDatabase();
        if (holder.getDefaultAccountNr() != AdapterView.INVALID_POSITION) {
            account = holder.getDefaultAccount();
            updateListView();
        }
    }

    @Override
    protected void onStart() {
        super.onStart(); // FIXME: test me
    }
}
