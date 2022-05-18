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

package com.cioinfotech.cychat.features.userdirectory

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.args
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.ActiveSessionHolder
import com.cioinfotech.cychat.core.di.DefaultSharedPreferences
import com.cioinfotech.cychat.core.extensions.cleanup
import com.cioinfotech.cychat.core.extensions.hideKeyboard
import com.cioinfotech.cychat.core.platform.VectorBaseFragment
import com.cioinfotech.cychat.core.utils.DimensionConverter
import com.cioinfotech.cychat.core.utils.startSharePlainTextIntent
import com.cioinfotech.cychat.databinding.FragmentUserListBinding
import com.cioinfotech.cychat.features.cycore.data.SearchedUser
import com.cioinfotech.cychat.features.cycore.viewmodel.CyCoreViewModel
import com.cioinfotech.cychat.features.home.AvatarRenderer
import com.cioinfotech.cychat.features.home.HomeActivity.Companion.isOneToOneChatOpen
import com.cioinfotech.cychat.features.homeserver.HomeServerCapabilitiesViewModel
import com.cioinfotech.cychat.features.userdirectory.adapter.ServerListAdapter
import com.cioinfotech.cychat.features.userdirectory.adapter.UserSearchAdapter
import com.google.android.material.chip.Chip
import com.jakewharton.rxbinding3.widget.textChanges
import org.matrix.android.sdk.api.session.identity.ThreePid
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.session.room.roomSummaryQueryParams
import org.matrix.android.sdk.api.session.user.model.User
import org.matrix.android.sdk.internal.cy_auth.data.FederatedDomain
import org.matrix.android.sdk.internal.network.NetworkConstants
import org.matrix.android.sdk.internal.network.NetworkConstants.CLID
import org.matrix.android.sdk.internal.network.NetworkConstants.USER_TYPE
import javax.inject.Inject

