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
import com.cioinfotech.cychat.features.home.notice.model.UpdateNoticeModel
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.matrix.android.sdk.BuildConfig
import org.matrix.android.sdk.internal.cy_auth.data.BaseResponse
import org.matrix.android.sdk.internal.network.NetworkConstants
import org.matrix.android.sdk.internal.network.RetrofitFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DefaultCyCoreService @Inject constructor(private val retrofitFactory: RetrofitFactory) : CyCoreService {

    override fun cyGetDomainDetails(hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).getDomainDetails(hashMap)
    override fun cyUpdateRecoveryKey(hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).updateRecoveryKey(hashMap)
    override fun cyDisplayname(hashMap: HashMap<String, String>, url: String) =buildCyCoreAPI(url).setDisplayName(hashMap)
    override fun cyDeleteOldSessions(hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).deleteOldSessions(hashMap)
    override fun cyGetFederatedDomains(hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).getFederatedDomains(hashMap)
    override fun cyGetDefaultURLs(hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).getDefaultURLs(hashMap)
    override fun cyUserSearch(hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).userSearch(hashMap)
    override fun getAddUserTypes(hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).getAddUserTypes(hashMap)
    override fun verifyAddUserType(hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).verifyAddUserType(hashMap)
    override fun verifyOTP(hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).verifyOTP(hashMap)
    override fun getProfileDetails(hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).getProfileDetails(hashMap)
    override fun setVisibility(hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).setVisibility(hashMap)
    override fun resendVerificationCode(hashMap: HashMap<String, String>, url: String) = buildCyCoreAPI(url).resendVerificationCode(hashMap)
    override fun deleteRequest(mapOf: HashMap<String, String>, url: String) = buildCyCoreAPI(url).deleteRequest(mapOf)
    override fun getNoticeBoards(hashMapOf: HashMap<String, String>) = buildCreateCentralAPI().getNoticeBoards(hashMapOf[NetworkConstants.CLID]
            ?: "", hashMapOf)

    override fun getPostList(map: HashMap<String, String>) = buildCreateCentralAPI().getPostList(map[NetworkConstants.CLID] ?: "", map)
    override fun updatePostDetails(clid: String, map: MutableMap<String, RequestBody>): Single<UpdateNoticeModel> {
        return buildCreateCentralAPI().updatePostDetails(clid, map)
    }

    override fun uploadMedia(clid: String, map: MutableMap<String, RequestBody>): Single<BaseResponse> {
        return buildCreateCentralAPI().uploadMedia(clid, map)
    }

    override fun getTimeZones(map: java.util.HashMap<String, String>) = buildCreateCentralAPI().getTimeZones(map[NetworkConstants.CLID] ?: "", map)

    private fun buildCyCoreAPI(url: String): CyCoreAPI {
        return retrofitFactory.createWithBaseURL(buildClient(), url).create(CyCoreAPI::class.java)
    }

    private fun buildCreateCentralAPI(): CyCoreAPI {
        return retrofitFactory.createCentralServer(buildClient()).create(CyCoreAPI::class.java)
    }

    private fun buildClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            addInterceptor(HttpLoggingInterceptor().apply { this.level = BuildConfig.OKHTTP_LOGGING_LEVEL })
            connectTimeout(1, TimeUnit.MINUTES)
            readTimeout(1, TimeUnit.MINUTES)
            writeTimeout(1, TimeUnit.MINUTES)
        }.build()
    }
}
