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

package com.cioinfotech.cychat.features.home.room.detail.timeline.factory

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.epoxy.VectorEpoxyModel
import com.cioinfotech.cychat.core.files.LocalFilesHelper
import com.cioinfotech.cychat.core.resources.ColorProvider
import com.cioinfotech.cychat.core.resources.StringProvider
import com.cioinfotech.cychat.core.utils.DebouncedClickListener
import com.cioinfotech.cychat.core.utils.DimensionConverter
import com.cioinfotech.cychat.core.utils.containsOnlyEmojis
import com.cioinfotech.cychat.features.home.room.detail.timeline.TimelineEventController
import com.cioinfotech.cychat.features.home.room.detail.timeline.helper.AvatarSizeProvider
import com.cioinfotech.cychat.features.home.room.detail.timeline.helper.ContentDownloadStateTrackerBinder
import com.cioinfotech.cychat.features.home.room.detail.timeline.helper.ContentUploadStateTrackerBinder
import com.cioinfotech.cychat.features.home.room.detail.timeline.helper.MessageInformationDataFactory
import com.cioinfotech.cychat.features.home.room.detail.timeline.helper.MessageItemAttributesFactory
import com.cioinfotech.cychat.features.home.room.detail.timeline.helper.TimelineMediaSizeProvider
import com.cioinfotech.cychat.features.home.room.detail.timeline.item.AbsMessageItem
import com.cioinfotech.cychat.features.home.room.detail.timeline.item.MessageBlockCodeItem
import com.cioinfotech.cychat.features.home.room.detail.timeline.item.MessageBlockCodeItem_
import com.cioinfotech.cychat.features.home.room.detail.timeline.item.MessageFileItem
import com.cioinfotech.cychat.features.home.room.detail.timeline.item.MessageFileItem_
import com.cioinfotech.cychat.features.home.room.detail.timeline.item.MessageImageVideoItem
import com.cioinfotech.cychat.features.home.room.detail.timeline.item.MessageImageVideoItem_
import com.cioinfotech.cychat.features.home.room.detail.timeline.item.MessageInformationData
import com.cioinfotech.cychat.features.home.room.detail.timeline.item.MessageOptionsItem_
import com.cioinfotech.cychat.features.home.room.detail.timeline.item.MessagePollItem_
import com.cioinfotech.cychat.features.home.room.detail.timeline.item.MessageTextItem
import com.cioinfotech.cychat.features.home.room.detail.timeline.item.MessageTextItem_
import com.cioinfotech.cychat.features.home.room.detail.timeline.item.RedactedMessageItem
import com.cioinfotech.cychat.features.home.room.detail.timeline.item.RedactedMessageItem_
import com.cioinfotech.cychat.features.home.room.detail.timeline.item.VerificationRequestItem
import com.cioinfotech.cychat.features.home.room.detail.timeline.item.VerificationRequestItem_
import com.cioinfotech.cychat.features.home.room.detail.timeline.tools.createLinkMovementMethod
import com.cioinfotech.cychat.features.home.room.detail.timeline.tools.linkify
import com.cioinfotech.cychat.features.html.CodeVisitor
import com.cioinfotech.cychat.features.html.EventHtmlRenderer
import com.cioinfotech.cychat.features.html.PillsPostProcessor
import com.cioinfotech.cychat.features.html.VectorHtmlCompressor
import com.cioinfotech.cychat.features.media.ImageContentRenderer
import com.cioinfotech.cychat.features.media.VideoContentRenderer
import dagger.Lazy
import me.gujun.android.span.span
import org.commonmark.node.Document
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.events.model.RelationType
import org.matrix.android.sdk.api.session.events.model.toModel
import org.matrix.android.sdk.api.session.room.model.message.MessageAudioContent
import org.matrix.android.sdk.api.session.room.model.message.MessageContent
import org.matrix.android.sdk.api.session.room.model.message.MessageContentWithFormattedBody
import org.matrix.android.sdk.api.session.room.model.message.MessageEmoteContent
import org.matrix.android.sdk.api.session.room.model.message.MessageFileContent
import org.matrix.android.sdk.api.session.room.model.message.MessageImageInfoContent
import org.matrix.android.sdk.api.session.room.model.message.MessageNoticeContent
import org.matrix.android.sdk.api.session.room.model.message.MessageOptionsContent
import org.matrix.android.sdk.api.session.room.model.message.MessagePollResponseContent
import org.matrix.android.sdk.api.session.room.model.message.MessageTextContent
import org.matrix.android.sdk.api.session.room.model.message.MessageType
import org.matrix.android.sdk.api.session.room.model.message.MessageVerificationRequestContent
import org.matrix.android.sdk.api.session.room.model.message.MessageVideoContent
import org.matrix.android.sdk.api.session.room.model.message.OPTION_TYPE_BUTTONS
import org.matrix.android.sdk.api.session.room.model.message.OPTION_TYPE_POLL
import org.matrix.android.sdk.api.session.room.model.message.getFileName
import org.matrix.android.sdk.api.session.room.model.message.getFileUrl
import org.matrix.android.sdk.api.session.room.timeline.getLastMessageContent
import org.matrix.android.sdk.api.util.MimeTypes
import org.matrix.android.sdk.internal.crypto.attachments.toElementToDecrypt
import org.matrix.android.sdk.internal.crypto.model.event.EncryptedEventContent
import javax.inject.Inject

