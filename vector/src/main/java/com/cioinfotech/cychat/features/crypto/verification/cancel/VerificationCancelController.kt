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

package com.cioinfotech.cychat.features.crypto.verification.cancel

import androidx.core.text.toSpannable
import com.airbnb.epoxy.EpoxyController
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.epoxy.dividerItem
import com.cioinfotech.cychat.core.resources.ColorProvider
import com.cioinfotech.cychat.core.resources.StringProvider
import com.cioinfotech.cychat.core.utils.colorizeMatchingText
import com.cioinfotech.cychat.features.crypto.verification.VerificationBottomSheetViewState
import com.cioinfotech.cychat.features.crypto.verification.epoxy.bottomSheetVerificationActionItem
import com.cioinfotech.cychat.features.crypto.verification.epoxy.bottomSheetVerificationNoticeItem
import javax.inject.Inject

class VerificationCancelController @Inject constructor(
        private val stringProvider: StringProvider,
        private val colorProvider: ColorProvider
) : EpoxyController() {

    var listener: Listener? = null

    private var viewState: VerificationBottomSheetViewState? = null

    fun update(viewState: VerificationBottomSheetViewState) {
        this.viewState = viewState
        requestModelBuild()
    }

    override fun buildModels() {
        val state = viewState ?: return

        if (state.isMe) {
            if (state.currentDeviceCanCrossSign) {
                bottomSheetVerificationNoticeItem {
                    id("notice")
                    notice(stringProvider.getString(R.string.verify_cancel_self_verification_from_trusted))
                }
            } else {
                bottomSheetVerificationNoticeItem {
                    id("notice")
                    notice(stringProvider.getString(R.string.verify_cancel_self_verification_from_untrusted))
                }
            }
        } else {
            val otherUserID = state.otherUserMxItem?.id ?: ""
            val otherDisplayName = state.otherUserMxItem?.displayName ?: ""
            bottomSheetVerificationNoticeItem {
                id("notice")
                notice(
                        stringProvider.getString(R.string.verify_cancel_other, otherDisplayName, otherUserID)
                                .toSpannable()
                                .colorizeMatchingText(otherUserID, colorProvider.getColorFromAttribute(R.attr.vctr_notice_text_color))
                )
            }
        }

        dividerItem {
            id("sep0")
        }

        bottomSheetVerificationActionItem {
            id("cancel")
            title(stringProvider.getString(R.string.skip))
            titleColor(colorProvider.getColor(R.color.riotx_destructive_accent))
            iconRes(R.drawable.ic_arrow_right)
            iconColor(colorProvider.getColor(R.color.riotx_destructive_accent))
            listener { listener?.onTapCancel() }
        }

        dividerItem {
            id("sep1")
        }

        bottomSheetVerificationActionItem {
            id("continue")
            title(stringProvider.getString(R.string._continue))
            titleColor(colorProvider.getColor(R.color.riotx_positive_accent))
            iconRes(R.drawable.ic_arrow_right)
            iconColor(colorProvider.getColor(R.color.riotx_positive_accent))
            listener { listener?.onTapContinue() }
        }
    }

    interface Listener {
        fun onTapCancel()
        fun onTapContinue()
    }
}
