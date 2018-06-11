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

package com.android.launcher3.aoscp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.aoscp.widget.RadioButtonPreference;

import com.google.android.apps.nexuslauncher.qsb.HotseatQsbWidget;

import java.util.ArrayList;
import java.util.List;

import static android.provider.Settings.System.LUNA_SEARCHBAR_THEME;

public class SearchBarColorActivity extends Activity {

    private static final String TAG = "SearchBarColorActivity";

    private static final String KEY_QSB_DEFAULT   = "qsb_default";
    private static final String KEY_QSB_THEME     = "qsb_theme";
    private static final String KEY_QSB_ACCENT    = "qsb_accent";
    private static final String KEY_QSB_WALLPAPER = "qsb_wallpaper";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchbar_color_settings);

        FragmentManager fragManager = getFragmentManager();
        Fragment fragment = fragManager.findFragmentById(R.id.searchbar_color_settings);
        if (fragment == null) {
            fragManager.beginTransaction()
                    .add(R.id.searchbar_color_settings, new SearchBarColorFragment())
                    .commit();
        }
    }

    public static class SearchBarColorFragment extends LauncherPreferenceFragment 
        implements RadioButtonPreference.OnClickListener {

        List<RadioButtonPreference> mPreferences = new ArrayList<>();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.launcher_preferences_searchbar_color);
            final PreferenceScreen screen = getPreferenceScreen();
            final Context context = getActivity();

            for (int i = 0; i < screen.getPreferenceCount(); i++) {
                Preference pref = screen.getPreference(i);
                if (pref instanceof RadioButtonPreference) {
                    RadioButtonPreference colorPref = (RadioButtonPreference) pref;
                    colorPref.setOnClickListener(this);
                    mPreferences.add(colorPref);
                }
            }

            ContentResolver resolver = context.getContentResolver();
            int searchbarColor = Settings.System.getIntForUser(resolver, 
                    LUNA_SEARCHBAR_THEME, 0, UserHandle.USER_CURRENT);
            switch (searchbarColor) {
                case 0:
                    updateColorPreference(KEY_QSB_DEFAULT);
                    break;
                case 1:
                    updateColorPreference(KEY_QSB_THEME);
                    break;
                case 2:
                    updateColorPreference(KEY_QSB_ACCENT);
                    break;
                case 3:
                    updateColorPreference(KEY_QSB_WALLPAPER);
                    break;
            }
        }

        private void updateColorPreference(String selectionKey) {
            for (RadioButtonPreference pref : mPreferences) {
                if (selectionKey.equals(pref.getKey())) {
                    pref.setChecked(true);
                } else {
                    pref.setChecked(false);
                }
            }
        }

        @Override
        public void onRadioButtonClicked(RadioButtonPreference pref) {
            final Context context = getActivity();
            switch (pref.getKey()) {
                case KEY_QSB_DEFAULT:
                    Settings.System.putInt(context.getContentResolver(),
                            LUNA_SEARCHBAR_THEME, 0);
                    break;
                case KEY_QSB_THEME:
                    Settings.System.putInt(context.getContentResolver(),
                            LUNA_SEARCHBAR_THEME, 1);
                    break;
                case KEY_QSB_ACCENT:
                    Settings.System.putInt(context.getContentResolver(),
                            LUNA_SEARCHBAR_THEME, 2);
                    break;
                case KEY_QSB_WALLPAPER:
                    Settings.System.putInt(context.getContentResolver(),
                            LUNA_SEARCHBAR_THEME, 3);
                    break;
            }
            updateColorPreference(pref.getKey());
        }
    }
}