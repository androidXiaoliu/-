package com.baofeng.aone.date;
/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.CharacterStyle;
import android.text.style.RelativeSizeSpan;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.baofeng.aone.AndroidManager;
import com.baofeng.aone.LauncherApplication;

@SuppressLint("NewApi")
public class DateManager extends AndroidManager{
    private static final String TAG = "DateManager";

    private final Date mCurrentDate = new Date();

    private SimpleDateFormat mDateFormat;
    private SimpleDateFormat mClockFormat;
    private Context mContext;
    private DateCallback mCallback;
    private Calendar mCalendar;
    private String mClockFormatString;
    private Locale mLocale;
    private static final String mDatePattern = "yyyyeeeMMMMd";
    private int mAmPmStyle = AM_PM_STYLE_GONE;
    private static DateManager mDateManager;

    public String timeFormat12 ="hh:mm a"; // "hh:mm a"
    public String timeFormat24 ="HH:mm"; // "HH:mm"
    private static final int AM_PM_STYLE_NORMAL  = 0;
    private static final int AM_PM_STYLE_SMALL   = 1;
    private static final int AM_PM_STYLE_GONE    = 2;

    public static AndroidManager getAndroidManager() {
        return getInstance();
    }

    private static DateManager getInstance() {
        if (mDateManager == null) {
            mDateManager = new DateManager();
        }
        return mDateManager;
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (Intent.ACTION_LOCALE_CHANGED.equals(action)
                    || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
                // need to get a fresh date format
                mDateFormat = null;
                String tz = intent.getStringExtra("time-zone");
                mCalendar = Calendar.getInstance(TimeZone.getTimeZone(tz));
                if (mClockFormat != null) {
                    mClockFormat.setTimeZone(mCalendar.getTimeZone());
                }
            }

            if (action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                final Locale newLocale = mContext.getResources().getConfiguration().locale;
                if (! newLocale.equals(mLocale)) {
                    mLocale = newLocale;
                    mClockFormatString = ""; // force refresh
                }
            }
            getSystemTime();
        }
    };

    private DateManager() {
        mContext = LauncherApplication.getInstance();
        // The time zone may have changed while the receiver wasn't registered, so update the Time
        mCalendar = Calendar.getInstance(TimeZone.getDefault());
    }

    /**
     * Update time and date per minute
     * (year/month/day week HH:mm a)
     * @param callback DateCallback
     */
    public void registerDateChangelistner(DateCallback callback) {
        mCallback = callback;
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        mContext.registerReceiver(mIntentReceiver, filter, null, null);

        getSystemTime();
    }
    /**
     * Cancel date and time update
     */
    public void unregisterDateChangelistner() {
        mDateFormat = null; // reload the locale next time
        if (mIntentReceiver != null)
            mContext.unregisterReceiver(mIntentReceiver);
    }

    /**
     * get system date (year/month/day week)
     * @param callback DateCallback
     */
    public void getDate(DateCallback callback){
        callback.onGetDate(updateDate());
    }

    /**
     * get system time (HH:mm a)
     * @param callback
     */
    public void getTime(DateCallback callback){
        callback.onGetTime(updateClock());
    }

    private void getSystemTime(){
        StringBuffer sb = new StringBuffer();
        sb.append(updateDate());
        sb.append(updateClock());
        mCallback.onUpdateDate(sb.toString());
    }

    private String updateDate() {
        if (mDateFormat == null) {
            final Locale l = Locale.getDefault();
            final String fmt = DateFormat.getBestDateTimePattern(l, mDatePattern);
            mDateFormat = new SimpleDateFormat(fmt, l);
        }
        mCurrentDate.setTime(System.currentTimeMillis());
        final String text = mDateFormat.format(mCurrentDate);
        return text;
    }

    private String updateClock() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        return getSmallTime().toString();
    }

    private final CharSequence getSmallTime() {
        boolean is24 = DateFormat.is24HourFormat(mContext);

        final char MAGIC1 = '\uEF00';
        final char MAGIC2 = '\uEF01';

        SimpleDateFormat sdf;
        String format = is24 ? timeFormat24 : timeFormat12;
        if (!format.equals(mClockFormatString)) {
            /*
             * Search for an unquoted "a" in the format string, so we can
             * add dummy characters around it to let us find it again after
             * formatting and change its size.
             */
            if (mAmPmStyle != AM_PM_STYLE_NORMAL) {
                int a = -1;
                boolean quoted = false;
                for (int i = 0; i < format.length(); i++) {
                    char c = format.charAt(i);

                    if (c == '\'') {
                        quoted = !quoted;
                    }
                    if (!quoted && c == 'a') {
                        a = i;
                        break;
                    }
                }

                if (a >= 0) {
                    // Move a back so any whitespace before AM/PM is also in the alternate size.
                    final int b = a;
                    while (a > 0 && Character.isWhitespace(format.charAt(a-1))) {
                        a--;
                    }
                    format = format.substring(0, a) + MAGIC1 + format.substring(a, b)
                        + "a" + MAGIC2 + format.substring(b + 1);
                }
            }
            mClockFormat = sdf = new SimpleDateFormat(format);
            mClockFormatString = format;
        } else {
            sdf = mClockFormat;
        }
        String result = sdf.format(mCalendar.getTime());

        if (mAmPmStyle != AM_PM_STYLE_NORMAL) {
            int magic1 = result.indexOf(MAGIC1);
            int magic2 = result.indexOf(MAGIC2);
            if (magic1 >= 0 && magic2 > magic1) {
                SpannableStringBuilder formatted = new SpannableStringBuilder(result);
                if (mAmPmStyle == AM_PM_STYLE_GONE) {
                    formatted.delete(magic1, magic2+1);
                } else {
                    if (mAmPmStyle == AM_PM_STYLE_SMALL) {
                        CharacterStyle style = new RelativeSizeSpan(0.7f);
                        formatted.setSpan(style, magic1, magic2,
                                          Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                    formatted.delete(magic2, magic2 + 1);
                    formatted.delete(magic1, magic1 + 1);
                }
                return formatted;
            }
        }
        return result;
    }
}
