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

package com.cioinfotech.cychat.features.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.cioinfotech.cychat.BuildConfig
import com.cioinfotech.cychat.core.di.DefaultSharedPreferences
import com.cioinfotech.cychat.databinding.FragmentLoginSplashBinding
import com.cioinfotech.cychat.features.settings.VectorPreferences
import org.matrix.android.sdk.internal.network.NetworkConstants
import org.matrix.android.sdk.internal.network.RetrofitFactory

import javax.inject.Inject

/**
 * In this screen, the user is viewing an introduction to what he can do with this application
 */
class LoginSplashFragment @Inject constructor(
        private val vectorPreferences: VectorPreferences
) : AbstractLoginFragment<FragmentLoginSplashBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentLoginSplashBinding {
        return FragmentLoginSplashBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        views.loginSplashSubmit.setOnClickListener { getStarted() }

        if (BuildConfig.debug_mode) {
            views.loginSplashVersion.isVisible = true
            @SuppressLint("SetTextI18n")
            views.loginSplashVersion.text = "Version : ${BuildConfig.VERSION_NAME}"
        }
    }

    /** Code for Different Screen logic When Release & Debug build is given environment screen is not shown in release */
    private fun getStarted() {
        if (BuildConfig.debug_mode)
            loginViewModel.handle(LoginAction.PostViewEvent(LoginViewEvents.OnLoginFlowRetrieved))
        else {
            val prefs = DefaultSharedPreferences.getInstance(requireContext())
            RetrofitFactory.BASE_URL = NetworkConstants.UAT_URL
            prefs.edit().apply {
                putString(NetworkConstants.BASE_URL, NetworkConstants.UAT_URL)
                putString(NetworkConstants.CY_CHAT_ENV, NetworkConstants.UAT)
                apply()
            }
            loginViewModel.handle(LoginAction.PostViewEvent(LoginViewEvents.OnHomeserverSelection))
        }
    }

    override fun resetViewModel() {}
}
