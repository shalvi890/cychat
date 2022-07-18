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
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.ActivityViewModelContext
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.ActiveSessionHolder
import com.cioinfotech.cychat.core.di.DefaultSharedPreferences
import com.cioinfotech.cychat.core.extensions.configureAndStart
import com.cioinfotech.cychat.core.extensions.exhaustive
import com.cioinfotech.cychat.core.platform.VectorViewModel
import com.cioinfotech.cychat.core.resources.StringProvider
import com.cioinfotech.cychat.core.utils.ensureTrailingSlash
import com.cioinfotech.cychat.features.cycore.AES
import com.cioinfotech.cychat.features.signout.soft.SoftLogoutActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.auth.AuthenticationService
import org.matrix.android.sdk.api.auth.HomeServerHistoryService
import org.matrix.android.sdk.api.auth.data.HomeServerConnectionConfig
import org.matrix.android.sdk.api.auth.data.LoginFlowResult
import org.matrix.android.sdk.api.auth.data.LoginFlowTypes
import org.matrix.android.sdk.api.auth.login.LoginWizard
import org.matrix.android.sdk.api.auth.registration.FlowResult
import org.matrix.android.sdk.api.auth.registration.RegistrationResult
import org.matrix.android.sdk.api.auth.registration.RegistrationWizard
import org.matrix.android.sdk.api.auth.registration.Stage
import org.matrix.android.sdk.api.auth.wellknown.WellknownResult
import org.matrix.android.sdk.api.failure.Failure
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.internal.cy_auth.data.BaseResponse
import org.matrix.android.sdk.internal.cy_auth.data.CheckOTPResponse
import org.matrix.android.sdk.internal.cy_auth.data.GetSettingsParent
import org.matrix.android.sdk.internal.cy_auth.data.GroupParent
import org.matrix.android.sdk.internal.cy_auth.data.LoginResponse
import org.matrix.android.sdk.internal.cy_auth.data.LoginResponseChild
import org.matrix.android.sdk.internal.cy_auth.data.MatrixLoginData
import org.matrix.android.sdk.internal.cy_auth.data.OrganizationParent
import org.matrix.android.sdk.internal.cy_auth.data.PasswordLoginParams
import org.matrix.android.sdk.internal.cy_auth.data.RecheckCodeResponse
import org.matrix.android.sdk.internal.cy_auth.data.UserTypeParent
import org.matrix.android.sdk.internal.network.NetworkConstants
import org.matrix.android.sdk.internal.network.NetworkConstants.CHECK_CODE_API
import org.matrix.android.sdk.internal.network.NetworkConstants.CHECK_OTP_API
import org.matrix.android.sdk.internal.network.NetworkConstants.CLID
import org.matrix.android.sdk.internal.network.NetworkConstants.CLIENT_NAME
import org.matrix.android.sdk.internal.network.NetworkConstants.COUNTRY_CODE
import org.matrix.android.sdk.internal.network.NetworkConstants.CY_VERSE_ANDROID
import org.matrix.android.sdk.internal.network.NetworkConstants.CY_VERSE_API_CLID
import org.matrix.android.sdk.internal.network.NetworkConstants.EMAIL_OTP
import org.matrix.android.sdk.internal.network.NetworkConstants.EMAIL_SMALL
import org.matrix.android.sdk.internal.network.NetworkConstants.EMAIL_VAL
import org.matrix.android.sdk.internal.network.NetworkConstants.F_NAME
import org.matrix.android.sdk.internal.network.NetworkConstants.GENERAL_DATA
import org.matrix.android.sdk.internal.network.NetworkConstants.GET_GROUPS_API
import org.matrix.android.sdk.internal.network.NetworkConstants.GET_IND_TYPE_API
import org.matrix.android.sdk.internal.network.NetworkConstants.GET_ORGANIZATION_API
import org.matrix.android.sdk.internal.network.NetworkConstants.GET_SETTINGS
import org.matrix.android.sdk.internal.network.NetworkConstants.GET_SETTINGS_API
import org.matrix.android.sdk.internal.network.NetworkConstants.GET_USER_TYPE_API
import org.matrix.android.sdk.internal.network.NetworkConstants.GROUP_VALUE
import org.matrix.android.sdk.internal.network.NetworkConstants.IMEI
import org.matrix.android.sdk.internal.network.NetworkConstants.LIVE
import org.matrix.android.sdk.internal.network.NetworkConstants.LOGIN
import org.matrix.android.sdk.internal.network.NetworkConstants.L_NAME
import org.matrix.android.sdk.internal.network.NetworkConstants.MOBILE
import org.matrix.android.sdk.internal.network.NetworkConstants.MOBILE_OTP
import org.matrix.android.sdk.internal.network.NetworkConstants.OP
import org.matrix.android.sdk.internal.network.NetworkConstants.REF_CODE
import org.matrix.android.sdk.internal.network.NetworkConstants.REQ_ID
import org.matrix.android.sdk.internal.network.NetworkConstants.RESENT_OTP_API
import org.matrix.android.sdk.internal.network.NetworkConstants.RE_CHECK_REF_CODE
import org.matrix.android.sdk.internal.network.NetworkConstants.SERVICE_NAME
import org.matrix.android.sdk.internal.network.NetworkConstants.SETUP_ID
import org.matrix.android.sdk.internal.network.NetworkConstants.SIGNING_MODE
import org.matrix.android.sdk.internal.network.NetworkConstants.SIGN_UP_SMALL
import org.matrix.android.sdk.internal.network.NetworkConstants.TYPE
import org.matrix.android.sdk.internal.network.NetworkConstants.USERTYPE_DATA
import org.matrix.android.sdk.internal.network.NetworkConstants.USER_CAT_ID
import org.matrix.android.sdk.internal.network.NetworkConstants.USER_ID
import org.matrix.android.sdk.internal.network.NetworkConstants.USER_LOGIN_API
import org.matrix.android.sdk.internal.network.NetworkConstants.USER_TYPE
import org.matrix.android.sdk.internal.network.NetworkConstants.USER_TYPE_NAME
import org.matrix.android.sdk.internal.network.NetworkConstants.VALIDATE_CODE
import timber.log.Timber
import java.util.concurrent.CancellationException

/**
 *
 */
