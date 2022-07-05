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

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.glide.GlideApp
import com.cioinfotech.cychat.core.utils.DimensionConverter
import com.cioinfotech.cychat.databinding.ItemNoticeBoardBinding
import com.cioinfotech.cychat.features.home.notice.model.Notice
import org.matrix.android.sdk.internal.network.NetworkConstants

class NoticeBoardAdapter(private val showMenu: Boolean = false) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var notices = mutableListOf<Notice>()
    private val LOADING = 0
    private val ITEM = 1
    var clickListener: ClickListener? = null
//    fun setNoticeList(notices: MutableList<Notice>) {
//        this.notices = notices
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM ->
                NoticeBoardViewHolder(ItemNoticeBoardBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else ->
                LoadingViewHolder(com.cioinfotech.cychat.databinding.ItemProgressBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val notice = notices[position]
        if (holder is NoticeBoardViewHolder && (notice.isLoadingItem == null || notice.isLoadingItem == false)) {
            val binding = holder.itemBinding
            binding.tvTitle.text = notice.title
            binding.tvNoticeBoard.text = notice.boardName
            binding.tvPostedBy.text = binding.root.context.getString(R.string.posted_by, notice.postedBy)
            binding.tvTextBefore.text = notice.textBefore
            binding.tvTextAfter.text = notice.textAfter
            binding.tvTextBefore.isVisible = !notice.textBefore.isNullOrEmpty()
            binding.tvTextAfter.isVisible = !notice.textAfter.isNullOrEmpty()
            binding.tvDate.text = binding.root.context.getString(R.string.posted_on, notice.createdAt)
            binding.ivMenu.isVisible = showMenu && notice.canEditPost == NetworkConstants.EDIT_POST_YES
            binding.ivThumbnail.isVisible = notice.media.isNotEmpty()
            notice.boardImage?.let {
                binding.ivPic.renderProfilePic(it)
            }

            binding.tvPreviewAttachment.isVisible = notice.attachments.isNotEmpty()
            if (notice.attachments.isNotEmpty()) {
                val url = notice.attachments[0].path ?: ""
                binding.tvPreviewAttachment.text = url.substring(url.lastIndexOf("/") + 1, url.length)
                binding.tvPreviewAttachment.setOnClickListener {
                    clickListener?.onAttachmentClicked(notice.attachments[0].path.toString())
                }
            }

            if (notice.media.isNotEmpty())
                notice.media[0].path?.let { binding.ivThumbnail.render(it) }

            if (showMenu)
                binding.ivMenu.setOnClickListener {
                    clickListener?.onClickListener(notice)
                }

            if (notice.event?.eventType == NetworkConstants.POST)
                binding.clEvent.isVisible = false
            else {
                binding.clEvent.isVisible = true
                binding.tvType.text = if (notice.event?.eventType == NetworkConstants.EVENT_ONLINE)
                    "Online Event"
                else
                    "Live Event"
                binding.btnAddToCalendar.isVisible = !showMenu
                binding.btnAddToCalendar.setOnClickListener {
                    clickListener?.onAddToCalendarClicked(notice)
                }
                binding.tvStartTime.text = "Start Time: " + notice.event?.eventStart
                binding.tvEndTime.text = "End Time: " + notice.event?.eventEnd
                binding.tvVenue.text = "Venue: " + notice.event?.eventVenue
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == notices.size - 1 && notices[position].isLoadingItem == true) LOADING else ITEM
    }

    fun addLoadingFooter() {
        add(Notice(isLoadingItem = true))
    }

    fun removeLoadingFooter() {
        if (notices.isNotEmpty()) {
            val position = notices.size - 1
            notices.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun add(notice: Notice?) {
        if (notice != null) {
            notices.add(notice)
            notifyItemInserted(notices.size - 1)
        }
    }

    fun addAll(moveResults: List<Notice?>) {
        for (result in moveResults)
            add(result)
    }

    override fun getItemCount() = notices.size

    inner class LoadingViewHolder(val itemBinding: com.cioinfotech.cychat.databinding.ItemProgressBinding) : RecyclerView.ViewHolder(itemBinding.root)
    inner class NoticeBoardViewHolder(val itemBinding: ItemNoticeBoardBinding) : RecyclerView.ViewHolder(itemBinding.root)

    interface ClickListener {
        fun onClickListener(notice: Notice)
        fun onAttachmentClicked(url: String)
        fun onAddToCalendarClicked(notice: Notice)
    }

    private fun ImageView.render(url: String) {
        GlideApp.with(this)
                .load(url)
                .placeholder(showCircularProgressDrawable(this.context))
                .error(showCircularProgressDrawable(this.context))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .dontAnimate()
                .fitCenter()
//                .transform(RoundedCorners(DimensionConverter(this.context.resources).dpToPx(8)))
                .into(this)
    }

    private fun ImageView.renderProfilePic(url: String) {
        GlideApp.with(this)
                .load(url)
                .placeholder(showCircularProgressDrawable(this.context))
                .error(showCircularProgressDrawable(this.context))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .dontAnimate()
                .transform(RoundedCorners(DimensionConverter(this.context.resources).dpToPx(20)))
                .into(this)
    }

    private fun showCircularProgressDrawable(context: Context): CircularProgressDrawable {
        return CircularProgressDrawable(context).apply {
            strokeWidth = 8f
            centerRadius = 48f
            start()
        }
    }
}
