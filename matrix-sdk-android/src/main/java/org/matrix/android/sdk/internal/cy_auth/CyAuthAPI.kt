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
import org.matrix.android.sdk.internal.cy_auth.data.GetSettingsParent
import org.matrix.android.sdk.internal.cy_auth.data.GroupParent
import org.matrix.android.sdk.internal.cy_auth.data.LoginResponse
import org.matrix.android.sdk.internal.cy_auth.data.UserTypeParent
import org.matrix.android.sdk.internal.network.NetworkConstants
import org.matrix.android.sdk.internal.network.NetworkConstants.ROOT_API
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface CyAuthAPI {

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(ROOT_API)
    fun login(@FieldMap map: HashMap<String, String>): Single<LoginResponse>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:10000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000", "no-encr:Y")
    @POST(ROOT_API)
    fun checkOTP(@FieldMap map: HashMap<String, String>): Single<CheckOTPResponse>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(ROOT_API)
    fun getSettings(@FieldMap map: HashMap<String, String>): Single<GetSettingsParent>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(ROOT_API)
    fun resendOTP(@FieldMap map: HashMap<String, String>): Single<BaseResponse>

    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.VALIDATE_SECURITY_CODE)
    fun validateSecurityCode(
            @Header("Authorization") auth: String?,
            @Body map: HashMap<String, String>): Single<BaseResponse>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(ROOT_API)
    fun getGroups(@FieldMap map: HashMap<String, String>): Single<GroupParent>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(ROOT_API)
    fun getUserType(@FieldMap map: HashMap<String, String>): Single<UserTypeParent>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:10000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(ROOT_API)
    fun cyNewCheckCode(@FieldMap map: HashMap<String, String>): Single<BaseResponse>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(ROOT_API)
    fun validateCodeBySupplier(@FieldMap map: HashMap<String, String>): Single<CheckOTPResponse>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(ROOT_API)
    fun recheckReferralCode(@FieldMap map: HashMap<String, String>): Single<BaseResponse>
}
