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

package com.cioinfotech.cychat.features.workers.signout

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.extensions.cannotLogoutSafely
import com.cioinfotech.cychat.core.extensions.vectorComponent
import com.cioinfotech.cychat.features.MainActivity
import com.cioinfotech.cychat.features.MainActivityArgs

class SignOutUiWorker(private val activity: FragmentActivity) {

    fun perform() {
        val session = activity.vectorComponent().activeSessionHolder().getSafeActiveSession() ?: return
        if (session.cannotLogoutSafely()) {
            // The backup check on logout flow has to be displayed if there are keys in the store, and the keys backup state is not Ready
            val signOutDialog = SignOutBottomSheetDialogFragment.newInstance()
            signOutDialog.onSignOut = Runnable {
                doSignOut()
            }
            signOutDialog.show(activity.supportFragmentManager, "SO")
        } else {
            // Display a simple confirmation dialog
            AlertDialog.Builder(activity)
                    .setTitle(R.string.action_sign_out)
                    .setMessage(R.string.action_sign_out_confirmation_simple)
                    .setPositiveButton(R.string.action_sign_out) { _, _ ->
                        doSignOut()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
        }
    }

    private fun doSignOut() {
        MainActivity.restartApp(activity, MainActivityArgs(clearCredentials = true))
    }
}
