package com.google.android.apps.nexuslauncher.qsb;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.android.launcher3.CellLayout;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.dragndrop.DragLayer;
import com.android.launcher3.dynamicui.WallpaperColorInfo;
import com.android.launcher3.util.Themes;

import static android.provider.Settings.System.LUNA_SEARCHBAR_THEME;

public class HotseatQsbWidget extends AbstractQsbLayout implements WallpaperColorInfo.OnChangeListener {

    private boolean mIsDefaultLiveWallpaper;
    private boolean mGoogleHasFocus;
    private boolean mSearchRequested;

    private AnimatorSet mAnimatorSet;
    private final BroadcastReceiver mBroadcastReceiver;

    public HotseatQsbWidget(Context context) {
        this(context, null);
    }

    public HotseatQsbWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HotseatQsbWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setGoogleColored();
            }
        };
        mIsDefaultLiveWallpaper = isDefaultLiveWallpaper();
        setQsbColor();
        setOnClickListener(this);
    }

    static int getBottomMargin(Launcher launcher) {
        Rect insets = launcher.getDragLayer().getInsets();
        Resources res = launcher.getResources();
        return res.getDimensionPixelSize(R.dimen.qsb_hotseat_bottom_margin) + (insets.bottom == 0 ?
                res.getDimensionPixelSize(R.dimen.qsb_hotseat_bottom_margin_hw) :
                insets.bottom);
    }

    private void setQsbDefault() {
        View.inflate(new ContextThemeWrapper(getContext(), mIsDefaultLiveWallpaper ? R.style.HotseatQsbTheme_Colored : R.style.HotseatQsbTheme), R.layout.qsb_hotseat_content, this);
        bz(mIsDefaultLiveWallpaper ? 0xCCFFFFFF : 0x99FAFAFA);
    }

    private void setQsbTheme() {
        View.inflate(new ContextThemeWrapper(getContext(), mIsDefaultLiveWallpaper ? R.style.HotseatQsbTheme_Colored : R.style.HotseatQsbTheme), R.layout.qsb_hotseat_content, this);
        int themeBlack = getContext().getResources().getColor(R.color.qsb_background_color_theme_black);
        int themeDark = getContext().getResources().getColor(R.color.qsb_background_color_theme_dark);
        int userThemeSetting = Settings.Secure.getIntForUser(getContext().getContentResolver(), 
                Settings.Secure.DEVICE_THEME, 0, UserHandle.USER_CURRENT);
        if (userThemeSetting == 0 || userThemeSetting == 1) {
            bz(mIsDefaultLiveWallpaper ? 0xCCFFFFFF : 0x99FAFAFA);
        } else if (userThemeSetting == 2) {
            bz(mIsDefaultLiveWallpaper ? 0xCCFFFFFF : themeDark);
        } else if (userThemeSetting == 3) {
            bz(mIsDefaultLiveWallpaper ? 0xCCFFFFFF : themeBlack);
        }
    }

    private void setQsbAccent() {
        int accent = getContext().getResources().getColor(R.color.qsb_background_color_accent);
        View.inflate(new ContextThemeWrapper(getContext(), mIsDefaultLiveWallpaper ? R.style.HotseatQsbTheme_Colored : R.style.HotseatQsbTheme), R.layout.qsb_hotseat_content, this);
        bz(mIsDefaultLiveWallpaper ? 0xCCFFFFFF : accent);
    }

    private void setQsbWallpaper() {
        int wallpaper = WallpaperColorInfo.getInstance(getContext()).getMainColor();
        View.inflate(new ContextThemeWrapper(getContext(), mIsDefaultLiveWallpaper ? R.style.HotseatQsbTheme_Colored : R.style.HotseatQsbTheme), R.layout.qsb_hotseat_content, this);
        bz(mIsDefaultLiveWallpaper ? 0xCCFFFFFF : wallpaper);
    }

    public void setQsbColor() {
        int searchbarColor = Settings.System.getIntForUser(
                getContext().getContentResolver(), 
                LUNA_SEARCHBAR_THEME, 0, UserHandle.USER_CURRENT);
        if (searchbarColor == 0) {
            setQsbDefault();
        } else if (searchbarColor == 1) {
            setQsbTheme();
        } else if (searchbarColor == 2) {
            setQsbAccent();
        } else if (searchbarColor == 3) {
            setQsbWallpaper();
        }
    }

    private void openQSB() {
        mSearchRequested = false;
        playAnimation(mGoogleHasFocus = true, true);
    }

    private void closeQSB(boolean longDuration) {
        mSearchRequested = false;
        if (mGoogleHasFocus) {
            playAnimation(mGoogleHasFocus = false, longDuration);
        }
    }

    private Intent getSearchIntent() {
        int[] array = new int[2];
        getLocationInWindow(array);
        Rect rect = new Rect(0, 0, getWidth(), getHeight());
        rect.offset(array[0], array[1]);
        rect.inset(getPaddingLeft(), getPaddingTop());
        return ConfigBuilder.getSearchIntent(rect, findViewById(R.id.g_icon), mMicIconView);
    }

    private void setGoogleColored() {
        if (mIsDefaultLiveWallpaper != isDefaultLiveWallpaper()) {
            mIsDefaultLiveWallpaper ^= true;
            removeAllViews();
            setQsbColor();
            loadAndGetPreferences();
        }
    }

    private void playQsbAnimation() {
        if (hasWindowFocus()) {
            mSearchRequested = true;
        } else {
            openQSB();
        }
    }

    private boolean isDefaultLiveWallpaper() {
        WallpaperInfo wallpaperInfo = WallpaperManager.getInstance(getContext()).getWallpaperInfo();
        return wallpaperInfo != null && wallpaperInfo.getComponent().flattenToString().equals(getContext().getString(R.string.default_live_wallpaper));
    }

    private void doOnClick() {
        final ConfigBuilder f = new ConfigBuilder(this, false);
        if (mActivity.getGoogleNow().startSearch(f.build(), f.getExtras())) {
            SharedPreferences devicePrefs = Utilities.getDevicePrefs(getContext());
            devicePrefs.edit().putInt("key_hotseat_qsb_tap_count", devicePrefs.getInt("key_hotseat_qsb_tap_count", 0) + 1).apply();
            playQsbAnimation();
        } else {
            getContext().sendOrderedBroadcast(getSearchIntent(), null,
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            Log.e("HotseatQsbSearch", getResultCode() + " " + getResultData());
                            if (getResultCode() == 0) {
                                fallbackSearch("com.google.android.googlequicksearchbox.TEXT_ASSIST");
                            } else {
                                playQsbAnimation();
                            }
                        }
                    }, null, 0, null, null);
        }
    }

    @Override
    protected void noGoogleAppSearch() {
        try {
            getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com")));
            playQsbAnimation();
        } catch (ActivityNotFoundException ignored) {
        }
    }

    private void playAnimation(boolean hideWorkspace, boolean longDuration) {
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                if (animation == mAnimatorSet) {
                    mAnimatorSet = null;
                }
            }
        });

        DragLayer dragLayer = mActivity.getDragLayer();
        float[] alphaValues = new float[1];
        float[] translationValues = new float[1];
        if (hideWorkspace) {
            alphaValues[0] = 0.0f;
            mAnimatorSet.play(ObjectAnimator.ofFloat(dragLayer, View.ALPHA, alphaValues));

            translationValues[0] = -mActivity.getHotseat().getHeight() / 2;
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(dragLayer, View.TRANSLATION_Y, translationValues);
            ofFloat.setInterpolator(new AccelerateInterpolator());
            mAnimatorSet.play(ofFloat);
        } else {
            alphaValues[0] = 1.0f;
            mAnimatorSet.play(ObjectAnimator.ofFloat(dragLayer, View.ALPHA, alphaValues));

            translationValues[0] = 0.0f;
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(dragLayer, View.TRANSLATION_Y, translationValues);
            ofFloat.setInterpolator(new DecelerateInterpolator());
            mAnimatorSet.play(ofFloat);
        }
        mAnimatorSet.setDuration(200L);
        mAnimatorSet.start();
        if (!longDuration) {
            mAnimatorSet.end();
        }
    }

    protected int getWidth(final int n) {
        CellLayout layout = mActivity.getHotseat().getLayout();
        return n - layout.getPaddingLeft() - layout.getPaddingRight();
    }

    protected void loadBottomMargin() {
        ((MarginLayoutParams) getLayoutParams()).bottomMargin = getBottomMargin(mActivity);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getContext().registerReceiver(mBroadcastReceiver, new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED));
        WallpaperColorInfo instance = WallpaperColorInfo.getInstance(getContext());
        instance.addOnChangeListener(this);
    }

    public void onClick(View view) {
        super.onClick(view);
        if (view == this) {
            doOnClick();
        }
    }

    protected void onDetachedFromWindow() {
        getContext().unregisterReceiver(mBroadcastReceiver);
        WallpaperColorInfo.getInstance(getContext()).removeOnChangeListener(this);
        super.onDetachedFromWindow();
    }

    public void onExtractedColorsChanged(final WallpaperColorInfo wallpaperColorInfo) {
        setQsbColor();
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus && mSearchRequested) {
            openQSB();
        } else if (hasWindowFocus) {
            closeQSB(true);
        }
    }

    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        closeQSB(false);
    }
}
