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

package im.vector.app.features.roomprofile.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.extensions.cleanup
import com.cioinfotech.cychat.core.extensions.configureWith
import com.cioinfotech.cychat.core.platform.VectorBaseFragment
import com.cioinfotech.cychat.databinding.FragmentRoomSettingGenericBinding
import com.cioinfotech.cychat.features.home.AvatarRenderer
import com.cioinfotech.cychat.features.roomprofile.notifications.RoomNotificationSettingsAction
import com.cioinfotech.cychat.features.roomprofile.notifications.RoomNotificationSettingsController
import com.cioinfotech.cychat.features.roomprofile.notifications.RoomNotificationSettingsViewEvents
import com.cioinfotech.cychat.features.roomprofile.notifications.RoomNotificationSettingsViewModel
import com.cioinfotech.cychat.features.roomprofile.notifications.RoomNotificationSettingsViewState
import com.cioinfotech.cychat.features.settings.VectorSettingsActivity
import org.matrix.android.sdk.api.session.room.notification.RoomNotificationState
import org.matrix.android.sdk.api.util.toMatrixItem
import javax.inject.Inject

class RoomNotificationSettingsFragment @Inject constructor(
        val viewModelFactory: RoomNotificationSettingsViewModel.Factory,
        private val roomNotificationSettingsController: RoomNotificationSettingsController,
        private val avatarRenderer: AvatarRenderer
) : VectorBaseFragment<FragmentRoomSettingGenericBinding>(),
        RoomNotificationSettingsController.Callback {

    private val viewModel: RoomNotificationSettingsViewModel by fragmentViewModel()

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRoomSettingGenericBinding {
        return FragmentRoomSettingGenericBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(views.roomSettingsToolbar)
        roomNotificationSettingsController.callback = this
        views.roomSettingsRecyclerView.configureWith(roomNotificationSettingsController, hasFixedSize = true)
        setupWaitingView()
        observeViewEvents()
    }

    override fun onDestroyView() {
        views.roomSettingsRecyclerView.cleanup()
        roomNotificationSettingsController.callback = null
        super.onDestroyView()
    }

    private fun setupWaitingView() {
        views.waitingView.waitingStatusText.setText(R.string.please_wait)
        views.waitingView.waitingStatusText.isVisible = true
    }

    private fun observeViewEvents() {
        viewModel.observeViewEvents {
            when (it) {
                is RoomNotificationSettingsViewEvents.Failure -> displayErrorDialog(it.throwable)
            }
        }
    }

    override fun invalidate() = withState(viewModel) { viewState ->
        roomNotificationSettingsController.setData(viewState)
        views.waitingView.root.isVisible = viewState.isLoading
        renderRoomSummary(viewState)
    }

    override fun didSelectRoomNotificationState(roomNotificationState: RoomNotificationState) {
        viewModel.handle(RoomNotificationSettingsAction.SelectNotificationState(roomNotificationState))
    }

    override fun didSelectAccountSettingsLink() {
        navigator.openSettings(requireContext(), VectorSettingsActivity.EXTRA_DIRECT_ACCESS_NOTIFICATIONS)
    }

    private fun renderRoomSummary(state: RoomNotificationSettingsViewState) {
        state.roomSummary()?.let {
            views.roomSettingsToolbarTitleView.text = it.displayName
            avatarRenderer.render(it.toMatrixItem(), views.roomSettingsToolbarAvatarImageView)
        }
    }
}