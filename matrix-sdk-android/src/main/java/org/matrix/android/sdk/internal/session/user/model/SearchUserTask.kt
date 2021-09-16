/*
 * Copyright 2020 The Matrix.org Foundation C.I.C.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.matrix.android.sdk.internal.session.user.model

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.matrix.android.sdk.api.session.user.model.User
import org.matrix.android.sdk.internal.network.GlobalErrorReceiver
import org.matrix.android.sdk.internal.network.HttpHeaders
import org.matrix.android.sdk.internal.network.RetrofitFactory
import org.matrix.android.sdk.internal.network.executeRequest
import org.matrix.android.sdk.internal.session.user.SearchUserAPI
import org.matrix.android.sdk.internal.task.Task
import org.matrix.android.sdk.internal.util.ensureProtocol
import org.matrix.android.sdk.internal.util.ensureTrailingSlash
import retrofit2.Retrofit
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal interface SearchUserTask : Task<SearchUserTask.Params, List<User>> {

    data class Params(
            val limit: Int,
            val search: String,
            val excludedUserIds: Set<String>,
            val baseURL: String?,
            val authKey: String?
    )
}

internal class DefaultSearchUserTask @Inject constructor(
        private val globalErrorReceiver: GlobalErrorReceiver,
        private val retrofitFactory: RetrofitFactory,
        private val retrofit: Retrofit
) : SearchUserTask {

    override suspend fun execute(params: SearchUserTask.Params): List<User> {

        val response = executeRequest(globalErrorReceiver) {
            try {
                val searchUserAPI = if (params.baseURL.isNullOrEmpty())
                    retrofit.create(SearchUserAPI::class.java)
                else {
                    val interceptor = HttpLoggingInterceptor().apply {
                        this.level = HttpLoggingInterceptor.Level.BODY
                    }
                    val okHttpClient = OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .addInterceptor { chain ->
                                var request = chain.request()
                                val newRequestBuilder = request.newBuilder()
                                newRequestBuilder.header(HttpHeaders.Authorization, "Bearer ${params.authKey}")
                                request = newRequestBuilder.build()
                                chain.proceed(request)
                            }
                            .addInterceptor(interceptor)
                            .build()

                    retrofitFactory.create(okHttpClient, params.baseURL.ensureTrailingSlash().ensureProtocol()).create(SearchUserAPI::class.java)
                }
                searchUserAPI.searchUsers(SearchUsersParams(params.search, params.limit))
            } catch (ex: Exception) {
                Timber.d(ex)
                SearchUsersResponse(false, mutableListOf())
            }
        }
        return response.users.map {
            User(it.userId, it.displayName, it.avatarUrl)
        }
    }
}
