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
package com.cioinfotech.cychat.features.roomprofile.alias.detail

import android.view.View
import com.airbnb.epoxy.TypedEpoxyController
import com.cioinfotech.cychat.core.epoxy.bottomsheet.bottomSheetActionItem
import com.cioinfotech.cychat.core.epoxy.dividerItem
import com.cioinfotech.cychat.core.epoxy.profiles.profileActionItem
import org.matrix.android.sdk.api.session.room.notification.RoomNotificationState
import javax.inject.Inject

/**
 * Epoxy controller for room alias actions
 */
class RoomAliasBottomSheetController @Inject constructor() : TypedEpoxyController<RoomAliasBottomSheetState>() {

    var listener: Listener? = null

    override fun buildModels(state: RoomAliasBottomSheetState) {
        profileActionItem {
            id("alias")
            title(state.alias)
            subtitle(state.matrixToLink)
            editable(false)
        }

        // Notifications
        dividerItem {
            id("aliasSeparator")
        }

        var idx = 0
        // Share
        state.matrixToLink?.let {
            RoomAliasBottomSheetSharedAction.ShareAlias(it).toBottomSheetItem(++idx)
        }

        // Action on published alias
        if (state.isPublished) {
            // Published address
            if (state.canEditCanonicalAlias) {
                if (state.isMainAlias) {
                    RoomAliasBottomSheetSharedAction.UnsetMainAlias.toBottomSheetItem(++idx)
                } else {
                    RoomAliasBottomSheetSharedAction.SetMainAlias(state.alias).toBottomSheetItem(++idx)
                }
                RoomAliasBottomSheetSharedAction.UnPublishAlias(state.alias).toBottomSheetItem(++idx)
            }
        }

        if (state.isLocal) {
            // Local address
            if (state.canEditCanonicalAlias && state.isPublished.not()) {
                // Publish
                RoomAliasBottomSheetSharedAction.PublishAlias(state.alias).toBottomSheetItem(++idx)
            }
            // Delete
            RoomAliasBottomSheetSharedAction.DeleteAlias(state.alias).toBottomSheetItem(++idx)
        }
    }

    private fun RoomAliasBottomSheetSharedAction.toBottomSheetItem(index: Int) {
        return bottomSheetActionItem {
            id("action_$index")
            iconRes(iconResId)
            textRes(titleRes)
            destructive(this@toBottomSheetItem.destructive)
            listener(View.OnClickListener { listener?.didSelectMenuAction(this@toBottomSheetItem) })
        }
    }

    interface Listener {
        fun didSelectMenuAction(quickAction: RoomAliasBottomSheetSharedAction)
    }
}
