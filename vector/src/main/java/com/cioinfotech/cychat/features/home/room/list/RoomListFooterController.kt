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

import com.airbnb.epoxy.TypedEpoxyController
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.epoxy.helpFooterItem
import com.cioinfotech.cychat.core.resources.StringProvider
import com.cioinfotech.cychat.core.resources.UserPreferencesProvider
import com.cioinfotech.cychat.features.home.RoomListDisplayMode
import com.cioinfotech.cychat.features.home.room.filtered.filteredRoomFooterItem
import javax.inject.Inject

class RoomListFooterController @Inject constructor(
        private val stringProvider: StringProvider,
        private val userPreferencesProvider: UserPreferencesProvider
) : TypedEpoxyController<RoomListViewState>() {

    var listener: RoomListListener? = null

    override fun buildModels(data: RoomListViewState?) {
        when (data?.displayMode) {
            RoomListDisplayMode.FILTERED -> {
                filteredRoomFooterItem {
                    id("filter_footer")
                    listener(listener)
                    currentFilter(data.roomFilter)
                }
            }
            else                         -> {
                if (userPreferencesProvider.shouldShowLongClickOnRoomHelp()) {
                    helpFooterItem {
                        id("long_click_help")
                        text(stringProvider.getString(R.string.help_long_click_on_room_for_more_options))
                    }
                }
            }
        }
    }
}
