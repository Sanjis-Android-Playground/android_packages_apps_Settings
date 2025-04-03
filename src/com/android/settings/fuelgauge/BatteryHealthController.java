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
package com.android.settings.fuelgauge;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.settings.core.BasePreferenceController;

public class BatteryHealthController extends BasePreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "BatteryHealthController";
    private static final String BATTERY_CHARGE_KEY = "persist.sys.battery_health_limit_charge";
    private static final String BATTERY_CHARGE_BY_LEVEL = "persist.sys.battery_health_limit_bypass_level";
    private static final String BATTERY_CHARGE_BY_USER_KEY = "smart_charge_by_user";

    public BatteryHealthController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return isGooglePixelDevice() ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreference) {
            SwitchPreference switchPreference = (SwitchPreference) preference;
            boolean isEnabled = "true".equals(SystemProperties.get(BATTERY_CHARGE_KEY, "false"));
            switchPreference.setChecked(isEnabled);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isEnabled = (Boolean) newValue;
        SystemProperties.set(BATTERY_CHARGE_KEY, isEnabled ? "true" : "false");
        SystemProperties.set(BATTERY_CHARGE_BY_LEVEL, "80");
        
        int modeValue = isEnabled ? 1 : 0;

        Settings.System.putIntForUser(
            mContext.getContentResolver(),
            BATTERY_CHARGE_KEY,
            modeValue,
            UserHandle.USER_CURRENT
        );
        Settings.System.putIntForUser(
            mContext.getContentResolver(),
            BATTERY_CHARGE_BY_USER_KEY,
            modeValue,
            UserHandle.USER_CURRENT
        );
        return true;
    }

    private boolean isGooglePixelDevice() {
        return Build.MANUFACTURER.equalsIgnoreCase("Google") &&
               Build.MODEL.toLowerCase().contains("pixel");
    }
}
