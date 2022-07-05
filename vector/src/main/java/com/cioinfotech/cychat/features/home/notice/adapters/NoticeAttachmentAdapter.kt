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

package com.cioinfotech.cychat.features.home.notice.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cioinfotech.cychat.databinding.ItemNoticeAttachmentBinding
import org.matrix.android.sdk.api.session.content.ContentAttachmentData

class NoticeAttachmentAdapter: RecyclerView.Adapter<NoticeAttachmentAdapter.NoticeAttachmentViewHolder>() {

    private var attachments = mutableListOf<ContentAttachmentData>()

//    fun add(notice: ContentAttachmentData) {
//        notices.add(notice)
//        notifyItemInserted(notices.size - 1)
//    }

    inner class NoticeAttachmentViewHolder(val itemBinding: ItemNoticeAttachmentBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeAttachmentViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: NoticeAttachmentViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }
}