class MessageItemFactory @Inject constructor(
        private val localFilesHelper: LocalFilesHelper,
        private val colorProvider: ColorProvider,
        private val dimensionConverter: DimensionConverter,
        private val timelineMediaSizeProvider: TimelineMediaSizeProvider,
        private val htmlRenderer: Lazy<EventHtmlRenderer>,
        private val htmlCompressor: VectorHtmlCompressor,
        private val stringProvider: StringProvider,
        private val imageContentRenderer: ImageContentRenderer,
        private val messageInformationDataFactory: MessageInformationDataFactory,
        private val messageItemAttributesFactory: MessageItemAttributesFactory,
        private val contentUploadStateTrackerBinder: ContentUploadStateTrackerBinder,
        private val contentDownloadStateTrackerBinder: ContentDownloadStateTrackerBinder,
        private val defaultItemFactory: DefaultItemFactory,
        private val noticeItemFactory: NoticeItemFactory,
        private val avatarSizeProvider: AvatarSizeProvider,
        private val pillsPostProcessorFactory: PillsPostProcessor.Factory,
        private val session: Session) {

    // TODO inject this properly?
    private var roomId: String = ""

    private val pillsPostProcessor by lazy {
        pillsPostProcessorFactory.create(roomId)
    }

    fun create(params: TimelineItemFactoryParams): VectorEpoxyModel<*>? {
        val event = params.event
        val highlight = params.isHighlighted
        val callback = params.callback
        event.root.eventId ?: return null
        roomId = event.roomId
        val informationData = messageInformationDataFactory.create(params)
        if (event.root.isRedacted()) {
            // message is redacted
            val attributes = messageItemAttributesFactory.create(null, informationData, callback)
            return buildRedactedItem(attributes, highlight)
        }

        val messageContent = event.getLastMessageContent()
        if (messageContent == null) {
            val malformedText = stringProvider.getString(R.string.malformed_message)
            return defaultItemFactory.create(malformedText, informationData, highlight, callback)
        }
        if (messageContent.relatesTo?.type == RelationType.REPLACE
                || event.isEncrypted() && event.root.content.toModel<EncryptedEventContent>()?.relatesTo?.type == RelationType.REPLACE
        ) {
            // This is an edit event, we should display it when debugging as a notice event
            return noticeItemFactory.create(params)
        }
        val attributes = messageItemAttributesFactory.create(messageContent, informationData, callback)

//        val all = event.root.toContent()
//        val ev = all.toModel<Event>()
        return when (messageContent) {
            is MessageEmoteContent               -> buildEmoteMessageItem(messageContent, informationData, highlight, callback, attributes)
            is MessageTextContent                -> buildItemForTextContent(messageContent, informationData, highlight, callback, attributes)
            is MessageImageInfoContent           -> buildImageMessageItem(messageContent, informationData, highlight, callback, attributes)
            is MessageNoticeContent              -> buildNoticeMessageItem(messageContent, informationData, highlight, callback, attributes)
            is MessageVideoContent               -> buildVideoMessageItem(messageContent, informationData, highlight, callback, attributes)
            is MessageFileContent                -> buildFileMessageItem(messageContent, highlight, attributes)
            is MessageAudioContent               -> buildAudioMessageItem(messageContent, informationData, highlight, attributes, callback)
            is MessageVerificationRequestContent -> buildVerificationRequestMessageItem(messageContent, informationData, highlight, callback, attributes)
            is MessageOptionsContent             -> buildOptionsMessageItem(messageContent, informationData, highlight, callback, attributes)
            is MessagePollResponseContent        -> noticeItemFactory.create(params)
            else                                 -> buildNotHandledMessageItem(messageContent, informationData, highlight, callback, attributes)
        }
    }

    private fun buildOptionsMessageItem(messageContent: MessageOptionsContent,
                                        informationData: MessageInformationData,
                                        highlight: Boolean,
                                        callback: TimelineEventController.Callback?,
                                        attributes: AbsMessageItem.Attributes): VectorEpoxyModel<*>? {
        return when (messageContent.optionType) {
            OPTION_TYPE_POLL    -> {
                MessagePollItem_()
                        .attributes(attributes)
                        .callback(callback)
                        .informationData(informationData)
                        .leftGuideline(avatarSizeProvider.leftGuideline)
                        .optionsContent(messageContent)
                        .highlighted(highlight)
            }
            OPTION_TYPE_BUTTONS -> {
                MessageOptionsItem_()
                        .attributes(attributes)
                        .callback(callback)
                        .informationData(informationData)
                        .leftGuideline(avatarSizeProvider.leftGuideline)
                        .optionsContent(messageContent)
                        .highlighted(highlight)
            }
            else                -> {
                // Not supported optionType
                buildNotHandledMessageItem(messageContent, informationData, highlight, callback, attributes)
            }
        }
    }

    private fun buildAudioMessageItem(messageContent: MessageAudioContent,
                                      @Suppress("UNUSED_PARAMETER")
                                      informationData: MessageInformationData,
                                      highlight: Boolean,
                                      attributes: AbsMessageItem.Attributes,
                                      callback: TimelineEventController.Callback?): MessageFileItem? {
        val fileUrl = messageContent.getFileUrl()?.let {
            if (informationData.sentByMe && !informationData.sendState.isSent()) {
                it
            } else {
                it.takeIf { it.startsWith("mxc://") }
            }
        } ?: ""
        return MessageFileItem_()
                .attributes(attributes)
                .izLocalFile(localFilesHelper.isLocalFile(fileUrl))
                .izDownloaded(session.fileService().isFileInCache(
                        fileUrl,
                        messageContent.getFileName(),
                        messageContent.mimeType,
                        messageContent.encryptedFileInfo?.toElementToDecrypt())
                )
                .mxcUrl(fileUrl)
                .contentUploadStateTrackerBinder(contentUploadStateTrackerBinder)
                .contentDownloadStateTrackerBinder(contentDownloadStateTrackerBinder)
                .highlighted(highlight)
                .leftGuideline(avatarSizeProvider.leftGuideline)
                .filename(messageContent.body)
                .iconRes(R.drawable.ic_play_arrow)
                .clickListener { _ -> callback?.onAudioMessageClicked(messageContent) }
    }

    private fun buildVerificationRequestMessageItem(messageContent: MessageVerificationRequestContent,
                                                    @Suppress("UNUSED_PARAMETER")
                                                    informationData: MessageInformationData,
                                                    highlight: Boolean,
                                                    callback: TimelineEventController.Callback?,
                                                    attributes: AbsMessageItem.Attributes): VerificationRequestItem? {
        // If this request is not sent by me or sent to me, we should ignore it in timeline
        val myUserId = session.myUserId
        if (informationData.senderId != myUserId && messageContent.toUserId != myUserId) {
            return null
        }

        val otherUserId = if (informationData.sentByMe) messageContent.toUserId else informationData.senderId
        val otherUserName = if (informationData.sentByMe) {
            session.getRoomMember(messageContent.toUserId, roomId)?.displayName
        } else {
            informationData.memberName
        }
        return VerificationRequestItem_()
                .attributes(
                        VerificationRequestItem.Attributes(
                                otherUserId = otherUserId,
                                otherUserName = otherUserName.toString(),
                                referenceId = informationData.eventId,
                                informationData = informationData,
                                avatarRenderer = attributes.avatarRenderer,
                                messageColorProvider = attributes.messageColorProvider,
                                itemLongClickListener = attributes.itemLongClickListener,
                                itemClickListener = attributes.itemClickListener,
                                reactionPillCallback = attributes.reactionPillCallback,
                                readReceiptsCallback = attributes.readReceiptsCallback,
                                emojiTypeFace = attributes.emojiTypeFace
                        )
                )
                .callback(callback)
                .highlighted(highlight)
                .leftGuideline(avatarSizeProvider.leftGuideline)
    }

    private fun buildFileMessageItem(messageContent: MessageFileContent,
//                                     informationData: MessageInformationData,
                                     highlight: Boolean,
//                                     callback: TimelineEventController.Callback?,
                                     attributes: AbsMessageItem.Attributes): MessageFileItem? {
        val mxcUrl = messageContent.getFileUrl() ?: ""
        return MessageFileItem_()
                .attributes(attributes)
                .leftGuideline(avatarSizeProvider.leftGuideline)
                .izLocalFile(localFilesHelper.isLocalFile(messageContent.getFileUrl()))
                .izDownloaded(session.fileService().isFileInCache(messageContent))
                .mxcUrl(mxcUrl)
                .contentUploadStateTrackerBinder(contentUploadStateTrackerBinder)
                .contentDownloadStateTrackerBinder(contentDownloadStateTrackerBinder)
                .highlighted(highlight)
                .filename(messageContent.body)
                .iconRes(R.drawable.ic_paperclip)
    }

    private fun buildNotHandledMessageItem(messageContent: MessageContent,
                                           informationData: MessageInformationData,
                                           highlight: Boolean,
                                           callback: TimelineEventController.Callback?,
                                           attributes: AbsMessageItem.Attributes): MessageTextItem? {
        // For compatibility reason we should display the body
        return buildMessageTextItem(messageContent.body, false, informationData, highlight, callback, attributes)
    }

    private fun buildImageMessageItem(messageContent: MessageImageInfoContent,
                                      @Suppress("UNUSED_PARAMETER")
                                      informationData: MessageInformationData,
                                      highlight: Boolean,
                                      callback: TimelineEventController.Callback?,
                                      attributes: AbsMessageItem.Attributes): MessageImageVideoItem? {
        val (maxWidth, maxHeight) = timelineMediaSizeProvider.getMaxSize()
        val data = ImageContentRenderer.Data(
                eventId = informationData.eventId,
                filename = messageContent.body,
                mimeType = messageContent.mimeType,
                url = messageContent.getFileUrl(),
                elementToDecrypt = messageContent.encryptedFileInfo?.toElementToDecrypt(),
                height = messageContent.info?.height,
                maxHeight = maxHeight,
                width = messageContent.info?.width,
                maxWidth = maxWidth,
                allowNonMxcUrls = informationData.sendState.isSending()
        )
        return MessageImageVideoItem_()
                .attributes(attributes)
                .leftGuideline(avatarSizeProvider.leftGuideline)
                .imageContentRenderer(imageContentRenderer)
                .contentUploadStateTrackerBinder(contentUploadStateTrackerBinder)
                .playable(messageContent.info?.mimeType == MimeTypes.Gif)
                .highlighted(highlight)
                .mediaData(data)
                .apply {
                    if (messageContent.msgType == MessageType.MSGTYPE_STICKER_LOCAL) {
                        mode(ImageContentRenderer.Mode.STICKER)
                    } else {
                        clickListener(
                                DebouncedClickListener({ view ->
                                    callback?.onImageMessageClicked(messageContent, data, view)
                                }))
                    }
                }
    }

    private fun buildVideoMessageItem(messageContent: MessageVideoContent,
                                      informationData: MessageInformationData,
                                      highlight: Boolean,
                                      callback: TimelineEventController.Callback?,
                                      attributes: AbsMessageItem.Attributes): MessageImageVideoItem? {
        val (maxWidth, maxHeight) = timelineMediaSizeProvider.getMaxSize()
        val thumbnailData = ImageContentRenderer.Data(
                eventId = informationData.eventId,
                filename = messageContent.body,
                mimeType = messageContent.mimeType,
                url = messageContent.videoInfo?.thumbnailFile?.url
                        ?: messageContent.videoInfo?.thumbnailUrl,
                elementToDecrypt = messageContent.videoInfo?.thumbnailFile?.toElementToDecrypt(),
                height = messageContent.videoInfo?.height,
                maxHeight = maxHeight,
                width = messageContent.videoInfo?.width,
                maxWidth = maxWidth,
                allowNonMxcUrls = informationData.sendState.isSending()
        )

        val videoData = VideoContentRenderer.Data(
                eventId = informationData.eventId,
                filename = messageContent.body,
                mimeType = messageContent.mimeType,
                url = messageContent.getFileUrl(),
                elementToDecrypt = messageContent.encryptedFileInfo?.toElementToDecrypt(),
                thumbnailMediaData = thumbnailData
        )

        return MessageImageVideoItem_()
                .leftGuideline(avatarSizeProvider.leftGuideline)
                .attributes(attributes)
                .imageContentRenderer(imageContentRenderer)
                .contentUploadStateTrackerBinder(contentUploadStateTrackerBinder)
                .playable(true)
                .highlighted(highlight)
                .mediaData(thumbnailData)
                .clickListener { view -> callback?.onVideoMessageClicked(messageContent, videoData, view.findViewById(R.id.messageThumbnailView)) }
    }

    private fun buildItemForTextContent(messageContent: MessageTextContent,
                                        informationData: MessageInformationData,
                                        highlight: Boolean,
                                        callback: TimelineEventController.Callback?,
                                        attributes: AbsMessageItem.Attributes): VectorEpoxyModel<*>? {
        val isFormatted = messageContent.matrixFormattedBody.isNullOrBlank().not()
        return if (isFormatted) {
            // First detect if the message contains some code block(s) or inline code
            val localFormattedBody = htmlRenderer.get().parse(messageContent.body) as Document
            val codeVisitor = CodeVisitor()
            codeVisitor.visit(localFormattedBody)
            when (codeVisitor.codeKind) {
                CodeVisitor.Kind.BLOCK  -> {
                    val codeFormattedBlock = htmlRenderer.get().render(localFormattedBody)
                    if (codeFormattedBlock == null) {
                        buildFormattedTextItem(messageContent, informationData, highlight, callback, attributes)
                    } else {
                        buildCodeBlockItem(codeFormattedBlock, informationData, highlight, callback, attributes)
                    }
                }
                CodeVisitor.Kind.INLINE -> {
                    val codeFormatted = htmlRenderer.get().render(localFormattedBody)
                    if (codeFormatted == null) {
                        buildFormattedTextItem(messageContent, informationData, highlight, callback, attributes)
                    } else {
                        buildMessageTextItem(codeFormatted, false, informationData, highlight, callback, attributes)
                    }
                }
                CodeVisitor.Kind.NONE   -> {
                    buildFormattedTextItem(messageContent, informationData, highlight, callback, attributes)
                }
            }
        } else {
            buildMessageTextItem(messageContent.body, false, informationData, highlight, callback, attributes)
        }
    }

    private fun buildFormattedTextItem(messageContent: MessageTextContent,
                                       informationData: MessageInformationData,
                                       highlight: Boolean,
                                       callback: TimelineEventController.Callback?,
                                       attributes: AbsMessageItem.Attributes): MessageTextItem? {
        val compressed = htmlCompressor.compress(messageContent.formattedBody!!)
        val formattedBody = htmlRenderer.get().render(compressed, pillsPostProcessor)
        return buildMessageTextItem(formattedBody, true, informationData, highlight, callback, attributes)
    }

    private fun buildMessageTextItem(body: CharSequence,
                                     isFormatted: Boolean,
                                     informationData: MessageInformationData,
                                     highlight: Boolean,
                                     callback: TimelineEventController.Callback?,
                                     attributes: AbsMessageItem.Attributes): MessageTextItem? {
        val linkifiedBody = body.linkify(callback)

        return MessageTextItem_().apply {
            if (informationData.hasBeenEdited) {
                val spannable = annotateWithEdited(linkifiedBody, callback, informationData)
                message(spannable)
            } else {
                message(linkifiedBody)
            }
        }
                .useBigFont(linkifiedBody.length <= MAX_NUMBER_OF_EMOJI_FOR_BIG_FONT * 2 && containsOnlyEmojis(linkifiedBody.toString()))
                .searchForPills(isFormatted)
                .previewUrlRetriever(callback?.getPreviewUrlRetriever())
                .imageContentRenderer(imageContentRenderer)
                .previewUrlCallback(callback)
                .leftGuideline(avatarSizeProvider.leftGuideline)
                .attributes(attributes)
                .highlighted(highlight)
                .movementMethod(createLinkMovementMethod(callback))
    }

    private fun buildCodeBlockItem(formattedBody: CharSequence,
                                   informationData: MessageInformationData,
                                   highlight: Boolean,
                                   callback: TimelineEventController.Callback?,
                                   attributes: AbsMessageItem.Attributes): MessageBlockCodeItem? {
        return MessageBlockCodeItem_()
                .apply {
                    if (informationData.hasBeenEdited) {
                        val spannable = annotateWithEdited("", callback, informationData)
                        editedSpan(spannable)
                    }
                }
                .leftGuideline(avatarSizeProvider.leftGuideline)
                .attributes(attributes)
                .highlighted(highlight)
                .message(formattedBody)
    }

    private fun annotateWithEdited(linkifiedBody: CharSequence,
                                   callback: TimelineEventController.Callback?,
                                   informationData: MessageInformationData): SpannableStringBuilder {
        val spannable = SpannableStringBuilder()
        spannable.append(linkifiedBody)
        val editedSuffix = stringProvider.getString(R.string.edited_suffix)
        spannable.append(" ").append(editedSuffix)
        val color = colorProvider.getColorFromAttribute(R.attr.riotx_text_secondary)
        val editStart = spannable.lastIndexOf(editedSuffix)
        val editEnd = editStart + editedSuffix.length
        spannable.setSpan(
                ForegroundColorSpan(color),
                editStart,
                editEnd,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

        // Note: text size is set to 14sp
        spannable.setSpan(
                AbsoluteSizeSpan(dimensionConverter.spToPx(13)),
                editStart,
                editEnd,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                callback?.onEditedDecorationClicked(informationData)
            }

            override fun updateDrawState(ds: TextPaint) {
                // nop
            }
        },
                editStart,
                editEnd,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return spannable
    }

    private fun buildNoticeMessageItem(messageContent: MessageNoticeContent,
                                       @Suppress("UNUSED_PARAMETER")
                                       informationData: MessageInformationData,
                                       highlight: Boolean,
                                       callback: TimelineEventController.Callback?,
                                       attributes: AbsMessageItem.Attributes): MessageTextItem? {
        val formattedBody = span {
            text = messageContent.getHtmlBody()
            textColor = colorProvider.getColorFromAttribute(R.attr.riotx_text_secondary)
            textStyle = "italic"
        }

        val message = formattedBody.linkify(callback)

        return MessageTextItem_()
                .leftGuideline(avatarSizeProvider.leftGuideline)
                .previewUrlRetriever(callback?.getPreviewUrlRetriever())
                .imageContentRenderer(imageContentRenderer)
                .previewUrlCallback(callback)
                .attributes(attributes)
                .message(message)
                .highlighted(highlight)
                .movementMethod(createLinkMovementMethod(callback))
    }

    private fun buildEmoteMessageItem(messageContent: MessageEmoteContent,
                                      informationData: MessageInformationData,
                                      highlight: Boolean,
                                      callback: TimelineEventController.Callback?,
                                      attributes: AbsMessageItem.Attributes): MessageTextItem? {
        val formattedBody = SpannableStringBuilder()
        formattedBody.append("* ${informationData.memberName} ")
        formattedBody.append(messageContent.getHtmlBody())

        val message = formattedBody.linkify(callback)

        return MessageTextItem_()
                .apply {
                    if (informationData.hasBeenEdited) {
                        val spannable = annotateWithEdited(message, callback, informationData)
                        message(spannable)
                    } else {
                        message(message)
                    }
                }
                .leftGuideline(avatarSizeProvider.leftGuideline)
                .previewUrlRetriever(callback?.getPreviewUrlRetriever())
                .imageContentRenderer(imageContentRenderer)
                .previewUrlCallback(callback)
                .attributes(attributes)
                .highlighted(highlight)
                .movementMethod(createLinkMovementMethod(callback))
    }

    private fun MessageContentWithFormattedBody.getHtmlBody(): CharSequence {
        return matrixFormattedBody
                ?.let { htmlCompressor.compress(it) }
                ?.let { htmlRenderer.get().render(it, pillsPostProcessor) }
                ?: body
    }

    private fun buildRedactedItem(attributes: AbsMessageItem.Attributes,
                                  highlight: Boolean): RedactedMessageItem? {
        return RedactedMessageItem_()
                .leftGuideline(avatarSizeProvider.leftGuideline)
                .attributes(attributes)
                .highlighted(highlight)
    }

    companion object {
        private const val MAX_NUMBER_OF_EMOJI_FOR_BIG_FONT = 5
    }
}
