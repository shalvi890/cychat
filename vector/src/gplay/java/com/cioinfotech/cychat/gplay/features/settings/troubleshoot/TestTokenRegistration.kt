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
package com.cioinfotech.cychat.gplay.features.settings.troubleshoot

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.matrix.android.sdk.api.session.pushers.PusherState
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.ActiveSessionHolder
import com.cioinfotech.cychat.core.pushers.PushersManager
import com.cioinfotech.cychat.core.resources.StringProvider
import com.cioinfotech.cychat.features.settings.troubleshoot.TroubleshootTest
import com.cioinfotech.cychat.push.fcm.FcmHelper
import javax.inject.Inject

/**
 * Force registration of the token to HomeServer
 */
class TestTokenRegistration @Inject constructor(private val context: AppCompatActivity,
                                                private val stringProvider: StringProvider,
                                                private val pushersManager: PushersManager,
                                                private val activeSessionHolder: ActiveSessionHolder)
    : TroubleshootTest(R.string.settings_troubleshoot_test_token_registration_title) {

    override fun perform(activityResultLauncher: ActivityResultLauncher<Intent>) {
        // Check if we have a registered pusher for this token
        val fcmToken = FcmHelper.getFcmToken(context) ?: run {
            status = TestStatus.FAILED
            return
        }
        val session = activeSessionHolder.getSafeActiveSession() ?: run {
            status = TestStatus.FAILED
            return
        }
        val pushers = session.getPushers().filter {
            it.pushKey == fcmToken && it.state == PusherState.REGISTERED
        }
        if (pushers.isEmpty()) {
            description = stringProvider.getString(R.string.settings_troubleshoot_test_token_registration_failed,
                    stringProvider.getString(R.string.sas_error_unknown))
            quickFix = object : TroubleshootQuickFix(R.string.settings_troubleshoot_test_token_registration_quick_fix) {
                override fun doFix() {
                    val workId = pushersManager.registerPusherWithFcmKey(fcmToken)
                    WorkManager.getInstance(context).getWorkInfoByIdLiveData(workId).observe(context, Observer { workInfo ->
                        if (workInfo != null) {
                            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                                manager?.retry(activityResultLauncher)
                            } else if (workInfo.state == WorkInfo.State.FAILED) {
                                manager?.retry(activityResultLauncher)
                            }
                        }
                    })
                }
            }

            status = TestStatus.FAILED
        } else {
            description = stringProvider.getString(R.string.settings_troubleshoot_test_token_registration_success)
            status = TestStatus.SUCCESS
        }
    }
}