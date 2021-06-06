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

package com.cioinfotech.cychat.core.epoxy.profiles

import androidx.annotation.DrawableRes
import com.airbnb.epoxy.EpoxyController
import com.cioinfotech.cychat.core.epoxy.ClickListener
import com.cioinfotech.cychat.core.epoxy.dividerItem
import com.cioinfotech.cychat.features.home.AvatarRenderer
import org.matrix.android.sdk.api.util.MatrixItem

fun EpoxyController.buildProfileSection(title: String) {
    profileSectionItem {
        id("section_$title")
        title(title)
    }
}

fun EpoxyController.buildProfileAction(
        id: String,
        title: String,
        dividerColor: Int,
        subtitle: String? = null,
        editable: Boolean = true,
        @DrawableRes icon: Int = 0,
        tintIcon: Boolean = true,
        @DrawableRes editableRes: Int? = null,
        destructive: Boolean = false,
        divider: Boolean = true,
        action: ClickListener? = null,
        @DrawableRes accessory: Int = 0,
        accessoryMatrixItem: MatrixItem? = null,
        avatarRenderer: AvatarRenderer? = null
) {
    profileActionItem {
        iconRes(icon)
        tintIcon(tintIcon)
        id("action_$id")
        subtitle(subtitle)
        editable(editable)
        editableRes?.let { editableRes(editableRes) }
        destructive(destructive)
        title(title)
        accessoryRes(accessory)
        accessoryMatrixItem(accessoryMatrixItem)
        avatarRenderer(avatarRenderer)
        listener(action)
    }

    if (divider) {
        dividerItem {
            id("divider_$title")
            color(dividerColor)
        }
    }
}
