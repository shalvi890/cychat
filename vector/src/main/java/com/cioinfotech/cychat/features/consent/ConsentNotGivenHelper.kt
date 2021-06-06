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

package com.cioinfotech.cychat.features.consent

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.dialogs.DialogLocker
import com.cioinfotech.cychat.core.platform.Restorable
import com.cioinfotech.cychat.features.webview.VectorWebViewActivity
import com.cioinfotech.cychat.features.webview.WebViewMode

class ConsentNotGivenHelper(private val activity: Activity,
                            private val dialogLocker: DialogLocker) :
        Restorable by dialogLocker {

    /* ==========================================================================================
     * Public methods
     * ========================================================================================== */

    /**
     * Display the consent dialog, if not already displayed
     */
    fun displayDialog(consentUri: String, homeServerHost: String) {
        dialogLocker.displayDialog {
            AlertDialog.Builder(activity)
                    .setTitle(R.string.settings_app_term_conditions)
                    .setMessage(activity.getString(R.string.dialog_user_consent_content, homeServerHost))
                    .setPositiveButton(R.string.dialog_user_consent_submit) { _, _ ->
                        openWebViewActivity(consentUri)
                    }
        }
    }

    /* ==========================================================================================
     * Private
     * ========================================================================================== */

    private fun openWebViewActivity(consentUri: String) {
        val intent = VectorWebViewActivity.getIntent(activity, consentUri, activity.getString(R.string.settings_app_term_conditions), WebViewMode.CONSENT)
        activity.startActivity(intent)
    }
}
