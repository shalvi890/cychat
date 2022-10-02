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

import com.cioinfotech.cychat.features.cycore.data.AddUserTypesResponse
import com.cioinfotech.cychat.features.cycore.data.DomainDetails
import com.cioinfotech.cychat.features.cycore.data.UserProfileData
import com.cioinfotech.cychat.features.cycore.data.UserSearch
import com.cioinfotech.cychat.features.cycore.data.VerifyAddUserTypeResponse
import com.cioinfotech.cychat.features.home.notice.model.NoticeBoardParent
import com.cioinfotech.cychat.features.home.notice.model.NoticeListParent
import com.cioinfotech.cychat.features.home.notice.model.TimezoneParent
import com.cioinfotech.cychat.features.home.notice.model.UpdateNoticeModel
import io.reactivex.Single
import okhttp3.RequestBody
import org.matrix.android.sdk.internal.cy_auth.data.BaseResponse
import org.matrix.android.sdk.internal.cy_auth.data.DefaultURLParent
import org.matrix.android.sdk.internal.cy_auth.data.FederatedDomainList
import org.matrix.android.sdk.internal.network.NetworkConstants
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PartMap

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
    fun setDisplayName(@FieldMap map: HashMap<String, String>): Single<BaseResponse>

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

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.ROOT_API)
    fun getAddUserTypes(@FieldMap map: HashMap<String, String>): Single<AddUserTypesResponse>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.ROOT_API)
    fun verifyAddUserType(@FieldMap map: HashMap<String, String>): Single<VerifyAddUserTypeResponse>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.ROOT_API)
    fun verifyOTP(@FieldMap map: HashMap<String, String>): Single<BaseResponse>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.ROOT_API)
    fun getProfileDetails(@FieldMap map: HashMap<String, String>): Single<UserProfileData>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.ROOT_API)
    fun setVisibility(@FieldMap map: HashMap<String, String>): Single<BaseResponse>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.ROOT_API)
    fun resendVerificationCode(@FieldMap map: HashMap<String, String>): Single<BaseResponse>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.ROOT_API)
    fun deleteRequest(@FieldMap map: HashMap<String, String>): Single<BaseResponse>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.ROOT_API)
    fun getNoticeBoards(@Header("Authorization") auth: String, @FieldMap map: HashMap<String, String>): Single<NoticeBoardParent>

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.ROOT_API)
    fun getPostList(@Header("Authorization") auth: String, @FieldMap map: HashMap<String, String>): Single<NoticeListParent>

    @POST(NetworkConstants.ROOT_API)
    @Multipart
    fun updatePostDetails(@Header("Authorization") clid: String, @PartMap map: MutableMap<String, RequestBody>): Single<UpdateNoticeModel>

    @POST(NetworkConstants.ROOT_API)
    @Multipart
    fun uploadMedia(@Header("Authorization") clid: String, @PartMap map: MutableMap<String, RequestBody>): Single<BaseResponse>

    @FormUrlEncoded
    @POST(NetworkConstants.ROOT_API)
    fun getTimeZones(@Header("Authorization") auth: String, @FieldMap map: HashMap<String, String>): Single<TimezoneParent>
}
