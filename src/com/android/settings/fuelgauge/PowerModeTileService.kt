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

import android.content.Context
import android.database.ContentObserver
import android.graphics.drawable.Icon
import android.os.*
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.android.settings.R

class PowerModeTileService : TileService() {

    companion object {
        private const val TAG = "PowerModeTileService"
        private const val DEVICE_POWER_MODE_KEY = "persist.sys.power_mode_perf"
    }

    private var settingsObserver: ContentObserver? = null

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
        registerSettingsObserver()
    }

    override fun onStopListening() {
        super.onStopListening()
        unregisterSettingsObserver()
    }

    override fun onClick() {
        super.onClick()
        togglePowerMode()
    }

    private fun updateTileState() {
        val isPerformanceMode = SystemProperties.get(DEVICE_POWER_MODE_KEY, "0") == "1"
        qsTile?.apply {
            label = getString(if (isPerformanceMode) R.string.power_mode_performance else R.string.power_mode_default)
            icon = Icon.createWithResource(
                this@PowerModeTileService,
                if (isPerformanceMode) R.drawable.ic_performance_mode else R.drawable.ic_power_default
            )
            state = if (isPerformanceMode) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTile()
        }
        Log.d(TAG, "Tile updated: ${if (isPerformanceMode) "Performance Mode ON" else "Performance Mode OFF"}")
    }

    private fun togglePowerMode() {
        val isCurrentlyEnabled = SystemProperties.get(DEVICE_POWER_MODE_KEY, "0") == "1"
        val newModeValue = if (isCurrentlyEnabled) "0" else "1"
        contentResolver?.let {
            Settings.System.putIntForUser(it, DEVICE_POWER_MODE_KEY, newModeValue.toInt(), UserHandle.USER_CURRENT)
        }
        SystemProperties.set(DEVICE_POWER_MODE_KEY, newModeValue)
        Log.d(TAG, "Power mode toggled: $newModeValue")
        updateTileState()
    }

    private fun registerSettingsObserver() {
        settingsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                updateTileState()
            }
        }
        contentResolver?.registerContentObserver(
            Settings.System.getUriFor(DEVICE_POWER_MODE_KEY),
            false,
            settingsObserver!!
        )
    }

    private fun unregisterSettingsObserver() {
        settingsObserver?.let {
            contentResolver?.unregisterContentObserver(it)
        }
    }
}
