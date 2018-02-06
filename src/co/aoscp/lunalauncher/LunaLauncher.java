package co.aoscp.lunalauncher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.graphics.ColorUtils;
import android.view.Menu;
import android.view.View;

import com.android.launcher3.AppInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherCallbacks;
import com.android.launcher3.LauncherExterns;
import com.android.launcher3.R;
import com.android.launcher3.SettingsActivity;
import com.android.launcher3.Utilities;
import com.android.launcher3.aoscp.LunaDrawableFactory;
import com.android.launcher3.dynamicui.WallpaperColorInfo;
import com.android.launcher3.util.ComponentKeyMapper;
import com.android.launcher3.util.Themes;
import co.aoscp.lunalauncher.search.ItemInfoUpdateReceiver;
import co.aoscp.lunalauncher.smartspace.SmartspaceView;
import co.aoscp.lunalauncher.smartspace.SmartspaceController;
import com.google.android.libraries.launcherclient.GoogleNow;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class LunaLauncher {
    private final Launcher mLauncher;
    public final LauncherCallbacks mLauncherCallbacks;
    private boolean fC;
    private final LauncherExterns mLauncherExterns;
    private boolean mRunning;
    public GoogleNow mGoogleNow;
    private LunaDrawableFactory mLunaDrawableFactory;
    public LunaLauncherOverlay mLunaLauncherOverlay;
    private boolean mStarted;
    private final Bundle mUiInformation = new Bundle();
    private ItemInfoUpdateReceiver mItemInfoUpdateReceiver;

    public LunaLauncher(LunaLauncherActivity activity) {
        mLauncher = activity;
        mLauncherExterns = activity;
        mLauncherCallbacks = new LunaLauncherCallbacks();
        mLauncherExterns.setLauncherCallbacks(mLauncherCallbacks);
    }

    private static GoogleNow.IntegerReference goolgeNowReference(SharedPreferences sharedPreferences) {
        return new GoogleNow.IntegerReference(
                (sharedPreferences.getBoolean(SettingsActivity.GOOGLE_NOW_PREF, true) ? 1 : 0) | 0x2 | 0x4 | 0x8);
    }

    class LunaLauncherCallbacks implements LauncherCallbacks, SharedPreferences.OnSharedPreferenceChangeListener, WallpaperColorInfo.OnChangeListener {
        private SmartspaceView mSmartspace;

        private ItemInfoUpdateReceiver getUpdateReceiver() {
            if (mItemInfoUpdateReceiver == null) {
                mItemInfoUpdateReceiver = new ItemInfoUpdateReceiver(mLauncher, mLauncherCallbacks);
            }
            return mItemInfoUpdateReceiver;
        }

        public void bindAllApplications(final ArrayList<AppInfo> list) {
            getUpdateReceiver().di();
        }

        public void dump(final String s, final FileDescriptor fileDescriptor, final PrintWriter printWriter, final String[] array) {
            SmartspaceController.get(mLauncher).cX(s, printWriter);
        }

        public void finishBindingItems(final boolean b) {
        }

        public List<ComponentKeyMapper<AppInfo>> getPredictedApps() {
            // Dummy return value
            return new ArrayList<>();
        }

        @Override
        public int getSearchBarHeight() {
            return LauncherCallbacks.SEARCH_BAR_HEIGHT_NORMAL;
        }

        public boolean handleBackPressed() {
            return false;
        }

        public boolean hasCustomContentToLeft() {
            return false;
        }

        public boolean hasSettings() {
            return true;
        }

        public void onActivityResult(final int n, final int n2, final Intent intent) {
        }

        public void onAttachedToWindow() {
            mGoogleNow.onAttachedToWindow();
        }

        public void onCreate(final Bundle bundle) {
            SharedPreferences prefs = Utilities.getPrefs(mLauncher);
            mLunaLauncherOverlay = new LunaLauncherOverlay(mLauncher);
            mGoogleNow = new GoogleNow(mLauncher, mLunaLauncherOverlay, goolgeNowReference(prefs));
            mLunaLauncherOverlay.setNowConnection(mGoogleNow);

            prefs.registerOnSharedPreferenceChangeListener(this);

            SmartspaceController.get(mLauncher).cW();
            mSmartspace = mLauncher.findViewById(R.id.search_container_workspace);

            mUiInformation.putInt("system_ui_visibility", mLauncher.getWindow().getDecorView().getSystemUiVisibility());
            WallpaperColorInfo instance = WallpaperColorInfo.getInstance(mLauncher);
            instance.addOnChangeListener(this);
            onExtractedColorsChanged(instance);

            getUpdateReceiver().onCreate();
        }

        public void onDestroy() {
            mGoogleNow.onDestroy();
            Utilities.getPrefs(mLauncher).unregisterOnSharedPreferenceChangeListener(this);

            getUpdateReceiver().onDestroy();
        }

        public void onDetachedFromWindow() {
            mGoogleNow.onDetachedFromWindow();
        }

        public void onHomeIntent() {
            mGoogleNow.closeOverlay(fC);
        }

        public void onInteractionBegin() {
        }

        public void onInteractionEnd() {
        }

        public void onLauncherProviderChange() {
        }

        public void onNewIntent(final Intent intent) {
        }

        public void onPause() {
            mRunning = false;
            mGoogleNow.onPause();

            if (mSmartspace != null) {
                mSmartspace.onPause();
            }
        }

        public void onPostCreate(final Bundle bundle) {
        }

        public boolean onPrepareOptionsMenu(final Menu menu) {
            return false;
        }

        public void onRequestPermissionsResult(final int n, final String[] array, final int[] array2) {
        }

        public void onResume() {
            mRunning = true;
            if (mStarted) {
                fC = true;
            }

            try {
                mGoogleNow.onResume();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (mSmartspace != null) {
                mSmartspace.onResume();
            }
        }

        public void onSaveInstanceState(final Bundle bundle) {
        }

        public void onStart() {
            mStarted = true;
            mGoogleNow.onStart();
        }

        public void onStop() {
            mStarted = false;
            mGoogleNow.onStop();
            if (!mRunning) {
                fC = false;
            }
            mLunaLauncherOverlay.stop();
        }

        public void onTrimMemory(int n) {
        }

        public void onWindowFocusChanged(boolean hasFocus) {
        }

        public void onWorkspaceLockedChanged() {
        }

        public void populateCustomContentContainer() {
        }

        @Override
        public View getQsbBar() {
            return null;
        }

        @Override
        public Bundle getAdditionalSearchWidgetOptions() {
            return null;
        }

        public void preOnCreate() {
            mLunaDrawableFactory = new LunaDrawableFactory(mLauncher);
        }

        public void preOnResume() {
        }

        public boolean shouldMoveToDefaultScreenOnHomeIntent() {
            return true;
        }

        public boolean startSearch(String s, boolean b, Bundle bundle) {
            View gIcon = mLauncher.findViewById(R.id.g_icon);
            while (gIcon != null && !gIcon.isClickable()) {
                if (gIcon.getParent() instanceof View) {
                    gIcon = (View)gIcon.getParent();
                } else {
                    gIcon = null;
                }
            }
            if (gIcon != null && gIcon.performClick()) {
                mLauncherExterns.clearTypedText();
                return true;
            }
            return false;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (SettingsActivity.GOOGLE_NOW_PREF.equals(key)) {
                mGoogleNow.RB(goolgeNowReference(sharedPreferences));
            }
        }

        @Override
        public void onExtractedColorsChanged(WallpaperColorInfo wallpaperColorInfo) {
            int alpha = mLauncher.getResources().getInteger(R.integer.extracted_color_gradient_alpha);

            mUiInformation.putInt("background_color_hint", primaryColor(wallpaperColorInfo, mLauncher, alpha));
            mUiInformation.putInt("background_secondary_color_hint", secondaryColor(wallpaperColorInfo, mLauncher, alpha));
            mUiInformation.putBoolean("is_background_dark", Themes.getAttrBoolean(mLauncher, R.attr.isMainColorDark));

            mGoogleNow.redraw(mUiInformation);
        }
    }

    public static int primaryColor(WallpaperColorInfo wallpaperColorInfo, Context context, int alpha) {
        return compositeAllApps(ColorUtils.setAlphaComponent(wallpaperColorInfo.getMainColor(), alpha), context);
    }

    public static int secondaryColor(WallpaperColorInfo wallpaperColorInfo, Context context, int alpha) {
        return compositeAllApps(ColorUtils.setAlphaComponent(wallpaperColorInfo.getSecondaryColor(), alpha), context);
    }

    private static int compositeAllApps(int color, Context context) {
        return ColorUtils.compositeColors(Themes.getAttrColor(context, R.attr.allAppsScrimColor), color);
    }
}
