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

package com.cioinfotech.cychat.features.media

import com.cioinfotech.cychat.core.date.VectorDateFormatter
import com.cioinfotech.cychat.core.resources.StringProvider
import com.cioinfotech.lib.attachmentviewer.AttachmentInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.android.sdk.api.session.file.FileService
import org.matrix.android.sdk.api.session.room.Room
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import org.matrix.android.sdk.api.util.MimeTypes
import java.io.File

class DataAttachmentRoomProvider(
        attachments: List<AttachmentData>,
        private val room: Room?,
        imageContentRenderer: ImageContentRenderer,
        dateFormatter: VectorDateFormatter,
        fileService: FileService,
        stringProvider: StringProvider
) : BaseAttachmentProvider<AttachmentData>(attachments, imageContentRenderer, fileService, dateFormatter, stringProvider) {

    override fun getAttachmentInfoAt(position: Int): AttachmentInfo {
        return getItem(position).let {
            when (it) {
                is ImageContentRenderer.Data -> {
                    if (it.mimeType == MimeTypes.Gif) {
                        AttachmentInfo.AnimatedImage(
                                uid = it.eventId,
                                url = it.url ?: "",
                                data = it
                        )
                    } else {
                        AttachmentInfo.Image(
                                uid = it.eventId,
                                url = it.url ?: "",
                                data = it
                        )
                    }
                }
                is VideoContentRenderer.Data -> {
                    AttachmentInfo.Video(
                            uid = it.eventId,
                            url = it.url ?: "",
                            data = it,
                            thumbnail = AttachmentInfo.Image(
                                    uid = it.eventId,
                                    url = it.thumbnailMediaData.url ?: "",
                                    data = it.thumbnailMediaData
                            )
                    )
                }
                else                         -> throw IllegalArgumentException()
            }
        }
    }

    override fun getTimelineEventAtPosition(position: Int): TimelineEvent? {
        val item = getItem(position)
        return room?.getTimeLineEvent(item.eventId)
    }

    override fun getFileForSharing(position: Int, callback: (File?) -> Unit) {
        val item = getItem(position)
        GlobalScope.launch {
            val result = runCatching {
                fileService.downloadFile(
                        fileName = item.filename,
                        mimeType = item.mimeType,
                        url = item.url,
                        elementToDecrypt = item.elementToDecrypt
                )
            }
            withContext(Dispatchers.Main) {
                callback(result.getOrNull())
            }
        }
    }
}