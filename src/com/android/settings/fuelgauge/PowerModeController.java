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
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;
import com.android.settings.core.BasePreferenceController;

public class PowerModeController extends BasePreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "PowerModeController";
    private static final String DEVICE_POWER_MODE_KEY = "persist.sys.power_mode_perf";

    public PowerModeController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreference) {
            SwitchPreference switchPreference = (SwitchPreference) preference;
            boolean isEnabled = "1".equals(SystemProperties.get(DEVICE_POWER_MODE_KEY, "0"));
            switchPreference.setChecked(isEnabled);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isEnabled = (Boolean) newValue;
        applyPowerMode(isEnabled);
        return true;
    }

    private void applyPowerMode(boolean enabled) {
        int modeValue = enabled ? 1 : 0;
        SystemProperties.set(DEVICE_POWER_MODE_KEY, String.valueOf(modeValue));
        Log.d(TAG, "Power mode set to: " + modeValue);
        Settings.System.putIntForUser(
            mContext.getContentResolver(),
            DEVICE_POWER_MODE_KEY,
            modeValue,
            UserHandle.USER_CURRENT
        );
    }
}
