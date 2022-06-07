/*
 * Copyright (c) 2022 New Vector Ltd
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

package com.cioinfotech.cychat.features.plugins

import com.cioinfotech.cychat.features.plugins.model.PluginListParentModel
import io.reactivex.Single
import org.matrix.android.sdk.internal.network.HttpHeaders.Authorization
import org.matrix.android.sdk.internal.network.NetworkConstants
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface PluginsAPI {

    @FormUrlEncoded
    @Headers("CONNECT_TIMEOUT:60000", "READ_TIMEOUT:60000", "WRITE_TIMEOUT:60000")
    @POST(NetworkConstants.ROOT_API)
    fun getUserPlugins(
            @Header(Authorization) header: String,
            @FieldMap map: HashMap<String, String>): Single<PluginListParentModel>
}
