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
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.extensions.hideKeyboard
import com.cioinfotech.cychat.core.platform.showOptimizedSnackbar
import com.cioinfotech.cychat.databinding.FragmentLoginBinding
import com.cioinfotech.cychat.databinding.FragmentValidateSecurityCodeBinding
import org.matrix.android.sdk.internal.cy_auth.data.CountryCode
import org.matrix.android.sdk.internal.cy_auth.data.CountryCodeParent
import org.matrix.android.sdk.internal.cy_auth.data.PasswordLoginParams
import timber.log.Timber
import javax.inject.Inject

/**
 * In this screen:
 * In signin mode:
 * - the user is asked for login (or email) and password to sign in to a homeserver.
 * - He also can reset his password
 * In signup mode:
 * - the user is asked for login and password
 */
class LoginFragment @Inject constructor() : AbstractSSOLoginFragment<FragmentLoginBinding>(), AdapterView.OnItemSelectedListener {
    private val emailRegex = Regex("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$")
    private var allSettings: CountryCodeParent? = null
    private var selectedCountry: CountryCode? = null
    private var firstTime = true
    private var isUserValidated = false
    private var securityCodeDialogShowing = false
    private var dialogBinding: FragmentValidateSecurityCodeBinding? = null
    //    private var passwordShown = false
//    private var isSignupMode = false
    // Temporary patch for https://github.com/vector-im/riotX-android/issues/1410,
    // waiting for https://github.com/matrix-org/synapse/issues/7576
//    private var isNumericOnlyUserIdForbidden = false

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentLoginBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var spinnerArrayAdapter: ArrayAdapter<*> = ArrayAdapter(requireContext(),
                R.layout.item_spinner_country,
                mutableListOf<String>())

        views.spinnerList.adapter = spinnerArrayAdapter
        loginViewModel.handleCountryList()
        loginViewModel.countryCodeList.observe(viewLifecycleOwner) {
            if (it != null && it.data.countries.isNotEmpty()) {
                allSettings = it
                val list = mutableListOf<String>()
                it.data.countries.forEach { countryCode -> list.add(countryCode.code + " " + countryCode.calling_code) }
                spinnerArrayAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_country,
                        list)
                views.spinnerList.adapter = spinnerArrayAdapter
                views.spinnerList.onItemSelectedListener = this
            }
        }


        setupSubmitButton()
//        setupForgottenPasswordButton()
//        setupPasswordReveal()
        views.mobileNumberField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

//        views.spinnerList.setOnItemClickListener { _, _, position, _ ->
//            Toast.makeText(requireContext(), listOfCountries[position], Toast.LENGTH_LONG).show()
//        }
//        views.ccp.setOnCountryChangeListener {
//            invalidMobileNumber()
//        }
    }

    private fun invalidMobileNumber(): Boolean {
        return if (views.mobileNumberField.text.toString().isNotBlank()) {
            if (selectedCountry != null && isMobileLengthInvalid()) {
                views.mobileNumberTil.error = getString(R.string.error_empty_field_enter_digit_mobile, selectedCountry?.mobile_size?.replace(",", " or ") ?: "10"
                )
                return true
            }
            false
        } else {
            views.mobileNumberTil.error = getString(R.string.error_empty_field_enter_digit_mobile, selectedCountry?.mobile_size?.replace(",", "\\"))
            true
        }
    }

    private fun isMobileLengthInvalid(): Boolean {
        try {
            val list = selectedCountry?.mobile_size?.split(",") ?: mutableListOf()
            for (item in list) {
                if (views.mobileNumberField.text?.trim()?.length == Integer.parseInt(item))
                    return false
            }
            return true
        } catch (ex: Exception) {
            Timber.d(ex)
            return true
        }
    }
//    private fun setupForgottenPasswordButton() {
//        views.forgetPasswordButton.setOnClickListener { forgetPasswordClicked() }
//    }

