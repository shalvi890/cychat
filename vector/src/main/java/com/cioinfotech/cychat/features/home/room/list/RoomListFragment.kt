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

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.OnModelBuildFinishedListener
import com.airbnb.mvrx.args
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.epoxy.LayoutManagerStateRestorer
import com.cioinfotech.cychat.core.extensions.cleanup
import com.cioinfotech.cychat.core.extensions.exhaustive
import com.cioinfotech.cychat.core.platform.OnBackPressed
import com.cioinfotech.cychat.core.platform.StateView
import com.cioinfotech.cychat.core.platform.VectorBaseFragment
import com.cioinfotech.cychat.core.resources.UserPreferencesProvider
import com.cioinfotech.cychat.databinding.FragmentRoomListBinding
import com.cioinfotech.cychat.features.home.AvatarRenderer
import com.cioinfotech.cychat.features.home.HomeActivity.Companion.isOneToOneChatOpen
import com.cioinfotech.cychat.features.home.RoomListDisplayMode
import com.cioinfotech.cychat.features.home.room.adapter.RoomHomeConcatAdapter
import com.cioinfotech.cychat.features.home.room.filtered.FilteredRoomFooterItem
import com.cioinfotech.cychat.features.home.room.list.actions.RoomListActionsArgs
import com.cioinfotech.cychat.features.home.room.list.actions.RoomListQuickActionsBottomSheet
import com.cioinfotech.cychat.features.home.room.list.actions.RoomListQuickActionsSharedAction
import com.cioinfotech.cychat.features.home.room.list.actions.RoomListQuickActionsSharedActionViewModel
import com.cioinfotech.cychat.features.notifications.NotificationDrawerManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.parcelize.Parcelize
import org.matrix.android.sdk.api.extensions.orTrue
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.session.room.model.tag.RoomTag
import org.matrix.android.sdk.api.session.room.notification.RoomNotificationState
import javax.inject.Inject

@Parcelize
data class RoomListParams(
        val displayMode: RoomListDisplayMode
) : Parcelable

