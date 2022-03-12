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

package com.cioinfotech.cychat.features.cycore

import com.cioinfotech.cychat.features.cycore.data.DomainDetails
import com.cioinfotech.cychat.features.cycore.data.UserSearch
import io.reactivex.Single
import org.matrix.android.sdk.internal.cy_auth.data.BaseResponse
import org.matrix.android.sdk.internal.cy_auth.data.DefaultURLParent
import org.matrix.android.sdk.internal.cy_auth.data.FederatedDomainList
import org.matrix.android.sdk.internal.network.NetworkConstants
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface CyCoreAPI {

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.ROOT_API)
    fun getDomainDetails(@FieldMap map: HashMap<String, String>): Single<DomainDetails>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.ROOT_API)
    fun updateRecoveryKey(@FieldMap map: HashMap<String, String>): Single<BaseResponse>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.ROOT_API)
    fun deleteOldSessions(@FieldMap map: HashMap<String, String>): Single<BaseResponse>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.ROOT_API)
    fun getFederatedDomains(@FieldMap map: HashMap<String, String>): Single<FederatedDomainList>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.ROOT_API)
    fun getDefaultURLs(@FieldMap map: HashMap<String, String>): Single<DefaultURLParent>


    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.ROOT_API)
    fun userSearch(@FieldMap map: HashMap<String, String>): Single<UserSearch>
}
