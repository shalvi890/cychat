/*
 * Copyright (c) 2021 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cioinfotech.cychat.features.settings.troubleshoot

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.resources.StringProvider
import com.cioinfotech.cychat.features.settings.VectorPreferences
import javax.inject.Inject

/**
 * Checks if notifications are enable in the system settings for this app.
 */
class TestDeviceSettings @Inject constructor(private val vectorPreferences: VectorPreferences,
                                             private val stringProvider: StringProvider)
    : TroubleshootTest(R.string.settings_troubleshoot_test_device_settings_title) {

    override fun perform(activityResultLauncher: ActivityResultLauncher<Intent>) {
        if (vectorPreferences.areNotificationEnabledForDevice()) {
            description = stringProvider.getString(R.string.settings_troubleshoot_test_device_settings_success)
            quickFix = null
            status = TestStatus.SUCCESS
        } else {
            quickFix = object : TroubleshootQuickFix(R.string.settings_troubleshoot_test_device_settings_quickfix) {
                override fun doFix() {
                    vectorPreferences.setNotificationEnabledForDevice(true)
                    manager?.retry(activityResultLauncher)
                }
            }
            description = stringProvider.getString(R.string.settings_troubleshoot_test_device_settings_failed)
            status = TestStatus.FAILED
        }
    }
}
