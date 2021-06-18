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
import org.matrix.android.sdk.internal.network.NetworkConstants
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
class CyCoreViewModel @Inject constructor(
        private val cyCoreService: CyCoreService,
        private val applicationContext: Context
) : ViewModel() {

    init {

    }

    private var domainMutData: MutableLiveData<Boolean> = MutableLiveData()
    val domainData: LiveData<Boolean> get() = domainMutData

    fun setDomainLiveData() {
        domainMutData.postValue(false)
    }

    fun handleCyGetDetails(auth: String, url: String) {
        cyCoreService.cyGetDomainDetails(auth, hashMapOf("user_id" to "37"), url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getDomainDetails())
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

            override fun onError(e: Throwable) {}
        }
    }
}
