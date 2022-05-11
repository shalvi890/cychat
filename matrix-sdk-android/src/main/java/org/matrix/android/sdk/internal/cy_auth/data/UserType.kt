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

package org.matrix.android.sdk.internal.cy_auth.data

data class UserType(
        val utype_id: String,
        val utype_name: String,
        val verify_mode: String,
        val reg_title: String,
        val ut_cat_id: String,
        val reg_type: String,
        val ut_parent_id: String,
        val ut_cat_name: String,
        val ut_cat_desc: String,
        val relevance: String,
        val cluster: String,
        val ut_show: String,
        val ut_mode: String,
        val setup_id: String,
        val cychat_url: String,
        val cychat_token: String
) : BaseResponse()
