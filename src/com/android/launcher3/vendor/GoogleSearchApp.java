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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.RemoteViews;

public class GoogleSearchApp {
    static long VALIDITY_DURATION = 7200000L;
    public RemoteViews mRemoteViews;
    public int gsaVersion;
    public long gsaUpdateTime;
    public long publishTime;

    public GoogleSearchApp(Context context, RemoteViews remoteViews) {
        PackageInfo packageInfo = null;
        mRemoteViews = remoteViews;
        try {
            packageInfo = context.getPackageManager().getPackageInfo("com.google.android.googlequicksearchbox", 0);
        } catch (NameNotFoundException e) {
        }
        if (packageInfo != null) {
            gsaUpdateTime = packageInfo.lastUpdateTime;
            gsaVersion = packageInfo.versionCode;
        } else {
            gsaUpdateTime = 0;
            gsaVersion = 0;
        }
        publishTime = SystemClock.uptimeMillis();
    }

    public GoogleSearchApp(Bundle bundle) {
        gsaVersion = bundle.getInt("gsa_version", 0);
        gsaUpdateTime = bundle.getLong("gsa_update_time", 0);
        publishTime = bundle.getLong("publish_time", 0);
        mRemoteViews = bundle.getParcelable("views");
    }

    public long validity() {
        return (VALIDITY_DURATION + publishTime) - SystemClock.uptimeMillis();
    }

    public Bundle getBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("gsa_version", gsaVersion);
        bundle.putLong("gsa_update_time", gsaUpdateTime);
        bundle.putLong("publish_time", publishTime);
        bundle.putParcelable("views", mRemoteViews);
        return bundle;
    }
}