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

package com.cioinfotech.cychat.features.cycore.data

import org.matrix.android.sdk.internal.cy_auth.data.BaseResponse

data class UserSearch(
        val data: UserSearchChild
) : BaseResponse()

data class UserSearchChild(
        val fed_list: MutableList<SearchedUser>
)

data class SearchedUser(
        val first_name: String?,
        val last_name: String?,
        val matrix_user_id: String
)
