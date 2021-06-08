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

import com.airbnb.epoxy.TypedEpoxyController
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.resources.StringProvider
import com.cioinfotech.cychat.core.ui.list.genericFooterItem
import com.cioinfotech.cychat.features.form.formEditTextItem
import com.cioinfotech.cychat.features.form.formMultiLineEditTextItem
import javax.inject.Inject

class RoomDevToolSendFormController @Inject constructor(
        private val stringProvider: StringProvider
) : TypedEpoxyController<RoomDevToolViewState>() {

    var interactionListener: DevToolsInteractionListener? = null

    override fun buildModels(data: RoomDevToolViewState?) {
        val sendEventForm = (data?.displayMode as? RoomDevToolViewState.Mode.SendEventForm) ?: return

        genericFooterItem {
            id("topSpace")
            text("")
        }
        formEditTextItem {
            id("event_type")
            enabled(true)
            value(data.sendEventDraft?.type)
            hint(stringProvider.getString(R.string.dev_tools_form_hint_type))
            showBottomSeparator(false)
            onTextChange { text ->
                interactionListener?.processAction(RoomDevToolAction.CustomEventTypeChange(text))
            }
        }

        if (sendEventForm.isState) {
            formEditTextItem {
                id("state_key")
                enabled(true)
                value(data.sendEventDraft?.stateKey)
                hint(stringProvider.getString(R.string.dev_tools_form_hint_state_key))
                showBottomSeparator(false)
                onTextChange { text ->
                    interactionListener?.processAction(RoomDevToolAction.CustomEventStateKeyChange(text))
                }
            }
        }

        formMultiLineEditTextItem {
            id("event_content")
            enabled(true)
            value(data.sendEventDraft?.content)
            hint(stringProvider.getString(R.string.dev_tools_form_hint_event_content))
            showBottomSeparator(false)
            onTextChange { text ->
                interactionListener?.processAction(RoomDevToolAction.CustomEventContentChange(text))
            }
        }
    }
}