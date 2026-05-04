/*
 * SPDX-FileCopyrightText: 2014-2015 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.lineageparts.statusbar;

import android.os.Bundle;

import androidx.preference.Preference;

import org.lineageos.lineageparts.R;
import org.lineageos.lineageparts.SettingsPreferenceFragment;
import org.lineageos.lineageparts.utils.SystemUtils;

public class QuickSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String MEDIA_WAVEFORM_SEEKBAR = "media_waveform_seekbar";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.quick_settings);

        findPreference(MEDIA_WAVEFORM_SEEKBAR).setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (MEDIA_WAVEFORM_SEEKBAR.equals(preference.getKey())) {
            SystemUtils.showSystemUiRestartDialog(requireContext());
            return true;
        }
        return false;
    }
}
