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

package com.cioinfotech.cychat.features.settings.threepids

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.dialogs.withColoredButton
import com.cioinfotech.cychat.core.extensions.cleanup
import com.cioinfotech.cychat.core.extensions.configureWith
import com.cioinfotech.cychat.core.extensions.exhaustive
import com.cioinfotech.cychat.core.extensions.getFormattedValue
import com.cioinfotech.cychat.core.extensions.hideKeyboard
import com.cioinfotech.cychat.core.extensions.isEmail
import com.cioinfotech.cychat.core.extensions.isMsisdn
import com.cioinfotech.cychat.core.extensions.registerStartForActivityResult
import com.cioinfotech.cychat.core.platform.OnBackPressed
import com.cioinfotech.cychat.core.platform.VectorBaseFragment
import com.cioinfotech.cychat.databinding.FragmentGenericRecyclerBinding
import com.cioinfotech.cychat.features.auth.ReAuthActivity
import org.matrix.android.sdk.api.auth.data.LoginFlowTypes
import org.matrix.android.sdk.api.session.identity.ThreePid
import javax.inject.Inject

class ThreePidsSettingsFragment @Inject constructor(
        private val viewModelFactory: ThreePidsSettingsViewModel.Factory,
        private val epoxyController: ThreePidsSettingsController
) :
        VectorBaseFragment<FragmentGenericRecyclerBinding>(),
        OnBackPressed,
        ThreePidsSettingsViewModel.Factory by viewModelFactory,
        ThreePidsSettingsController.InteractionListener {

    private val viewModel: ThreePidsSettingsViewModel by fragmentViewModel()

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentGenericRecyclerBinding {
        return FragmentGenericRecyclerBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        views.genericRecyclerView.configureWith(epoxyController)
        epoxyController.interactionListener = this

        viewModel.observeViewEvents {
            when (it) {
                is ThreePidsSettingsViewEvents.Failure -> displayErrorDialog(it.throwable)
                is ThreePidsSettingsViewEvents.RequestReAuth -> askAuthentication(it)
            }.exhaustive
        }
    }

    //    private fun askUserPassword() {
//        PromptPasswordDialog().show(requireActivity()) { password ->
//            viewModel.handle(ThreePidsSettingsAction.AccountPassword(password))
//        }
//    }

    private fun askAuthentication(event: ThreePidsSettingsViewEvents.RequestReAuth) {
        ReAuthActivity.newIntent(requireContext(),
                event.registrationFlowResponse,
                event.lastErrorCode,
                getString(R.string.settings_add_email_address)).let { intent ->
            reAuthActivityResultLauncher.launch(intent)
        }
    }
    private val reAuthActivityResultLauncher = registerStartForActivityResult { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            when (activityResult.data?.extras?.getString(ReAuthActivity.RESULT_FLOW_TYPE)) {
                LoginFlowTypes.SSO -> {
                    viewModel.handle(ThreePidsSettingsAction.SsoAuthDone)
                }
                LoginFlowTypes.PASSWORD -> {
                    val password = activityResult.data?.extras?.getString(ReAuthActivity.RESULT_VALUE) ?: ""
                    viewModel.handle(ThreePidsSettingsAction.PasswordAuthDone(password))
                }
                else                    -> {
                    viewModel.handle(ThreePidsSettingsAction.ReAuthCancelled)
                }
            }
        } else {
            viewModel.handle(ThreePidsSettingsAction.ReAuthCancelled)
        }
    }

    override fun onDestroyView() {
        views.genericRecyclerView.cleanup()
        epoxyController.interactionListener = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.setTitle(R.string.settings_emails_and_phone_numbers_title)
    }

    override fun invalidate() = withState(viewModel) { state ->
        if (state.isLoading) {
            showLoadingDialog()
        } else {
            dismissLoadingDialog()
        }
        epoxyController.setData(state)
    }

    override fun addEmail() {
        viewModel.handle(ThreePidsSettingsAction.ChangeUiState(ThreePidsSettingsUiState.AddingEmail(null)))
    }

    override fun doAddEmail(email: String) {
        // Sanity
        val safeEmail = email.trim().replace(" ", "")
        viewModel.handle(ThreePidsSettingsAction.ChangeUiState(ThreePidsSettingsUiState.AddingEmail(null)))

        // Check that email is valid
        if (!safeEmail.isEmail()) {
            viewModel.handle(ThreePidsSettingsAction.ChangeUiState(ThreePidsSettingsUiState.AddingEmail(getString(R.string.auth_invalid_email))))
            return
        }

        viewModel.handle(ThreePidsSettingsAction.AddThreePid(ThreePid.Email(safeEmail)))
    }

    override fun addMsisdn() {
        viewModel.handle(ThreePidsSettingsAction.ChangeUiState(ThreePidsSettingsUiState.AddingPhoneNumber(null)))
    }

    override fun doAddMsisdn(msisdn: String) {
        // Sanity
        val safeMsisdn = msisdn.trim().replace(" ", "")

        viewModel.handle(ThreePidsSettingsAction.ChangeUiState(ThreePidsSettingsUiState.AddingPhoneNumber(null)))

        // Check that phone number is valid
        if (!msisdn.startsWith("+")) {
            viewModel.handle(
                    ThreePidsSettingsAction.ChangeUiState(ThreePidsSettingsUiState.AddingPhoneNumber(getString(R.string.login_msisdn_error_not_international)))
            )
            return
        }

        if (!msisdn.isMsisdn()) {
            viewModel.handle(
                    ThreePidsSettingsAction.ChangeUiState(ThreePidsSettingsUiState.AddingPhoneNumber(getString(R.string.login_msisdn_error_other)))
            )
            return
        }

        viewModel.handle(ThreePidsSettingsAction.AddThreePid(ThreePid.Msisdn(safeMsisdn)))
    }

    override fun submitCode(threePid: ThreePid.Msisdn, code: String) {
        viewModel.handle(ThreePidsSettingsAction.SubmitCode(threePid, code))
        // Hide the keyboard
        view?.hideKeyboard()
    }

    override fun cancelAdding() {
        viewModel.handle(ThreePidsSettingsAction.ChangeUiState(ThreePidsSettingsUiState.Idle))
        // Hide the keyboard
        view?.hideKeyboard()
    }

    override fun continueThreePid(threePid: ThreePid) {
        viewModel.handle(ThreePidsSettingsAction.ContinueThreePid(threePid))
    }

    override fun cancelThreePid(threePid: ThreePid) {
        viewModel.handle(ThreePidsSettingsAction.CancelThreePid(threePid))
    }

    override fun deleteThreePid(threePid: ThreePid) {
        AlertDialog.Builder(requireActivity())
                .setMessage(getString(R.string.settings_remove_three_pid_confirmation_content, threePid.getFormattedValue()))
                .setPositiveButton(R.string.remove) { _, _ ->
                    viewModel.handle(ThreePidsSettingsAction.DeleteThreePid(threePid))
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
                .withColoredButton(DialogInterface.BUTTON_POSITIVE)
    }

    override fun onBackPressed(toolbarButton: Boolean): Boolean {
        return withState(viewModel) {
            if (it.uiState is ThreePidsSettingsUiState.Idle) {
                false
            } else {
                cancelAdding()
                true
            }
        }
    }
}