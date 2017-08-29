/*
 * Copyright (C) 2016-2017 Google Inc.
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

package com.android.launcher3.vendor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.launcher3.Alarm;
import com.android.launcher3.OnAlarmListener;
import com.android.launcher3.util.Preconditions;

import java.util.ArrayList;

public class WeatherListener extends BroadcastReceiver implements OnAlarmListener {
    static long INITIAL_LOAD_TIMEOUT = 5000L;
    private static WeatherListener instance;
    private GoogleSearchApp gsa;
    private boolean bq = false;
    private final AppWidgetManagerHelper appWidgetManagerHelper;
    private final Alarm mAlarm;
    private final Context mContext;
    private final ArrayList<OnGsaListener> mListeners;

    public static WeatherListener getInstance(Context context) {
        if (instance == null) {
            instance = new WeatherListener(context.getApplicationContext());
        }
        return instance;
    }

    WeatherListener(Context context) {
        mContext = context;
        mListeners = new ArrayList<>();
        appWidgetManagerHelper = new AppWidgetManagerHelper(mContext);
        mAlarm = new Alarm();
        mAlarm.setOnAlarmListener(this);
        onReceive(null, null);
        mAlarm.setAlarm(INITIAL_LOAD_TIMEOUT);
        aU(gsa);
        context.registerReceiver(this, Util.createIntentFilter("android.intent.action.PACKAGE_ADDED", "android.intent.action.PACKAGE_CHANGED", "android.intent.action.PACKAGE_REMOVED", "android.intent.action.PACKAGE_DATA_CLEARED"));
    }

    private void aU(GoogleSearchApp gsa) {
        Bundle appWidgetOptions = appWidgetManagerHelper.getAppWidgetOptions();
        if (appWidgetOptions != null) {
            GoogleSearchApp gsa2 = new GoogleSearchApp(appWidgetOptions);
            if (gsa2.mRemoteViews != null && gsa2.gsaVersion == gsa.gsaVersion && gsa2.gsaUpdateTime == gsa.gsaUpdateTime && gsa2.validity() > 0) {
                this.gsa = gsa2;
                aX();
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SuperWeatherListener", "onReceive");
        gsa = new GoogleSearchApp(mContext, null);
        mContext.sendBroadcast(new Intent("com.google.android.apps.gsa.weatherwidget.ENABLE_UPDATE").setPackage("com.google.android.googlequicksearchbox"));
    }

    private void aX() {
        bq = true;
        for (OnGsaListener bb : mListeners) {
            bb.onGsa(gsa.mRemoteViews);
        }
        mAlarm.cancelAlarm();
        if (gsa.mRemoteViews != null) {
            mAlarm.setAlarm(gsa.validity());
        }
    }

    public static WeatherListener getInstanceUI(final Context context) {
        Preconditions.assertUIThread();
        if (WeatherListener.instance == null) {
            WeatherListener.instance = new WeatherListener(context.getApplicationContext());
        }
        return WeatherListener.instance;
    }

    public void bH(final RemoteViews remoteViews) {
        if (remoteViews != null) {
            this.gsa = new GoogleSearchApp(this.mContext, remoteViews);
            this.aX();
        }
    }


    @Override
    public void onAlarm(Alarm alarm) {
        if (gsa.mRemoteViews != null || !bq) {
            gsa = new GoogleSearchApp(mContext, null);
            aX();
        }
    }

    public GoogleSearchApp getGoogleSearchAppAndAddListener(OnGsaListener onGsaListener) {
        mListeners.add(onGsaListener);
        return bq ? gsa : null;
    }

    public void removeListener(OnGsaListener onGsaListener) {
        mListeners.remove(onGsaListener);
    }

    public interface OnGsaListener {
        void onGsa(RemoteViews views);
    }
}