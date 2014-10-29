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

import pl.nkg.geokrety.activities.filters.RegExInputFilter;
import pl.nkg.geokrety.activities.listeners.VerifyResponseListener;
import pl.nkg.geokrety.services.AbstractVerifyService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

abstract public class AbstractNotifiedEditText extends EditText {

    private NotifyTextView mNotifyTextView;
    final protected RegExInputFilter regExInputFilter;
    protected VerifyResponseListener verifyResponseListener;

    private final TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(final Editable s) {
            validate();
        }

        @Override
        public void beforeTextChanged(final CharSequence s, final int start, final int count,
                final int after) {
        }

        @Override
        public void onTextChanged(final CharSequence s, final int start, final int before,
                final int count) {
        }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (getText()
                    .toString()
                    .equals(intent.getExtras().getString(AbstractVerifyService.INTENT_VALUE))) {
                final Bundle bundle = intent.getExtras();
                final int type = bundle.getInt(AbstractVerifyService.INTENT_MESSAGE_TYPE);
                setLabel(intent.getExtras().getString(AbstractVerifyService.INTENT_MESSAGE), type);
                if (verifyResponseListener != null) {
                    verifyResponseListener.onVerifyResponse(bundle
                            .getString(AbstractVerifyService.INTENT_RESPONSE),
                            type == NotifyTextView.GOOD);
                }
            }
        }
    };

    public AbstractNotifiedEditText(final Context context, final AttributeSet attrs,
            final int defStyle, final RegExInputFilter regExInputFilter) {
        super(context, attrs, defStyle);
        this.regExInputFilter = regExInputFilter;
        addTextChangedListener(mTextWatcher);
    }

    public AbstractNotifiedEditText(final Context context, final AttributeSet attrs,
            final RegExInputFilter regExInputFilter) {
        super(context, attrs);
        this.regExInputFilter = regExInputFilter;
        addTextChangedListener(mTextWatcher);
    }

    public AbstractNotifiedEditText(final Context context, final RegExInputFilter regExInputFilter) {
        super(context);
        this.regExInputFilter = regExInputFilter;
        addTextChangedListener(mTextWatcher);
    }

    public void bindWithNotifyTextView(final NotifyTextView notifyTextView) {
        mNotifyTextView = notifyTextView;
    }

    public void registerReceiver() {
        getContext().registerReceiver(broadcastReceiver,
                new IntentFilter(getServiceBroadcast()));
        validate();
    }

    public void setVerifyResponseListener(final VerifyResponseListener verifyResponseListener) {
        this.verifyResponseListener = verifyResponseListener;
    }

    public void unregisterReceiver() {
        getContext().unregisterReceiver(broadcastReceiver);
    }

    public void validate() {
        if (isVerifierEnabled()) {
            if (regExInputFilter.validate(getText().toString())) {
                setLabel(getWaitMessage(), NotifyTextView.INFO);
                runVerifyService();
            } else {
                setLabel(getInvalidateMessage(), NotifyTextView.ERROR);
            }
        } else {
            setLabel(getNotEnabledMessage(), NotifyTextView.OFF);
        }
    }

    abstract protected CharSequence getInvalidateMessage();

    abstract protected CharSequence getNotEnabledMessage();

    abstract protected String getServiceBroadcast();

    abstract protected Class<?> getServiceClass();

    abstract protected CharSequence getWaitMessage();

    abstract protected boolean isVerifierEnabled();

    protected void runVerifyService() {
        final Intent intent = new Intent(getContext(), getServiceClass());
        intent.putExtra(AbstractVerifyService.INTENT_VALUE, getText().toString());
        getContext().stopService(intent);
        getContext().startService(intent);
    }

    protected void setLabel(final CharSequence content, final int color) {
        if (mNotifyTextView != null) {
            mNotifyTextView.setLabel(content, color);
        }
    }
}
