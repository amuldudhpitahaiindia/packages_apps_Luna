package com.android.launcher3.aoscp;

import android.content.ComponentName;
import android.content.Context;
import android.preference.PreferenceManager;

import com.android.launcher3.AppFilter;
import com.android.launcher3.AppInfo;
import com.android.launcher3.SettingsActivity;
import com.android.launcher3.Utilities;

import co.aoscp.lunalauncher.utils.IconPackUtils;

import java.util.HashSet;
import java.util.Set;

public class LunaAppFilter implements AppFilter {

    private final HashSet<ComponentName> mHideList = new HashSet<>();

    private final Context mContext;
    private final boolean hasIconPack;

    public LunaAppFilter(Context context) {
        mContext = context;
        hasIconPack = !Utilities.getPrefs(context).getString(SettingsActivity.ICON_PACK_PREF, "").isEmpty();

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
        return hiddenApps == null || !hiddenApps.contains(packageName) &&
                (!hasIconPack || !IconPackUtils.isPackProvider(mContext, packageName))
                 && !mHideList.contains(packageName);
    }
}
