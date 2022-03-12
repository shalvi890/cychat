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
import com.cioinfotech.cychat.features.cycore.data.UserSearch
import dagger.Lazy
import io.reactivex.Single
import okhttp3.OkHttpClient
import org.matrix.android.sdk.internal.network.RetrofitFactory
import javax.inject.Inject

class DefaultCyCoreService @Inject constructor(
        private val okHttpClient: Lazy<OkHttpClient>,
        private val retrofitFactory: RetrofitFactory) : CyCoreService {

    override fun cyGetDomainDetails(hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).getDomainDetails(hashMap)
    override fun cyUpdateRecoveryKey(hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).updateRecoveryKey(hashMap)
    override fun cyDeleteOldSessions(hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).deleteOldSessions(hashMap)
    override fun cyGetFederatedDomains(hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).getFederatedDomains(hashMap)
    override fun cyGetDefaultURLs(hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).getDefaultURLs(hashMap)
    override fun cyUserSearch(hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).userSearch(hashMap)

    private fun buildCyCoreAPI(url: String): CyCoreAPI {
        return retrofitFactory.createWithBaseURL(buildClient(), url).create(CyCoreAPI::class.java)
    }

    private fun buildClient(): OkHttpClient {
        return okHttpClient.get()
                .newBuilder()
                .build()
    }
}
