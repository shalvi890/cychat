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
package com.cioinfotech.cychat.features.form

import android.widget.Button
import androidx.annotation.StringRes
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.epoxy.ClickListener
import com.cioinfotech.cychat.core.epoxy.VectorEpoxyHolder
import com.cioinfotech.cychat.core.epoxy.onClick
import com.cioinfotech.cychat.core.extensions.setTextOrHide

@EpoxyModelClass(layout = R.layout.item_form_submit_button)
abstract class FormSubmitButtonItem : EpoxyModelWithHolder<FormSubmitButtonItem.Holder>() {

    @EpoxyAttribute
    var enabled: Boolean = true

    @EpoxyAttribute
    var buttonTitle: String? = null

    @EpoxyAttribute
    @StringRes
    var buttonTitleId: Int? = null

    @EpoxyAttribute
    var buttonClickListener: ClickListener? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        if (buttonTitleId != null) {
            holder.button.setText(buttonTitleId!!)
        } else {
            holder.button.setTextOrHide(buttonTitle)
        }

        holder.button.isEnabled = enabled
        holder.button.onClick(buttonClickListener)
    }

    class Holder : VectorEpoxyHolder() {
        val button by bind<Button>(R.id.form_submit_button)
    }
}
