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

package com.cioinfotech.cychat.features.createdirect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.viewModel
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.ScreenComponent
import com.cioinfotech.cychat.core.error.ErrorFormatter
import com.cioinfotech.cychat.core.extensions.addFragment
import com.cioinfotech.cychat.core.extensions.addFragmentToBackstack
import com.cioinfotech.cychat.core.extensions.exhaustive
import com.cioinfotech.cychat.core.platform.SimpleFragmentActivity
import com.cioinfotech.cychat.core.platform.WaitingViewData
import com.cioinfotech.cychat.core.utils.PERMISSIONS_FOR_MEMBERS_SEARCH
import com.cioinfotech.cychat.core.utils.PERMISSIONS_FOR_TAKING_PHOTO
import com.cioinfotech.cychat.core.utils.PERMISSION_REQUEST_CODE_LAUNCH_CAMERA
import com.cioinfotech.cychat.core.utils.PERMISSION_REQUEST_CODE_READ_CONTACTS
import com.cioinfotech.cychat.core.utils.allGranted
import com.cioinfotech.cychat.core.utils.checkPermissions
import com.cioinfotech.cychat.core.utils.onPermissionDeniedSnackbar
import com.cioinfotech.cychat.features.contactsbook.ContactsBookFragment
import com.cioinfotech.cychat.features.contactsbook.ContactsBookViewModel
import com.cioinfotech.cychat.features.contactsbook.ContactsBookViewState
import com.cioinfotech.cychat.features.userdirectory.UserListFragment
import com.cioinfotech.cychat.features.userdirectory.UserListFragmentArgs
import com.cioinfotech.cychat.features.userdirectory.UserListSharedAction
import com.cioinfotech.cychat.features.userdirectory.UserListSharedActionViewModel
import com.cioinfotech.cychat.features.userdirectory.UserListViewModel
import com.cioinfotech.cychat.features.userdirectory.UserListViewState

import org.matrix.android.sdk.api.failure.Failure
import org.matrix.android.sdk.api.session.room.failure.CreateRoomFailure
import java.net.HttpURLConnection
import javax.inject.Inject

class CreateDirectRoomActivity : SimpleFragmentActivity(), UserListViewModel.Factory, CreateDirectRoomViewModel.Factory, ContactsBookViewModel.Factory {

    private val viewModel: CreateDirectRoomViewModel by viewModel()
    private lateinit var sharedActionViewModel: UserListSharedActionViewModel
    @Inject lateinit var userListViewModelFactory: UserListViewModel.Factory
    @Inject lateinit var createDirectRoomViewModelFactory: CreateDirectRoomViewModel.Factory
    @Inject lateinit var contactsBookViewModelFactory: ContactsBookViewModel.Factory
    @Inject lateinit var errorFormatter: ErrorFormatter

    override fun injectWith(injector: ScreenComponent) {
        super.injectWith(injector)
        injector.inject(this)
    }

    override fun create(initialState: UserListViewState) = userListViewModelFactory.create(initialState)

    override fun create(initialState: CreateDirectRoomViewState) = createDirectRoomViewModelFactory.create(initialState)

    override fun create(initialState: ContactsBookViewState) = contactsBookViewModelFactory.create(initialState)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        views.toolbar.visibility = View.GONE

