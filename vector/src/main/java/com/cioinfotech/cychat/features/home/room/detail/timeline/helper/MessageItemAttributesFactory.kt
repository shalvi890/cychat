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
package com.cioinfotech.cychat.features.home.room.detail.timeline.helper

import com.cioinfotech.cychat.EmojiCompatFontProvider
import com.cioinfotech.cychat.core.utils.DebouncedClickListener
import com.cioinfotech.cychat.features.home.AvatarRenderer
import com.cioinfotech.cychat.features.home.room.detail.timeline.MessageColorProvider
import com.cioinfotech.cychat.features.home.room.detail.timeline.TimelineEventController
import com.cioinfotech.cychat.features.home.room.detail.timeline.item.AbsMessageItem
import com.cioinfotech.cychat.features.home.room.detail.timeline.item.MessageInformationData
import javax.inject.Inject

class MessageItemAttributesFactory @Inject constructor(
        private val avatarRenderer: AvatarRenderer,
        private val messageColorProvider: MessageColorProvider,
        private val avatarSizeProvider: AvatarSizeProvider,
        private val emojiCompatFontProvider: EmojiCompatFontProvider) {

    fun create(messageContent: Any?,
               informationData: MessageInformationData,
               callback: TimelineEventController.Callback?): AbsMessageItem.Attributes {
        return AbsMessageItem.Attributes(
                avatarSize = avatarSizeProvider.avatarSize,
                informationData = informationData,
                avatarRenderer = avatarRenderer,
                messageColorProvider = messageColorProvider,
                itemLongClickListener = { view ->
                    callback?.onEventLongClicked(informationData, messageContent, view) ?: false
                },
                itemClickListener = DebouncedClickListener({ view ->
                    callback?.onEventCellClicked(informationData, messageContent, view)
                }),
                memberClickListener = DebouncedClickListener({
                    callback?.onMemberNameClicked(informationData)
                }),
                reactionPillCallback = callback,
                avatarCallback = callback,
                readReceiptsCallback = callback,
                emojiTypeFace = emojiCompatFontProvider.typeface
        )
    }
}
