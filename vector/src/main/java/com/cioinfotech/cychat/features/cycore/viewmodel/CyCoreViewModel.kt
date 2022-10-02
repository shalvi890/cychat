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

package com.cioinfotech.cychat.features.cycore.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cioinfotech.cychat.core.di.DefaultSharedPreferences
import com.cioinfotech.cychat.features.cycore.data.AddUserTypesResponse
import com.cioinfotech.cychat.features.cycore.data.DomainDetails
import com.cioinfotech.cychat.features.cycore.data.ErrorModel
import com.cioinfotech.cychat.features.cycore.data.UserProfileData
import com.cioinfotech.cychat.features.cycore.data.VerifyAddUserTypeResponse
import com.cioinfotech.cychat.features.cycore.service.CyCoreService
import com.cioinfotech.cychat.features.home.notice.model.EventModel
import com.cioinfotech.cychat.features.home.notice.model.Notice
import com.cioinfotech.cychat.features.home.notice.model.NoticeBoardParent
import com.cioinfotech.cychat.features.home.notice.model.NoticeListParent
import com.cioinfotech.cychat.features.home.notice.model.UpdateNoticeModel
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.RequestBody
import org.matrix.android.sdk.internal.cy_auth.data.BaseResponse
import org.matrix.android.sdk.internal.cy_auth.data.DefaultURLParent
import org.matrix.android.sdk.internal.cy_auth.data.FederatedDomainList
import org.matrix.android.sdk.internal.network.NetworkConstants
import org.matrix.android.sdk.internal.network.NetworkConstants.BASE_URL
import org.matrix.android.sdk.internal.network.NetworkConstants.CLID
import org.matrix.android.sdk.internal.network.NetworkConstants.CLIENT_NAME
import org.matrix.android.sdk.internal.network.NetworkConstants.CODE
import org.matrix.android.sdk.internal.network.NetworkConstants.CY_VERSE_ANDROID
import org.matrix.android.sdk.internal.network.NetworkConstants.CY_VERSE_API_CLID
import org.matrix.android.sdk.internal.network.NetworkConstants.DELETE_REQUEST
import org.matrix.android.sdk.internal.network.NetworkConstants.DEVICE_ID
import org.matrix.android.sdk.internal.network.NetworkConstants.EXCLUDE_USER_TYPE
import org.matrix.android.sdk.internal.network.NetworkConstants.FEDERATION
import org.matrix.android.sdk.internal.network.NetworkConstants.FETCH_COUNT
import org.matrix.android.sdk.internal.network.NetworkConstants.FILTERS
import org.matrix.android.sdk.internal.network.NetworkConstants.GET_ADD_USER_TYPES
import org.matrix.android.sdk.internal.network.NetworkConstants.GET_NOTICES
import org.matrix.android.sdk.internal.network.NetworkConstants.GET_NOTICE_BOARDS
import org.matrix.android.sdk.internal.network.NetworkConstants.GET_TIMEZONES
import org.matrix.android.sdk.internal.network.NetworkConstants.GET_USER_PROFILE
import org.matrix.android.sdk.internal.network.NetworkConstants.LAST_POST
import org.matrix.android.sdk.internal.network.NetworkConstants.LIST_FEDERATED_API
import org.matrix.android.sdk.internal.network.NetworkConstants.MATRIX_URL
import org.matrix.android.sdk.internal.network.NetworkConstants.OP
import org.matrix.android.sdk.internal.network.NetworkConstants.OTP
import org.matrix.android.sdk.internal.network.NetworkConstants.POST_GET_ALL
import org.matrix.android.sdk.internal.network.NetworkConstants.POST_ID
import org.matrix.android.sdk.internal.network.NetworkConstants.REQ_ID
import org.matrix.android.sdk.internal.network.NetworkConstants.RESEND_VERIFICATION_CODE
import org.matrix.android.sdk.internal.network.NetworkConstants.SERVICE_NAME
import org.matrix.android.sdk.internal.network.NetworkConstants.SETUP_ID
import org.matrix.android.sdk.internal.network.NetworkConstants.SET_VISIBILITY
import org.matrix.android.sdk.internal.network.NetworkConstants.UPDATE_POST_DETAILS
import org.matrix.android.sdk.internal.network.NetworkConstants.USER_ID
import org.matrix.android.sdk.internal.network.NetworkConstants.USER_ROLE_ID
import org.matrix.android.sdk.internal.network.NetworkConstants.USER_TYPE
import org.matrix.android.sdk.internal.network.NetworkConstants.VERIFY_ADD_USER_TYPE
import org.matrix.android.sdk.internal.network.NetworkConstants.VERIFY_OTP
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
class CyCoreViewModel @Inject constructor(
        private val cyCoreService: CyCoreService,
        applicationContext: Context
) : ViewModel() {

    private var pref = DefaultSharedPreferences.getInstance(applicationContext)
    private var url = pref.getString(BASE_URL, "") ?: ""

    private val clid = pref.getString(CLID, "") ?: ""

    private var userId = pref.getString(USER_ID, null) ?: ""
    private var reqId = pref.getString(REQ_ID, null)
//    private var email = pref.getString(EMAIL, null)

    private var domainMutData: MutableLiveData<Boolean> = MutableLiveData()
    val domainData: LiveData<Boolean> get() = domainMutData

    private var federatedDomainListData: MutableLiveData<FederatedDomainList> = MutableLiveData()
    val federatedDomainList: LiveData<FederatedDomainList> get() = federatedDomainListData

    fun setDomainLiveData() {
        domainMutData.postValue(false)
    }

    fun handleCyGetDetails() {
        cyCoreService.cyGetDomainDetails(
                hashMapOf(
                        CLIENT_NAME to CY_VERSE_ANDROID,
                        OP to NetworkConstants.GET_COMPANY_DETAILS_API,
                        SERVICE_NAME to NetworkConstants.MISC_FUNC,
                        SETUP_ID to (pref.getString(SETUP_ID, "") ?: ""),
                        CLID to clid
                ), url).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getDomainDetails())
    }

    private fun getDomainDetails(): SingleObserver<DomainDetails> {
        return object : SingleObserver<DomainDetails> {

            override fun onSuccess(t: DomainDetails) {
                if (t.status == "ok") {
                    val data = t.data
                    pref.edit().apply {
                        if (!data.companyName.isNullOrEmpty())
                            this.putString(NetworkConstants.DOMAIN_NAME, data.companyName)
                        if (!data.logoURL.isNullOrEmpty())
                            this.putString(NetworkConstants.DOMAIN_IMAGE, data.logoURL)
                        if (!data.appName.isNullOrEmpty())
                            this.putString(NetworkConstants.DOMAIN_IMAGE, data.appName)
                        apply()
                        domainMutData.postValue(true)
                    }
                }
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                Timber.log(1, e)
            }
        }
    }

    fun handleUpdateRecoveryToken(text: String) {
        cyCoreService.cyUpdateRecoveryKey(hashMapOf(
                CLIENT_NAME to CY_VERSE_ANDROID,
                OP to NetworkConstants.SET_SECRET_KEY_API,
                SERVICE_NAME to NetworkConstants.MISC_FUNC,
                CLID to clid,
                REQ_ID to (reqId ?: ""),
                NetworkConstants.SECRET_CODE_SMALL to text), url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(updateRecoveryToken())
    }

    private fun updateRecoveryToken(): SingleObserver<BaseResponse> {
        return object : SingleObserver<BaseResponse> {

            override fun onSuccess(t: BaseResponse) {}

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {}
        }
    }

    fun handleDeleteOldSessions(deviceId: String) {
        Log.e("@@", "@@ handleDeleteOldSession ")
        cyCoreService.cyDeleteOldSessions(hashMapOf(
                DEVICE_ID to deviceId,
                CLIENT_NAME to CY_VERSE_ANDROID,
                MATRIX_URL to "https://awscyberia2.cioinfotech.com",
                OP to NetworkConstants.DELETE_SESSION_API,
                SERVICE_NAME to NetworkConstants.MISC_FUNC,
                USER_ID to userId,
                CLID to clid,
        ), url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deleteOldSessions())
    }

    fun handleDisplayName(displayName: String) {
        Log.e("@@", "@@ handleDisplayName ")
        cyCoreService.cyDisplayname(hashMapOf(
                CLIENT_NAME to CY_VERSE_ANDROID,
                OP to NetworkConstants.OP_SET_NAME,
                SERVICE_NAME to NetworkConstants.MISC_FUNC,
                "displayName" to displayName,
                USER_ID to userId,
                CLID to clid,
        ), url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(displaySetName())
    }

    private fun deleteOldSessions(): SingleObserver<BaseResponse> {
        return object : SingleObserver<BaseResponse> {

            override fun onSuccess(t: BaseResponse) {
                if (t.status != "error")
                    Log.e("delete session @@", "Success")
                pref.edit().apply {
                    putBoolean(NetworkConstants.SESSION_UPDATED, true)
                    apply()
                }
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                Log.e("delete session @@", "onError")
            }
        }
    }

    private fun displaySetName(): SingleObserver<BaseResponse> {
        return object : SingleObserver<BaseResponse> {

            override fun onSuccess(t: BaseResponse) {
                if (t.status != "error")
                    Log.e("displaySetName session @@", "Success")
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                Log.e("displaySetName session @@", "onError")
            }
        }
    }

    fun getFederatedDomains(excludeDomain: String = "-1") {
        cyCoreService.cyGetFederatedDomains(hashMapOf(
                OP to LIST_FEDERATED_API,
                SERVICE_NAME to FEDERATION,
                CLIENT_NAME to CY_VERSE_ANDROID,
                USER_TYPE to (pref.getString(USER_TYPE, "") ?: ""),
                CLID to (pref.getString(CLID, "") ?: ""),
                EXCLUDE_USER_TYPE to excludeDomain
        ), url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(handleGetFederatedDomains())
    }

    private fun handleGetFederatedDomains(): SingleObserver<FederatedDomainList> {
        return object : SingleObserver<FederatedDomainList> {

            override fun onSuccess(t: FederatedDomainList) {
                federatedDomainListData.postValue(t)
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {}
        }
    }

    fun handleGetDefaultURLs() {
        cyCoreService.cyGetDefaultURLs(hashMapOf(), url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getDefaultURLs())
    }

    private fun getDefaultURLs(): SingleObserver<DefaultURLParent> {
        return object : SingleObserver<DefaultURLParent> {

            override fun onSuccess(t: DefaultURLParent) {
                if (t.status != "error")
                    pref.edit().apply {
                        putString(NetworkConstants.JITSI, t.data.jitsi)
                        putString(NetworkConstants.SYGNAL, t.data.sygnal)
                        apply()
                    }
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {}
        }
    }

    val addUserTypesLiveData = MutableLiveData<AddUserTypesResponse>()
    fun handleAddUserTypes() {
        cyCoreService.getAddUserTypes(
                hashMapOf(
                        CLIENT_NAME to CY_VERSE_ANDROID,
                        OP to GET_ADD_USER_TYPES,
                        SERVICE_NAME to NetworkConstants.USERTYPE_DATA,
                        CLID to clid,
                        USER_ID to userId
                ), url).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getAddUserTypes())
    }

    private fun getAddUserTypes(): SingleObserver<AddUserTypesResponse> {
        return object : SingleObserver<AddUserTypesResponse> {

            override fun onSuccess(t: AddUserTypesResponse) {
                if (t.status == "ok")
                    addUserTypesLiveData.postValue(t)
                else
                    errorData.postValue(ErrorModel(GET_ADD_USER_TYPES, t.message))
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                Timber.log(1, e)
            }
        }
    }

    fun handleVerifyAddUserType(uTypeId: String, code: String) {
        cyCoreService.verifyAddUserType(
                hashMapOf(
                        CLIENT_NAME to CY_VERSE_ANDROID,
                        OP to VERIFY_ADD_USER_TYPE,
                        SERVICE_NAME to NetworkConstants.USERTYPE_DATA,
                        CLID to clid,
                        USER_ID to userId,
                        CODE to code,
                        USER_TYPE to uTypeId
                ), url).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(verifyAddUserType())
    }

    val verifyAddUserTypeResponse = MutableLiveData<VerifyAddUserTypeResponse>()
    private fun verifyAddUserType(): SingleObserver<VerifyAddUserTypeResponse> {
        return object : SingleObserver<VerifyAddUserTypeResponse> {

            override fun onSuccess(t: VerifyAddUserTypeResponse) {
                if (t.status == "ok") {
                    verifyAddUserTypeResponse.postValue(t)
                } else
                    errorData.postValue(ErrorModel(VERIFY_ADD_USER_TYPE, t.message))
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                Timber.log(1, e)
            }
        }
    }

    fun handleVerifyOTP(reqId: String, uTypeId: String, otp: String, userRoleID: String) {
        cyCoreService.verifyOTP(
                hashMapOf(
                        CLIENT_NAME to CY_VERSE_ANDROID,
                        OP to VERIFY_OTP,
                        SERVICE_NAME to NetworkConstants.USERTYPE_DATA,
                        CLID to clid,
                        USER_TYPE to uTypeId,
                        REQ_ID to reqId,
                        OTP to otp,
                        USER_ROLE_ID to userRoleID

                ), url).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(verifyOTP())
    }

    val otpVerifiedData = MutableLiveData<BaseResponse>()
    private fun verifyOTP(): SingleObserver<BaseResponse> {
        return object : SingleObserver<BaseResponse> {

            override fun onSuccess(t: BaseResponse) {
                if (t.status == "ok")
                    otpVerifiedData.postValue(t)
                else
                    errorData.postValue(ErrorModel(VERIFY_OTP, t.message))
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                Timber.log(1, e)
            }
        }
    }

    fun handleGetProfileDetails() {
        cyCoreService.getProfileDetails(
                hashMapOf(
                        CLIENT_NAME to CY_VERSE_ANDROID,
                        OP to GET_USER_PROFILE,
                        SERVICE_NAME to NetworkConstants.USERTYPE_DATA,
                        CLID to clid,
                        USER_ID to userId
                ), url).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getProfileDetails())
    }

    val userProfileData = MutableLiveData<UserProfileData>()
    private fun getProfileDetails(): SingleObserver<UserProfileData> {
        return object : SingleObserver<UserProfileData> {

            override fun onSuccess(t: UserProfileData) {
                if (t.status == "ok")
                    userProfileData.postValue(t)
                else
                    errorData.postValue(ErrorModel(GET_USER_PROFILE, t.message))
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                Timber.log(1, e)
            }
        }
    }

    fun handleResendVerificationCode(reqId: String) {
        cyCoreService.resendVerificationCode(
                hashMapOf(
                        CLIENT_NAME to CY_VERSE_ANDROID,
                        OP to RESEND_VERIFICATION_CODE,
                        SERVICE_NAME to NetworkConstants.USERTYPE_DATA,
                        CLID to clid,
                        REQ_ID to reqId,
                        CODE to (pref.getString(CODE, "") ?: "")
                ), url).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resendVerificationCode())
    }

    val resendVerificationCode = MutableLiveData<BaseResponse>()
    private fun resendVerificationCode(): SingleObserver<BaseResponse> {
        return object : SingleObserver<BaseResponse> {

            override fun onSuccess(t: BaseResponse) {
                if (t.status == "ok")
                    resendVerificationCode.postValue(t)
                else
                    errorData.postValue(ErrorModel(RESEND_VERIFICATION_CODE, t.message))
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                Timber.log(1, e)
            }
        }
    }

    val errorData = MutableLiveData<ErrorModel?>()

    fun setVisibility() {
        cyCoreService.setVisibility(
                hashMapOf(
                        CLIENT_NAME to CY_VERSE_ANDROID,
                        OP to SET_VISIBILITY,
                        SERVICE_NAME to NetworkConstants.MISC_FUNC,
                        USER_ID to userId
                ), url).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getVisibility())
    }

    val visibilityLiveData = MutableLiveData<BaseResponse?>()
    private fun getVisibility(): SingleObserver<BaseResponse> {
        return object : SingleObserver<BaseResponse> {

            override fun onSuccess(t: BaseResponse) {
                if (t.status == "ok")
                    visibilityLiveData.postValue(t)
                else
                    errorData.postValue(ErrorModel(SET_VISIBILITY, t.message))
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                Timber.log(1, e)
            }
        }
    }

    fun deleteRequest(reqId: String) {
        cyCoreService.deleteRequest(
                hashMapOf(
                        CLIENT_NAME to CY_VERSE_ANDROID,
                        OP to DELETE_REQUEST,
                        SERVICE_NAME to NetworkConstants.MISC_FUNC,
                        CLID to clid,
                        REQ_ID to reqId
                ), url).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(handleDeleteRequest())
    }

    val deleteRequestLiveData = MutableLiveData<BaseResponse?>()
    private fun handleDeleteRequest(): SingleObserver<BaseResponse> {
        return object : SingleObserver<BaseResponse> {

            override fun onSuccess(t: BaseResponse) {
                if (t.status == "ok")
                    deleteRequestLiveData.postValue(t)
                else
                    errorData.postValue(ErrorModel(DELETE_REQUEST, t.message))
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                Timber.log(1, e)
            }
        }
    }

    fun getNoticeBoards(postId: Int = 0) {
        cyCoreService.getNoticeBoards(
                hashMapOf(
                        CLIENT_NAME to CY_VERSE_ANDROID,
                        OP to GET_NOTICE_BOARDS,
                        SERVICE_NAME to NetworkConstants.EDIT_POSTS,
                        USER_ID to userId,
                        POST_ID to postId.toString(),
                        CLID to CY_VERSE_API_CLID
                )).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(handleGetNoticeBoards())
    }

    val noticeBoardsLiveData = MutableLiveData<NoticeBoardParent>()
    private fun handleGetNoticeBoards(): SingleObserver<NoticeBoardParent> {
        return object : SingleObserver<NoticeBoardParent> {

            override fun onSuccess(t: NoticeBoardParent) {
                if (t.status == "ok")
                    noticeBoardsLiveData.postValue(t)
                else
                    errorData.postValue(ErrorModel(GET_NOTICE_BOARDS, t.message))
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                Timber.log(1, e)
            }
        }
    }

    var selectedNotice = MutableLiveData<Notice?>()

    fun getPostList(lastPost: Int = -1, postType: String = POST_GET_ALL): Single<NoticeListParent> {
        val map = hashMapOf(
                CLIENT_NAME to CY_VERSE_ANDROID,
                OP to GET_NOTICES,
                SERVICE_NAME to NetworkConstants.POST_DATA,
                USER_ID to userId,
                FILTERS to "{\"type\": \"${postType}\"}",
                FETCH_COUNT to "50",
                CLID to CY_VERSE_API_CLID
        )
        if (lastPost != -1)
            map[LAST_POST] = lastPost.toString()
        return cyCoreService.getPostList(map)
    }

    fun updatePostDetails(map: MutableMap<String, RequestBody>) {
        cyCoreService.updatePostDetails(CY_VERSE_API_CLID, map).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(handleUpdatePostDetails())
    }

    val postDetailsLiveData = MutableLiveData<UpdateNoticeModel>()
    private fun handleUpdatePostDetails(): SingleObserver<UpdateNoticeModel> {
        return object : SingleObserver<UpdateNoticeModel> {

            override fun onSuccess(t: UpdateNoticeModel) {
                if (t.status == "ok")
                    postDetailsLiveData.postValue(t)
                else
                    errorData.postValue(ErrorModel(UPDATE_POST_DETAILS, t.message))
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                Timber.log(1, e)
            }
        }
    }

    fun uploadMedia(map: MutableMap<String, RequestBody>) {
        Log.e("@@",map.toString())
        cyCoreService.uploadMedia(CY_VERSE_API_CLID, map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(handleUploadMedia())
    }

    val postUploadedLiveData = MutableLiveData<BaseResponse>()
    private fun handleUploadMedia(): SingleObserver<BaseResponse> {
        return object : SingleObserver<BaseResponse> {

            override fun onSuccess(t: BaseResponse) {
                Log.e("@@","Suceess" )
                postUploadedLiveData.postValue(t)
            }

            override fun onSubscribe(d: Disposable) {

            }

            override fun onError(e: Throwable) {
                Log.e("@@ in error",e.toString())

            }
        }
    }

    fun getTimeZones() = cyCoreService.getTimeZones(hashMapOf(
            CLIENT_NAME to CY_VERSE_ANDROID,
            OP to GET_TIMEZONES,
            SERVICE_NAME to NetworkConstants.TIME_ZONES,
            CLID to CY_VERSE_API_CLID
    ))

    val eventLiveData = MutableLiveData<EventModel?>()
}
