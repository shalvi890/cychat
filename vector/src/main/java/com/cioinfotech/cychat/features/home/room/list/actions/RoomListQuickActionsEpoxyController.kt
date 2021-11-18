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
package com.cioinfotech.cychat.features.home.room.list.actions

import android.view.View
import androidx.annotation.StringRes
import com.airbnb.epoxy.TypedEpoxyController
import com.cioinfotech.cychat.BuildConfig
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.epoxy.bottomSheetDividerItem
import com.cioinfotech.cychat.core.epoxy.bottomsheet.bottomSheetActionItem
import com.cioinfotech.cychat.core.epoxy.bottomsheet.bottomSheetRoomPreviewItem
import com.cioinfotech.cychat.core.epoxy.profiles.notifications.radioButtonItem
import com.cioinfotech.cychat.core.resources.ColorProvider
import com.cioinfotech.cychat.core.resources.StringProvider
import com.cioinfotech.cychat.features.home.AvatarRenderer
import com.cioinfotech.cychat.features.roomprofile.notifications.notificationOptions
import com.cioinfotech.cychat.features.roomprofile.notifications.notificationStateMapped
import org.matrix.android.sdk.api.session.room.notification.RoomNotificationState
import org.matrix.android.sdk.api.util.toMatrixItem
import javax.inject.Inject

/**
 * Epoxy controller for room list actions
 */
class RoomListQuickActionsEpoxyController @Inject constructor(
        private val avatarRenderer: AvatarRenderer,
        private val colorProvider: ColorProvider,
        private val stringProvider: StringProvider
) : TypedEpoxyController<RoomListQuickActionsState>() {

    var listener: Listener? = null

    override fun buildModels(state: RoomListQuickActionsState) {
        val notificationViewState = state.notificationSettingsViewState
        val roomSummary = notificationViewState.roomSummary() ?: return
        val host = this
        val isV2 = BuildConfig.USE_NOTIFICATION_SETTINGS_V2
        // V2 always shows full details as we no longer display the sheet from RoomProfile > Notifications
        val showFull = state.roomListActionsArgs.mode == RoomListActionsArgs.Mode.FULL || isV2

        if (showFull) {
            // Preview, favorite, settings
            bottomSheetRoomPreviewItem {
                id("room_preview")
                avatarRenderer(host.avatarRenderer)
                matrixItem(roomSummary.toMatrixItem())
                stringProvider(host.stringProvider)
                colorProvider(host.colorProvider)
                izLowPriority(roomSummary.isLowPriority)
                izFavorite(roomSummary.isFavorite)
                settingsClickListener { host.listener?.didSelectMenuAction(RoomListQuickActionsSharedAction.Settings(roomSummary.roomId)) }
                favoriteClickListener { host.listener?.didSelectMenuAction(RoomListQuickActionsSharedAction.Favorite(roomSummary.roomId)) }
                lowPriorityClickListener { host.listener?.didSelectMenuAction(RoomListQuickActionsSharedAction.LowPriority(roomSummary.roomId)) }
            }

            // Notifications
            bottomSheetDividerItem {
                id("notifications_separator")
            }
        }

        if (isV2) {
            notificationViewState.notificationOptions.forEach { notificationState ->
                val title = titleForNotificationState(notificationState)
                radioButtonItem {
                    id(notificationState.name)
                    titleRes(title)
                    selected(notificationViewState.notificationStateMapped() == notificationState)
                    listener {
                        host.listener?.didSelectRoomNotificationState(notificationState)
                    }
                }
            }
        } else {
            val selectedRoomState = notificationViewState.notificationState()
            RoomListQuickActionsSharedAction.NotificationsAllNoisy(roomSummary.roomId).toBottomSheetItem(0, selectedRoomState)
            RoomListQuickActionsSharedAction.NotificationsAll(roomSummary.roomId).toBottomSheetItem(1, selectedRoomState)
            RoomListQuickActionsSharedAction.NotificationsMentionsOnly(roomSummary.roomId).toBottomSheetItem(2, selectedRoomState)
            RoomListQuickActionsSharedAction.NotificationsMute(roomSummary.roomId).toBottomSheetItem(3, selectedRoomState)
        }

        if (showFull) {
            RoomListQuickActionsSharedAction.Leave(roomSummary.roomId, showIcon = !isV2).toBottomSheetItem(5)
        }
    }

    @StringRes
    private fun titleForNotificationState(notificationState: RoomNotificationState): Int? = when (notificationState) {
        RoomNotificationState.ALL_MESSAGES_NOISY -> R.string.room_settings_all_messages
        RoomNotificationState.MENTIONS_ONLY      -> R.string.room_settings_mention_and_keyword_only
        RoomNotificationState.MUTE               -> R.string.room_settings_none
        else                                     -> null
    }

    private fun RoomListQuickActionsSharedAction.toBottomSheetItem(index: Int, roomNotificationState: RoomNotificationState? = null) {
        val selected = when (this) {
            is RoomListQuickActionsSharedAction.NotificationsAllNoisy     -> roomNotificationState == RoomNotificationState.ALL_MESSAGES_NOISY
            is RoomListQuickActionsSharedAction.NotificationsAll          -> roomNotificationState == RoomNotificationState.ALL_MESSAGES
            is RoomListQuickActionsSharedAction.NotificationsMentionsOnly -> roomNotificationState == RoomNotificationState.MENTIONS_ONLY
            is RoomListQuickActionsSharedAction.NotificationsMute         -> roomNotificationState == RoomNotificationState.MUTE
            else                                                          -> false
        }
        return bottomSheetActionItem {
            id("action_$index")
            selected(selected)
            iconRes(iconResId)
            textRes(titleRes)
            destructive(this@toBottomSheetItem.destructive)
            listener(View.OnClickListener { listener?.didSelectMenuAction(this@toBottomSheetItem) })
        }
    }

    interface Listener {
        fun didSelectMenuAction(quickAction: RoomListQuickActionsSharedAction)
        fun didSelectRoomNotificationState(roomNotificationState: RoomNotificationState)
    }
}
