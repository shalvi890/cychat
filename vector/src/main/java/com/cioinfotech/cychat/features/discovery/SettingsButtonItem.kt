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
package com.cioinfotech.cychat.features.discovery

import android.widget.Button
import androidx.annotation.StringRes
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.epoxy.ClickListener
import com.cioinfotech.cychat.core.epoxy.VectorEpoxyHolder
import com.cioinfotech.cychat.core.epoxy.attributes.ButtonStyle
import com.cioinfotech.cychat.core.epoxy.onClick
import com.cioinfotech.cychat.core.extensions.setTextOrHide
import com.cioinfotech.cychat.core.resources.ColorProvider

@EpoxyModelClass(layout = R.layout.item_settings_button)
abstract class SettingsButtonItem : EpoxyModelWithHolder<SettingsButtonItem.Holder>() {

    @EpoxyAttribute
    lateinit var colorProvider: ColorProvider

    @EpoxyAttribute
    var buttonTitle: String? = null

    @EpoxyAttribute
    @StringRes
    var buttonTitleId: Int? = null

    @EpoxyAttribute
    var buttonStyle: ButtonStyle = ButtonStyle.POSITIVE

    @EpoxyAttribute
    var buttonClickListener: ClickListener? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        if (buttonTitleId != null) {
            holder.button.setText(buttonTitleId!!)
        } else {
            holder.button.setTextOrHide(buttonTitle)
        }

        when (buttonStyle) {
            ButtonStyle.POSITIVE    -> {
                holder.button.setTextColor(colorProvider.getColor(R.color.riotx_accent))
            }
            ButtonStyle.DESTRUCTIVE -> {
                holder.button.setTextColor(colorProvider.getColor(R.color.riotx_destructive_accent))
            }
        }

        holder.button.onClick(buttonClickListener)
    }

    class Holder : VectorEpoxyHolder() {
        val button by bind<Button>(R.id.settings_item_button)
    }
}
