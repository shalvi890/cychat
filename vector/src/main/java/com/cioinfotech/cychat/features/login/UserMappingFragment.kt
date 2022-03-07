/*
 * Copyright (c) 2022 New Vector Ltd
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

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.DefaultSharedPreferences
import com.cioinfotech.cychat.databinding.FragmentUserMappingBinding
import org.matrix.android.sdk.internal.network.NetworkConstants
import javax.inject.Inject

class UserMappingFragment @Inject constructor() : AbstractSSOLoginFragment<FragmentUserMappingBinding>(), AdapterView.OnItemSelectedListener {

    private var buttonEnabledByTimer = false
    private var isSendCodeAPIHit = false
    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentUserMappingBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        views.btnSubmitSupplier.setOnClickListener { sendTokenToSupplier() }

        views.supplierField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                sendTokenToSupplier()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        views.btnCheckCode.setOnClickListener { login() }

        views.secretCodeField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        val pref = DefaultSharedPreferences.getInstance(requireContext())
        views.supplierField.setText(pref.getString(NetworkConstants.REF_CODE, null))

        views.supplierField.doOnTextChanged { text, _, _, _ ->
            text?.let {
                if (views.supplierField.text.toString().isDigitsOnly() && it.isNotEmpty()) {
                    views.supplierField.error = null
                    views.btnSubmitSupplier.isEnabled = buttonEnabledByTimer
                }
            }
        }

        views.secretCodeField.doOnTextChanged { text, _, _, _ ->
            text?.let {
                if (views.secretCodeField.text.toString().isDigitsOnly()
                        && views.secretCodeField.text.toString().length == 4 && it.isNotEmpty()) {
                    views.secretCodeField.error = null
                }
            }
        }
    }

    private fun startCountDownForEmailOTP() {
        var counter = 300
        views.emailOTPTimer.isVisible = true
        views.btnSubmitSupplier.isEnabled = false
        object : CountDownTimer((counter * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                try {
                    views.emailOTPTimer.text = if (counter > 60)
                        getString(R.string.auth_resend_email_otp_query_min, counter / 60, counter % 60)
                    else
                        getString(R.string.auth_resend_mobile_otp_query, counter)
                    counter--
                } catch (ex: Exception) {
                }
            }

            override fun onFinish() {
                try {
                    views.emailOTPTimer.isVisible = false
                    views.btnSubmitSupplier.isEnabled = true
                } catch (ex: Exception) {
                }
            }
        }.start()
    }

    private fun sendTokenToSupplier() {
        startCountDownForEmailOTP()
        isSendCodeAPIHit = true
        loginViewModel.handleUserMapping(views.supplierField.text.toString())
    }

    private fun login() {
        isSendCodeAPIHit = false
        loginViewModel.handleUserMappingConfirmed(views.secretCodeField.text.toString())
    }

    override fun onError(throwable: Throwable) {
        showErrorInSnackbar(
                if (throwable.message?.contains("502") == true)
                    Throwable(getString(R.string.something_went_wrong))
                else throwable
        )
        if(isSendCodeAPIHit) {
            views.emailOTPTimer.isVisible = false
            views.btnSubmitSupplier.isEnabled = true
        }
    }

    override fun resetViewModel() {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {}

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}