        sharedActionViewModel = viewModelProvider.get(UserListSharedActionViewModel::class.java)
        sharedActionViewModel
                .observe()
                .subscribe { action ->
                    when (action) {
                        UserListSharedAction.Close                 -> finish()
                        UserListSharedAction.GoBack                -> onBackPressed()
                        is UserListSharedAction.OnMenuItemSelected -> onMenuItemSelected(action)
                        UserListSharedAction.OpenPhoneBook         -> openPhoneBook()
                        UserListSharedAction.AddByQrCode           -> openAddByQrCode()
                    }.exhaustive
                }
                .disposeOnDestroy()
        if (isFirstCreation()) {
            addFragment(
                    R.id.container,
                    UserListFragment::class.java,
                    UserListFragmentArgs(
                            title = getString(R.string.fab_menu_create_chat),
                            menuResId = R.menu.vector_create_direct_room
                    )
            )
        }
        viewModel.selectSubscribe(this, CreateDirectRoomViewState::createAndInviteState) {
            renderCreateAndInviteState(it)
        }
    }

    private fun openAddByQrCode() {
        if (checkPermissions(PERMISSIONS_FOR_TAKING_PHOTO, this, PERMISSION_REQUEST_CODE_LAUNCH_CAMERA, 0)) {
            addFragment(R.id.container, CreateDirectRoomByQrCodeFragment::class.java)
        }
    }

    private fun openPhoneBook() {
        // Check permission first
        if (checkPermissions(PERMISSIONS_FOR_MEMBERS_SEARCH,
                        this,
                        PERMISSION_REQUEST_CODE_READ_CONTACTS,
                        0)) {
            addFragmentToBackstack(R.id.container, ContactsBookFragment::class.java)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (allGranted(grantResults)) {
            if (requestCode == PERMISSION_REQUEST_CODE_READ_CONTACTS) {
                doOnPostResume { addFragmentToBackstack(R.id.container, ContactsBookFragment::class.java) }
            } else if (requestCode == PERMISSION_REQUEST_CODE_LAUNCH_CAMERA) {
                addFragment(R.id.container, CreateDirectRoomByQrCodeFragment::class.java)
            }
        } else {
            if (requestCode == PERMISSION_REQUEST_CODE_LAUNCH_CAMERA) {
                onPermissionDeniedSnackbar(R.string.permissions_denied_qr_code)
            } else if (requestCode == PERMISSION_REQUEST_CODE_READ_CONTACTS) {
                onPermissionDeniedSnackbar(R.string.permissions_denied_add_contact)
            }
        }
    }

    private fun onMenuItemSelected(action: UserListSharedAction.OnMenuItemSelected) {
        if (action.itemId == R.id.action_create_direct_room) {
            viewModel.handle(CreateDirectRoomAction.CreateRoomAndInviteSelectedUsers(
                    action.selections,
                    null
            ))
        }
    }

    private fun renderCreateAndInviteState(state: Async<String>) {
        when (state) {
            is Loading -> renderCreationLoading()
            is Success -> renderCreationSuccess(state())
            is Fail    -> renderCreationFailure(state.error)
        }
    }

    private fun renderCreationLoading() {
        updateWaitingView(WaitingViewData(getString(R.string.creating_direct_room)))
    }

    private fun renderCreationFailure(error: Throwable) {
        hideWaitingView()
        when (error) {
            is CreateRoomFailure.CreatedWithTimeout           -> {
                finish()
            }
            is CreateRoomFailure.CreatedWithFederationFailure -> {
                AlertDialog.Builder(this)
                        .setMessage(getString(R.string.create_room_federation_error, error.matrixError.message))
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok) { _, _ -> finish() }
                        .show()
            }
            else                                              -> {
                val message = if (error is Failure.ServerError && error.httpCode == HttpURLConnection.HTTP_INTERNAL_ERROR /*500*/) {
                    // This error happen if the invited userId does not exist.
                    getString(R.string.create_room_dm_failure)
                } else {
                    errorFormatter.toHumanReadable(error)
                }
                AlertDialog.Builder(this)
                        .setMessage(message)
                        .setPositiveButton(R.string.ok, null)
                        .show()
            }
        }
    }

    private fun renderCreationSuccess(roomId: String?) {
        // Navigate to freshly created room
        if (roomId != null) {
            navigator.openRoom(this, roomId)
        }
        finish()
    }

    companion object {

        fun getIntent(context: Context): Intent {
            return Intent(context, CreateDirectRoomActivity::class.java)
        }
    }
}
