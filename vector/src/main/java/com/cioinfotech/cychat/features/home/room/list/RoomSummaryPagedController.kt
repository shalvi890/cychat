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

package com.cioinfotech.cychat.features.home.room.list

import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.cioinfotech.cychat.core.utils.createUIHandler
import com.cioinfotech.cychat.features.home.RoomListDisplayMode
import org.matrix.android.sdk.api.session.room.members.ChangeMembershipState
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import javax.inject.Inject

class RoomSummaryPagedControllerFactory @Inject constructor(
        private val roomSummaryItemFactory: RoomSummaryItemFactory
) {

    fun createRoomSummaryPagedController(displayMode: RoomListDisplayMode): RoomSummaryPagedController {
        return RoomSummaryPagedController(roomSummaryItemFactory, displayMode)
    }
}

class RoomSummaryPagedController(
        private val roomSummaryItemFactory: RoomSummaryItemFactory,
        private val displayMode: RoomListDisplayMode
) : PagedListEpoxyController<RoomSummary>(
        // Important it must match the PageList builder notify Looper
        modelBuildingHandler = createUIHandler()
) {

    var listener: RoomListListener? = null

    private var roomChangeMembershipStates: Map<String, ChangeMembershipState>? = null
        set(value) {
            field = value
            // ideally we could search for visible models and update only those
            requestForcedModelBuild()
        }

    override fun buildItemModel(currentPosition: Int, item: RoomSummary?): EpoxyModel<*> {
        // for place holder if enabled
        item ?: return roomSummaryItemFactory.createRoomItem(
                roomSummary = RoomSummary(
                        roomId = "null_item_pos_$currentPosition",
                        name = "",
                        encryptionEventTs = null,
                        isEncrypted = false,
                        typingUsers = emptyList()
                ),
                selectedRoomIds = emptySet(),
                onClick = null,
                onLongClick = null,
                onProfileClick = null
        )

        return roomSummaryItemFactory.create(item, roomChangeMembershipStates.orEmpty(), emptySet(), listener, displayMode)
    }
}
