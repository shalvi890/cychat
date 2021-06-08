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
package com.cioinfotech.cychat.push.fcm

import androidx.fragment.app.Fragment
import com.cioinfotech.cychat.features.settings.troubleshoot.NotificationTroubleshootTestManager
import com.cioinfotech.cychat.features.settings.troubleshoot.TestAccountSettings
import com.cioinfotech.cychat.features.settings.troubleshoot.TestDeviceSettings
import com.cioinfotech.cychat.features.settings.troubleshoot.TestNotification
import com.cioinfotech.cychat.features.settings.troubleshoot.TestPushRulesSettings
import com.cioinfotech.cychat.features.settings.troubleshoot.TestSystemSettings
import com.cioinfotech.cychat.gplay.features.settings.troubleshoot.TestFirebaseToken
import com.cioinfotech.cychat.gplay.features.settings.troubleshoot.TestPlayServices
import com.cioinfotech.cychat.gplay.features.settings.troubleshoot.TestPushFromPushGateway
import com.cioinfotech.cychat.gplay.features.settings.troubleshoot.TestTokenRegistration
import javax.inject.Inject

class NotificationTroubleshootTestManagerFactory @Inject constructor(
        private val testSystemSettings: TestSystemSettings,
        private val testAccountSettings: TestAccountSettings,
        private val testDeviceSettings: TestDeviceSettings,
        private val testBingRulesSettings: TestPushRulesSettings,
        private val testPlayServices: TestPlayServices,
        private val testFirebaseToken: TestFirebaseToken,
        private val testTokenRegistration: TestTokenRegistration,
        private val testPushFromPushGateway: TestPushFromPushGateway,
        private val testNotification: TestNotification
) {

    fun create(fragment: Fragment): NotificationTroubleshootTestManager {
        val mgr = NotificationTroubleshootTestManager(fragment)
        mgr.addTest(testSystemSettings)
        mgr.addTest(testAccountSettings)
        mgr.addTest(testDeviceSettings)
        mgr.addTest(testBingRulesSettings)
        mgr.addTest(testPlayServices)
        mgr.addTest(testFirebaseToken)
        mgr.addTest(testTokenRegistration)
        mgr.addTest(testPushFromPushGateway)
        mgr.addTest(testNotification)
        return mgr
    }
}