class UserListFragment @Inject constructor(
        private val userListController: UserListController,
        private val dimensionConverter: DimensionConverter,
        val homeServerCapabilitiesViewModelFactory: HomeServerCapabilitiesViewModel.Factory,
        private val avatarRenderer: AvatarRenderer
) : VectorBaseFragment<FragmentUserListBinding>(),
        UserSearchAdapter.Callback {
    private lateinit var pref: SharedPreferences
    private var userList = mutableListOf<SearchedUser>()
    private var roomList = listOf<RoomSummary>()
    @Inject lateinit var sessionHolder: ActiveSessionHolder
    private lateinit var userSearchAdapter: UserSearchAdapter
    private var calledAtAttach = false
    private var selectedDomain: FederatedDomain? = null
    private val args: UserListFragmentArgs by args()
    private lateinit var cyCoreViewModel: CyCoreViewModel
    private val viewModel: UserListViewModel by activityViewModel()
    private val homeServerCapabilitiesViewModel: HomeServerCapabilitiesViewModel by fragmentViewModel()
    private lateinit var sharedActionViewModel: UserListSharedActionViewModel
    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentUserListBinding {
        return FragmentUserListBinding.inflate(inflater, container, false)
    }

    override fun getMenuRes() = args.menuResId

    override fun onAttach(context: Context) {
        super.onAttach(context)
        cyCoreViewModel = fragmentViewModelProvider.get(CyCoreViewModel::class.java)
        cyCoreViewModel.getFederatedDomains()
        calledAtAttach = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pref = DefaultSharedPreferences.getInstance(requireContext())
        userSearchAdapter = UserSearchAdapter(avatarRenderer, args.title == getString(R.string.fab_menu_create_chat))
        userSearchAdapter.callback = this
        if (calledAtAttach) {
            views.pbProgress.isVisible = true
            calledAtAttach = false
            views.userListSearch.isEnabled = false
            cyCoreViewModel.federatedDomainList.observe(viewLifecycleOwner) {
                val defaultDomain = pref.getString(USER_TYPE, "")
                for (item in it.data.fedList) {
                    if (defaultDomain == item.toUtypeID) {
                        views.tvServerName.text = item.utypeName
                        selectedDomain = item
                        viewModel.setDomain(item)
                        break
                    }
                }
                views.userListSearch.isEnabled = true
                views.userListSearch.requestFocus()
                views.pbProgress.isVisible = false
            }
        } else
            views.userListSearch.requestFocus()

        if (isOneToOneChatOpen && ::sessionHolder.isInitialized)
            sessionHolder.getSafeActiveSession()?.getRoomSummaries(roomSummaryQueryParams {
                memberships = Membership.activeMemberships()
            })?.let {
                roomList = it
            }

        sharedActionViewModel = activityViewModelProvider.get(UserListSharedActionViewModel::class.java)
        if (args.showToolbar) {
            views.userListTitle.text = args.title
            vectorBaseActivity.setSupportActionBar(views.userListToolbar)
            setupCloseView()
            views.userListToolbar.isVisible = true
        } else
            views.userListToolbar.isVisible = false

        setupRecyclerView()
        setupSearchView()

        homeServerCapabilitiesViewModel.subscribe {
            views.userListE2EbyDefaultDisabled.isVisible = !it.isE2EByDefault
        }

        viewModel.selectSubscribe(this, UserListViewState::pendingSelections) {
            renderSelectedUsers(it)
            val tempList = mutableListOf<SearchedUser>()
            for (item in it)
                for (item2 in userSearchAdapter.selectedUsers)
                    if (item.getUId() == item2.matrixUserID)
                        tempList.add(item2)
            userSearchAdapter.selectedUsers = tempList
            userSearchAdapter.notifyDataSetChanged()
        }

        viewModel.observeViewEvents {
            when (it) {
                is UserListViewEvents.OpenShareMatrixToLing -> {
                    val text = getString(R.string.invite_friends_text, it.link)
                    startSharePlainTextIntent(
                            fragment = this,
                            activityResultLauncher = null,
                            chooserTitle = getString(R.string.invite_friends),
                            text = text,
                            extraTitle = getString(R.string.invite_friends_rich_title)
                    )
                }
            }
        }

        views.tvServerName.setOnClickListener {
            ServerListFragment.getInstance(object : ServerListAdapter.ItemClickListener {
                override fun onClick(item: FederatedDomain) {
                    views.tvServerName.text = item.utypeName
                    selectedDomain = item
                    viewModel.setDomain(item)
                    views.pbProgress.isVisible = true
                    viewModel.handle(UserListAction.SearchUsers(views.userListSearch.text.toString(),
                            selectedDomain?.cychatURL!!,
                            pref.getString(CLID, "") ?: "",
                            selectedDomain?.toUtypeID!!,
                            pref.getString(NetworkConstants.USER_ID, "") ?: ""))
                }
            }, selectedDomain?.toUtypeID ?: "-1").show(childFragmentManager, "")
        }
    }

    override fun onDestroyView() {
        views.userListRecyclerView.cleanup()
        super.onDestroyView()
    }

    override fun onDestroy() {
        selectedDomain = null
        viewModel.setDomain()
        super.onDestroy()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        withState(viewModel) {
            it.excludedUserIds?.let { it1 -> userSearchAdapter.updateExcludeList(it1) }
            val showMenuItem = it.pendingSelections.isNotEmpty()
            menu.forEach { menuItem -> menuItem.isVisible = showMenuItem }
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = withState(viewModel) {
        sharedActionViewModel.post(UserListSharedAction.OnMenuItemSelected(item.itemId, it.pendingSelections))
        return@withState true
    }

    private fun setupRecyclerView() {
        views.userListRecyclerView.adapter = userSearchAdapter
        viewModel.userSearchLiveData.observe(viewLifecycleOwner) {
            userList = if (views.userListSearch.text.toString().isBlank())
                mutableListOf() else it.data.fedList
            userSearchAdapter.setUserList(userList)
            views.pbProgress.isVisible = false
        }
//        userListController.callback = this
        // Don't activate animation as we might have way to much item animation when filtering
//        views.userListRecyclerView.configureWith(userListController, disableItemAnimation = true)
    }

    private fun setupSearchView() {
        withState(viewModel) {
            views.userListSearch.hint = getString(R.string.user_directory_search_hint)
        }
        views.userListSearch
                .textChanges()
                .startWith(views.userListSearch.text)
                .subscribe { text ->
                    val searchValue = text.trim()
                    val action = if (searchValue.isBlank()) {
                        userSearchAdapter.setUserList(mutableListOf())
                        views.pbProgress.isVisible = false
                        UserListAction.ClearSearchUsers
                    } else {
                        views.pbProgress.isVisible = true
                        UserListAction.SearchUsers(searchValue.toString(),
                                selectedDomain?.cychatURL!!,
                                pref.getString(CLID, "") ?: "",
                                selectedDomain?.toUtypeID!!,
                                pref.getString(NetworkConstants.USER_ID, "") ?: "")
                    }
                    viewModel.handle(action)
                }.disposeOnDestroyView()
//        views.userListSearch.setupAsSearch()
    }

    private fun setupCloseView() {
        views.userListClose.debouncedClicks {
            requireActivity().finish()
        }
    }

    override fun invalidate() = withState(viewModel) {
        userListController.setData(it)
    }

    private fun renderSelectedUsers(selections: Set<PendingSelection>) {
        invalidateOptionsMenu()

        val currentNumberOfChips = views.chipGroup.childCount
        val newNumberOfChips = selections.size

        views.chipGroup.removeAllViews()
        selections.forEach { addChipToGroup(it) }

        // Scroll to the bottom when adding chips. When removing chips, do not scroll
        if (newNumberOfChips >= currentNumberOfChips) {
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                views.chipGroupScrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
    }

    private fun addChipToGroup(pendingSelection: PendingSelection) {
        val chip = Chip(requireContext())
        chip.setChipBackgroundColorResource(android.R.color.transparent)
        chip.chipStrokeWidth = dimensionConverter.dpToPx(1).toFloat()
        chip.text = pendingSelection.getBestName()
        chip.isClickable = true
        chip.isCheckable = false
        chip.isCloseIconVisible = true
        views.chipGroup.addView(chip)
        chip.setOnCloseIconClickListener {
            viewModel.handle(UserListAction.RemovePendingSelection(pendingSelection))
        }
    }

    fun getCurrentState() = withState(viewModel) { it }

    override fun onInviteFriendClick() {
        viewModel.handle(UserListAction.ComputeMatrixToLinkForSharing)
    }

    override fun onContactBookClick() {
        sharedActionViewModel.post(UserListSharedAction.OpenPhoneBook)
    }

    override fun onItemClick(user: User) {
        view?.hideKeyboard()
        if (isOneToOneChatOpen) {
            var selectedRoom: RoomSummary? = null
            for (roomSummary in roomList) {
                if (roomSummary.isDirect && roomSummary.otherMemberIds.size == 1 && roomSummary.otherMemberIds[0] == user.userId) {
                    selectedRoom = roomSummary
                    break
                }
            }
            if (selectedRoom != null)
                navigator.openRoom(requireActivity(), selectedRoom.roomId)
            else {
                val pendingSelection = PendingSelection.UserPendingSelection(user)
                sharedActionViewModel.post(UserListSharedAction.OnMenuItemSelected(R.id.action_create_direct_room, setOf(pendingSelection)))
            }
        } else
            viewModel.handle(UserListAction.AddPendingSelection(PendingSelection.UserPendingSelection(user)))
    }

    override fun onMatrixIdClick(matrixId: String) {
        view?.hideKeyboard()
        viewModel.handle(UserListAction.AddPendingSelection(PendingSelection.UserPendingSelection(User(matrixId))))
    }

    override fun onThreePidClick(threePid: ThreePid) {
        view?.hideKeyboard()
        viewModel.handle(UserListAction.AddPendingSelection(PendingSelection.ThreePidPendingSelection(threePid)))
    }

    override fun showToastOnEmptyList(isListEmpty: Boolean) {
        views.tvNoUsers.isVisible = views.userListSearch.text.toString().isNotEmpty() && isListEmpty
    }

    override fun onUseQRCode() {
        view?.hideKeyboard()
        sharedActionViewModel.post(UserListSharedAction.AddByQrCode)
    }
}
