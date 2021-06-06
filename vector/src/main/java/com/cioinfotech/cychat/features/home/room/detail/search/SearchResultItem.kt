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

package com.cioinfotech.cychat.features.home.room.detail.search

import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.epoxy.ClickListener
import com.cioinfotech.cychat.core.epoxy.VectorEpoxyHolder
import com.cioinfotech.cychat.core.epoxy.VectorEpoxyModel
import com.cioinfotech.cychat.core.epoxy.onClick
import com.cioinfotech.cychat.core.extensions.setTextOrHide
import com.cioinfotech.cychat.features.home.AvatarRenderer
import org.matrix.android.sdk.api.util.MatrixItem

@EpoxyModelClass(layout = R.layout.item_search_result)
abstract class SearchResultItem : VectorEpoxyModel<SearchResultItem.Holder>() {

    @EpoxyAttribute lateinit var avatarRenderer: AvatarRenderer
    @EpoxyAttribute var formattedDate: String? = null
    @EpoxyAttribute lateinit var spannable: CharSequence
    @EpoxyAttribute var sender: MatrixItem? = null
    @EpoxyAttribute var listener: ClickListener? = null

    override fun bind(holder: Holder) {
        super.bind(holder)

        holder.view.onClick(listener)
        sender?.let { avatarRenderer.render(it, holder.avatarImageView) }
        holder.memberNameView.setTextOrHide(sender?.getBestName())
        holder.timeView.text = formattedDate
        holder.contentView.text = spannable
    }

    class Holder : VectorEpoxyHolder() {
        val avatarImageView by bind<ImageView>(R.id.messageAvatarImageView)
        val memberNameView by bind<TextView>(R.id.messageMemberNameView)
        val timeView by bind<TextView>(R.id.messageTimeView)
        val contentView by bind<TextView>(R.id.messageContentView)
    }
}
