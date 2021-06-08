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

package com.cioinfotech.cychat.features.home.room.detail.timeline.item

import android.text.method.MovementMethod
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.text.PrecomputedTextCompat
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.ui.views.SendStateImageView
import com.cioinfotech.cychat.features.home.room.detail.timeline.TimelineEventController
import com.cioinfotech.cychat.features.home.room.detail.timeline.tools.findPillsAndProcess
import com.cioinfotech.cychat.features.home.room.detail.timeline.url.PreviewUrlRetriever
import com.cioinfotech.cychat.features.home.room.detail.timeline.url.PreviewUrlUiState
import com.cioinfotech.cychat.features.home.room.detail.timeline.url.PreviewUrlView
import com.cioinfotech.cychat.features.media.ImageContentRenderer

@Suppress("DEPRECATION")
@EpoxyModelClass(layout = R.layout.item_timeline_event_base)
abstract class MessageTextItem : AbsMessageItem<MessageTextItem.Holder>() {

    @EpoxyAttribute
    var searchForPills: Boolean = false

    @EpoxyAttribute
    var message: CharSequence? = null

    @EpoxyAttribute
    var useBigFont: Boolean = false

    @EpoxyAttribute
    var previewUrlRetriever: PreviewUrlRetriever? = null

    @EpoxyAttribute
    var previewUrlCallback: TimelineEventController.PreviewUrlCallback? = null

    @EpoxyAttribute
    var imageContentRenderer: ImageContentRenderer? = null

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var movementMethod: MovementMethod? = null

    private val previewUrlViewUpdater = PreviewUrlViewUpdater()
    private val expandText = ""
    override fun bind(holder: Holder) {
        // Preview URL
        previewUrlViewUpdater.previewUrlView = holder.previewUrlView
        previewUrlViewUpdater.imageContentRenderer = imageContentRenderer
        val safePreviewUrlRetriever = previewUrlRetriever
        if (safePreviewUrlRetriever == null) {
            holder.previewUrlView.isVisible = false
        } else {
            safePreviewUrlRetriever.addListener(attributes.informationData.eventId, previewUrlViewUpdater)
        }
        holder.previewUrlView.delegate = previewUrlCallback

        if (useBigFont) {
            holder.messageView.textSize = 44F
        } else {
            holder.messageView.textSize = 14F
        }
        if (searchForPills) {
            message?.findPillsAndProcess(coroutineScope) {
                // mmm.. not sure this is so safe in regards to cell reuse
                it.bind(holder.messageView)
            }
        }
        val textFuture = PrecomputedTextCompat.getTextFuture(
                message ?: "",
                TextViewCompat.getTextMetricsParams(holder.messageView),
                null)
        super.bind(holder)
        holder.messageView.movementMethod = movementMethod

        renderSendState(holder.messageView, holder.messageView)
        holder.messageView.setOnClickListener(attributes.itemClickListener)
        holder.messageView.setOnLongClickListener(attributes.itemLongClickListener)
        holder.messageView.setTextFuture(textFuture)

        val constraintSet = ConstraintSet()
        constraintSet.clone(holder.clParentText as ConstraintLayout)
        if (attributes.informationData.sentByMe) {
            constraintSet.setHorizontalBias(holder.clText.id, 1f)
        } else {
            constraintSet.setHorizontalBias(holder.clText.id, 0f)
        }
        constraintSet.applyTo(holder.clParentText as ConstraintLayout)
        holder.textTimeView.text = attributes.informationData.time
        holder.sendStateImageView.render(attributes.informationData.sendStateDecoration)
//        ShowMoreText.makeTextViewResizable(holder.messageView, ".. See More", true)
    }

    override fun unbind(holder: Holder) {
        super.unbind(holder)
        previewUrlViewUpdater.previewUrlView = null
        previewUrlViewUpdater.imageContentRenderer = null
        previewUrlRetriever?.removeListener(attributes.informationData.eventId, previewUrlViewUpdater)
    }

    override fun getViewType() = STUB_ID

    class Holder : AbsMessageItem.Holder(STUB_ID) {
        val messageView by bind<AppCompatTextView>(R.id.messageTextView)
        val previewUrlView by bind<PreviewUrlView>(R.id.messageUrlPreview)
        val textTimeView by bind<TextView>(R.id.messageTextTimeView)
        val clText by bind<ConstraintLayout>(R.id.clText)
        val clParentText by bind<ViewGroup>(R.id.clParentText)
        val sendStateImageView by bind<SendStateImageView>(R.id.messageSendStateImageView)
    }

    inner class PreviewUrlViewUpdater : PreviewUrlRetriever.PreviewUrlRetrieverListener {
        var previewUrlView: PreviewUrlView? = null
        var imageContentRenderer: ImageContentRenderer? = null

        override fun onStateUpdated(state: PreviewUrlUiState) {
            val safeImageContentRenderer = imageContentRenderer
            if (safeImageContentRenderer == null) {
                previewUrlView?.isVisible = false
                return
            }
            previewUrlView?.render(state, safeImageContentRenderer)
        }
    }

    companion object {
        private const val STUB_ID = R.id.messageContentTextStub
    }
}