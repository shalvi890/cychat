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

package com.cioinfotech.cychat.features.cycore.service

import com.cioinfotech.cychat.features.cycore.CyCoreAPI
import dagger.Lazy
import okhttp3.OkHttpClient
import org.matrix.android.sdk.internal.network.RetrofitFactory
import javax.inject.Inject

class DefaultCyCoreService @Inject constructor(
        private val okHttpClient: Lazy<OkHttpClient>,
        private val retrofitFactory: RetrofitFactory) : CyCoreService {

    override fun cyGetDomainDetails(auth: String, userId: String, url: String) = buildCyCoreAPI(url).getDomainDetails(auth, userId)
    override fun cyUpdateRecoveryKey(auth: String, hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).updateRecoveryKey(auth, hashMap)

    private fun buildCyCoreAPI(url: String): CyCoreAPI {
        val retrofit = retrofitFactory.createWithBaseURL(buildClient(), url)
        return retrofit.create(CyCoreAPI::class.java)
    }

    private fun buildClient(): OkHttpClient {
        return okHttpClient.get()
                .newBuilder()
                .build()
    }
}