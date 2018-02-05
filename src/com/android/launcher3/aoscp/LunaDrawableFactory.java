package com.android.launcher3.aoscp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Process;

import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.SettingsActivity;
import com.android.launcher3.Utilities;
import com.android.launcher3.graphics.DrawableFactory;

import co.aoscp.lunalauncher.clock.DynamicClock;

public class LunaDrawableFactory extends DrawableFactory {

    private final DynamicClock mDynamicClockDrawer;

    private final boolean hasIconPack;

    public LunaDrawableFactory(Context context) {
        hasIconPack = !Utilities.getPrefs(context).getString(SettingsActivity.ICON_PACK_PREF, "").isEmpty();
        mDynamicClockDrawer = new DynamicClock(context);
    }

    @Override
    public FastBitmapDrawable newIcon(Bitmap icon, ItemInfo info) {
        if (info != null &&
                info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION &&
                DynamicClock.DESK_CLOCK.equals(info.getTargetComponent()) &&
                info.user.equals(Process.myUserHandle())) {
            return mDynamicClockDrawer.drawIcon(icon);
        }
        if (hasIconPack) {
            return new FastBitmapDrawable(icon);
        }
        return super.newIcon(icon, info);
    }
}
