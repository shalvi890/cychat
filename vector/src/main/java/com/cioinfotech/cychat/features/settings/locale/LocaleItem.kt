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

package com.cioinfotech.cychat.features.settings.locale

import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.epoxy.ClickListener
import com.cioinfotech.cychat.core.epoxy.VectorEpoxyHolder
import com.cioinfotech.cychat.core.epoxy.VectorEpoxyModel
import com.cioinfotech.cychat.core.epoxy.onClick
import com.cioinfotech.cychat.core.extensions.setTextOrHide

@EpoxyModelClass(layout = R.layout.item_locale)
abstract class LocaleItem : VectorEpoxyModel<LocaleItem.Holder>() {

    @EpoxyAttribute var title: String? = null
    @EpoxyAttribute var subtitle: String? = null
    @EpoxyAttribute var clickListener: ClickListener? = null

    override fun bind(holder: Holder) {
        super.bind(holder)

        holder.view.onClick { clickListener?.invoke() }
        holder.titleView.setTextOrHide(title)
        holder.subtitleView.setTextOrHide(subtitle)
    }

    class Holder : VectorEpoxyHolder() {
        val titleView by bind<TextView>(R.id.localeTitle)
        val subtitleView by bind<TextView>(R.id.localeSubtitle)
    }
}
