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

package com.cioinfotech.cychat.features.contactsbook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.extensions.cleanup
import com.cioinfotech.cychat.core.extensions.configureWith
import com.cioinfotech.cychat.core.extensions.hideKeyboard
import com.cioinfotech.cychat.core.platform.VectorBaseFragment
import com.cioinfotech.cychat.databinding.FragmentContactsBookBinding
import com.cioinfotech.cychat.features.userdirectory.PendingSelection
import com.cioinfotech.cychat.features.userdirectory.UserListAction
import com.cioinfotech.cychat.features.userdirectory.UserListSharedAction
import com.cioinfotech.cychat.features.userdirectory.UserListSharedActionViewModel
import com.cioinfotech.cychat.features.userdirectory.UserListViewModel
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.textChanges

import org.matrix.android.sdk.api.session.identity.ThreePid
import org.matrix.android.sdk.api.session.user.model.User
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ContactsBookFragment @Inject constructor(
        private val contactsBookViewModelFactory: ContactsBookViewModel.Factory,
        private val contactsBookController: ContactsBookController
) : VectorBaseFragment<FragmentContactsBookBinding>(), ContactsBookController.Callback, ContactsBookViewModel.Factory {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentContactsBookBinding {
        return FragmentContactsBookBinding.inflate(inflater, container, false)
    }

    private val viewModel: UserListViewModel by activityViewModel()

    // Use activityViewModel to avoid loading several times the data
    private val contactsBookViewModel: ContactsBookViewModel by activityViewModel()

    private lateinit var sharedActionViewModel: UserListSharedActionViewModel

    override fun create(initialState: ContactsBookViewState): ContactsBookViewModel {
        return contactsBookViewModelFactory.create(initialState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedActionViewModel = activityViewModelProvider.get(UserListSharedActionViewModel::class.java)
        setupRecyclerView()
        setupFilterView()
        setupConsentView()
        setupOnlyBoundContactsView()
        setupCloseView()
    }

    private fun setupConsentView() {
        views.phoneBookSearchForMatrixContacts.setOnClickListener {
            withState(contactsBookViewModel) { state ->
                AlertDialog.Builder(requireActivity())
                        .setTitle(R.string.identity_server_consent_dialog_title)
                        .setMessage(getString(R.string.identity_server_consent_dialog_content, state.identityServerUrl ?: ""))
                        .setPositiveButton(R.string.yes) { _, _ ->
                            contactsBookViewModel.handle(ContactsBookAction.UserConsentGranted)
                        }
                        .setNegativeButton(R.string.no, null)
                        .show()
            }
        }
    }

    private fun setupOnlyBoundContactsView() {
        views.phoneBookOnlyBoundContacts.checkedChanges()
                .subscribe {
                    contactsBookViewModel.handle(ContactsBookAction.OnlyBoundContacts(it))
                }
                .disposeOnDestroyView()
    }

    private fun setupFilterView() {
        views.phoneBookFilter
                .textChanges()
                .skipInitialValue()
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribe {
                    contactsBookViewModel.handle(ContactsBookAction.FilterWith(it.toString()))
                }
                .disposeOnDestroyView()
    }

    override fun onDestroyView() {
        views.phoneBookRecyclerView.cleanup()
        contactsBookController.callback = null
        super.onDestroyView()
    }

    private fun setupRecyclerView() {
        contactsBookController.callback = this
        views.phoneBookRecyclerView.configureWith(contactsBookController)
    }

    private fun setupCloseView() {
        views.phoneBookClose.debouncedClicks {
            sharedActionViewModel.post(UserListSharedAction.GoBack)
        }
    }

    override fun invalidate() = withState(contactsBookViewModel) { state ->
        views.phoneBookSearchForMatrixContacts.isVisible = state.filteredMappedContacts.isNotEmpty() && state.identityServerUrl != null && !state.userConsent
        views.phoneBookOnlyBoundContacts.isVisible = state.isBoundRetrieved
        contactsBookController.setData(state)
    }

    override fun onMatrixIdClick(matrixId: String) {
        view?.hideKeyboard()
        viewModel.handle(UserListAction.AddPendingSelection(PendingSelection.UserPendingSelection(User(matrixId))))
        sharedActionViewModel.post(UserListSharedAction.GoBack)
    }

    override fun onThreePidClick(threePid: ThreePid) {
        view?.hideKeyboard()
        viewModel.handle(UserListAction.AddPendingSelection(PendingSelection.ThreePidPendingSelection(threePid)))
        sharedActionViewModel.post(UserListSharedAction.GoBack)
    }
}
