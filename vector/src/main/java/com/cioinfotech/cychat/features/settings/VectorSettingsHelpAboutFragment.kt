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

import androidx.preference.Preference
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.preference.VectorPreference
import com.cioinfotech.cychat.core.utils.FirstThrottler
import com.cioinfotech.cychat.core.utils.copyToClipboard
import com.cioinfotech.cychat.core.utils.displayInWebView
import com.cioinfotech.cychat.core.utils.openAppSettingsPage
import com.cioinfotech.cychat.features.version.VersionProvider
import com.cioinfotech.cychat.openOssLicensesMenuActivity
import org.matrix.android.sdk.api.Matrix
import javax.inject.Inject

class VectorSettingsHelpAboutFragment @Inject constructor(
        private val versionProvider: VersionProvider
) : VectorSettingsBaseFragment() {

    override var titleRes = R.string.preference_root_help_about
    override val preferenceXmlRes = R.xml.vector_settings_help_about

    private val firstThrottler = FirstThrottler(1000)

    override fun bindPref() {
        // preference to start the App info screen, to facilitate App permissions access
        findPreference<VectorPreference>(APP_INFO_LINK_PREFERENCE_KEY)!!
                .onPreferenceClickListener = Preference.OnPreferenceClickListener {
            activity?.let { openAppSettingsPage(it) }
            true
        }

        // application version
        findPreference<VectorPreference>(VectorPreferences.SETTINGS_VERSION_PREFERENCE_KEY)!!.let {
            it.summary = buildString {
                append(versionProvider.getVersion())
            }

            it.setOnPreferenceClickListener { pref ->
                copyToClipboard(requireContext(), pref.summary)
                true
            }
        }

        // SDK version
        findPreference<VectorPreference>(VectorPreferences.SETTINGS_SDK_VERSION_PREFERENCE_KEY)!!.let {
            it.summary = Matrix.getSdkVersion()

            it.setOnPreferenceClickListener { pref ->
                copyToClipboard(requireContext(), pref.summary)
                true
            }
        }

        // olm version
        findPreference<VectorPreference>(VectorPreferences.SETTINGS_OLM_VERSION_PREFERENCE_KEY)!!
                .summary = session.cryptoService().getCryptoVersion(requireContext(), false)

        // copyright
        findPreference<VectorPreference>(VectorPreferences.SETTINGS_COPYRIGHT_PREFERENCE_KEY)!!
                .onPreferenceClickListener = Preference.OnPreferenceClickListener {
            activity?.displayInWebView(VectorSettingsUrls.COPYRIGHT)
            false
        }

        // terms & conditions
        findPreference<VectorPreference>(VectorPreferences.SETTINGS_APP_TERM_CONDITIONS_PREFERENCE_KEY)!!
                .onPreferenceClickListener = Preference.OnPreferenceClickListener {
            activity?.displayInWebView(VectorSettingsUrls.TAC)
            false
        }

        // privacy policy
        findPreference<VectorPreference>(VectorPreferences.SETTINGS_PRIVACY_POLICY_PREFERENCE_KEY)!!
                .onPreferenceClickListener = Preference.OnPreferenceClickListener {
            activity?.displayInWebView(VectorSettingsUrls.PRIVACY_POLICY)
            false
        }

        // third party notice
        findPreference<VectorPreference>(VectorPreferences.SETTINGS_THIRD_PARTY_NOTICES_PREFERENCE_KEY)!!
                .onPreferenceClickListener = Preference.OnPreferenceClickListener {
            if (firstThrottler.canHandle() is FirstThrottler.CanHandlerResult.Yes) {
                activity?.displayInWebView(VectorSettingsUrls.THIRD_PARTY_LICENSES)
            }
            false
        }

        // Note: preference is not visible on F-Droid build
        findPreference<VectorPreference>(VectorPreferences.SETTINGS_OTHER_THIRD_PARTY_NOTICES_PREFERENCE_KEY)!!
                .onPreferenceClickListener = Preference.OnPreferenceClickListener {
            // See https://developers.google.com/android/guides/opensource
            openOssLicensesMenuActivity(requireActivity())
            false
        }
    }

    companion object {
        private const val APP_INFO_LINK_PREFERENCE_KEY = "APP_INFO_LINK_PREFERENCE_KEY"
    }
}
