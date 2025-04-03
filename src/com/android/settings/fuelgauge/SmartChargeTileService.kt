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

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.graphics.drawable.Icon
import android.content.Context
import android.os.UserHandle
import android.os.SystemProperties
import android.util.Log
import com.android.settings.R

class SmartChargeTileService : TileService() {

    companion object {
        private const val BATTERY_CHARGE_KEY = "persist.sys.battery_health_limit_charge"
    }

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            updateTileState()
        }
    }

    override fun onStartListening() {
        contentResolver.registerContentObserver(
            Settings.System.getUriFor(BATTERY_CHARGE_KEY),
            false,
            contentObserver
        )
        updateTileState()
    }

    override fun onStopListening() {
        contentResolver.unregisterContentObserver(contentObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(contentObserver)
    }

    override fun onClick() {
        if (!isGooglePixelDevice()) return

        val isEnabled = getSmartChargeEnabled()
        val newState = if (isEnabled) 0 else 1
        val newPropertyState = if (isEnabled) "false" else "true"

        Settings.System.putIntForUser(
            contentResolver,
            BATTERY_CHARGE_KEY,
            newState,
            UserHandle.USER_CURRENT
        )

        SystemProperties.set(BATTERY_CHARGE_KEY, newPropertyState)
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return

        if (!isGooglePixelDevice()) {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.updateTile()
            return
        }

        val enabled = getSmartChargeEnabled()
        tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.icon = Icon.createWithResource(this, if (enabled) R.drawable.battery_unified_attr_defend else R.drawable.battery_unified_attr_charging)
        tile.label = getString(
            if (enabled) R.string.qs_tile_smart_charge_enabled
            else R.string.qs_tile_smart_charge_disabled
        )
        tile.updateTile()
    }

    private fun getSmartChargeEnabled(): Boolean {
        return Settings.System.getIntForUser(
            contentResolver,
            BATTERY_CHARGE_KEY,
            0,
            UserHandle.USER_CURRENT
        ) == 1
    }

    private fun isGooglePixelDevice(): Boolean {
        return android.os.Build.MANUFACTURER.equals("Google", ignoreCase = true) &&
                android.os.Build.MODEL.lowercase().contains("pixel")
    }
}
