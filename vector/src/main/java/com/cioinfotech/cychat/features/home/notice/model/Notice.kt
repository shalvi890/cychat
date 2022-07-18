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

package com.cioinfotech.cychat.features.home.notice.model

data class Notice(
        val attachments: List<Media> = mutableListOf(),
        val boardImage: String? = null,
        val boardName: String? = null,
        val createdAt: String? = null,
        val media: List<Media> = mutableListOf(),
        val postedBy: String? = null,
        val postid: Int = 0,
        val textAfter: String? = null,
        val textBefore: String? = null,
        val title: String? = null,
        val canEditPost: String? = "No",
        var isLoadingItem: Boolean? = false,
        var event: EventModel? = null
)
