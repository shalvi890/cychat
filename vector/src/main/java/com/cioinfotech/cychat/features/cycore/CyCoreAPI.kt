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
import io.reactivex.Single
import org.matrix.android.sdk.internal.cy_auth.data.BaseResponse
import org.matrix.android.sdk.internal.network.NetworkConstants
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface CyCoreAPI {
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @GET(NetworkConstants.GET_DOMAIN_DETAILS)
    fun getDomainDetails(
            @Header("Authorization") auth: String?,
            @Header("reqid") reqId: String?,
            @Query("user_id") userId: String?): Single<DomainDetails>

    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000", "no-encr:N")
    @POST(NetworkConstants.UPDATE_RECOVERY_KEY)
    fun updateRecoveryKey(
            @Header("Authorization") auth: String?,
            @Header("reqid") reqId: String?,
            @Body map: HashMap<String, String>): Single<BaseResponse>

    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @DELETE(NetworkConstants.DELETE_OLD_SESSION)
    fun deleteOldSessions(
            @Header("Authorization") auth: String?,
            @Header("reqid") reqId: String?,
            @QueryMap map: HashMap<String, String>): Single<BaseResponse>
}
