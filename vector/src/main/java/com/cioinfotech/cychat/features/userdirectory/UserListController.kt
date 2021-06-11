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

import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.epoxy.errorWithRetryItem
import com.cioinfotech.cychat.core.epoxy.loadingItem
import com.cioinfotech.cychat.core.epoxy.noResultItem
import com.cioinfotech.cychat.core.error.ErrorFormatter
import com.cioinfotech.cychat.core.resources.StringProvider
import com.cioinfotech.cychat.features.home.AvatarRenderer
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.identity.ThreePid
import org.matrix.android.sdk.api.session.user.model.User
import org.matrix.android.sdk.api.util.toMatrixItem
import org.matrix.android.sdk.api.util.toNormalEmail
import javax.inject.Inject

class UserListController @Inject constructor(private val session: Session,
                                             private val avatarRenderer: AvatarRenderer,
                                             private val stringProvider: StringProvider,
                                             private val errorFormatter: ErrorFormatter) : EpoxyController() {

    private var state: UserListViewState? = null

    var callback: Callback? = null

    fun setData(state: UserListViewState) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {
        val currentState = state ?: return

        // Build generic items
//        if (currentState.searchTerm.isBlank()) {
//            if (false) {
////                if (currentState.showInviteActions()) {
//                actionItem {
//                    id(R.drawable.ic_share)
//                    title(stringProvider.getString(R.string.invite_friends))
//                    actionIconRes(R.drawable.ic_share)
//                    clickAction(View.OnClickListener {
//                        callback?.onInviteFriendClick()
//                    })
//                }
//            }
////            if (currentState.showContactBookAction) {
//            if (false) {
//                actionItem {
//                    id(R.drawable.ic_baseline_perm_contact_calendar_24)
//                    title(stringProvider.getString(R.string.contacts_book_title))
//                    actionIconRes(R.drawable.ic_baseline_perm_contact_calendar_24)
//                    clickAction(View.OnClickListener {
//                        callback?.onContactBookClick()
//                    })
//                }
//            }
//            if (false) {
////                if (currentState.showInviteActions()) {
//                actionItem {
//                    id(R.drawable.ic_qr_code_add).title(stringProvider.getString(R.string.qr_code))
//                    actionIconRes(R.drawable.ic_qr_code_add)
//                    clickAction(View.OnClickListener {
//                        callback?.onUseQRCode()
//                    })
//                }
//            }
//        }

        when (currentState.knownUsers) {
            is Uninitialized -> renderEmptyState()
            is Loading       -> renderLoading()
            is Fail          -> renderFailure(currentState.knownUsers.error)
            is Success       -> buildKnownUsers(currentState, currentState.getSelectedMatrixId())
        }

        when (val asyncUsers = currentState.directoryUsers) {
            is Uninitialized -> {
            }
            is Loading       -> renderLoading()
            is Fail          -> renderFailure(asyncUsers.error)
            is Success       -> buildDirectoryUsers(
                    asyncUsers(),
                    currentState.getSelectedMatrixId(),
                    currentState.searchTerm,
                    // to avoid showing twice same user in known and suggestions
                    currentState.knownUsers.invoke()?.map { it.userId }.orEmpty()
            )
        }
    }

    private fun buildKnownUsers(currentState: UserListViewState, selectedUsers: List<String>) {
        currentState.knownUsers()?.let { userList ->
            userListHeaderItem {
                id("known_header")
                header(stringProvider.getString(R.string.direct_room_user_list_known_title))
            }
            if (userList.isEmpty()) {
                renderEmptyState()
                return
            }
            userList.filter { user -> user.userId != session.myUserId }.forEach { item ->
                val isSelected = selectedUsers.contains(item.userId)
                userDirectoryUserItem {
                    id(item.userId)
                    selected(isSelected)
                    matrixItem(item.toMatrixItem())
                    avatarRenderer(avatarRenderer)
                    clickListener { _ ->
                        callback?.onItemClick(item)
                    }
                }
            }
        }
    }

    private fun buildDirectoryUsers(directoryUsers: List<User>, selectedUsers: List<String>, searchTerms: String, ignoreIds: List<String>) {
        val toDisplay = directoryUsers.filter { !ignoreIds.contains(it.userId) }
        if (toDisplay.isEmpty() && searchTerms.isBlank()) {
            return
        }
        userListHeaderItem {
            id("suggestions")
            header(stringProvider.getString(R.string.direct_room_user_list_suggestions_title))
        }
        if (toDisplay.isEmpty()) {
            renderEmptyState()
        } else {
            toDisplay.forEach { user ->
                if (user.userId != session.myUserId) {
                    val isSelected = selectedUsers.contains(user.userId)
                    userDirectoryUserItem {
                        id(user.userId.toNormalEmail())
                        selected(isSelected)
                        matrixItem(user.toMatrixItem())
                        avatarRenderer(avatarRenderer)
                        clickListener { _ ->
                            callback?.onItemClick(user)
                        }
                    }
                }
            }
        }
    }

    private fun renderLoading() {
        loadingItem {
            id("loading")
        }
    }

    private fun renderEmptyState() {
        noResultItem {
            id("noResult")
            text(stringProvider.getString(R.string.no_result_placeholder))
        }
    }

    private fun renderFailure(failure: Throwable) {
        errorWithRetryItem {
            id("error")
            text(errorFormatter.toHumanReadable(failure))
        }
    }

    interface Callback {
        fun onInviteFriendClick()
        fun onContactBookClick()
        fun onUseQRCode()
        fun onItemClick(user: User)
        fun onMatrixIdClick(matrixId: String)
        fun onThreePidClick(threePid: ThreePid)
    }
}
