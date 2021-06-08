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
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.error.ErrorFormatter
import com.cioinfotech.cychat.core.pushers.PushersManager
import com.cioinfotech.cychat.core.resources.StringProvider
import com.cioinfotech.cychat.features.settings.troubleshoot.TroubleshootTest
import com.cioinfotech.cychat.push.fcm.FcmHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.pushers.PushGatewayFailure
import javax.inject.Inject

/**
 * Test Push by asking the Push Gateway to send a Push back
 */
class TestPushFromPushGateway @Inject constructor(private val context: AppCompatActivity,
                                                  private val stringProvider: StringProvider,
                                                  private val errorFormatter: ErrorFormatter,
                                                  private val pushersManager: PushersManager)
    : TroubleshootTest(R.string.settings_troubleshoot_test_push_loop_title) {

    private var action: Job? = null

    override fun perform(activityResultLauncher: ActivityResultLauncher<Intent>) {
        val fcmToken = FcmHelper.getFcmToken(context) ?: run {
            status = TestStatus.FAILED
            return
        }
        action = GlobalScope.launch {
            status = runCatching { pushersManager.testPush(fcmToken) }
                    .fold(
                            {
                                // Wait for the push to be received
                                description = stringProvider.getString(R.string.settings_troubleshoot_test_push_loop_waiting_for_push)
                                TestStatus.RUNNING
                            },
                            {
                                description = if (it is PushGatewayFailure.PusherRejected) {
                                    stringProvider.getString(R.string.settings_troubleshoot_test_push_loop_failed)
                                } else {
                                    errorFormatter.toHumanReadable(it)
                                }
                                TestStatus.FAILED
                            }
                    )
        }
    }

    override fun onPushReceived() {
        description = stringProvider.getString(R.string.settings_troubleshoot_test_push_loop_success)
        status = TestStatus.SUCCESS
    }

    override fun cancel() {
        action?.cancel()
    }
}