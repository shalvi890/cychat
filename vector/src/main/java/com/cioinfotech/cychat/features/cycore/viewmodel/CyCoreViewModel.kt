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
import org.matrix.android.sdk.internal.network.NetworkConstants
import org.matrix.android.sdk.internal.network.NetworkConstants.AUTH_KEY
import org.matrix.android.sdk.internal.network.NetworkConstants.BASE_URL
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

    init {
        handleCyGetDetails()
    }

    private var domainMutData: MutableLiveData<Boolean> = MutableLiveData()
    val domainData: LiveData<Boolean> get() = domainMutData

    fun setDomainLiveData() {
        domainMutData.postValue(false)
    }

    private fun handleCyGetDetails() {
        var userId = ""
        pref.getString(USER_ID, "")?.let { userId = it }
        url?.let {
            cyCoreService.cyGetDomainDetails(AUTH_KEY, userId, it)
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

    fun handleUpdateRecoveryToken(auth: String, text: String) {
        var userId = ""
        pref.getString(USER_ID, "")?.let { userId = it }
        url?.let {
            cyCoreService.cyUpdateRecoveryKey(auth, hashMapOf(USER_ID_SMALL to userId, SECRET_KEY_SMALL to text), it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(updateRecoveryToken())
        }
    }

    private fun updateRecoveryToken(): SingleObserver<BaseResponse> {
        return object : SingleObserver<BaseResponse> {

            override fun onSuccess(t: BaseResponse) {
                if (t.status != "ok")
                    Timber.log(0, t.toString())
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {}
        }
    }
}