class RoomListFragment @Inject constructor(
        private val pagedControllerFactory: RoomSummaryPagedControllerFactory,
        val roomListViewModelFactory: RoomListViewModel.Factory,
        private val notificationDrawerManager: NotificationDrawerManager,
        private val footerController: RoomListFooterController,
        private val userPreferencesProvider: UserPreferencesProvider,
        private val avatarRenderer: AvatarRenderer
) : VectorBaseFragment<FragmentRoomListBinding>(),
        RoomListListener,
        OnBackPressed,
        FilteredRoomFooterItem.Listener,
        FilteredRoomFooterItem.FilteredRoomFooterItemListener {

    private var modelBuildListener: OnModelBuildFinishedListener? = null
    private lateinit var sharedActionViewModel: RoomListQuickActionsSharedActionViewModel
    private val roomListParams: RoomListParams by args()
    private val roomListViewModel: RoomListViewModel by fragmentViewModel()
    private lateinit var stateRestorer: LayoutManagerStateRestorer

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRoomListBinding {
        return FragmentRoomListBinding.inflate(inflater, container, false)
    }

    data class SectionKey(
            val name: String,
            val isExpanded: Boolean,
            val notifyOfLocalEcho: Boolean
    )

    data class SectionAdapterInfo(
            var section: SectionKey,
            val headerHeaderAdapter: SectionHeaderAdapter,
            val contentAdapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>
    )

    private val adapterInfosList = mutableListOf<SectionAdapterInfo>()
    private var concatAdapter: ConcatAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        views.stateView.contentView = views.roomListView
        views.stateView.state = StateView.State.Loading
        setupCreateRoomButton()
        setupRecyclerView()
        sharedActionViewModel = activityViewModelProvider.get(RoomListQuickActionsSharedActionViewModel::class.java)
        roomListViewModel.observeViewEvents {
            when (it) {
                is RoomListViewEvents.Loading    -> showLoading(it.message)
                is RoomListViewEvents.Failure    -> showFailure(it.throwable)
                is RoomListViewEvents.SelectRoom -> handleSelectRoom(it)
                is RoomListViewEvents.Done       -> Unit
            }.exhaustive
        }

        views.createChatFabMenu.listener = this

        sharedActionViewModel
                .observe()
                .subscribe { handleQuickActions(it) }
                .disposeOnDestroyView()

        //showAddRoleDialog()

//        roomListViewModel.selectSubscribe(viewLifecycleOwner, RoomListViewState::roomMembershipChanges) { ms ->
//            // it's for invites local echo
//            adapterInfosList.filter { it.section.notifyOfLocalEcho }
//                    .onEach {
//                        it.roomChangeMembershipStates = ms
//                    }
//        }
    }

 /*   private  fun showAddRoleDialog(){

        AlertDialog.Builder(requireContext())
                .setTitle("Add user role")
                .setMessage("now you can add using Add user role, also you can add role later from Settings-Manage Profile-Request for new role")
                .setPositiveButton(R.string.add_new_role) { _, _ ->
                   // roomListViewModel.handle(RoomListAction.LeaveRoom(roomId))
                }
                .setNegativeButton(R.string.not_now, null)
                .show()
    }*/
    private fun refreshCollapseStates() {
        var contentInsertIndex = 1
        roomListViewModel.sections.forEachIndexed { index, roomsSection ->
            val actualBlock = adapterInfosList[index]
            val isRoomSectionExpanded = roomsSection.isExpanded.value.orTrue()
            if (actualBlock.section.isExpanded && !isRoomSectionExpanded) {
                // we have to remove the content adapter
                concatAdapter?.removeAdapter(actualBlock.contentAdapter)
            } else if (!actualBlock.section.isExpanded && isRoomSectionExpanded) {
                // we must add it back!
                concatAdapter?.addAdapter(contentInsertIndex, actualBlock.contentAdapter)
            }
            contentInsertIndex = if (isRoomSectionExpanded) {
                contentInsertIndex + 2
            } else {
                contentInsertIndex + 1
            }
            actualBlock.section = actualBlock.section.copy(
                    isExpanded = isRoomSectionExpanded
            )
            actualBlock.headerHeaderAdapter.updateSection(
                    actualBlock.headerHeaderAdapter.roomsSectionData.copy(isExpanded = isRoomSectionExpanded)
            )
        }
    }

    override fun showFailure(throwable: Throwable) {
        showErrorInSnackbar(throwable)
    }

    override fun onDestroyView() {
//        adapterInfosList.onEach { it.removeModelBuildListener(modelBuildListener) }
        adapterInfosList.clear()
        modelBuildListener = null
        views.roomListView.cleanup()
        footerController.listener = null
        stateRestorer.clear()
        views.createChatFabMenu.listener = null
        concatAdapter = null
        super.onDestroyView()
    }

    private fun handleSelectRoom(event: RoomListViewEvents.SelectRoom) {
        navigator.openRoom(requireActivity(), event.roomSummary.roomId)
    }

    private fun setupCreateRoomButton() {
        when (roomListParams.displayMode) {
//            RoomListDisplayMode.NOTIFICATIONS,
//            RoomListDisplayMode.HOME   -> views.createChatFabMenu.isVisible = true
            RoomListDisplayMode.PEOPLE -> views.createChatRoomButton.isVisible = true
            RoomListDisplayMode.ROOMS  -> views.createGroupRoomButton.isVisible = true
            else                       -> Unit // No button in this mode
        }

        views.createChatRoomButton.debouncedClicks {
            fabCreateDirectChat()
        }
        views.createGroupRoomButton.debouncedClicks {
            fabOpenRoomDirectory()
        }

        // Hide FAB when list is scrolling
        views.roomListView.addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        views.createChatFabMenu.removeCallbacks(showFabRunnable)
                        when (newState) {
                            RecyclerView.SCROLL_STATE_IDLE     -> {
                                views.createChatFabMenu.postDelayed(showFabRunnable, 250)
                            }
                            RecyclerView.SCROLL_STATE_DRAGGING,
                            RecyclerView.SCROLL_STATE_SETTLING -> {
                                when (roomListParams.displayMode) {
                                    RoomListDisplayMode.NOTIFICATIONS -> views.createChatFabMenu.hide()
                                    RoomListDisplayMode.PEOPLE        -> views.createChatRoomButton.hide()
                                    RoomListDisplayMode.ROOMS         -> views.createGroupRoomButton.hide()
                                    else                              -> Unit
                                }
                            }
                        }
                    }
                })
    }

    fun filterRoomsWith(filter: String) {
        // Scroll the list to top
        views.roomListView.scrollToPosition(0)

        roomListViewModel.handle(RoomListAction.FilterWith(filter))
    }

    // FilteredRoomFooterItem.Listener
    override fun createRoom(initialName: String) {
        navigator.openCreateRoom(requireActivity(), initialName)
    }

    override fun createDirectChat() {
        navigator.openCreateDirectRoom(requireActivity())
    }

    override fun openRoomDirectory(initialFilter: String) {
        navigator.openRoomDirectory(requireActivity(), initialFilter)
    }

    override fun fabCreateDirectChat() {
        isOneToOneChatOpen = true
        navigator.openCreateDirectRoom(requireActivity())
    }

    override fun fabOpenRoomDirectory() {
        isOneToOneChatOpen = false
        navigator.openRoomDirectory(requireActivity(), "")
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        stateRestorer = LayoutManagerStateRestorer(layoutManager).register()
        views.roomListView.layoutManager = layoutManager
        views.roomListView.itemAnimator = RoomListAnimator()
        layoutManager.recycleChildrenOnDetach = true

        modelBuildListener = OnModelBuildFinishedListener { it.dispatchTo(stateRestorer) }

        val concatAdapter = ConcatAdapter()

        roomListViewModel.sections.forEach { section ->
            val sectionAdapter = SectionHeaderAdapter(
                    { roomListViewModel.handle(RoomListAction.ToggleSection(section)) },
                    roomListParams.displayMode != RoomListDisplayMode.HOME
            ).also {
                it.updateSection(SectionHeaderAdapter.RoomsSectionData(section.sectionName))
            }

            val contentAdapter = pagedControllerFactory.createRoomSummaryPagedController(roomListParams.displayMode)
                    .also { controller ->
                        section.livePages.observe(viewLifecycleOwner) { pl ->
                            controller.submitList(pl)
                            sectionAdapter.updateSection(sectionAdapter.roomsSectionData.copy(isHidden = pl.isEmpty()))
                            checkEmptyState()
                        }
                        section.notificationCount.observe(viewLifecycleOwner) { counts ->
                            sectionAdapter.updateSection(sectionAdapter.roomsSectionData.copy(
                                    notificationCount = counts.totalCount,
                                    isHighlighted = counts.isHighlight
                            ))
                        }
                        section.isExpanded.observe(viewLifecycleOwner) {
                            refreshCollapseStates()
                        }
                        controller.listener = this
                    }
            adapterInfosList.add(
                    SectionAdapterInfo(
                            SectionKey(
                                    name = section.sectionName,
                                    isExpanded = section.isExpanded.value.orTrue(),
                                    notifyOfLocalEcho = section.notifyOfLocalEcho
                            ),
                            sectionAdapter,
                            if (roomListParams.displayMode != RoomListDisplayMode.HOME)
                                contentAdapter.adapter else
                                RoomHomeConcatAdapter(requireContext(), contentAdapter.adapter)
                    )
            )
            concatAdapter.addAdapter(sectionAdapter)
            concatAdapter.addAdapter(if (roomListParams.displayMode != RoomListDisplayMode.HOME)
                contentAdapter.adapter else
                RoomHomeConcatAdapter(requireContext(), contentAdapter.adapter))
        }

        // Add the footer controller
        footerController.listener = this
        concatAdapter.addAdapter(footerController.adapter)

        this.concatAdapter = concatAdapter
        views.roomListView.adapter = concatAdapter
    }

    private val showFabRunnable = Runnable {
        if (isAdded) {
            when (roomListParams.displayMode) {
                RoomListDisplayMode.NOTIFICATIONS -> views.createChatFabMenu.show()
                RoomListDisplayMode.PEOPLE        -> views.createChatRoomButton.show()
                RoomListDisplayMode.ROOMS         -> views.createGroupRoomButton.show()
                else                              -> Unit
            }
        }
    }

    private fun handleQuickActions(quickAction: RoomListQuickActionsSharedAction) {
        when (quickAction) {
            is RoomListQuickActionsSharedAction.NotificationsAllNoisy     -> {
                roomListViewModel.handle(RoomListAction.ChangeRoomNotificationState(quickAction.roomId, RoomNotificationState.ALL_MESSAGES_NOISY))
            }
            is RoomListQuickActionsSharedAction.NotificationsAll          -> {
                roomListViewModel.handle(RoomListAction.ChangeRoomNotificationState(quickAction.roomId, RoomNotificationState.ALL_MESSAGES))
            }
            is RoomListQuickActionsSharedAction.NotificationsMentionsOnly -> {
                roomListViewModel.handle(RoomListAction.ChangeRoomNotificationState(quickAction.roomId, RoomNotificationState.MENTIONS_ONLY))
            }
            is RoomListQuickActionsSharedAction.NotificationsMute         -> {
                roomListViewModel.handle(RoomListAction.ChangeRoomNotificationState(quickAction.roomId, RoomNotificationState.MUTE))
            }
            is RoomListQuickActionsSharedAction.Settings                  -> {
                navigator.openRoomProfile(requireActivity(), quickAction.roomId)
            }
            is RoomListQuickActionsSharedAction.Favorite                  -> {
                roomListViewModel.handle(RoomListAction.ToggleTag(quickAction.roomId, RoomTag.ROOM_TAG_FAVOURITE))
            }
            is RoomListQuickActionsSharedAction.LowPriority               -> {
                roomListViewModel.handle(RoomListAction.ToggleTag(quickAction.roomId, RoomTag.ROOM_TAG_LOW_PRIORITY))
            }
            is RoomListQuickActionsSharedAction.Leave                     -> {
                promptLeaveRoom(quickAction.roomId)
            }
        }.exhaustive
    }

    private fun promptLeaveRoom(roomId: String) {
        val isPublicRoom = roomListViewModel.isPublicRoom(roomId)
        val message = buildString {
            append(getString(R.string.room_participants_leave_prompt_msg))
            if (!isPublicRoom) {
                append("\n\n")
              //  append(getString(R.string.room_participants_leave_private_warning))
            }
        }

       // Snackbar.make(requireView(), "This functionality is coming soon", BaseTransientBottomBar.LENGTH_SHORT).show()

        AlertDialog.Builder(requireContext())
                .setTitle(if (isOneToOneChatOpen) R.string.direct_room_profile_section_more_leave else R.string.room_participants_leave_prompt_title)
                .setMessage(message)
                .setPositiveButton(R.string.leave) { _, _ ->
                    roomListViewModel.handle(RoomListAction.LeaveRoom(roomId))
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
    }

    override fun invalidate() = withState(roomListViewModel) { state ->
        footerController.setData(state)
    }

    private fun checkEmptyState() {
        val hasNoRoom = adapterInfosList.all { it.headerHeaderAdapter.roomsSectionData.isHidden }
        if (hasNoRoom) {
            val emptyState = when (roomListParams.displayMode) {
                RoomListDisplayMode.NOTIFICATIONS -> {
                    StateView.State.Empty(
                            title = getString(R.string.room_list_catchup_empty_title),
                            image = ContextCompat.getDrawable(requireContext(), R.drawable.ic_noun_party_popper),
                            message = getString(R.string.room_list_catchup_empty_body))
                }
                RoomListDisplayMode.FAV           -> {
                    StateView.State.Empty(
                            title = getString(R.string.bottom_action_favourites),
                            image = ContextCompat.getDrawable(requireContext(), R.drawable.empty_state_dm),
                            isBigImage = true,
                            message = null
                    )
                }
                RoomListDisplayMode.NOTICE_BOARD           -> {
                    StateView.State.Empty(
                            title = getString(R.string.room_list_catchup_empty_title),
                            image = ContextCompat.getDrawable(requireContext(), R.drawable.ic_noun_party_popper),
                            message = getString(R.string.room_list_catchup_empty_body_notices))
                }
                RoomListDisplayMode.HOME          -> {
                    StateView.State.Empty(
                            title = getString(R.string.room_list_people_empty_title),
                            image = ContextCompat.getDrawable(requireContext(), R.drawable.empty_state_dm),
                            isBigImage = true,
                            message = null
                    )
                }
                RoomListDisplayMode.PEOPLE        ->
                    StateView.State.Empty(
                            title = getString(R.string.room_list_people_empty_title),
                            image = ContextCompat.getDrawable(requireContext(), R.drawable.empty_state_dm),
                            isBigImage = true,
                            message = getString(R.string.room_list_people_empty_body)
                    )
                RoomListDisplayMode.ROOMS         ->
                    StateView.State.Empty(
                            title = getString(R.string.room_list_rooms_empty_title),
                            image = ContextCompat.getDrawable(requireContext(), R.drawable.empty_state_room),
                            isBigImage = true,
                            message = getString(R.string.room_list_rooms_empty_body)
                    )
                else                              ->
                    // Always display the content in this mode, because if the footer
                    StateView.State.Content
            }
            views.stateView.state = emptyState
        } else {
            views.stateView.state = StateView.State.Content
        }
    }

    override fun onBackPressed(toolbarButton: Boolean): Boolean {
        if (views.createChatFabMenu.onBackPressed()) {
            return true
        }
        return false
    }

    // RoomSummaryController.Callback **************************************************************

    override fun onRoomClicked(room: RoomSummary) {
        isOneToOneChatOpen = room.isDirect
        roomListViewModel.handle(RoomListAction.SelectRoom(room))
    }

    override fun onRoomLongClicked(room: RoomSummary): Boolean {
        userPreferencesProvider.neverShowLongClickOnRoomHelpAgain()
        isOneToOneChatOpen = room.isDirect
        withState(roomListViewModel) {
            // refresh footer
            footerController.setData(it)
        }
        RoomListQuickActionsBottomSheet
                .newInstance(room.roomId, RoomListActionsArgs.Mode.FULL)
                .show(childFragmentManager, "ROOM_LIST_QUICK_ACTIONS")
        return true
    }

    override fun onAcceptRoomInvitation(room: RoomSummary) {
        notificationDrawerManager.clearMemberShipNotificationForRoom(room.roomId)
        roomListViewModel.handle(RoomListAction.AcceptInvitation(room))
    }

    override fun onRoomProfileClicked(room: RoomSummary) {
        isOneToOneChatOpen = room.isDirect
        ShowProfileDialogFragment.getInstance(room, avatarRenderer, navigator, this).show(childFragmentManager, "")
    }

    override fun onRejectRoomInvitation(room: RoomSummary) {
        notificationDrawerManager.clearMemberShipNotificationForRoom(room.roomId)
        roomListViewModel.handle(RoomListAction.RejectInvitation(room))
    }
}
