/*
 * Copyright (C) 2025 The AxionAOSP Project
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
package com.android.settings.fuelgauge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.SystemProperties
import android.os.UserHandle
import android.provider.Settings
import android.util.Log

class PowerModeBootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PowerModeBootCompletedReceiver"
        private const val DEVICE_POWER_MODE_KEY = "persist.sys.power_mode_perf"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            Log.d(TAG, "Boot completed - applying saved power mode")

            val perfEnabled = Settings.System.getIntForUser(
                context.contentResolver,
                DEVICE_POWER_MODE_KEY,
                0,
                UserHandle.USER_CURRENT
            ) != 0

            SystemProperties.set(DEVICE_POWER_MODE_KEY, "0")

            Handler(Looper.getMainLooper()).postDelayed({
                SystemProperties.set(DEVICE_POWER_MODE_KEY, if (perfEnabled) "1" else "0")
                Log.d(TAG, "Power mode applied: ${if (perfEnabled) "Performance" else "Normal"}")
            }, 5000)
        }
    }
}
