/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.lineageparts.gestures;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import org.lineageos.lineageparts.R;
import org.lineageos.lineageparts.SettingsPreferenceFragment;

public class GestureSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_THREE_FINGERS_SWIPE = "three_fingers_swipe_action";

    private ListPreference mThreeFingerSwipe;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.gesture_settings);

        mThreeFingerSwipe = findPreference(KEY_THREE_FINGERS_SWIPE);
        int action = Settings.System.getInt(getContentResolver(),
                KEY_THREE_FINGERS_SWIPE, 0);
        mThreeFingerSwipe.setValue(String.valueOf(action));
        mThreeFingerSwipe.setSummary(mThreeFingerSwipe.getEntry());
        mThreeFingerSwipe.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mThreeFingerSwipe) {
            int action = Integer.parseInt((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    KEY_THREE_FINGERS_SWIPE, action);
            int index = mThreeFingerSwipe.findIndexOfValue((String) newValue);
            mThreeFingerSwipe.setSummary(mThreeFingerSwipe.getEntries()[index]);
            return true;
        }
        return false;
    }
}
