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

package com.cioinfotech.cychat.features.devtools

import android.view.View
import com.airbnb.epoxy.EpoxyController
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.resources.StringProvider
import com.cioinfotech.cychat.core.ui.list.genericButtonItem
import javax.inject.Inject

class RoomDevToolRootController @Inject constructor(
        private val stringProvider: StringProvider
) : EpoxyController() {

    init {
        requestModelBuild()
    }

    var interactionListener: DevToolsInteractionListener? = null

    override fun buildModels() {
        genericButtonItem {
            id("explore")
            text(stringProvider.getString(R.string.dev_tools_explore_room_state))
            buttonClickAction(View.OnClickListener {
                interactionListener?.processAction(RoomDevToolAction.ExploreRoomState)
            })
        }
        genericButtonItem {
            id("send")
            text(stringProvider.getString(R.string.dev_tools_send_custom_event))
            buttonClickAction(View.OnClickListener {
                interactionListener?.processAction(RoomDevToolAction.SendCustomEvent(false))
            })
        }
        genericButtonItem {
            id("send_state")
            text(stringProvider.getString(R.string.dev_tools_send_state_event))
            buttonClickAction(View.OnClickListener {
                interactionListener?.processAction(RoomDevToolAction.SendCustomEvent(true))
            })
        }
    }
}