@SuppressLint("StaticFieldLeak")
class LoginViewModel @AssistedInject constructor(
        @Assisted initialState: LoginViewState,
        private val applicationContext: Context,
        private val authenticationService: AuthenticationService,
        private val activeSessionHolder: ActiveSessionHolder,
        private val homeServerConnectionConfigFactory: HomeServerConnectionConfigFactory,
        private val reAuthHelper: ReAuthHelper,
        private val stringProvider: StringProvider,
        private val homeServerHistoryService: HomeServerHistoryService
) : VectorViewModel<LoginViewState, LoginAction, LoginViewEvents>(initialState) {

    @AssistedFactory
    interface Factory {
        fun create(initialState: LoginViewState): LoginViewModel
    }

    private var pref: SharedPreferences = DefaultSharedPreferences.getInstance(applicationContext)
    private var reqId = pref.getString(REQ_ID, null)

    //    private var accessToken = pref.getString(NetworkConstants.ACCESS_TOKEN, null).attachBearer()
    var groupValue = LIVE

//    private fun String?.attachBearer(): String {
//        return when {
//            this.isNullOrEmpty()  -> return ""
//            this.contains(BEARER) -> return this
//            else                  -> BEARER + this
//        }
//    }

    init {
        getKnownCustomHomeServersUrls()
    }

    private fun getKnownCustomHomeServersUrls() {
        setState {
            copy(knownCustomHomeServersUrls = homeServerHistoryService.getKnownServersUrls())
        }
    }

    companion object : MvRxViewModelFactory<LoginViewModel, LoginViewState> {

        @JvmStatic
        override fun create(viewModelContext: ViewModelContext, state: LoginViewState): LoginViewModel {
            return when (val activity: FragmentActivity = (viewModelContext as ActivityViewModelContext).activity()) {
                is LoginActivity      -> activity.loginViewModelFactory.create(state)
                is SoftLogoutActivity -> activity.loginViewModelFactory.create(state)
                else                  -> error("Invalid Activity")
            }
        }
    }

    // Store the last action, to redo it after user has trusted the untrusted certificate
    private var lastAction: LoginAction? = null
    private var currentHomeServerConnectionConfig: HomeServerConnectionConfig? = null

    private val matrixOrgUrl = stringProvider.getString(R.string.matrix_org_server_url).ensureTrailingSlash()

    val currentThreePid: String? get() = registrationWizard.currentThreePid

    // True when login and password has been sent with success to the homeserver
    val isRegistrationStarted: Boolean
        get() = authenticationService.isRegistrationStarted

    private val registrationWizard: RegistrationWizard get() = authenticationService.getRegistrationWizard()

    private val loginWizard: LoginWizard get() = authenticationService.getLoginWizard()

    private var loginConfig: LoginConfig? = null

    private var loginParams: PasswordLoginParams? = null

    val countryCodeList: MutableLiveData<GetSettingsParent> = MutableLiveData()

    val signUpSignInData: MutableLiveData<LoginResponseChild> = MutableLiveData()

    private var currentJob: Job? = null
        set(value) {
            // Cancel any previous Job
            field?.cancel()
            field = value
        }

    override fun handle(action: LoginAction) {
        when (action) {
            is LoginAction.UpdateSignMode             -> startAuthenticationFlow()//handleUpdateSignMode(action)
            is LoginAction.InitWith                   -> handleInitWith(action)
            is LoginAction.UpdateHomeServer           -> handleUpdateHomeserver(action).also { lastAction = action }
            is LoginAction.LoginOrRegister            -> handleLogin(action).also { lastAction = action }
            is LoginAction.LoginWithToken             -> handleLoginWithToken(action)
            is LoginAction.WebLoginSuccess            -> handleWebLoginSuccess(action)
            is LoginAction.ResetPassword              -> handleResetPassword(action)
            is LoginAction.ResetPasswordMailConfirmed -> handleResetPasswordMailConfirmed()
            is LoginAction.RegisterAction             -> handleRegisterAction(action)
            is LoginAction.ResetAction                -> handleResetAction(action)
            is LoginAction.SetupSsoForSessionRecovery -> handleSetupSsoForSessionRecovery(action)
            is LoginAction.UserAcceptCertificate      -> handleUserAcceptCertificate(action)
            LoginAction.ClearHomeServerHistory        -> handleClearHomeServerHistory()
            is LoginAction.PostViewEvent              -> _viewEvents.post(action.viewEvent)
            else                                      -> Unit
            //This Case Added By Me As we wont be needing all above cases working
        }.exhaustive
    }

    /** CyChat API- Start */
    fun handleSupplierConfirmation(code: String,
                                   utypeId: String,
                                   clid: String,
                                   setupId: String,
                                   utypeName: String) {
        pref.edit {
            putString(CLID, clid)
            putString(USER_TYPE, utypeId)
            putString(USER_TYPE_NAME, utypeName)
            putString(REF_CODE, code)
            putString(SETUP_ID, setupId)
            apply()
        }
//        if (isSkipApi) {
//            _viewEvents.post(LoginViewEvents.OnSupplierConfirmed)
//            setState {
//                copy(
//                        asyncSupplierConfirmed = Success(Unit)
//                )
//            }
//        } else {
        val tempMap = hashMapOf(
                CLIENT_NAME to CY_VERSE_ANDROID,
                GROUP_VALUE to groupValue,
                OP to CHECK_CODE_API,
                REF_CODE to code,
                USER_TYPE to utypeId,
                SERVICE_NAME to USERTYPE_DATA,
                CLID to clid
        )
        authenticationService.cyNewCheckCode(tempMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(checkCodeObserver())
        setState { copy(asyncSupplierConfirmed = Loading()) }
//        }
    }

    private fun checkCodeObserver(): SingleObserver<BaseResponse> {
        return object : SingleObserver<BaseResponse> {

            override fun onSuccess(t: BaseResponse) {
                if (t.status == "ok") {
                    _viewEvents.post(LoginViewEvents.OnSupplierConfirmed)
                    setState { copy(asyncSupplierConfirmed = Success(Unit)) }
                } else {
                    _viewEvents.post(LoginViewEvents.Failure(Throwable(t.message ?: "Please, Check Referral Code!")))
                    setState { copy(asyncSupplierConfirmed = Fail(Throwable(t.message ?: "Please, Check Referral Code!"))) }
                }
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                setState { copy(asyncSupplierConfirmed = Fail(e)) }
            }
        }
    }

    fun handleUserMapping(supplierCode: String) {
        val tempMap = hashMapOf(
                SERVICE_NAME to LOGIN,
                CLIENT_NAME to CY_VERSE_ANDROID,
                OP to RE_CHECK_REF_CODE,
                REF_CODE to supplierCode,
                REQ_ID to (reqId ?: ""),
                CLID to (pref.getString(CLID, "") ?: ""),
                F_NAME to (pref.getString(F_NAME, "") ?: ""),
                L_NAME to (pref.getString(L_NAME, "") ?: "")
        )
        authenticationService.recheckReferralCode(tempMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getHandleUserMappingObserver())
        setState {
            copy(asyncSupplierReConfirmed = Loading())
        }
    }

    /** CyChat Get Settings API Implementation-
     * Function waits for Get Country List API Response
     * */
    private fun getHandleUserMappingObserver(): SingleObserver<RecheckCodeResponse> {
        return object : SingleObserver<RecheckCodeResponse> {
            override fun onSuccess(t: RecheckCodeResponse) {
                if (t.status == "ok") {
                    if (t.data?.email != null)
                        _viewEvents.post(LoginViewEvents.OnTokenSentConfirmed)
                    else if (t.data != null)
                        startLogin(t.data!!)
                    setState {
                        copy(asyncSupplierReConfirmed = Success(Unit))
                    }
                } else {
                    _viewEvents.post(LoginViewEvents.Failure(Throwable(t.message)))
                    setState {
                        copy(asyncSupplierReConfirmed = Fail(Throwable(t.message)))
                    }
                }
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                _viewEvents.post(LoginViewEvents.Failure(e))
                setState {
                    copy(
                            asyncSupplierReConfirmed = Fail(e)
                    )
                }
            }
        }
    }

    /** CyChat Get Country List / Get Settings API Implementation-
     * No Params just to get all countries with codes.
     * */
    fun handleUserMappingConfirmed(supplierCode: String) {
        val tempMap = hashMapOf(
                SERVICE_NAME to LOGIN,
                CLIENT_NAME to CY_VERSE_ANDROID,
                OP to VALIDATE_CODE,
                EMAIL_VAL to supplierCode,
                REF_CODE to (pref.getString(REF_CODE, "") ?: ""),
                F_NAME to (pref.getString(F_NAME, "") ?: ""),
                L_NAME to (pref.getString(L_NAME, "") ?: ""),
                REQ_ID to (reqId ?: ""),
                CLID to (pref.getString(CLID, "") ?: "")
        )
        authenticationService.validateCodeBySupplier(tempMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getUserMappingConfirmedObserver())
        setState {
            copy(asyncUserMapped = Loading())
        }
    }

    /** CyChat Get Settings API Implementation-
     * Function waits for Get Country List API Response
     * */
    private fun getUserMappingConfirmedObserver(): SingleObserver<CheckOTPResponse> {
        return object : SingleObserver<CheckOTPResponse> {
            override fun onSuccess(t: CheckOTPResponse) {
                if (t.status == "ok") {
                    startLogin(t.data)
                    setState {
                        copy(
                                asyncUserMapped = Success(Unit)
                        )
                    }
                } else {
                    _viewEvents.post(LoginViewEvents.Failure(Throwable(t.message)))
                    setState {
                        copy(
                                asyncUserMapped = Fail(Throwable(t.message))
                        )
                    }
                }
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                _viewEvents.post(LoginViewEvents.Failure(e))
                setState {
                    copy(
                            asyncUserMapped = Fail(e)
                    )
                }
            }
        }
    }

//    private fun String?.getEmailDomain() = this?.substring(this.lastIndexOf("@") + 1, this.length) ?: ""

    /** CyChat Login API Implementation-
     * @param passwordLoginParams- Mobile Number & Email of User
     * */
    fun handleCyLogin(passwordLoginParams: PasswordLoginParams) {
        loginParams = passwordLoginParams
        val map = hashMapOf(
                SERVICE_NAME to LOGIN,
                CLIENT_NAME to CY_VERSE_ANDROID,
                CLID to (pref.getString(CLID, "") ?: ""),
                OP to USER_LOGIN_API,
                USER_TYPE to (pref.getString(USER_TYPE, "") ?: ""),
                MOBILE to loginParams!!.mobile,
                EMAIL_SMALL to loginParams!!.email,
                IMEI to loginParams!!.imei_no,
                COUNTRY_CODE to loginParams!!.countryCode
        )
        authenticationService.cyLogin(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getCyLoginObserver(passwordLoginParams.mobile))
        setState {
            copy(
                    asyncCyLogin = Loading()
            )
        }
    }

    /** CyChat Login API Implementation-
     * Function waits for Login API Response
     * */
    private fun getCyLoginObserver(mobile: String): SingleObserver<LoginResponse> {
        return object : SingleObserver<LoginResponse> {

            override fun onSuccess(t: LoginResponse) {
                if (t.status == "ok") {
                    isUserValidated.postValue(true)
                    _viewEvents.post(LoginViewEvents.OnSendOTPs)
                    signUpSignInData.postValue(t.data)
                    pref.edit {
                        putString(REQ_ID, t.data.reqID)
                        putString(F_NAME, t.data.firstName)
                        putString(L_NAME, t.data.lastName)
                        putString(MOBILE, mobile)

                        reqId = t.data.reqID
                        if (t.data.type == SIGN_UP_SMALL)
                            putBoolean(SIGNING_MODE, true)
                        else
                            putBoolean(SIGNING_MODE, false)
                        apply()
                    }
                    setState {
                        copy(
                                asyncCyLogin = Success(Unit)
                        )
                    }
                } else {
                    isUserValidated.postValue(true)
                    _viewEvents.post(LoginViewEvents.Failure(Throwable(t.message)))
                    setState {
                        copy(
                                asyncCyLogin = Fail(Throwable(t.message))
                        )
                    }
                }
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                _viewEvents.post(LoginViewEvents.Failure(e))
                setState {
                    copy(
                            asyncCyLogin = Fail(e)
                    )
                }
            }
        }
    }

    /** CyChat Check OTP API Implementation-
     * @param emailOTP (Email OTP sent to User)
     * @param mobileOTP (Mobile OTP sent to User)
     * @param firstName (optional, first name for signup)
     * @param lastName (optional, last name for signup)
     * */
    fun handleCyCheckOTP(emailOTP: String, mobileOTP: String, firstName: String, lastName: String) {
        loginParams?.let {
            val map = hashMapOf(
                    SERVICE_NAME to LOGIN,
                    CLIENT_NAME to CY_VERSE_ANDROID,
                    CLID to (pref.getString(CLID, "") ?: ""),
                    OP to CHECK_OTP_API,
                    USER_TYPE to (pref.getString(USER_TYPE, "") ?: ""),
                    IMEI to it.imei_no,
                    F_NAME to firstName,
                    L_NAME to lastName,
                    TYPE to signUpSignInData.value!!.type,
                    EMAIL_OTP to emailOTP,
                    MOBILE_OTP to mobileOTP,
                    REQ_ID to (pref.getString(REQ_ID, "") ?: "")
            )
            authenticationService.checkOTP(map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(getCyCheckOTPObserver(firstName, lastName))
            setState {
                copy(
                        asyncCyCheckOTP = Loading()
                )
            }
        }
    }

    /** CyChat Check OTP API Implementation-
     * Function waits for Check OTP API Response
     * */
    private fun getCyCheckOTPObserver(firstName: String, lastName: String): SingleObserver<CheckOTPResponse> {
        return object : SingleObserver<CheckOTPResponse> {

            override fun onSuccess(t: CheckOTPResponse) {
                if (t.status == "ok") {
                    pref.edit().apply {
                        putString(F_NAME, firstName)
                        putString(L_NAME, lastName)
                        apply()
                    }
                    if (t.data.mapped == "Y")
                        startLogin(t.data)
                    else {
                        reqId = t.data.reqID
                        pref.edit {
                            putString(REQ_ID, t.data.reqID)
                            apply()
                        }
                        _viewEvents.post(LoginViewEvents.OnMappingConfirmed)
                    }
                    setState {
                        copy(
                                asyncCyCheckOTP = Success(Unit)
                        )
                    }
                } else {
                    _viewEvents.post(LoginViewEvents.Failure(Throwable(t.message)))
                    setState {
                        copy(
                                asyncCyCheckOTP = Fail(Throwable(t.message))
                        )
                    }
                }
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                _viewEvents.post(LoginViewEvents.Failure(e))
                setState {
                    copy(
                            asyncCyCheckOTP = Fail(e)
                    )
                }
            }
        }
    }

    private fun startLogin(data: MatrixLoginData) {
        pref.edit().apply {
            putString(USER_ID, data.userID)
            putString(NetworkConstants.SECRET_KEY, data.secretKey)
            putString(NetworkConstants.API_SERVER, data.apiServer)
            if (pref.getBoolean(SIGNING_MODE, false))
                putString(NetworkConstants.FULL_NAME, "${pref.getString(F_NAME, "")} ${pref.getString(L_NAME, "")}")
            apply()
        }
        val mobile = pref.getString(MOBILE, "") ?: ""
        handle(
                LoginAction.UpdateHomeServer(
                        data.apiServer,
                        "${data.userID}-$mobile",
                        AES.decrypt(
                                data.password,
                                AES.createSecretKey(data.userID, mobile)
                        )
                )
        )
    }

    /** CyChat Get Country List / Get Settings API Implementation-
     * No Params just to get all countries with codes.
     * */
    fun handleGetSettings() {
        val tempMap = hashMapOf(
                SERVICE_NAME to GET_SETTINGS,
                CLIENT_NAME to CY_VERSE_ANDROID,
                GROUP_VALUE to groupValue,
                OP to GET_SETTINGS_API,
                CLID to (pref.getString(CLID, "") ?: "")
        )
        authenticationService.getSettings(tempMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getSettingsObserver())
        setState {
            copy(asyncGetCountryList = Loading())
        }
    }

    /** CyChat Get Settings API Implementation-
     * Function waits for Get Country List API Response
     * */
    private fun getSettingsObserver(): SingleObserver<GetSettingsParent> {
        return object : SingleObserver<GetSettingsParent> {

            override fun onSuccess(t: GetSettingsParent) {
                if (t.status == "ok") {
                    countryCodeList.postValue(t)
                    pref.edit().apply {
                        putString(NetworkConstants.JITSI, t.jitsi)
                        apply()
                    }
                    setState {
                        copy(
                                asyncGetCountryList = Success(Unit)
                        )
                    }
                } else {
                    _viewEvents.post(LoginViewEvents.Failure(Throwable(t.message)))
                    setState {
                        copy(
                                asyncGetCountryList = Fail(Throwable(t.message))
                        )
                    }
                }
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                _viewEvents.post(LoginViewEvents.Failure(e))
                setState {
                    copy(
                            asyncGetCountryList = Fail(e)
                    )
                }
            }
        }
    }

    private val isUserValidated: MutableLiveData<Boolean> = MutableLiveData(false)

//    /** CyChat Validate Security Code API Implementation-
//     * Function waits for Resend OTP API Response
//     * @param code - this parameter is code for that domain
//     * */
//    fun validateSecurityCode(code: String) {
//        val map = hashMapOf(
//                "secr_code" to code,
//                "email_domain" to loginParams?.email.getEmailDomain()
//        )
//        authenticationService.cyValidateSecurityCode("", map)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(postValidateSecurityCode())
//        setState {
//            copy(
//                    asyncCyValidateSecurityCode = Loading()
//            )
//        }
//    }

    /** CyChat Validate Security Code API Implementation-
     * Function waits for Security Code API Response
     * */
//    private fun postValidateSecurityCode(): SingleObserver<BaseResponse> {
//        return object : SingleObserver<BaseResponse> {
//
//            override fun onSuccess(t: BaseResponse) {
//                if (t.status == "ok") {
//                    setState {
//                        copy(
//                                asyncCyValidateSecurityCode = Success(Unit)
//                        )
//                    }
//                    val map = hashMapOf(
//                            SERVICE_NAME to LOGIN,
//                            CLIENT_NAME to CY_VERSE_ANDROID,
//                            CLID to (pref.getString(CLID, "") ?: ""),
//                            OP to USER_LOGIN_API,
//                            MOBILE to loginParams!!.mobile,
//                            EMAIL_SMALL to loginParams!!.email,
//                            IMEI to loginParams!!.imei_no,
//                            COUNTRY_CODE to loginParams!!.country_code
//                    )
//                    authenticationService.cyLogin(map)
//                            .subscribeOn(Schedulers.io())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(getCyLoginObserver(loginParams!!.email))
//                } else {
//                    _viewEvents.post(LoginViewEvents.Failure(Throwable(t.message)))
//                    setState {
//                        copy(
//                                asyncCyValidateSecurityCode = Fail(Throwable(t.message))
//                        )
//                    }
//                }
//            }
//
//            override fun onSubscribe(d: Disposable) {}
//
//            override fun onError(e: Throwable) {
//                setState {
//                    copy(
//                            asyncCyValidateSecurityCode = Fail(e)
//                    )
//                }
//            }
//        }
//    }

    /** CyChat Resend OTP API Implementation-
     * Function waits for Resend OTP API Response
     * @param type - this parameter is to differentiate api to send email or mobile otp
     * */
    fun resendOTP(type: String) {
        loginParams?.let {
            val map = hashMapOf(
                    SERVICE_NAME to LOGIN,
                    CLIENT_NAME to CY_VERSE_ANDROID,
                    CLID to (pref.getString(CLID, "") ?: ""),
                    OP to RESENT_OTP_API,
                    REQ_ID to (reqId ?: ""),
                    TYPE to type
            )
            authenticationService.cyResendOTP(map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(getResendOTPObserver())
            setState {
                copy(
                        resendOTP = Loading()
                )
            }
        }
    }

    /** CyChat Resend OTP API Implementation-
     * Function waits for Resend OTP API Response
     * */
    private fun getResendOTPObserver(): SingleObserver<BaseResponse> {
        return object : SingleObserver<BaseResponse> {

            override fun onSuccess(t: BaseResponse) {
                if (t.status == "ok") {
                    _viewEvents.post(LoginViewEvents.OnResendOTP)
                    setState {
                        copy(
                                resendOTP = Success(Unit)
                        )
                    }
                } else {
                    _viewEvents.post(LoginViewEvents.Failure(Throwable(t.message)))
                    setState {
                        copy(
                                resendOTP = Fail(Throwable(t.message))
                        )
                    }
                }
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                setState {
                    copy(
                            resendOTP = Fail(e)
                    )
                }
            }
        }
    }

    /** CyChat Get Groups API-
     * Function waits to get all groups API Response
     * */
    fun getGroup() {
        val tempMap = hashMapOf(
                SERVICE_NAME to GENERAL_DATA,
                CLIENT_NAME to CY_VERSE_ANDROID,
                CLID to CY_VERSE_API_CLID,
                GROUP_VALUE to groupValue,
                OP to GET_GROUPS_API
        )
        authenticationService.cyGetGroups(tempMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getGroupObserver())
        setState {
            copy(
                    asyncGetGroup = Loading()
            )
        }
    }

    /** Get Group API Implementation-
     * Function waits for Get Group API Response
     * */
    private fun getGroupObserver(): SingleObserver<GroupParent> {
        return object : SingleObserver<GroupParent> {

            override fun onSuccess(t: GroupParent) {
                Timber.d(t.toString())
                if (t.status == "ok") {
                    _viewEvents.post(LoginViewEvents.OnGetGroupsConfirmed(t))
                    setState {
                        copy(
                                asyncGetGroup = Success(Unit)
                        )
                    }
                } else {
                    _viewEvents.post(LoginViewEvents.Failure(Throwable(t.message)))
                    setState {
                        copy(
                                asyncGetGroup = Fail(Throwable(t.message))
                        )
                    }
                }
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                setState {
                    copy(asyncGetGroup = Fail(e))
                }
            }
        }
    }

    /** CyChat Get User Types API-
     * Function waits to get all groups API Response
     * */
    fun getUserType(orgId: String) {
        val tempMap = hashMapOf(
                SERVICE_NAME to GENERAL_DATA,
                CLIENT_NAME to CY_VERSE_ANDROID,
                OP to GET_USER_TYPE_API,
                CLID to CY_VERSE_API_CLID,
                USER_CAT_ID to orgId
        )
        authenticationService.cyGetUserType(tempMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getUserTypeObserver())
        setState { copy(asyncGetUserType = Loading()) }
    }

    /** Get Get User Types API Implementation-
     * Function waits for Get Group API Response
     * */
    private fun getUserTypeObserver(): SingleObserver<UserTypeParent> {
        return object : SingleObserver<UserTypeParent> {

            override fun onSuccess(t: UserTypeParent) {
                if (t.status == "ok") {
                    _viewEvents.post(LoginViewEvents.OnUserTypeConfirmed(t))
                    setState { copy(asyncGetUserType = Success(Unit)) }
                } else {
                    _viewEvents.post(LoginViewEvents.Failure(Throwable(t.message)))
                    setState { copy(asyncGetUserType = Fail(Throwable(t.message))) }
                }
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                setState {
                    copy(asyncGetUserType = Fail(e))
                }
            }
        }
    }

    /** CyChat Get User Types API-
     * Function waits to get all groups API Response
     * */
    fun getOrganizations() {
        val tempMap = hashMapOf(
                SERVICE_NAME to GENERAL_DATA,
                CLIENT_NAME to CY_VERSE_ANDROID,
                OP to GET_ORGANIZATION_API,
                CLID to CY_VERSE_API_CLID
        )
        authenticationService.cyGetOrganizations(tempMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getOrganizationsObserver())
        setState { copy(asyncGetOrganization = Loading()) }
    }

    /** Get Get User Types API Implementation-
     * Function waits for Get Group API Response
     * */
    private fun getOrganizationsObserver(): SingleObserver<OrganizationParent> {
        return object : SingleObserver<OrganizationParent> {

            override fun onSuccess(t: OrganizationParent) {
                if (t.status == "ok") {
                    _viewEvents.post(LoginViewEvents.OnOrganizationConfirmed(t))
                    setState { copy(asyncGetOrganization = Success(Unit)) }
                } else {
                    _viewEvents.post(LoginViewEvents.Failure(Throwable(t.message)))
                    setState { copy(asyncGetOrganization = Fail(Throwable(t.message))) }
                }
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                setState {
                    copy(asyncGetOrganization = Fail(e))
                }
            }
        }
    }

    /** CyChat Get User Types API-
     * Function waits to get all groups API Response
     * */
    fun getIndividualType() {
        val tempMap = hashMapOf(
                SERVICE_NAME to GENERAL_DATA,
                CLIENT_NAME to CY_VERSE_ANDROID,
                OP to GET_IND_TYPE_API,
                CLID to CY_VERSE_API_CLID
        )
        authenticationService.cyGetUserType(tempMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getIndividualTypeObserver())
//        setState { copy(asyncGetUserType = Loading()) }
    }

    /** Get Get User Types API Implementation-
     * Function waits for Get Group API Response
     * */
    private fun getIndividualTypeObserver(): SingleObserver<UserTypeParent> {
        return object : SingleObserver<UserTypeParent> {

            override fun onSuccess(t: UserTypeParent) {
                if (t.status == "ok") {
                    _viewEvents.post(LoginViewEvents.OnIndividualConfirmed(t))
//                    setState { copy(asyncGetUserType = Success(Unit)) }
                } else {
                    _viewEvents.post(LoginViewEvents.Failure(Throwable(t.message)))
//                    setState { copy(asyncGetUserType = Fail(Throwable(t.message))) }
                }
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
//                setState {
//                    copy(asyncGetUserType = Fail(e))
//                }
            }
        }
    }

    /** CyChat API- END */
    private fun handleUserAcceptCertificate(action: LoginAction.UserAcceptCertificate) {
        // It happens when we get the login flow, or during direct authentication.
        // So alter the homeserver config and retrieve again the login flow
        when (val finalLastAction = lastAction) {
            is LoginAction.UpdateHomeServer -> {
                currentHomeServerConnectionConfig
                        ?.let { it.copy(allowedFingerprints = it.allowedFingerprints + action.fingerprint) }
                        ?.let { getLoginFlow(it) }
            }
            is LoginAction.LoginOrRegister  ->
                handleDirectLogin(
                        finalLastAction,
                        HomeServerConnectionConfig.Builder()
                                // Will be replaced by the task
                                .withHomeServerUri("https://dummy.org")
                                .withAllowedFingerPrints(listOf(action.fingerprint))
                                .build()
                )
            else                            -> Unit
        }
    }

    private fun rememberHomeServer(homeServerUrl: String) {
        homeServerHistoryService.addHomeServerToHistory(homeServerUrl)
        getKnownCustomHomeServersUrls()
    }

    private fun handleClearHomeServerHistory() {
        homeServerHistoryService.clearHistory()
        getKnownCustomHomeServersUrls()
    }

    private fun handleLoginWithToken(action: LoginAction.LoginWithToken) {
        val safeLoginWizard = loginWizard

        setState {
            copy(
                    asyncLoginAction = Loading()
            )
        }

        currentJob = viewModelScope.launch {
            try {
                safeLoginWizard.loginWithToken(action.loginToken)
            } catch (failure: Throwable) {
                _viewEvents.post(LoginViewEvents.Failure(failure))
                setState {
                    copy(
                            asyncLoginAction = Fail(failure)
                    )
                }
                null
            }
                    ?.let { onSessionCreated(it) }
        }
    }

    private fun handleSetupSsoForSessionRecovery(action: LoginAction.SetupSsoForSessionRecovery) {
        setState {
            copy(
                    signMode = SignMode.SignIn,
                    loginMode = LoginMode.Sso(action.ssoIdentityProviders),
                    homeServerUrl = action.homeServerUrl,
                    deviceId = action.deviceId
            )
        }
    }

    private fun handleRegisterAction(action: LoginAction.RegisterAction) {
        when (action) {
            is LoginAction.CaptchaDone                  -> handleCaptchaDone(action)
            is LoginAction.AcceptTerms                  -> handleAcceptTerms()
            is LoginAction.RegisterDummy                -> handleRegisterDummy()
            is LoginAction.AddThreePid                  -> handleAddThreePid(action)
            is LoginAction.SendAgainThreePid            -> handleSendAgainThreePid()
            is LoginAction.ValidateThreePid             -> handleValidateThreePid(action)
            is LoginAction.CheckIfEmailHasBeenValidated -> handleCheckIfEmailHasBeenValidated(action)
            is LoginAction.StopEmailValidationCheck     -> handleStopEmailValidationCheck()
        }
    }

    private fun handleCheckIfEmailHasBeenValidated(action: LoginAction.CheckIfEmailHasBeenValidated) {
        // We do not want the common progress bar to be displayed, so we do not change asyncRegistration value in the state
        currentJob = executeRegistrationStep(withLoading = false) {
            it.checkIfEmailHasBeenValidated(action.delayMillis)
        }
    }

    private fun handleStopEmailValidationCheck() {
        currentJob = null
    }

    private fun handleValidateThreePid(action: LoginAction.ValidateThreePid) {
        currentJob = executeRegistrationStep {
            it.handleValidateThreePid(action.code)
        }
    }

    private fun executeRegistrationStep(withLoading: Boolean = true,
                                        block: suspend (RegistrationWizard) -> RegistrationResult): Job {
        if (withLoading) {
            setState { copy(asyncRegistration = Loading()) }
        }
        return viewModelScope.launch {
            try {
                block(registrationWizard)
                /*
                   // Simulate registration disabled
                   throw Failure.ServerError(MatrixError(
                           code = MatrixError.FORBIDDEN,
                           message = "Registration is disabled"
                   ), 403))
                */
            } catch (failure: Throwable) {
                if (failure !is CancellationException) {
                    _viewEvents.post(LoginViewEvents.Failure(failure))
                }
                null
            }
                    ?.let { data ->
                        when (data) {
                            is RegistrationResult.Success      -> onSessionCreated(data.session)
                            is RegistrationResult.FlowResponse -> onFlowResponse(data.flowResult)
                        }
                    }

            setState {
                copy(
                        asyncRegistration = Uninitialized
                )
            }
        }
    }

    private fun handleAddThreePid(action: LoginAction.AddThreePid) {
        setState { copy(asyncRegistration = Loading()) }
        currentJob = viewModelScope.launch {
            try {
                registrationWizard.addThreePid(action.threePid)
            } catch (failure: Throwable) {
                _viewEvents.post(LoginViewEvents.Failure(failure))
            }
            setState {
                copy(
                        asyncRegistration = Uninitialized
                )
            }
        }
    }

    private fun handleSendAgainThreePid() {
        setState { copy(asyncRegistration = Loading()) }
        currentJob = viewModelScope.launch {
            try {
                registrationWizard.sendAgainThreePid()
            } catch (failure: Throwable) {
                _viewEvents.post(LoginViewEvents.Failure(failure))
            }
            setState {
                copy(
                        asyncRegistration = Uninitialized
                )
            }
        }
    }

    private fun handleAcceptTerms() {
        currentJob = executeRegistrationStep {
            it.acceptTerms()
        }
    }

    private fun handleRegisterDummy() {
        currentJob = executeRegistrationStep {
            it.dummy()
        }
    }

//    private fun handleRegisterWith(action: LoginAction.LoginOrRegister) {
//        reAuthHelper.data = action.password
//        currentJob = executeRegistrationStep {
//            it.createAccount(
//                    action.username,
//                    action.password,
//                    action.initialDeviceName
//            )
//        }
//    }

    private fun handleCaptchaDone(action: LoginAction.CaptchaDone) {
        currentJob = executeRegistrationStep {
            it.performReCaptcha(action.captchaResponse)
        }
    }

    private fun handleResetAction(action: LoginAction.ResetAction) {
        // Cancel any request
        currentJob = null

        when (action) {
            LoginAction.ResetHomeServerType -> {
                setState {
                    copy(
                            serverType = ServerType.Unknown
                    )
                }
            }
            LoginAction.ResetHomeServerUrl  -> {
                viewModelScope.launch {
                    authenticationService.reset()
                    setState {
                        copy(
                                asyncHomeServerLoginFlowRequest = Uninitialized,
                                homeServerUrl = null,
                                loginMode = LoginMode.Unknown,
                                serverType = ServerType.Unknown,
                                loginModeSupportedTypes = emptyList()
                        )
                    }
                }
            }
            LoginAction.ResetSignMode       -> {
                setState {
                    copy(
                            asyncHomeServerLoginFlowRequest = Uninitialized,
                            signMode = SignMode.Unknown,
                            loginMode = LoginMode.Unknown,
                            loginModeSupportedTypes = emptyList()
                    )
                }
            }
            LoginAction.ResetLogin          -> {
                viewModelScope.launch {
                    authenticationService.cancelPendingLoginOrRegistration()
                    setState {
                        copy(
                                asyncLoginAction = Uninitialized,
                                asyncRegistration = Uninitialized
                        )
                    }
                }
            }
            LoginAction.ResetResetPassword  -> {
                setState {
                    copy(
                            asyncResetPassword = Uninitialized,
                            asyncResetMailConfirmed = Uninitialized,
                            resetPasswordEmail = null
                    )
                }
            }
        }
    }

//    private fun handleUpdateSignMode(action: LoginAction.UpdateSignMode) {
//        setState {
//            copy(
//                    signMode = action.signMode
//            )
//        }
//
//        when (action.signMode) {
//            SignMode.SignUp             -> startRegistrationFlow()
//            SignMode.SignIn             -> startAuthenticationFlow()
//            SignMode.SignInWithMatrixId -> _viewEvents.post(LoginViewEvents.OnSignModeSelected(SignMode.SignInWithMatrixId))
//            SignMode.Unknown            -> Unit
//        }
//    }

//    private fun handleUpdateServerType(action: LoginAction.UpdateServerType) {
//        setState {
//            copy(
//                    serverType = action.serverType
//            )
//        }
//
//        when (action.serverType) {
//            ServerType.Unknown   -> Unit /* Should not happen */
//            ServerType.MatrixOrg ->
//                // Request login flow here
//                handle(LoginAction.UpdateHomeServer(matrixOrgUrl))
//            ServerType.EMS,
//            ServerType.Other     -> _viewEvents.post(LoginViewEvents.OnServerSelectionDone(action.serverType))
//        }.exhaustive
//    }

    private fun handleInitWith(action: LoginAction.InitWith) {
        loginConfig = action.loginConfig

        // If there is a pending email validation continue on this step
        try {
            if (registrationWizard.isRegistrationStarted) {
                currentThreePid?.let {
                    handle(LoginAction.PostViewEvent(LoginViewEvents.OnSendEmailSuccess(it)))
                }
            }
        } catch (e: Throwable) {
            // NOOP. API is designed to use wizards in a login/registration flow,
            // but we need to check the state anyway.
        }
    }

    private fun handleResetPassword(action: LoginAction.ResetPassword) {
        val safeLoginWizard = loginWizard

        setState {
            copy(
                    asyncResetPassword = Loading(),
                    asyncResetMailConfirmed = Uninitialized
            )
        }

        currentJob = viewModelScope.launch {
            try {
                safeLoginWizard.resetPassword(action.email, action.newPassword)
            } catch (failure: Throwable) {
                setState {
                    copy(
                            asyncResetPassword = Fail(failure)
                    )
                }
                return@launch
            }

            setState {
                copy(
                        asyncResetPassword = Success(Unit),
                        resetPasswordEmail = action.email
                )
            }

            _viewEvents.post(LoginViewEvents.OnResetPasswordSendThreePidDone)
        }
    }

    private fun handleResetPasswordMailConfirmed() {
        val safeLoginWizard = loginWizard

        setState {
            copy(
                    asyncResetPassword = Uninitialized,
                    asyncResetMailConfirmed = Loading()
            )
        }

        currentJob = viewModelScope.launch {
            try {
                safeLoginWizard.resetPasswordMailConfirmed()
            } catch (failure: Throwable) {
                setState {
                    copy(
                            asyncResetMailConfirmed = Fail(failure)
                    )
                }
                return@launch
            }
            setState {
                copy(
                        asyncResetMailConfirmed = Success(Unit),
                        resetPasswordEmail = null
                )
            }

            _viewEvents.post(LoginViewEvents.OnResetPasswordMailConfirmationSuccess)
        }
    }

//    private fun handleLoginOrRegister(action: LoginAction.LoginOrRegister) = withState { state ->
//        when (state.signMode) {
//            SignMode.Unknown            -> error("Developer error, invalid sign mode")
//            SignMode.SignIn             -> handleLogin(action)
//            SignMode.SignUp             -> handleRegisterWith(action)
//            SignMode.SignInWithMatrixId -> handleDirectLogin(action, null)
//        }.exhaustive
//    }

    private fun handleDirectLogin(action: LoginAction.LoginOrRegister, homeServerConnectionConfig: HomeServerConnectionConfig?) {
        setState {
            copy(
                    asyncLoginAction = Loading()
            )
        }

        currentJob = viewModelScope.launch {
            val data = try {
                authenticationService.getWellKnownData(action.username, homeServerConnectionConfig)
            } catch (failure: Throwable) {
                onDirectLoginError(failure)
                return@launch
            }
            when (data) {
                is WellknownResult.Prompt          ->
                    onWellknownSuccess(action, data, homeServerConnectionConfig)
                is WellknownResult.FailPrompt      ->
                    // Relax on IS discovery if home server is valid
                    if (data.homeServerUrl != null && data.wellKnown != null) {
                        onWellknownSuccess(action, WellknownResult.Prompt(data.homeServerUrl!!, null, data.wellKnown!!), homeServerConnectionConfig)
                    } else {
                        onWellKnownError()
                    }
                is WellknownResult.InvalidMatrixId -> {
                    setState {
                        copy(
                                asyncLoginAction = Uninitialized
                        )
                    }
                    _viewEvents.post(LoginViewEvents.Failure(Exception(stringProvider.getString(R.string.login_signin_matrix_id_error_invalid_matrix_id))))
                }
                else                               -> {
                    onWellKnownError()
                }
            }.exhaustive
        }
    }

    private fun onWellKnownError() {
        setState {
            copy(
                    asyncLoginAction = Uninitialized
            )
        }
        _viewEvents.post(LoginViewEvents.Failure(Exception(stringProvider.getString(R.string.autodiscover_well_known_error))))
    }

    private suspend fun onWellknownSuccess(action: LoginAction.LoginOrRegister,
                                           wellKnownPrompt: WellknownResult.Prompt,
                                           homeServerConnectionConfig: HomeServerConnectionConfig?) {
        val alteredHomeServerConnectionConfig = homeServerConnectionConfig
                ?.copy(
                        homeServerUri = Uri.parse(wellKnownPrompt.homeServerUrl),
                        identityServerUri = wellKnownPrompt.identityServerUrl?.let { Uri.parse(it) }
                )
                ?: HomeServerConnectionConfig(
                        homeServerUri = Uri.parse(wellKnownPrompt.homeServerUrl),
                        identityServerUri = wellKnownPrompt.identityServerUrl?.let { Uri.parse(it) }
                )

        val data = try {
            authenticationService.directAuthentication(
                    alteredHomeServerConnectionConfig,
                    action.username,
                    action.password,
                    action.initialDeviceName)
        } catch (failure: Throwable) {
            onDirectLoginError(failure)
            return
        }
        onSessionCreated(data)
    }

    private fun onDirectLoginError(failure: Throwable) {
        if (failure is Failure.UnrecognizedCertificateFailure) {
            // Display this error in a dialog
            _viewEvents.post(LoginViewEvents.Failure(failure))
            setState {
                copy(
                        asyncLoginAction = Uninitialized
                )
            }
        } else {
            setState {
                copy(
                        asyncLoginAction = Fail(failure)
                )
            }
        }
    }

    private fun handleLogin(action: LoginAction.LoginOrRegister) {
        val safeLoginWizard = loginWizard

        setState {
            copy(
                    asyncLoginAction = Loading()
            )
        }

        currentJob = viewModelScope.launch {
            try {
                safeLoginWizard.login(
                        action.username,
                        action.password,
                        action.initialDeviceName
                )
            } catch (failure: Throwable) {
                setState {
                    copy(
                            asyncLoginAction = Fail(failure)
                    )
                }
                null
            }?.let {
                reAuthHelper.data = action.password
                onSessionCreated(it)
            }
        }
    }

//    private fun startRegistrationFlow() {
//        currentJob = executeRegistrationStep {
//            it.getRegistrationFlow()
//        }
//    }

    private fun startAuthenticationFlow() {
        // Ensure Wizard is ready
        _viewEvents.post(LoginViewEvents.OnSignModeSelected(SignMode.SignIn))
    }

    private fun onFlowResponse(flowResult: FlowResult) {
        // If dummy stage is mandatory, and password is already sent, do the dummy stage now
        if (isRegistrationStarted
                && flowResult.missingStages.any { it is Stage.Dummy && it.mandatory }) {
            handleRegisterDummy()
        } else {
            // Notify the user
            _viewEvents.post(LoginViewEvents.RegistrationFlowResult(flowResult, isRegistrationStarted))
        }
    }

    private suspend fun onSessionCreated(session: Session) {
        activeSessionHolder.setActiveSession(session)

        authenticationService.reset()
        session.configureAndStart(applicationContext)
        setState {
            copy(
                    asyncLoginAction = Success(Unit)
            )
        }
    }

    private fun handleWebLoginSuccess(action: LoginAction.WebLoginSuccess) = withState { state ->
        val homeServerConnectionConfigFinal = homeServerConnectionConfigFactory.create(state.homeServerUrl)

        if (homeServerConnectionConfigFinal == null) {
            // Should not happen
            Timber.w("homeServerConnectionConfig is null")
        } else {
            currentJob = viewModelScope.launch {
                try {
                    authenticationService.createSessionFromSso(homeServerConnectionConfigFinal, action.credentials)
                } catch (failure: Throwable) {
                    setState {
                        copy(asyncLoginAction = Fail(failure))
                    }
                    null
                }
                        ?.let { onSessionCreated(it) }
            }
        }
    }

    private fun handleUpdateHomeserver(action: LoginAction.UpdateHomeServer) {
        val homeServerConnectionConfig = homeServerConnectionConfigFactory.create(action.homeServerUrl)
        if (homeServerConnectionConfig == null)
            _viewEvents.post(LoginViewEvents.Failure(Throwable("Unable to create a HomeServerConnectionConfig")))
        else
            getLoginFlow(homeServerConnectionConfig, action.id, action.password)
    }

    private fun getLoginFlow(homeServerConnectionConfig: HomeServerConnectionConfig, id: String? = null, password: String? = null) {
        currentHomeServerConnectionConfig = homeServerConnectionConfig

        currentJob = viewModelScope.launch {
            authenticationService.cancelPendingLoginOrRegistration()

            setState {
                copy(
                        asyncHomeServerLoginFlowRequest = Loading(),
                        // If user has entered https://matrix.org, ensure that server type is ServerType.MatrixOrg
                        // It is also useful to set the value again in the case of a certificate error on matrix.org
                        serverType = if (homeServerConnectionConfig.homeServerUri.toString() == matrixOrgUrl) ServerType.MatrixOrg else serverType
                )
            }

            val data = try {
                authenticationService.getLoginFlow(homeServerConnectionConfig)
            } catch (failure: Throwable) {
                _viewEvents.post(LoginViewEvents.Failure(failure))
                setState {
                    copy(
                            asyncHomeServerLoginFlowRequest = Uninitialized,
                            // If we were trying to retrieve matrix.org login flow, also reset the serverType
                            serverType = if (serverType == ServerType.MatrixOrg) ServerType.Unknown else serverType
                    )
                }
                null
            }

            if (data is LoginFlowResult.Success) {
                // Valid Homeserver, add it to the history.
                // Note: we add what the user has input, data.homeServerUrl can be different
                rememberHomeServer(homeServerConnectionConfig.homeServerUri.toString())

                val loginMode = when {
                    // SSO login is taken first
                    data.supportedLoginTypes.contains(LoginFlowTypes.SSO)
                            && data.supportedLoginTypes.contains(LoginFlowTypes.PASSWORD) -> LoginMode.SsoAndPassword(data.ssoIdentityProviders)
                    data.supportedLoginTypes.contains(LoginFlowTypes.SSO)                 -> LoginMode.Sso(data.ssoIdentityProviders)
                    data.supportedLoginTypes.contains(LoginFlowTypes.PASSWORD)            -> LoginMode.Password
                    else                                                                  -> LoginMode.Unsupported
                }

                setState {
                    copy(
                            asyncHomeServerLoginFlowRequest = Uninitialized,
                            homeServerUrl = data.homeServerUrl,
                            loginMode = loginMode,
                            loginModeSupportedTypes = data.supportedLoginTypes.toList()
                    )
                }
                if (id != null && password != null)
                    handle(LoginAction.LoginOrRegister(id, password, ""))

                if ((loginMode == LoginMode.Password && !data.isLoginAndRegistrationSupported)
                        || data.isOutdatedHomeserver) {
                    // Notify the UI
                    _viewEvents.post(LoginViewEvents.OutdatedHomeserver)
                }
            }
        }
    }

//    fun getInitialHomeServerUrl() = loginConfig?.homeServerUrl

    fun getSsoUrl(redirectUrl: String, deviceId: String?, providerId: String?): String? {
        return authenticationService.getSsoUrl(redirectUrl, deviceId, providerId)
    }

    fun getFallbackUrl(forSignIn: Boolean, deviceId: String?): String? {
        return authenticationService.getFallbackUrl(forSignIn, deviceId)
    }
}
