/*
 * Copyright (C) 2016 The Android Open Source Project
 * Copyright (C) 2018 CypherOS
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

package com.android.launcher3.aoscp;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.XmlRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceGroupAdapter;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.SettingsActivity;
import com.android.settingslib.core.lifecycle.ObservablePreferenceFragment;

/**
 * Base class for Launcher settings fragments, with some helper functions.
 */
public abstract class LauncherPreferenceFragment extends ObservablePreferenceFragment {

    private static final String TAG = "LauncherPreferenceFragment";

    @VisibleForTesting
    static final int DELAY_HIGHLIGHT_DURATION_MILLIS = 600;

    private static final String SAVE_HIGHLIGHTED_KEY = "android:preference_highlighted";

    // Cache the content resolver for async callbacks
    private ContentResolver mContentResolver;

    private String mPreferenceKey;

    private RecyclerView.Adapter mCurrentRootAdapter;
    private boolean mIsDataSetObserverRegistered = false;
    private RecyclerView.AdapterDataObserver mDataSetObserver =
            new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    onDataSetChanged();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    onDataSetChanged();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                    onDataSetChanged();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    onDataSetChanged();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    onDataSetChanged();
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    onDataSetChanged();
                }
            };

    private View mEmptyView;
    private LinearLayoutManager mLayoutManager;
    private ArrayMap<String, Preference> mPreferenceCache;
    private boolean mAnimationAllowed;

    @VisibleForTesting
    public HighlightablePreferenceGroupAdapter mAdapter;
    @VisibleForTesting
    public boolean mPreferenceHighlighted = false;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (icicle != null) {
            mPreferenceHighlighted = icicle.getBoolean(SAVE_HIGHLIGHTED_KEY);
        }
    }

    @Override
    public void addPreferencesFromResource(@XmlRes int preferencesResId) {
        super.addPreferencesFromResource(preferencesResId);
        checkAvailablePrefs(getPreferenceScreen());
    }

    private void checkAvailablePrefs(PreferenceGroup preferenceGroup) {
        if (preferenceGroup == null) return;
        for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
            Preference pref = preferenceGroup.getPreference(i);
            if (pref instanceof PreferenceGroup) {
                checkAvailablePrefs((PreferenceGroup) pref);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVE_HIGHLIGHTED_KEY, mPreferenceHighlighted);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        final Bundle args = getArguments();
        if (args != null) {
            mPreferenceKey = args.getString(SettingsActivity.EXTRA_FRAGMENT_ARG_KEY);
            highlightPreferenceIfNeeded();
        }
    }

    @Override
    protected void onBindPreferences() {
        registerObserverIfNeeded();
    }

    @Override
    protected void onUnbindPreferences() {
        unregisterObserverIfNeeded();
    }

    public void registerObserverIfNeeded() {
        if (!mIsDataSetObserverRegistered) {
            if (mCurrentRootAdapter != null) {
                mCurrentRootAdapter.unregisterAdapterDataObserver(mDataSetObserver);
            }
            mCurrentRootAdapter = getListView().getAdapter();
            mCurrentRootAdapter.registerAdapterDataObserver(mDataSetObserver);
            mIsDataSetObserverRegistered = true;
            onDataSetChanged();
        }
    }

    public void unregisterObserverIfNeeded() {
        if (mIsDataSetObserverRegistered) {
            if (mCurrentRootAdapter != null) {
                mCurrentRootAdapter.unregisterAdapterDataObserver(mDataSetObserver);
                mCurrentRootAdapter = null;
            }
            mIsDataSetObserverRegistered = false;
        }
    }

    public void highlightPreferenceIfNeeded() {
        if (isAdded() && !mPreferenceHighlighted &&!TextUtils.isEmpty(mPreferenceKey)) {
            getView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    highlightPreference(mPreferenceKey);
                }
            }, DELAY_HIGHLIGHT_DURATION_MILLIS);
        }
    }

    protected void onDataSetChanged() {
        highlightPreferenceIfNeeded();
        updateEmptyView();
    }

    @Override
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (preferenceScreen != null && !preferenceScreen.isAttached()) {
            // Without ids generated, the RecyclerView won't animate changes to the preferences.
            preferenceScreen.setShouldUseGeneratedIds(mAnimationAllowed);
        }
        super.setPreferenceScreen(preferenceScreen);
    }

    @VisibleForTesting
    void updateEmptyView() {
        if (mEmptyView == null) return;
        if (getPreferenceScreen() != null) {
            final View listContainer = getActivity().findViewById(android.R.id.list_container);
            boolean show = (getPreferenceScreen().getPreferenceCount()) <= 0
                    || (listContainer != null && listContainer.getVisibility() != View.VISIBLE);
            mEmptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    public void setEmptyView(View v) {
        if (mEmptyView != null) {
            mEmptyView.setVisibility(View.GONE);
        }
        mEmptyView = v;
        updateEmptyView();
    }

    public View getEmptyView() {
        return mEmptyView;
    }

    /**
     * Return a valid ListView position or -1 if none is found
     */
    private int canUseListViewForHighLighting(String key) {
        if (getListView() == null) {
            return -1;
        }

        RecyclerView listView = getListView();
        RecyclerView.Adapter adapter = listView.getAdapter();

        if (adapter != null && adapter instanceof PreferenceGroupAdapter) {
            return findListPositionFromKey((PreferenceGroupAdapter) adapter, key);
        }

        return -1;
    }

    @Override
    public RecyclerView.LayoutManager onCreateLayoutManager() {
        mLayoutManager = new LinearLayoutManager(getContext());
        return mLayoutManager;
    }

    @Override
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        mAdapter = new HighlightablePreferenceGroupAdapter(preferenceScreen);
        return mAdapter;
    }

    protected void setAnimationAllowed(boolean animationAllowed) {
        mAnimationAllowed = animationAllowed;
    }

    protected void cacheRemoveAllPrefs(PreferenceGroup group) {
        mPreferenceCache = new ArrayMap<String, Preference>();
        final int N = group.getPreferenceCount();
        for (int i = 0; i < N; i++) {
            Preference p = group.getPreference(i);
            if (TextUtils.isEmpty(p.getKey())) {
                continue;
            }
            mPreferenceCache.put(p.getKey(), p);
        }
    }

    protected Preference getCachedPreference(String key) {
        return mPreferenceCache != null ? mPreferenceCache.remove(key) : null;
    }

    protected void removeCachedPrefs(PreferenceGroup group) {
        for (Preference p : mPreferenceCache.values()) {
            group.removePreference(p);
        }
        mPreferenceCache = null;
    }

    protected int getCachedCount() {
        return mPreferenceCache != null ? mPreferenceCache.size() : 0;
    }

    private void highlightPreference(String key) {
        final int position = canUseListViewForHighLighting(key);
        if (position < 0) {
            return;
        }

        mPreferenceHighlighted = true;
        mLayoutManager.scrollToPosition(position);
        mAdapter.highlight(position);
    }

    private int findListPositionFromKey(PreferenceGroupAdapter adapter, String key) {
        final int count = adapter.getItemCount();
        for (int n = 0; n < count; n++) {
            final Preference preference = adapter.getItem(n);
            final String preferenceKey = preference.getKey();
            if (preferenceKey != null && preferenceKey.equals(key)) {
                return n;
            }
        }
        return -1;
    }

    protected boolean removePreference(String key) {
        return removePreference(getPreferenceScreen(), key);
    }

    @VisibleForTesting
    boolean removePreference(PreferenceGroup group, String key) {
        final int preferenceCount = group.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            final Preference preference = group.getPreference(i);
            final String curKey = preference.getKey();

            if (TextUtils.equals(curKey, key)) {
                return group.removePreference(preference);
            }

            if (preference instanceof PreferenceGroup) {
                if (removePreference((PreferenceGroup) preference, key)) {
                    return true;
                }
            }
        }
        return false;
    }

    public final void finishFragment() {
        getActivity().onBackPressed();
    }

    /**
     * Returns the ContentResolver from the owning Activity.
     */
    protected ContentResolver getContentResolver() {
        Context context = getActivity();
        if (context != null) {
            mContentResolver = context.getContentResolver();
        }
        return mContentResolver;
    }

    /**
     * Returns the specified system service from the owning Activity.
     */
    protected Object getSystemService(final String name) {
        return getActivity().getSystemService(name);
    }

    /**
     * Returns the PackageManager from the owning Activity.
     */
    protected PackageManager getPackageManager() {
        return getActivity().getPackageManager();
    }

    public void finish() {
        Activity activity = getActivity();
        if (activity == null) return;
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            activity.finish();
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    protected final Context getPrefContext() {
        return getPreferenceManager().getContext();
    }

    public static class HighlightablePreferenceGroupAdapter extends PreferenceGroupAdapter {

        @VisibleForTesting(otherwise=VisibleForTesting.NONE)
        int initialHighlightedPosition = -1;

        private int mHighlightPosition = -1;

        public HighlightablePreferenceGroupAdapter(PreferenceGroup preferenceGroup) {
            super(preferenceGroup);
        }

        public void highlight(int position) {
            mHighlightPosition = position;
            initialHighlightedPosition = position;
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(PreferenceViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            if (position == mHighlightPosition) {
                View v = holder.itemView;
                v.post(() -> {
                    if (v.getBackground() != null) {
                        final int centerX = v.getWidth() / 2;
                        final int centerY = v.getHeight() / 2;
                        v.getBackground().setHotspot(centerX, centerY);
                    }
                    v.setPressed(true);
                    v.setPressed(false);
                    mHighlightPosition = -1;
                });
            }
        }
    }
}
