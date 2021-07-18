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

package org.matrix.android.sdk.internal.cy_auth

import io.reactivex.Single
import org.matrix.android.sdk.internal.cy_auth.data.BaseResponse
import org.matrix.android.sdk.internal.cy_auth.data.CheckOTPResponse
import org.matrix.android.sdk.internal.cy_auth.data.CountryCodeParent
import org.matrix.android.sdk.internal.cy_auth.data.LoginResponse
import org.matrix.android.sdk.internal.cy_auth.data.PasswordLoginParams
import org.matrix.android.sdk.internal.cy_auth.data.VerifyOTPParams
import org.matrix.android.sdk.internal.network.NetworkConstants
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface CyAuthAPI {

    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.USER_LOGIN)
    fun login(
            @Header("Authorization") auth: String,
            @Body loginParams: PasswordLoginParams): Single<LoginResponse>

    @Headers("CONNECT_TIMEOUT:10000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000", "no-encr:Y")
    @POST(NetworkConstants.CHECK_OTP)
    fun checkOTP(
            @Header("Authorization") auth: String?,
            @Header("reqid") reqId: String?,
            @Body loginParams: VerifyOTPParams): Single<CheckOTPResponse>

    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @GET(NetworkConstants.COUNTRY_LIST)
    fun getCountryList(@Header("Authorization") auth: String): Single<CountryCodeParent>

    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.RESENT_OTP)
    fun resendOTP(
            @Header("Authorization") auth: String?,
            @Header("reqid") reqId: String?,
            @Body map: HashMap<String, String>): Single<BaseResponse>
}
