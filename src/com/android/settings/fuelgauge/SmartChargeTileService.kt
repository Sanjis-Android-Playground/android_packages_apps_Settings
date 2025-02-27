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

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.os.SystemProperties
import android.os.Build
import android.graphics.drawable.Icon
import com.android.settings.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class SmartChargeTileService : TileService() {

    companion object {
        private const val SYS_PROP = "persist.sys.battery_health_limit_charge"
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _smartChargeFlow = MutableStateFlow(SystemProperties.getBoolean(SYS_PROP, false))
    private var smartChargeJob: Job? = null

    override fun onStartListening() {
        observeSmartChargeProperty()
    }

    override fun onStopListening() {
        smartChargeJob?.cancel()
    }

    override fun onClick() {
        if (!isGooglePixelDevice()) return

        val newState = !_smartChargeFlow.value
        SystemProperties.set(SYS_PROP, newState.toString())
        _smartChargeFlow.value = newState
    }

    private fun observeSmartChargeProperty() {
        smartChargeJob?.cancel()
        smartChargeJob = coroutineScope.launch {
            flow {
                while (currentCoroutineContext().isActive) {
                    emit(SystemProperties.getBoolean(SYS_PROP, false))
                    delay(1000)
                }
            }.distinctUntilChanged()
             .collect { newValue ->
                _smartChargeFlow.value = newValue
                updateTile()
            }
        }
    }

    private fun updateTile() {
        val tile = qsTile ?: return

        if (!isGooglePixelDevice()) {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.updateTile()
            return
        }

        val enabled = _smartChargeFlow.value
        tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.icon = Icon.createWithResource(this, if (enabled) R.drawable.battery_unified_attr_defend else R.drawable.battery_unified_attr_charging)
        tile.label = getString(
            if (enabled) R.string.qs_tile_smart_charge_enabled
            else R.string.qs_tile_smart_charge_disabled
        )
        tile.updateTile()
    }

    private fun isGooglePixelDevice(): Boolean {
        return Build.MANUFACTURER.equals("Google", ignoreCase = true) &&
                Build.MODEL.lowercase().contains("pixel")
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
