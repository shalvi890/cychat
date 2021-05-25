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
import androidx.core.widget.doOnTextChanged
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.jakewharton.rxbinding3.widget.textChanges
import im.vector.app.R
import im.vector.app.core.extensions.hideKeyboard
import im.vector.app.databinding.FragmentLoginOTPBinding
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy

class LoginOTPFragment : AbstractLoginFragment<FragmentLoginOTPBinding>() {
    val nameRegex = Regex("[a-zA-Z]+(\\s+[a-zA-Z]+)*")
    var isSignUp = true
    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentLoginOTPBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginViewModel.signUpSignInData.observe(viewLifecycleOwner) {
            if (it.type == "sign-up") {
                isSignUp = true
                views.firstNameFieldTil.isVisible = true
                views.lastNameFieldTil.isVisible = true
                views.loginTitle.text = getString(R.string.auth_register)
            } else if (it.type == "sign-in") {
                isSignUp = false
                views.firstNameFieldTil.isVisible = false
                views.lastNameFieldTil.isVisible = false
                views.loginTitle.text = getString(R.string.sign_in_pf)
            }
        }
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

        loginViewModel.observeViewEvents { loginViewEvents ->
            if (loginViewEvents is LoginViewEvents.OnResendOTP) {
                Toast.makeText(requireContext(), "OTP Sent", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupEmailOTP() {
        views.btnResendEmailOTP.setOnClickListener {
            loginViewModel.resendOTP("Bearer Avdhut", "email")
            startCountDownForEmailOTP()
        }
        startCountDownForEmailOTP()
    }

    private fun startCountDownForEmailOTP() {
        var counter = 600
        views.emailOTPTimer.isVisible = true
        views.btnResendEmailOTP.isEnabled = false
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
                    views.btnResendEmailOTP.isEnabled = true
                } catch (ex: Exception) {
                }
            }
        }.start()
    }

    private fun setupMobileOTP() {
        views.btnResendMobileOTP.setOnClickListener {
            loginViewModel.resendOTP("Bearer Avdhut", "mobile")
            startCountDownForMobileOTP()
        }
        startCountDownForMobileOTP()
    }

    private fun startCountDownForMobileOTP() {
        var counter = 600
        views.mobileOTPTimer.isVisible = true
        views.btnResendMobileOTP.isEnabled = false
        object : CountDownTimer((counter * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                try {
                    views.mobileOTPTimer.text = if (counter > 60)
                        getString(R.string.auth_resend_email_otp_query_min, counter / 60, counter % 60)
                    else
                        getString(R.string.auth_resend_mobile_otp_query, counter)
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
        val firstName = views.firstNameField.text.toString()
        val lastName = views.lastNameField.text.toString()
        val emailOTP = views.otpEmailField.text.toString()
        val mobileOTP = views.otpMobileField.text.toString()
        var error = 0
        if (isSignUp) {
            if (firstName.isEmpty()) {
                views.firstNameFieldTil.error = getString(R.string.error_empty_field_enter_first_name)
                error++
            }
            if (!firstName.matches(nameRegex)) {
                views.firstNameFieldTil.error = getString(R.string.error_empty_field_enter_valid_first_name)
                error++
            }
            if (lastName.isEmpty()) {
                views.lastNameFieldTil.error = getString(R.string.error_empty_field_enter_last_name)
                error++
            }
            if (!lastName.matches(nameRegex)) {
                views.lastNameFieldTil.error = getString(R.string.error_empty_field_enter_valid_last_name)
                error++
            }
        }

        if (emailOTP.isEmpty() || !emailOTP.isDigitsOnly() || emailOTP.length != 4) {
            views.otpEmailFieldTil.error = getString(R.string.error_empty_field_enter_email_otp)
            error++
        }

        if (mobileOTP.isEmpty() || !mobileOTP.isDigitsOnly() || mobileOTP.length != 4) {
            views.otpMobileFieldTil.error = getString(R.string.error_empty_field_enter_mobile_otp)
            error++
        }

        if (error == 0)
            loginViewModel.handleCyCheckOTP("Bearer Avdhut", mobileOTP, emailOTP, firstName, lastName)
    }

    private fun cleanupUi() {
        views.loginSubmit.hideKeyboard()
        views.otpEmailFieldTil.error = null
        views.otpMobileFieldTil.error = null
        views.firstNameFieldTil.error = null
        views.lastNameFieldTil.error = null
    }

    private fun setupSubmitButton() {
        views.loginSubmit.setOnClickListener { submit() }
        views.otpEmailField.doOnTextChanged { it, _, _, _ ->
            views.otpEmailFieldTil.error = when {
                (it?.trim()?.isEmpty() == true || it?.isDigitsOnly() == false || it?.length != 4) -> {
                    getString(R.string.error_empty_field_enter_email_otp)
                }
                else                                                                              -> null
            }
        }

        views.otpMobileField.doOnTextChanged { it, _, _, _ ->
            views.otpMobileFieldTil.error = when {
                (it?.trim()?.isEmpty() == true || it?.isDigitsOnly() == false || it?.length != 4) -> {
                    getString(R.string.error_empty_field_enter_email_otp)
                }
                else                                                                              -> null
            }
        }

        views.firstNameField.doOnTextChanged { firstName, _, _, _ ->
            views.firstNameFieldTil.error = when {
                firstName?.isEmpty() == true          -> getString(R.string.error_empty_field_enter_first_name)
                firstName?.matches(nameRegex) != true -> getString(R.string.error_empty_field_enter_valid_first_name)
                else                                  -> null
            }
        }

        views.lastNameField.doOnTextChanged { lastName, _, _, _ ->
            views.lastNameFieldTil.error = when {
                lastName?.isEmpty() == true          -> getString(R.string.error_empty_field_enter_first_name)
                lastName?.matches(nameRegex) != true -> getString(R.string.error_empty_field_enter_valid_first_name)
                else                                 -> null
            }
        }
    }

    override fun resetViewModel() = loginViewModel.handle(LoginAction.ResetLogin)

    override fun onError(throwable: Throwable) {
        Toast.makeText(requireContext(), throwable.message, Toast.LENGTH_LONG).show()
    }

    override fun updateWithState(state: LoginViewState) {
//        isSignupMode = state.signMode == SignMode.SignUp
//        isNumericOnlyUserIdForbidden = state.serverType == ServerType.MatrixOrg

//        setupUi(state)
//        setupAutoFill(state)
//        setupButtons(state)

        when (state.asyncGetCountryList) {
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
