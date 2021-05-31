/*
 * Copyright 2019 New Vector Ltd
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

package im.vector.app.features.home.room.detail.timeline.item

import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import im.vector.app.R
import im.vector.app.core.ui.views.SendStateImageView
import im.vector.app.features.home.room.detail.timeline.helper.ContentDownloadStateTrackerBinder
import im.vector.app.features.home.room.detail.timeline.helper.ContentUploadStateTrackerBinder

@EpoxyModelClass(layout = R.layout.item_timeline_event_base)
abstract class MessageFileItem : AbsMessageItem<MessageFileItem.Holder>() {

    @EpoxyAttribute
    var filename: CharSequence = ""

    @EpoxyAttribute
    var mxcUrl: String = ""

    @EpoxyAttribute
    @DrawableRes
    var iconRes: Int = 0

//    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
//    var clickListener: View.OnClickListener? = null

    @EpoxyAttribute
    var izLocalFile = false

    @EpoxyAttribute
    var izDownloaded = false

    @EpoxyAttribute
    lateinit var contentUploadStateTrackerBinder: ContentUploadStateTrackerBinder

    @EpoxyAttribute
    lateinit var contentDownloadStateTrackerBinder: ContentDownloadStateTrackerBinder

    override fun bind(holder: Holder) {
        super.bind(holder)
        renderSendState(holder.fileLayout, holder.filenameView)
        if (!attributes.informationData.sendState.hasFailed()) {
            contentUploadStateTrackerBinder.bind(attributes.informationData.eventId, izLocalFile, holder.progressLayout)
        } else {
            holder.fileImageView.setImageResource(R.drawable.ic_cross)
            holder.progressLayout.isVisible = false
        }
        holder.filenameView.text = filename
        if (attributes.informationData.sendState.isSending()) {
            holder.fileImageView.setImageResource(iconRes)
        } else {
            if (izDownloaded) {
                holder.fileImageView.setImageResource(iconRes)
                holder.fileDownloadProgress.progress = 100
            } else {
                contentDownloadStateTrackerBinder.bind(mxcUrl, holder)
                holder.fileImageView.setImageResource(R.drawable.ic_download)
                holder.fileDownloadProgress.progress = 0
            }
        }
//        holder.view.setOnClickListener(clickListener)

        holder.filenameView.setOnClickListener(attributes.itemClickListener)
        holder.filenameView.setOnLongClickListener(attributes.itemLongClickListener)
        holder.fileImageWrapper.setOnClickListener(attributes.itemClickListener)
        holder.fileImageWrapper.setOnLongClickListener(attributes.itemLongClickListener)
        holder.filenameView.paintFlags = (holder.filenameView.paintFlags or Paint.UNDERLINE_TEXT_FLAG)
        val constraintSet = ConstraintSet()
        constraintSet.clone(holder.fileLayout as ConstraintLayout)
        if (attributes.informationData.sentByMe)
            constraintSet.setHorizontalBias(holder.clFiles.id, 1f)
        else
            constraintSet.setHorizontalBias(holder.clFiles.id, 0f)
        constraintSet.applyTo(holder.fileLayout as ConstraintLayout)
        holder.textTimeView.text = attributes.informationData.time
        holder.sendStateImageView.render(attributes.informationData.sendStateDecoration)
    }

    override fun unbind(holder: Holder) {
        super.unbind(holder)
        contentUploadStateTrackerBinder.unbind(attributes.informationData.eventId)
        contentDownloadStateTrackerBinder.unbind(mxcUrl)
    }

    override fun getViewType() = STUB_ID

    class Holder : AbsMessageItem.Holder(STUB_ID) {
        val progressLayout by bind<ViewGroup>(R.id.messageFileUploadProgressLayout)
        val fileLayout by bind<ViewGroup>(R.id.messageFileLayout)
        val fileImageView by bind<ImageView>(R.id.messageFileIconView)
        val fileImageWrapper by bind<ViewGroup>(R.id.messageFileImageView)
        val fileDownloadProgress by bind<ProgressBar>(R.id.messageFileProgressbar)
        val filenameView by bind<TextView>(R.id.messageFilenameView)
        val clFiles by bind<ConstraintLayout>(R.id.clFiles)
        val textTimeView by bind<TextView>(R.id.messageTextTimeView)
        val sendStateImageView by bind<SendStateImageView>(R.id.messageSendStateImageView)
    }

    companion object {
        private const val STUB_ID = R.id.messageContentFileStub
    }
}