//    private fun setupAutoFill(state: LoginViewState) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            when (state.signMode) {
//                SignMode.Unknown            -> error("developer error")
//                SignMode.SignUp             -> {
//                    views.loginField.setAutofillHints(HintConstants.AUTOFILL_HINT_NEW_USERNAME)
//                    views.passwordField.setAutofillHints(HintConstants.AUTOFILL_HINT_NEW_PASSWORD)
//                    views.loginSocialLoginButtons.mode = SocialLoginButtonsView.Mode.MODE_SIGN_UP
//                }
//                SignMode.SignIn,
//                SignMode.SignInWithMatrixId -> {
//                    views.loginField.setAutofillHints(HintConstants.AUTOFILL_HINT_USERNAME)
//                    views.passwordField.setAutofillHints(HintConstants.AUTOFILL_HINT_PASSWORD)
//                    views.loginSocialLoginButtons.mode = SocialLoginButtonsView.Mode.MODE_SIGN_IN
//                }
//            }.exhaustive
//        }
//    }
    /**
     * Submit & Validation for phone number & email
     */
    @SuppressLint("HardwareIds")
    private fun submit() {
        cleanupUi()

        val login = views.loginField.text.toString()
        val mobileNo = views.mobileNumberField.text.toString()

        var error = 0

        if (login.isEmpty()) {
            views.loginFieldTil.error = getString(R.string.error_empty_field_enter_user_name)
            error++
        }
        if (!login.matches(emailRegex)) {
            views.loginFieldTil.error = getString(R.string.error_empty_field_enter_user_name)
            error++
        }
        if (mobileNo.isEmpty()) {
            views.mobileNumberTil.error = getString(R.string.error_empty_field_enter_mobile)
            error++
        }
        if (!mobileNo.isDigitsOnly()) {
            views.mobileNumberTil.error = getString(R.string.error_empty_field_enter_digit_mobile, selectedCountry?.mobile_size)
            error++
        }
        if (invalidMobileNumber()) error++
        if (error == 0) {
            val deviceId = Settings.Secure.getString(requireActivity().contentResolver, Settings.Secure.ANDROID_ID)
            loginViewModel.handleCyLogin(PasswordLoginParams(login.lowercase(), mobileNo, deviceId, selectedCountry?.code
                    ?: "IN"), allSettings?.data?.secCodeDomains)

            if ((allSettings?.data?.secCodeDomains?.contains(login.getEmailDomain()) == true) && !isUserValidated) {
                val dialog = Dialog(requireContext())
                FragmentValidateSecurityCodeBinding.inflate(
                        layoutInflater,
                        views.root,
                        false
                ).apply {
                    dialogBinding = this
                    dialog.setContentView(this.root)
                    dialog.window?.setLayout(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT
                    )
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    this.btnValidate.setOnClickListener {
                        this.otpTil.error = null
                        this.btnValidate.hideKeyboard()

                        if (this.otpField.text.toString().isEmpty())
                            this.otpTil.error = getString(R.string.please_enter_security_code)
                        else {
                            dialogBinding?.btnValidate?.isEnabled = false
                            pbProgress.isVisible = true
                            loginViewModel.validateSecurityCode(this.otpField.text.toString())
                        }
                    }
                    this.tvTitle.setOnClickListener {
                        securityCodeDialogShowing = false
                        dialog.dismiss()
                    }
                    securityCodeDialogShowing = true
                    dialog.show()
                }
                loginViewModel.isUserValidatedLiveData.observe(viewLifecycleOwner) {
                    if (it) {
                        securityCodeDialogShowing = false
                        isUserValidated = true
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    private fun String?.getEmailDomain() = this?.substring(this.lastIndexOf("@") + 1, this.length) ?: ""

    private fun cleanupUi() {
        views.loginSubmit.hideKeyboard()
        views.loginFieldTil.error = null
        views.mobileNumberTil.error = null
    }

//    private fun setupUi(state: LoginViewState) {
//        views.loginFieldTil.hint = getString(when (state.signMode) {
//            SignMode.Unknown            -> error("developer error")
//            SignMode.SignUp             -> R.string.login_signup_username_hint
//            SignMode.SignIn             -> R.string.login_signin_username_hint
//            SignMode.SignInWithMatrixId -> R.string.login_signin_matrix_id_hint
//        })

// Handle direct signin first
//        if (state.signMode == SignMode.SignInWithMatrixId) {
//            views.loginServerIcon.isVisible = false
//            views.loginTitle.text = getString(R.string.login_signin_matrix_id_title)
//            views.loginNotice.text = getString(R.string.login_signin_matrix_id_notice)
//            views.loginPasswordNotice.isVisible = true
//        } else {
//            val resId = when (state.signMode) {
//                SignMode.Unknown            -> error("developer error")
//                SignMode.SignUp             -> R.string.login_signup_to
//                SignMode.SignIn             -> R.string.login_connect_to
//                SignMode.SignInWithMatrixId -> R.string.login_connect_to
//            }

//            when (state.serverType) {
//                ServerType.MatrixOrg -> {
//                    views.loginServerIcon.isVisible = true
//                    views.loginServerIcon.setImageResource(R.drawable.ic_logo_matrix_org)
//                    views.loginTitle.text = getString(resId, state.homeServerUrl.toReducedUrl())
//                    views.loginNotice.text = getString(R.string.login_server_matrix_org_text)
//                }
//                ServerType.EMS       -> {
//                    views.loginServerIcon.isVisible = true
//                    views.loginServerIcon.setImageResource(R.drawable.ic_logo_element_matrix_services)
//                    views.loginTitle.text = getString(resId, "Element Matrix Services")
//                    views.loginNotice.text = getString(R.string.login_server_modular_text)
//                }
//                ServerType.Other     -> {
//                    views.loginServerIcon.isVisible = false
//                    views.loginTitle.text = getString(resId, state.homeServerUrl.toReducedUrl())
//                    views.loginNotice.text = getString(R.string.login_server_other_text)
//                }
//                ServerType.Unknown   -> Unit /* Should not happen */
//            }
//            views.loginPasswordNotice.isVisible = false

//            if (state.loginMode is LoginMode.SsoAndPassword) {
//                views.loginSocialLoginContainer.isVisible = true
//                views.loginSocialLoginButtons.ssoIdentityProviders = state.loginMode.ssoIdentityProviders
//                views.loginSocialLoginButtons.listener = object : SocialLoginButtonsView.InteractionListener {
//                    override fun onProviderSelected(id: String?) {
//                        loginViewModel.getSsoUrl(
//                                redirectUrl = LoginActivity.VECTOR_REDIRECT_URL,
//                                deviceId = state.deviceId,
//                                providerId = id
//                        )
//                                ?.let { openInCustomTab(it) }
//                    }
//                }
//            } else {
//                views.loginSocialLoginContainer.isVisible = false
//                views.loginSocialLoginButtons.ssoIdentityProviders = null
//            }
//        }
//    }

//    private fun setupButtons(state: LoginViewState) {
//        views.forgetPasswordButton.isVisible = state.signMode == SignMode.SignIn

//        views.loginSubmit.text = getString(when (state.signMode) {
//            SignMode.Unknown            -> error("developer error")
//            SignMode.SignUp             -> R.string.login_signup_submit
//            SignMode.SignIn,
//            SignMode.SignInWithMatrixId -> R.string.login_signin
//        })
//    }
    /** UI Logic For Cychat API Calls & after that actual login to Matrix Server*/
    private fun setupSubmitButton() {
        views.loginSubmit.setOnClickListener { submit() }
        views.loginField.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                views.loginFieldTil.error = when {
                    views.loginField.text.toString().isEmpty() || !views.loginField.text.toString().matches(emailRegex) ->
                        getString(R.string.error_empty_field_enter_user_name)
                    else                                                                                                -> null
                }

        }
        views.loginField.doOnTextChanged { text, _, _, _ ->
            text?.let {
                if (it.matches(emailRegex) && it.isNotEmpty()) {
                    views.loginFieldTil.error = null
                }
            }
        }
        views.mobileNumberField.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                if (!invalidMobileNumber())
                    views.mobileNumberTil.error = null
        }
        views.mobileNumberField.doOnTextChanged { _, _, _, _ ->
            if (selectedCountry != null && isMobileLengthInvalid())
                views.mobileNumberTil.error = null
        }
    }

//    private fun forgetPasswordClicked() {
//        loginViewModel.handle(LoginAction.PostViewEvent(LoginViewEvents.OnForgetPasswordClicked))
//    }

//    private fun setupPasswordReveal() {
//        passwordShown = false
//
//        views.passwordReveal.setOnClickListener {
//            passwordShown = !passwordShown
//
//            renderPasswordField()
//        }
//
//        renderPasswordField()
//    }

//    private fun renderPasswordField() {
//        views.passwordField.showPassword(passwordShown)
//        views.passwordReveal.render(passwordShown)
//    }

    override fun resetViewModel() = loginViewModel.handle(LoginAction.ResetLogin)

    override fun onError(throwable: Throwable) {
        if (securityCodeDialogShowing) {
            dialogBinding?.pbProgress?.isVisible = false
            dialogBinding?.btnValidate?.isEnabled = true
            dialogBinding?.root?.showOptimizedSnackbar(errorFormatter.toHumanReadable(throwable))
        } else
            showErrorInSnackbar(if (throwable.message?.contains("502") == true) Throwable("Server is Offline") else throwable)
    }

    override fun updateWithState(state: LoginViewState) {
//        isSignupMode = state.signMode == SignMode.SignUp
//        isNumericOnlyUserIdForbidden = state.serverType == ServerType.MatrixOrg

//        setupUi(state)
//        setupAutoFill(state)
//        setupButtons(state)

//        when (state.asyncLoginAction) {
//            is Loading -> {
        // Ensure password is hidden
//                passwordShown = false
//                renderPasswordField()
//            }
//            is Fail    -> {
//                val error = state.asyncLoginAction.error
//                if (error is Failure.ServerError
//                        && error.error.code == MatrixError.M_FORBIDDEN
//                        && error.error.message.isEmpty()) {
//                    // Login with email, but email unknown
//                    views.loginFieldTil.error = getString(R.string.login_login_with_email_error)
//                } else {
        // Trick to display the error without text.
//                    views.loginFieldTil.error = " "
//                    if (error.isInvalidPassword() && spaceInPassword()) {
//                        views.passwordFieldTil.error = getString(R.string.auth_invalid_login_param_space_in_password)
//                    } else {
//                        views.passwordFieldTil.error = errorFormatter.toHumanReadable(error)
//                    }
//                }
//            }
        // Success is handled by the LoginActivity
//            else       -> Unit
//        }

//        when (state.asyncRegistration) {
//            is Loading -> {
//                // Ensure password is hidden
//                passwordShown = false
////                renderPasswordField()
//            }
//            // Success is handled by the LoginActivity
//            else       -> Unit
//        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        allSettings?.data?.countries?.let {
            selectedCountry = it[position]
        }
        if (firstTime)
            firstTime = false
        else
            invalidMobileNumber()

        if (selectedCountry?.local_code.isNullOrEmpty()) {
            views.optionalDigit.isVisible = false
        } else {
            views.optionalDigit.isVisible = true
            views.optionalDigit.text = selectedCountry?.local_code
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

//    private fun spaceInPassword() = views.passwordField.text.toString().let { it.trim() != it }
}
