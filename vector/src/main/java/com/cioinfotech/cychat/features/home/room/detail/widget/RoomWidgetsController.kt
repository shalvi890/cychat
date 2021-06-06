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

package com.cioinfotech.cychat.features.home.room.detail.widget

import android.view.View
import com.airbnb.epoxy.TypedEpoxyController
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.resources.ColorProvider
import com.cioinfotech.cychat.core.resources.StringProvider
import com.cioinfotech.cychat.core.ui.list.genericButtonItem
import com.cioinfotech.cychat.core.ui.list.genericFooterItem
import org.matrix.android.sdk.api.session.widgets.model.Widget
import javax.inject.Inject

/**
 * Epoxy controller for room widgets list
 */
class RoomWidgetsController @Inject constructor(
        val stringProvider: StringProvider,
        val colorProvider: ColorProvider)
    : TypedEpoxyController<List<Widget>>() {

    var listener: Listener? = null

    override fun buildModels(widgets: List<Widget>) {
        if (widgets.isEmpty()) {
            genericFooterItem {
                id("empty")
                text(stringProvider.getString(R.string.room_no_active_widgets))
            }
        } else {
            widgets.forEach {
                roomWidgetItem {
                    id(it.widgetId)
                    widget(it)
                    widgetClicked { listener?.didSelectWidget(it) }
                }
            }
        }
        genericButtonItem {
            id("addIntegration")
            text(stringProvider.getString(R.string.room_manage_integrations))
            textColor(colorProvider.getColor(R.color.riotx_accent))
            buttonClickAction(View.OnClickListener { listener?.didSelectManageWidgets() })
        }
    }

    interface Listener {
        fun didSelectWidget(widget: Widget)
        fun didSelectManageWidgets()
    }
}
