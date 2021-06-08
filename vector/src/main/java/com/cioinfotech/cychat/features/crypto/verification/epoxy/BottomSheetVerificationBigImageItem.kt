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
package com.cioinfotech.cychat.features.crypto.verification.epoxy

import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.epoxy.VectorEpoxyHolder
import com.cioinfotech.cychat.core.epoxy.VectorEpoxyModel
import com.cioinfotech.cychat.core.ui.views.ShieldImageView
import org.matrix.android.sdk.api.crypto.RoomEncryptionTrustLevel

/**
 * A action for bottom sheet.
 */
@EpoxyModelClass(layout = R.layout.item_verification_big_image)
abstract class BottomSheetVerificationBigImageItem : VectorEpoxyModel<BottomSheetVerificationBigImageItem.Holder>() {

    @EpoxyAttribute
    lateinit var roomEncryptionTrustLevel: RoomEncryptionTrustLevel

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.image.render(roomEncryptionTrustLevel)
    }

    class Holder : VectorEpoxyHolder() {
        val image by bind<ShieldImageView>(R.id.itemVerificationBigImage)
    }
}