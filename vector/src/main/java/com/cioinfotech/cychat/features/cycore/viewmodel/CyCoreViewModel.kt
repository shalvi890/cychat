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
import android.content.SharedPreferences
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
import org.matrix.android.sdk.internal.cy_auth.data.FederatedDomainList
import org.matrix.android.sdk.internal.network.NetworkConstants
import org.matrix.android.sdk.internal.network.NetworkConstants.ACCESS_TOKEN
import org.matrix.android.sdk.internal.network.NetworkConstants.API_SERVER
import org.matrix.android.sdk.internal.network.NetworkConstants.BASE_URL
import org.matrix.android.sdk.internal.network.NetworkConstants.CURRENT_DOMAIN
import org.matrix.android.sdk.internal.network.NetworkConstants.DEVICE_ID
import org.matrix.android.sdk.internal.network.NetworkConstants.REQ_ID
import org.matrix.android.sdk.internal.network.NetworkConstants.SECRET_KEY_SMALL
import org.matrix.android.sdk.internal.network.NetworkConstants.USER_ID
import org.matrix.android.sdk.internal.network.NetworkConstants.USER_ID_SMALL
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
class CyCoreViewModel @Inject constructor(
        private val cyCoreService: CyCoreService,
        private val applicationContext: Context
) : ViewModel() {

    private var pref: SharedPreferences = DefaultSharedPreferences.getInstance(applicationContext)
    private var url = pref.getString(BASE_URL, null)
    private var userId = pref.getString(USER_ID, null)
    private var reqId = pref.getString(REQ_ID, null)
    private var accessToken = pref.getString(ACCESS_TOKEN, null)
    private var serverURL = pref.getString(API_SERVER, null)

    private var domainMutData: MutableLiveData<Boolean> = MutableLiveData()
    val domainData: LiveData<Boolean> get() = domainMutData

    private var federatedDomainListData: MutableLiveData<FederatedDomainList> = MutableLiveData()
    val federatedDomainList: LiveData<FederatedDomainList> get() = federatedDomainListData

    fun setDomainLiveData() {
        domainMutData.postValue(false)
    }

    fun handleCyGetDetails() {
        url?.let {
            cyCoreService.cyGetDomainDetails(accessToken, reqId, userId, it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(getDomainDetails())
        }
    }

    private fun getDomainDetails(): SingleObserver<DomainDetails> {
        return object : SingleObserver<DomainDetails> {

            override fun onSuccess(t: DomainDetails) {
                if (t.status == "ok") {
                    val prefs = DefaultSharedPreferences.getInstance(applicationContext)
                    val data = t.data
                    prefs.edit().apply {
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
        url?.let {
            cyCoreService.cyUpdateRecoveryKey(accessToken, reqId, hashMapOf(USER_ID_SMALL to userId!!, SECRET_KEY_SMALL to text), it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(updateRecoveryToken())
        }
    }

    private fun updateRecoveryToken(): SingleObserver<BaseResponse> {
        return object : SingleObserver<BaseResponse> {

            override fun onSuccess(t: BaseResponse) {}

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {}
        }
    }

    fun handleDeleteOldSessions(deviceId: String) {
        url?.let {
            cyCoreService.cyDeleteOldSessions(accessToken, reqId, hashMapOf(DEVICE_ID to deviceId), it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(deleteOldSessions())
        }
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

    fun getFederatedDomains() {
        url?.let {
            cyCoreService.cyGetFederatedDomains(accessToken, reqId, hashMapOf(CURRENT_DOMAIN to serverURL.toString()), it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(handleGetFederatedDomains())
        }
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
}
