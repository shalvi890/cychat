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

package com.cioinfotech.cychat.core.epoxy.profiles.notifications

import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.epoxy.ClickListener
import com.cioinfotech.cychat.core.epoxy.VectorEpoxyHolder
import com.cioinfotech.cychat.core.epoxy.VectorEpoxyModel
import com.cioinfotech.cychat.core.extensions.setTextWithColoredPart

@EpoxyModelClass(layout = R.layout.item_notifications_footer)
abstract class NotificationSettingsFooterItem : VectorEpoxyModel<NotificationSettingsFooterItem.Holder>() {

    @EpoxyAttribute
    var encrypted: Boolean = false

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var clickListener: ClickListener? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        val accountSettingsString = holder.view.context.getString(R.string.room_settings_room_notifications_account_settings)
        val manageNotificationsString = holder.view.context.getString(
                R.string.room_settings_room_notifications_manage_notifications,
                accountSettingsString
        )
        val manageNotificationsBuilder = StringBuilder(manageNotificationsString)
        if (encrypted) {
            val encryptionNotice = holder.view.context.getString(R.string.room_settings_room_notifications_encryption_notice)
            manageNotificationsBuilder.appendLine().append(encryptionNotice)
        }

        holder.textView.setTextWithColoredPart(
                manageNotificationsBuilder.toString(),
                accountSettingsString,
                underline = true
        ) {
            clickListener?.invoke()
        }
    }

    class Holder : VectorEpoxyHolder() {
        val textView by bind<TextView>(R.id.footerText)
    }
}
