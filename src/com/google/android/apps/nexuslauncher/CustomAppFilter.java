package com.google.android.apps.nexuslauncher;

import android.content.ComponentName;
import android.content.Context;
import android.preference.PreferenceManager;

import com.android.launcher3.AppFilter;
import com.android.launcher3.SettingsActivity;
import com.android.launcher3.Utilities;

import java.util.HashSet;
import java.util.Set;

public class CustomAppFilter implements AppFilter {

    private final Context mContext;

    private final HashSet<ComponentName> mHideList = new HashSet<>();

    public CustomAppFilter(Context context) {
        mContext = context;

        //Voice Search
        mHideList.add(ComponentName.unflattenFromString("com.google.android.googlequicksearchbox/.VoiceSearchActivity"));

        //Wallpapers
        mHideList.add(ComponentName.unflattenFromString("com.google.android.apps.wallpaper/.picker.CategoryPickerActivity"));

        //Google Now Launcher
        mHideList.add(ComponentName.unflattenFromString("com.google.android.launcher/com.google.android.launcher.StubApp"));
    }

    @Override
    public boolean shouldShowApp(String packageName, Context context) {
        Set<String> hiddenApps = PreferenceManager.getDefaultSharedPreferences(context).getStringSet(Utilities.KEY_HIDDEN_APPS_SET, null);
        return hiddenApps == null || !hiddenApps.contains(packageName) && !mHideList.contains(packageName);
    }
}
