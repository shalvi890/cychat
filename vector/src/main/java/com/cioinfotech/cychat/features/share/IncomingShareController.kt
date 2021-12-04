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

package com.cioinfotech.cychat.features.share

import com.airbnb.epoxy.TypedEpoxyController
import com.airbnb.mvrx.Incomplete
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.epoxy.loadingItem
import com.cioinfotech.cychat.core.epoxy.noResultItem
import com.cioinfotech.cychat.core.resources.StringProvider
import com.cioinfotech.cychat.features.home.room.list.RoomSummaryItemFactory
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import javax.inject.Inject

class IncomingShareController @Inject constructor(private val roomSummaryItemFactory: RoomSummaryItemFactory,
                                                  private val stringProvider: StringProvider) : TypedEpoxyController<IncomingShareViewState>() {

    interface Callback {
        fun onRoomClicked(roomSummary: RoomSummary)
        fun onRoomLongClicked(roomSummary: RoomSummary): Boolean
        fun onRoomProfileClicked(room: RoomSummary)
    }

    var callback: Callback? = null

    override fun buildModels(data: IncomingShareViewState) {
        if (data.sharedData == null || data.filteredRoomSummaries is Incomplete) {
            loadingItem {
                id("loading")
            }
            return
        }
        val roomSummaries = data.filteredRoomSummaries()
        if (roomSummaries.isNullOrEmpty()) {
            noResultItem {
                id("no_result")
                text(stringProvider.getString(R.string.no_result_placeholder))
            }
        } else {
            roomSummaries.forEach { roomSummary ->
                roomSummaryItemFactory
                        .createRoomItem(roomSummary, data.selectedRoomIds, callback?.let { it::onRoomClicked }, callback?.let { it::onRoomLongClicked }, callback?.let { it::onRoomProfileClicked })
                        .addTo(this)
            }
        }
    }
}
