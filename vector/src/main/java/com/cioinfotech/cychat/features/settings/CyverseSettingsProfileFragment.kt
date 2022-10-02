/*
 * Copyright (c) 2022 New Vector Ltd
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

package com.cioinfotech.cychat.features.settings

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.dialogs.GalleryOrCameraDialogHelper
import com.cioinfotech.cychat.core.intent.getFilenameFromUri
import com.cioinfotech.cychat.core.resources.ColorProvider
import com.cioinfotech.cychat.databinding.FragmentCyverseSettingsProfileBinding
import com.cioinfotech.cychat.features.cycore.viewmodel.CyCoreViewModel
import com.cioinfotech.cychat.features.home.AvatarRenderer
import com.cioinfotech.cychat.features.settings.adapter.CyverseRoleAdapter
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.user.model.User
import org.matrix.android.sdk.api.util.MatrixItem
import org.matrix.android.sdk.api.util.toMatrixItem
import org.matrix.android.sdk.rx.rx
import org.matrix.android.sdk.rx.unwrap
import java.util.UUID
import javax.inject.Inject

class CyverseSettingsProfileFragment @Inject constructor(
        colorProvider: ColorProvider,
        private val avatarRenderer: AvatarRenderer
) : CyverseSettingsBaseFragment(),
        GalleryOrCameraDialogHelper.Listener {

    private val binding: FragmentCyverseSettingsProfileBinding get() = viewBinding!!
    private var viewBinding: FragmentCyverseSettingsProfileBinding? = null
    private val galleryOrCameraDialogHelper = GalleryOrCameraDialogHelper(this, colorProvider)
    private var userItem: MatrixItem.UserItem? = null

    override var titleRes = R.string.my_profile

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentCyverseSettingsProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        cyCoreViewModel.handleGetProfileDetails()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivProfile.setOnClickListener {
            galleryOrCameraDialogHelper.show()
        }

        binding.tvName.setOnClickListener {
            binding.etName.setText(binding.tvName.text.toString())
            setNameEditVisibility(true)
        }

        binding.icCancel.setOnClickListener {
            setNameEditVisibility(false)
        }

        binding.icSave.setOnClickListener {
            onDisplayNameChanged()
        }

        binding.etName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onDisplayNameChanged()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        binding.btnNewRole.setOnClickListener {
            (requireActivity() as VectorSettingsActivity).changeFragment()
        }

        binding.progressBar.isVisible = true
        cyCoreViewModel.userProfileData.observe(viewLifecycleOwner) {
            binding.rvRoles.adapter = CyverseRoleAdapter().apply {
                updateData(it.data.activeRoles.toMutableList())
            }
            binding.switchVisibility.isEnabled = true
            binding.switchVisibility.isVisible = it.data.allowVisible == "Y"
            binding.tvSwitchDescription.isVisible = it.data.allowVisible == "Y" && it.data.tooltip != null
            if (it.data.allowVisible == "Y")
                binding.switchVisibility.isChecked = it.data.visible == "Y"

            it.data.tooltip?.let { tooltip ->
                binding.tvSwitchDescription.text = tooltip
            }
            if (it.data.pendingRoles == null) {
                binding.btnNewRole.isVisible = true
                binding.tvPendingRequestsTitle.isVisible = false
                binding.ivDelete.isVisible = false
            } else {
                binding.btnNewRole.isVisible = false
                binding.ivDelete.isVisible = true
                binding.tvPendingRequestsTitle.isVisible = true
                binding.tvPendingRequestsTitle.text = getString(R.string.pending_requests, it.data.pendingRoles.utypeName)
                binding.tvPendingRequestsTitle.setOnClickListener { _ ->
                    (requireActivity() as VectorSettingsActivity).changeFragment(
                            bundleOf(
                                    "typeName" to it.data.pendingRoles.utypeName,
                                    "typeId" to it.data.pendingRoles.utypeID,
                                    "typeDescription" to it.data.pendingRoles.utypeDesc,
                                    "reqId" to it.data.pendingRoles.reqID,
                                    "userRoleId" to it.data.pendingRoles.userRoleID
                            )
                    )
                }
                binding.ivDelete.setOnClickListener { _ ->
                    cyCoreViewModel.deleteRequest(it.data.pendingRoles.reqID)
                    binding.progressBar.isVisible = true
                }
            }
            binding.progressBar.isVisible = false
        }
        observeUserAvatar()
        observeUserDisplayName()


        cyCoreViewModel.errorData.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.progressBar.isVisible = false
                Snackbar.make(requireView(), it.error ?: getString(R.string.something_went_wrong), LENGTH_SHORT).show()
//                when(it.error){
//
//                }
            }
        }

        binding.switchVisibility.setOnClickListener {
            binding.switchVisibility.isEnabled = false
            cyCoreViewModel.setVisibility()
        }

        cyCoreViewModel.visibilityLiveData.observe(viewLifecycleOwner) {
            if (it != null && it.errorCode.isNullOrEmpty()) {
                cyCoreViewModel.handleGetProfileDetails()
                cyCoreViewModel.visibilityLiveData.postValue(null)
            }
        }

        cyCoreViewModel.deleteRequestLiveData.observe(viewLifecycleOwner) {
            if (it != null && it.errorCode.isNullOrEmpty())
                cyCoreViewModel.handleGetProfileDetails()
            else
                binding.progressBar.isVisible = false
        }
    }

    private fun setNameEditVisibility(startEditing: Boolean) {
        binding.tvName.visibility = if (startEditing) View.INVISIBLE else View.VISIBLE
        binding.etName.isVisible = startEditing
        binding.icCancel.isVisible = startEditing
        binding.icSave.isVisible = startEditing
    }

    private fun observeUserDisplayName() {
        session.rx()
                .liveUser(session.myUserId)
                .unwrap()
                .map { it.displayName ?: "" }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { displayName ->
                    binding.tvName.text = displayName
                }
                .disposeOnDestroyView()
    }

    private fun onDisplayNameChanged() {
        val value = binding.etName.text.toString()
        if (value.isEmpty()) {
            binding.etName.error = getString(R.string.enter_display_name)
            binding.etName.requestFocus()
        } else if (value != binding.tvName.text.toString()) {
            setNameEditVisibility(false)
            val currentDisplayName = session.getUser(session.myUserId)?.displayName ?: ""
            if (currentDisplayName != value) {
                binding.progressBar.isVisible = true
                cyCoreViewModel.handleDisplayName(value)
                lifecycleScope.launch {
                    val result = runCatching { session.setDisplayName(session.myUserId, value) }
                    if (!isAdded) return@launch
                    result.fold(
                            {
                                binding.tvName.text = value
                                binding.progressBar.isVisible = false
                                onCommonDone(null)
                            },
                            {
                                binding.progressBar.isVisible = false
                                onCommonDone(it.localizedMessage)
                            }
                    )
                }
            }
        }
    }

    private fun observeUserAvatar() {
        session.rx()
                .liveUser(session.myUserId)
                .unwrap()
                .distinctUntilChanged { user -> user.avatarUrl }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { refreshAvatar(it) }
                .disposeOnDestroyView()
    }

    fun refreshAvatar(user: User) {
        userItem = user.toMatrixItem()
        refreshUi()
    }

    private fun refreshUi() {
        val safeUserItem = userItem ?: return
        binding.ivProfile.let { avatarRenderer.render(safeUserItem, it) }
    }

    override fun onImageReady(uri: Uri?) {
        if (uri != null) {
            binding.progressBar.isVisible = true
            uploadAvatar(uri)
        } else
            Toast.makeText(requireContext(), "Cannot retrieve cropped image", Toast.LENGTH_SHORT).show()
    }

    private fun uploadAvatar(uri: Uri) {
        binding.progressBar.isVisible = true
        lifecycleScope.launch {
            val result = runCatching {
                session.updateAvatar(session.myUserId, uri, getFilenameFromUri(context, uri) ?: UUID.randomUUID().toString())
            }
            if (!isAdded) return@launch
            binding.progressBar.isVisible = false
            onCommonDone(result.fold({ null }, { it.localizedMessage }))
        }
    }
}
