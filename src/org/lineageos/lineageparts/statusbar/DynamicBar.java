/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.lineageparts.statusbar;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import org.json.JSONArray;
import org.lineageos.lineageparts.R;
import org.lineageos.lineageparts.SettingsPreferenceFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DynamicBar extends SettingsPreferenceFragment {

    private static final String SETTINGS_KEY_ENABLED = "ax_dynamic_bar_enabled";
    private static final String SETTINGS_KEY_KEYGUARD_ENABLED = "ax_dynamic_bar_keyguard_enabled";
    private static final String SETTINGS_KEY_KEYGUARD_BATTERY_CHIP_MODE = "ax_dynamic_bar_keyguard_battery_chip_mode";
    private static final String SETTINGS_KEY_EVENTS = "ax_dynamic_bar_events";
    private static final String SETTINGS_KEY_COMPACT_NOTIFICATIONS = "ax_dynamic_bar_compact_notifications";

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private ContentObserver mSettingsObserver;

    private static final List<String> EVENT_TYPE_IDS = Arrays.asList(
        "screen_recording",
        "privacy",
        "audio_recording",
        "call",
        "media",
        "notification",
        "timer",
        "stopwatch",
        "alarm",
        "charging",
        "bluetooth",
        "hotspot",
        "ringer",
        "vpn",
        "clipboard",
        "torch",
        "casting",
        "promoted_ongoing",
        "sports",
        "app_switch",
        "biometric_unlock"
    );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.dynamic_bar);

        Preference enabledPref = findPreference(SETTINGS_KEY_ENABLED);
        if (enabledPref != null) {
            enabledPref.setOnPreferenceChangeListener((preference, newValue) -> {
                updateEnabledState((Boolean) newValue);
                return true;
            });
        }

        setupEventToggles();
        updateCompactNotificationVisibility();
        updateKeyguardSubPrefsVisibility();
        updateEnabledState(isDynamicBarEnabled());
        registerObserver();
    }

    private boolean isDynamicBarEnabled() {
        return Settings.Secure.getIntForUser(
            getContentResolver(), SETTINGS_KEY_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
    }

    private void updateEnabledState(boolean enabled) {
        Preference category = findPreference("dynamic_bar_events_category");
        if (category != null) {
            category.setEnabled(enabled);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSettingsObserver != null) {
            getContentResolver().unregisterContentObserver(mSettingsObserver);
        }
    }

    private void setupEventToggles() {
        Set<String> disabledEvents = getDisabledEvents();
        for (String typeId : EVENT_TYPE_IDS) {
            SwitchPreferenceCompat pref = findPreference("event_" + typeId);
            if (pref != null) {
                pref.setChecked(!disabledEvents.contains(typeId));
                pref.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean enabled = (Boolean) newValue;
                    toggleEvent(typeId, enabled);
                    if (typeId.equals("notification")) {
                        updateCompactNotificationVisibility();
                    }
                    return true;
                });
            }
        }
    }

    private Set<String> getDisabledEvents() {
        String json = Settings.Secure.getStringForUser(
            getContentResolver(),
            SETTINGS_KEY_EVENTS,
            UserHandle.USER_CURRENT
        );

        if (json == null || json.isEmpty()) {
            return new HashSet<>();
        }

        try {
            JSONArray arr = new JSONArray(json);
            Set<String> disabled = new HashSet<>();
            for (int i = 0; i < arr.length(); i++) {
                disabled.add(arr.getString(i));
            }
            return disabled;
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    private void toggleEvent(String typeId, boolean enabled) {
        Set<String> current = getDisabledEvents();
        if (enabled) {
            current.remove(typeId);
        } else {
            current.add(typeId);
        }
        
        String json = current.isEmpty() ? "" : new JSONArray(new ArrayList<>(current)).toString();

        Settings.Secure.putStringForUser(
            getContentResolver(),
            SETTINGS_KEY_EVENTS,
            json,
            UserHandle.USER_CURRENT
        );
    }

    private void updateCompactNotificationVisibility() {
        Preference compactPref = findPreference(SETTINGS_KEY_COMPACT_NOTIFICATIONS);
        SwitchPreferenceCompat notifPref = findPreference("event_notification");
        if (compactPref != null) {
            compactPref.setVisible(notifPref != null && notifPref.isChecked());
        }
    }

    private void updateKeyguardSubPrefsVisibility() {
        Preference batteryPref = findPreference(SETTINGS_KEY_KEYGUARD_BATTERY_CHIP_MODE);
        Preference keyguardPref = findPreference(SETTINGS_KEY_KEYGUARD_ENABLED);
        boolean enabled = keyguardPref != null && ((SwitchPreferenceCompat) keyguardPref).isChecked();
        if (batteryPref != null) {
            batteryPref.setVisible(enabled);
        }
    }

    private void registerObserver() {
        mSettingsObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                if (uri == null) return;
                String key = uri.getLastPathSegment();
                if (key.equals(SETTINGS_KEY_EVENTS)) {
                    Set<String> disabledEvents = getDisabledEvents();
                    for (String typeId : EVENT_TYPE_IDS) {
                        SwitchPreferenceCompat pref = findPreference("event_" + typeId);
                        if (pref != null) {
                            pref.setChecked(!disabledEvents.contains(typeId));
                        }
                    }
                    updateCompactNotificationVisibility();
                } else if (key.equals(SETTINGS_KEY_KEYGUARD_ENABLED)) {
                    updateKeyguardSubPrefsVisibility();
                } else if (key.equals(SETTINGS_KEY_ENABLED)) {
                    updateEnabledState(isDynamicBarEnabled());
                }
            }
        };

        getContentResolver().registerContentObserver(
            Settings.Secure.getUriFor(SETTINGS_KEY_EVENTS),
            false,
            mSettingsObserver
        );
        getContentResolver().registerContentObserver(
            Settings.Secure.getUriFor(SETTINGS_KEY_KEYGUARD_ENABLED),
            false,
            mSettingsObserver
        );
        getContentResolver().registerContentObserver(
            Settings.Secure.getUriFor(SETTINGS_KEY_ENABLED),
            false,
            mSettingsObserver
        );
    }
}