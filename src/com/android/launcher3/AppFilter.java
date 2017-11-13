package com.android.launcher3;

import android.content.Context;

public interface AppFilter {

    public boolean shouldShowApp(String packageName, Context context);
}
