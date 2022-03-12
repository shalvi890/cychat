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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cioinfotech.cychat.core.di.DefaultSharedPreferences
import com.cioinfotech.cychat.features.cycore.data.DomainDetails
import com.cioinfotech.cychat.features.cycore.service.CyCoreService
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.matrix.android.sdk.internal.cy_auth.data.BaseResponse
import org.matrix.android.sdk.internal.cy_auth.data.DefaultURLParent
import org.matrix.android.sdk.internal.cy_auth.data.FederatedDomainList
import org.matrix.android.sdk.internal.network.NetworkConstants
import org.matrix.android.sdk.internal.network.NetworkConstants.BASE_URL
import org.matrix.android.sdk.internal.network.NetworkConstants.CLID
import org.matrix.android.sdk.internal.network.NetworkConstants.CLIENT_NAME
import org.matrix.android.sdk.internal.network.NetworkConstants.CY_VERSE_ANDROID
import org.matrix.android.sdk.internal.network.NetworkConstants.DEVICE_ID
import org.matrix.android.sdk.internal.network.NetworkConstants.EXCLUDE_USER_TYPE
import org.matrix.android.sdk.internal.network.NetworkConstants.FEDERATION
import org.matrix.android.sdk.internal.network.NetworkConstants.LIST_FEDERATED_API
import org.matrix.android.sdk.internal.network.NetworkConstants.OP
import org.matrix.android.sdk.internal.network.NetworkConstants.REQ_ID
import org.matrix.android.sdk.internal.network.NetworkConstants.SERVICE_NAME
import org.matrix.android.sdk.internal.network.NetworkConstants.SETUP_ID
import org.matrix.android.sdk.internal.network.NetworkConstants.USER_TYPE
import org.matrix.android.sdk.internal.network.NetworkConstants.USER_TYPE_DASH
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

    //    private var userId = pref.getString(USER_ID, null)
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
                        if (!data.logo.isNullOrEmpty())
                            this.putString(NetworkConstants.DOMAIN_IMAGE, data.logo)
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
        cyCoreService.cyDeleteOldSessions(hashMapOf(DEVICE_ID to deviceId), url)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(deleteOldSessions())
    }

    private fun deleteOldSessions(): SingleObserver<BaseResponse> {
        return object : SingleObserver<BaseResponse> {

            override fun onSuccess(t: BaseResponse) {
                if (t.status != "error")
                    pref.edit().apply {
                        putBoolean(NetworkConstants.SESSION_UPDATED, true)
                        apply()
                    }
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {}
        }
    }

    fun getFederatedDomains(excludeDomain: String = "-1") {
        cyCoreService.cyGetFederatedDomains(hashMapOf(
                OP to LIST_FEDERATED_API,
                SERVICE_NAME to FEDERATION,
                CLIENT_NAME to CY_VERSE_ANDROID,
                USER_TYPE_DASH to (pref.getString(USER_TYPE, "") ?: ""),
                CLID to (pref.getString(CLID, "") ?: ""),
                EXCLUDE_USER_TYPE to excludeDomain
        ), url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(handleGetFederatedDomains())
    }

//    private fun String?.getEmailDomain() = this?.substring(this.lastIndexOf("@") + 1, this.length) ?: ""

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
}
