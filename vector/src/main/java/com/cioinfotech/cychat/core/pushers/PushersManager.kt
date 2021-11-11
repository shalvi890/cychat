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

package com.cioinfotech.cychat.core.pushers

import android.content.Context
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.ActiveSessionHolder
import com.cioinfotech.cychat.core.di.DefaultSharedPreferences
import com.cioinfotech.cychat.core.resources.AppNameProvider
import com.cioinfotech.cychat.core.resources.LocaleProvider
import com.cioinfotech.cychat.core.resources.StringProvider
import org.matrix.android.sdk.internal.network.NetworkConstants
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs

private const val DEFAULT_PUSHER_FILE_TAG = "mobile"

class PushersManager @Inject constructor(
        private val activeSessionHolder: ActiveSessionHolder,
        private val localeProvider: LocaleProvider,
        private val stringProvider: StringProvider,
        private val appNameProvider: AppNameProvider,
        context: Context
) {

    private val pref = DefaultSharedPreferences.getInstance(context)
    private val defaultURL = stringProvider.getString(R.string.pusher_http_url)
    private val jitsiURL = pref.getString(NetworkConstants.SYGNAL, "")

    suspend fun testPush(pushKey: String) {
        val currentSession = activeSessionHolder.getActiveSession()

        currentSession.testPush(
                if (jitsiURL.isNullOrEmpty()) defaultURL else "$jitsiURL/_matrix/push/v1/notify",
                stringProvider.getString(R.string.pusher_app_id),
                pushKey,
                TEST_EVENT_ID
        )
    }

    fun registerPusherWithFcmKey(pushKey: String): UUID {
        val currentSession = activeSessionHolder.getActiveSession()
        val profileTag = DEFAULT_PUSHER_FILE_TAG + "_" + abs(currentSession.myUserId.hashCode())

        return currentSession.addHttpPusher(
                pushKey,
                stringProvider.getString(R.string.pusher_app_id),
                profileTag,
                localeProvider.current().language,
                appNameProvider.getAppName(),
                currentSession.sessionParams.deviceId ?: "MOBILE",
                if (jitsiURL.isNullOrEmpty()) defaultURL else "$jitsiURL/_matrix/push/v1/notify",
                append = false,
                withEventIdOnly = true
        )
    }

    suspend fun unregisterPusher(pushKey: String) {
        val currentSession = activeSessionHolder.getSafeActiveSession() ?: return
        currentSession.removeHttpPusher(pushKey, stringProvider.getString(R.string.pusher_app_id))
    }

    companion object {
        const val TEST_EVENT_ID = "\$THIS_IS_A_FAKE_EVENT_ID"
    }
}
