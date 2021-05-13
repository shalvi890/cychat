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

package im.vector.app.features.login

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.jakewharton.rxbinding3.widget.textChanges
import im.vector.app.R
import im.vector.app.core.extensions.hideKeyboard
import im.vector.app.databinding.FragmentLoginOTPBinding
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy

class LoginOTPFragment : AbstractLoginFragment<FragmentLoginOTPBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentLoginOTPBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSubmitButton()
        setupEmailOTP()
        setupMobileOTP()
        views.otpMobileField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun setupEmailOTP() {
        views.btnResendEmailOTP.setOnClickListener {
            Toast.makeText(requireContext(), "Email OTP Send", Toast.LENGTH_LONG).show()
            startCountDownForEmailOTP()
        }
        startCountDownForEmailOTP()
    }

    private fun startCountDownForEmailOTP() {
        var counter = 60
        views.emailOTPTimer.isVisible = true
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                try {
                    views.emailOTPTimer.text = getString(R.string.auth_resend_email_otp_query, counter)
                    counter--
                } catch (ex: Exception) {
                }
            }

            override fun onFinish() {
                try {
                    views.emailOTPTimer.isVisible = false
                    views.btnResendEmailOTP.isEnabled = true
                } catch (ex: Exception) {
                }
            }
        }.start()
    }

    private fun setupMobileOTP() {
        views.btnResendMobileOTP.setOnClickListener {
            Toast.makeText(requireContext(), "Mobile OTP Send", Toast.LENGTH_LONG).show()
            startCountDownForMobileOTP()
        }
        startCountDownForMobileOTP()
    }

    private fun startCountDownForMobileOTP() {
        var counter = 60
        views.mobileOTPTimer.isVisible = true
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                try {
                    views.mobileOTPTimer.text = getString(R.string.auth_resend_mobile_otp_query, counter)
                    counter--
                } catch (ex: Exception) {
                }
            }

            override fun onFinish() {
                try {
                    views.mobileOTPTimer.isVisible = false
                    views.btnResendMobileOTP.isEnabled = true
                } catch (ex: Exception) {
                }
            }
        }.start()
    }

    private fun submit() {
        cleanupUi()

        val emailOTP = views.otpEmailField.text.toString()
        val mobileOTP = views.otpMobileField.text.toString()

        // This can be called by the IME action, so deal with empty cases
        var error = 0
        if (emailOTP.isEmpty() || !emailOTP.isDigitsOnly() || emailOTP.length != 4) {
            views.otpEmailFieldTil.error = getString(R.string.error_empty_field_enter_email_otp)
            error++
        }

        if (mobileOTP.isEmpty() || !mobileOTP.isDigitsOnly() || mobileOTP.length != 4) {
            views.otpMobileFieldTil.error = getString(R.string.error_empty_field_enter_mobile_otp)
            error++
        }

        if (error == 0)
            loginViewModel.handleCyCheckOTP("Bearer Avdhut", mobileOTP, emailOTP)

//        loginViewModel.handle(LoginAction.UpdateHomeServer("https://cyberia1.cioinfotech.com"))
    }

    private fun cleanupUi() {
        views.loginSubmit.hideKeyboard()
        views.otpEmailFieldTil.error = null
        views.otpMobileFieldTil.error = null
    }

    private fun setupSubmitButton() {
        views.loginSubmit.setOnClickListener { submit() }
        Observable
                .combineLatest(
                        views.otpEmailField.textChanges().map { it.trim().isNotEmpty() && it.isDigitsOnly() && it.length == 4 },
                        views.otpMobileField.textChanges().map { it.trim().isNotEmpty() && it.isDigitsOnly() && it.length == 4 },
                        { isLoginNotEmpty, isPasswordNotEmpty ->
                            isLoginNotEmpty && isPasswordNotEmpty
                        }
                ).subscribeBy {
                    views.otpEmailFieldTil.error = null
                    views.otpMobileFieldTil.error = null
                    views.loginSubmit.isEnabled = it
                }.disposeOnDestroyView()
    }

    override fun resetViewModel() = loginViewModel.handle(LoginAction.ResetLogin)

    override fun onError(throwable: Throwable) {
        views.otpMobileFieldTil.error = errorFormatter.toHumanReadable(throwable)
    }

    override fun updateWithState(state: LoginViewState) {
//        isSignupMode = state.signMode == SignMode.SignUp
//        isNumericOnlyUserIdForbidden = state.serverType == ServerType.MatrixOrg

//        setupUi(state)
//        setupAutoFill(state)
//        setupButtons(state)

        when (state.asyncLoginAction) {
            is Loading -> {
            }
            is Fail    -> {
//                val error = state.asyncLoginAction.error
//                if (error is Failure.ServerError
//                        && error.error.code == MatrixError.M_FORBIDDEN
//                        && error.error.message.isEmpty()) {
//                    // Login with email, but email unknown
//                    views.loginFieldTil.error = @TODO
//                } else {
//                    // Trick to display the error without text.
//                    views.loginFieldTil.error = " "
////                    if (error.isInvalidPassword() && spaceInPassword()) {
////                        views.passwordFieldTil.error = getString(R.string.auth_invalid_login_param_space_in_password)
////                    } else {
////                        views.passwordFieldTil.error = errorFormatter.toHumanReadable(error)
////                    }
//                }
            }
            // Success is handled by the LoginActivity
            else       -> Unit
        }
    }
}
