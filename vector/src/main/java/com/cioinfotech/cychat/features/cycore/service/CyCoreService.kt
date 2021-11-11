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

import com.cioinfotech.cychat.features.cycore.data.DomainDetails
import io.reactivex.Single
import org.matrix.android.sdk.internal.cy_auth.data.BaseResponse
import org.matrix.android.sdk.internal.cy_auth.data.DefaultURLParent
import org.matrix.android.sdk.internal.cy_auth.data.FederatedDomainList

interface CyCoreService {
    fun cyGetDomainDetails(auth: String?, reqId: String?, userId: String?, url: String): Single<DomainDetails>
    fun cyUpdateRecoveryKey(auth: String?, reqId: String?, hashMap: HashMap<String, String>, url: String): Single<BaseResponse>
    fun cyDeleteOldSessions(auth: String?, reqId: String?, hashMap: HashMap<String, String>, url: String): Single<BaseResponse>
    fun cyGetFederatedDomains(auth: String?, reqId: String?, hashMap: HashMap<String, String>, url: String): Single<FederatedDomainList>
    fun cyGetDefaultURLs(auth: String?, url: String): Single<DefaultURLParent>
}
