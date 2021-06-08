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

package com.cioinfotech.cychat.features.crypto.verification.qrconfirmation

import com.airbnb.epoxy.EpoxyController
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.resources.ColorProvider
import com.cioinfotech.cychat.core.resources.StringProvider
import com.cioinfotech.cychat.features.crypto.verification.epoxy.bottomSheetVerificationBigImageItem
import com.cioinfotech.cychat.features.crypto.verification.epoxy.bottomSheetVerificationNoticeItem
import com.cioinfotech.cychat.features.crypto.verification.epoxy.bottomSheetVerificationWaitingItem
import org.matrix.android.sdk.api.crypto.RoomEncryptionTrustLevel
import javax.inject.Inject

class VerificationQRWaitingController @Inject constructor(
        private val stringProvider: StringProvider,
        private val colorProvider: ColorProvider
) : EpoxyController() {

    private var args: VerificationQRWaitingFragment.Args? = null

    fun update(args: VerificationQRWaitingFragment.Args) {
        this.args = args
        requestModelBuild()
    }

    override fun buildModels() {
        val params = args ?: return

        bottomSheetVerificationNoticeItem {
            id("notice")
            apply {
                notice(stringProvider.getString(R.string.qr_code_scanned_verif_waiting_notice))
            }
        }

        bottomSheetVerificationBigImageItem {
            id("image")
            roomEncryptionTrustLevel(RoomEncryptionTrustLevel.Trusted)
        }

        bottomSheetVerificationWaitingItem {
            id("waiting")
            title(stringProvider.getString(R.string.qr_code_scanned_verif_waiting, params.otherUserName))
        }
    }
}