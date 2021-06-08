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

package com.cioinfotech.cychat.features.signout.soft.epoxy

import android.widget.Button
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.epoxy.VectorEpoxyHolder
import com.cioinfotech.cychat.core.epoxy.VectorEpoxyModel
import com.cioinfotech.cychat.core.extensions.setTextOrHide

@EpoxyModelClass(layout = R.layout.item_login_red_button)
abstract class LoginRedButtonItem : VectorEpoxyModel<LoginRedButtonItem.Holder>() {

    @EpoxyAttribute var text: String? = null
    @EpoxyAttribute var listener: (() -> Unit)? = null

    override fun bind(holder: Holder) {
        super.bind(holder)

        holder.button.setTextOrHide(text)
        holder.button.setOnClickListener {
            listener?.invoke()
        }
    }

    class Holder : VectorEpoxyHolder() {
        val button by bind<Button>(R.id.itemLoginRedButton)
    }
}