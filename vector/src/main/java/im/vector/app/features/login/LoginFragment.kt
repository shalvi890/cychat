/*
 * Copyright 2019 New Vector Ltd
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

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import im.vector.app.R
import im.vector.app.core.extensions.hideKeyboard
import im.vector.app.databinding.FragmentLoginBinding
import org.matrix.android.sdk.internal.cy_auth.data.CountryCode
import org.matrix.android.sdk.internal.cy_auth.data.PasswordLoginParams
import javax.inject.Inject

/**
 * In this screen:
 * In signin mode:
 * - the user is asked for login (or email) and password to sign in to a homeserver.
 * - He also can reset his password
 * In signup mode:
 * - the user is asked for login and password
 */
class LoginFragment @Inject constructor() : AbstractSSOLoginFragment<FragmentLoginBinding>() {
    val emailRegex = Regex("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$")
    var listOfCountries = mutableListOf<CountryCode>()
    var selectedCountry: CountryCode? = null
    private var firstTime = true
    //    private var passwordShown = false
//    private var isSignupMode = false
    // Temporary patch for https://github.com/vector-im/riotX-android/issues/1410,
    // waiting for https://github.com/matrix-org/synapse/issues/7576
//    private var isNumericOnlyUserIdForbidden = false

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentLoginBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        loginViewModel.handle(LoginAction.UpdateHomeServer("https://cyberia1.cioinfotech.com"))

        loginViewModel.handleCountryList("Bearer Avdhut")
        loginViewModel.countryCodeList.observe(viewLifecycleOwner) {
            if (it != null) {
                listOfCountries = it.data.countries
                val list = mutableListOf<String>()
                it.data.countries.forEach { countryCode -> list.add(countryCode.code + " " + countryCode.calling_code) }
                val spinnerArrayAdapter: ArrayAdapter<*> = ArrayAdapter(requireContext(),
                        R.layout.item_spinner_country,
                        list)
                views.spinnerList.adapter = spinnerArrayAdapter
                views.spinnerList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        selectedCountry = listOfCountries[position]
                        if (!firstTime) {
                            views.mobileNumberTil.error = null
                            invalidMobileNumber()
                        } else
                            firstTime = false

                        if (selectedCountry?.local_code.isNullOrEmpty()) {
                            views.optionalDigit.isVisible = false
                        } else {
                            views.optionalDigit.isVisible = true
                            views.optionalDigit.text = selectedCountry!!.local_code
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
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

    fun invalidMobileNumber(): Boolean {
        return if (views.mobileNumberField.text.toString().isNotBlank()) {
            if (selectedCountry != null && views.mobileNumberField.text?.length != selectedCountry?.noOfDigits) {
                views.mobileNumberTil.error = getString(R.string.error_empty_field_enter_digit_mobile, selectedCountry?.noOfDigits ?: 10
                )
                return true
            }
            false
        } else {
            views.mobileNumberTil.error = getString(R.string.error_empty_field_enter_digit_mobile, selectedCountry?.noOfDigits)
            true
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
            views.mobileNumberTil.error = getString(R.string.error_empty_field_enter_digit_mobile, selectedCountry?.noOfDigits)
            error++
        }
        if (invalidMobileNumber()) error++
        if (error == 0) {
            val deviceId = Settings.Secure.getString(requireActivity().contentResolver, Settings.Secure.ANDROID_ID)
            loginViewModel.handleCyLogin("Bearer Avdhut", PasswordLoginParams(login, mobileNo, deviceId, selectedCountry?.code ?: "IN"))
//            loginViewModel.handle(LoginAction.LoginOrRegister(login, password, getString(R.string.login_default_session_public_name)))
        }
    }

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

    private fun setupSubmitButton() {
        views.loginSubmit.setOnClickListener { submit() }
        views.loginField.doOnTextChanged { email, _, _, _ ->
            views.loginFieldTil.error = when {
                email?.isEmpty() == true || (email?.matches(emailRegex) != true) ->
                    getString(R.string.error_empty_field_enter_first_name)
                else                                                             -> null
            }

        }
        views.mobileNumberField.doOnTextChanged { _, _, _, _ ->
            if (!invalidMobileNumber())
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
        // Show M_WEAK_PASSWORD error in the password field
//        if (throwable is Failure.ServerError
//                && throwable.error.code == MatrixError.M_WEAK_PASSWORD) {
//            views.passwordFieldTil.error = errorFormatter.toHumanReadable(throwable)
//        } else {
        Toast.makeText(requireContext(), throwable.message, Toast.LENGTH_LONG).show()
//        views.loginFieldTil.error = errorFormatter.toHumanReadable(throwable)
//        }
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

    /**
     * Detect if password ends or starts with spaces
     */
//    private fun spaceInPassword() = views.passwordField.text.toString().let { it.trim() != it }
}
