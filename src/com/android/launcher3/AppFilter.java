package com.android.launcher3;

import android.content.ComponentName;
import android.content.Context;

public interface AppFilter {

    public static AppFilter newInstance(Context context) {
        return Utilities.getOverrideObject(AppFilter.class, context, R.string.app_filter_class);
    }

    public boolean shouldShowApp(String packageName, Context context);
}
