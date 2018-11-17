/*
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
package com.android.launcher3.quickspace;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings;
import android.view.View;

import com.android.internal.ambient.play.AmbientPlayHistoryEntry;
import com.android.internal.ambient.play.AmbientPlayHistoryManager;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;

import java.util.List;

public class QuickEvents {

    private static final int AMBIENT_INFO_MAX_DURATION = 120000; // 2 minutes
    private static final String SETTING_DEVICE_INTRO_COMPLETED = "device_introduction_completed";

    private Context mContext;

    private String mEventTitleText;
    private String mEventTitleSubText;
    private int mEventSubIcon;

    private boolean mIsQuickEvent = false;

// Event identifiers start

    // Device Intro
    private boolean mEventIntro = false;
    private boolean mEventIntroClicked = false;
    private boolean mIsFirstTimeDone;

    // Ambient Play
    private boolean mEventAmbientPlay = false;
    private long mLastAmbientInfo;
    private String mAmbientPlaySong;
    private String mAmbientPlayArtist;

// Event identifiers end

    public QuickEvents(Context context) {
        mContext = context;
        initQuickEvents();
    }

    public void initQuickEvents() {
        mIsFirstTimeDone = Settings.System.getInt(mContext.getContentResolver(), SETTING_DEVICE_INTRO_COMPLETED, 0) != 0;
        deviceIntroEvent();
        ambientPlayEvent();
    }

    private void deviceIntroEvent() {
        if (mIsFirstTimeDone || mEventIntroClicked) {
            mEventIntro = false;
            return;
        }
        mIsQuickEvent = true;
        mEventIntro = true;
        mEventTitleText = mContext.getResources().getString(R.string.quick_event_aoscp_intro_welcome);
        mEventTitleSubText = mContext.getResources().getString(R.string.quick_event_aoscp_intro_learn);
    }

    public void ambientPlayEvent() {
        if (mEventAmbientPlay) {
            boolean infoExpired = System.currentTimeMillis() - mLastAmbientInfo > AMBIENT_INFO_MAX_DURATION;
            if (infoExpired) {
                mIsQuickEvent = false;
                mEventAmbientPlay = false;
            }
        }
    }

    public void initAmbientPlayEvent() {
        if (mEventIntro) return;
        List<AmbientPlayHistoryEntry> songInfo = AmbientPlayHistoryManager.getSongs(mContext);
        if (songInfo.size() < 1) return;
        AmbientPlayHistoryEntry entry = songInfo.get(0);
        mEventTitleText = mContext.getResources().getString(R.string.quick_event_ambient_now_playing);
        mEventTitleSubText = String.format(mContext.getResources().getString(
                R.string.quick_event_ambient_song_artist), entry.getSongTitle(), entry.getArtistTitle());
        mEventSubIcon = R.drawable.ic_music_note_24dp;
        mIsQuickEvent = true;
        mEventAmbientPlay = true;
        mAmbientPlaySong = entry.getSongTitle();
        mAmbientPlayArtist = entry.getArtistTitle();
        mLastAmbientInfo = System.currentTimeMillis();
    }

    public boolean isQuickEvent() {
        return mIsQuickEvent;
    }

    public String getEventTitle() {
        return mEventTitleText;
    }

    public String getEventSubTitle() {
        return mEventTitleSubText;
    }

    public int getEventSubIcon() {
        return mEventSubIcon;
    }

    public void setOnClickTask(View view) {
        if (mEventIntro) {
            final Intent intent = new Intent(Settings.ACTION_DEVICE_INTRODUCTION)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            try {
                Launcher.getLauncher(mContext).startActivitySafely(view, intent, null);
            } catch (ActivityNotFoundException ex) {
            }
            mIsQuickEvent = false;
            mEventIntroClicked = true;
        } else if (mEventAmbientPlay) {
            String query = String.format(mContext.getResources().getString(
                    R.string.quick_event_ambient_song_artist), mAmbientPlaySong, mAmbientPlayArtist);
            final Intent ambient = new Intent(Intent.ACTION_WEB_SEARCH)
                    .putExtra(SearchManager.QUERY, query);
            try {
                Launcher.getLauncher(mContext).startActivitySafely(view, ambient, null);
            } catch (ActivityNotFoundException ex) {
            }
        }
    }
}