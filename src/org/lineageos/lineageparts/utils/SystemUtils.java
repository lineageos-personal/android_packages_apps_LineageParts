/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.lineageparts.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.android.internal.util.losp.Utils;

import org.lineageos.lineageparts.R;

public class SystemUtils {

    public static void showSystemUiRestartDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.systemui_restart_title)
                .setMessage(R.string.systemui_restart_message)
                .setPositiveButton(R.string.dlg_ok, (dialog, which) -> restartSystemUI(context))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public static void restartSystemUI(Context context) {
        Toast.makeText(context, R.string.systemui_restart_process, Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Utils.restartSystemUI();
        }, 2000);
    }
}
