/*
 * Copyright 2019 New Vector Ltd
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

package im.vector.app.features.home.room.detail.timeline

import androidx.annotation.ColorInt
import im.vector.app.R
import im.vector.app.core.resources.ColorProvider
import im.vector.app.features.home.room.detail.timeline.helper.MatrixItemColorProvider
import im.vector.app.features.settings.VectorPreferences
import org.matrix.android.sdk.api.session.room.send.SendState
import org.matrix.android.sdk.api.util.MatrixItem
import javax.inject.Inject

class MessageColorProvider @Inject constructor(
        private val colorProvider: ColorProvider,
        private val matrixItemColorProvider: MatrixItemColorProvider,
        private val vectorPreferences: VectorPreferences) {

    @ColorInt
    fun getMemberNameTextColor(matrixItem: MatrixItem): Int {
        return matrixItemColorProvider.getColor(matrixItem)
    }

    @ColorInt
    fun getMessageTextColor(sendState: SendState): Int {
        return when (sendState) {
            // SendStates, in the classical order they will occur
            SendState.UNKNOWN,
            SendState.UNSENT                 -> colorProvider.getColorFromAttribute(R.attr.riotx_text_secondary_contrast)
            SendState.ENCRYPTING             -> colorProvider.getColorFromAttribute(R.attr.riotx_text_secondary_contrast)
            SendState.SENDING                -> colorProvider.getColorFromAttribute(R.attr.riotx_text_secondary_contrast)
            SendState.SENT,
            SendState.SYNCED                 -> colorProvider.getColorFromAttribute(R.attr.riotx_text_primary_contrast)
            SendState.UNDELIVERED,
            SendState.FAILED_UNKNOWN_DEVICES -> colorProvider.getColorFromAttribute(R.attr.riotx_text_secondary_contrast)
            else                             -> colorProvider.getColorFromAttribute(R.attr.riotx_text_primary_contrast)
        }
        //        if (vectorPreferences.developerMode()) {
//        } else {
//            // When not in developer mode, we use only one color
//            colorProvider.getColorFromAttribute(R.attr.vctr_message_text_color)
//        }
    }
}
