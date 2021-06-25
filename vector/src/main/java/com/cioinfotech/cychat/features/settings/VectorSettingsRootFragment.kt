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

package com.cioinfotech.cychat.features.settings

import android.os.Bundle
import android.view.View
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.DefaultSharedPreferences
import com.cioinfotech.cychat.core.preference.VectorPreference
import org.matrix.android.sdk.internal.network.NetworkConstants
import javax.inject.Inject

class VectorSettingsRootFragment @Inject constructor() : VectorSettingsBaseFragment() {

    override var titleRes: Int = R.string.title_activity_settings
    override val preferenceXmlRes = R.xml.vector_settings_root

    override fun bindPref() {
        tintIcons()
    }

    private fun tintIcons() {
        for (i in 0 until preferenceScreen.preferenceCount) {
            (preferenceScreen.getPreference(i) as? VectorPreference)?.let { it.tintIcon = true }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pref = DefaultSharedPreferences.getInstance(requireContext())
        findPreference<VectorPreference>(ENVIRONMENT)?.summary = pref.getString(NetworkConstants.CY_CHAT_ENV, "")
    }

    companion object {
        //Cychat Preference
        const val ENVIRONMENT = "ENVIRONMENT"
    }
}
