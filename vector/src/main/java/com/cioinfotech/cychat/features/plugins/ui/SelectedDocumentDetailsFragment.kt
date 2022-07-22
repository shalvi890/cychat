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

package com.cioinfotech.cychat.features.plugins.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.extensions.exhaustive
import com.cioinfotech.cychat.core.extensions.registerStartForActivityResult
import com.cioinfotech.cychat.core.platform.VectorBaseFragment
import com.cioinfotech.cychat.core.utils.KeyboardStateUtils
import com.cioinfotech.cychat.core.utils.checkPermissions
import com.cioinfotech.cychat.core.utils.registerForPermissionsResult
import com.cioinfotech.cychat.databinding.FragmentSelectedDocumentDetailsBinding
import com.cioinfotech.cychat.features.attachments.AttachmentTypeSelectorView
import com.cioinfotech.cychat.features.attachments.AttachmentsHelper
import com.cioinfotech.cychat.features.plugins.PluginsActivity
import org.matrix.android.sdk.api.session.content.ContentAttachmentData

class SelectedDocumentDetailsFragment : VectorBaseFragment<FragmentSelectedDocumentDetailsBinding>(), AttachmentsHelper.Callback, AttachmentTypeSelectorView.Callback {
    private lateinit var attachmentsHelper: AttachmentsHelper
    private var selectedAttachments: ContentAttachmentData? = null
    private lateinit var keyboardStateUtils: KeyboardStateUtils

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentSelectedDocumentDetailsBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as PluginsActivity).setToolbarTitle(getString(R.string.select_document))
        views.btnUpload.isEnabled = false
        keyboardStateUtils = KeyboardStateUtils(requireActivity())
        attachmentsHelper = AttachmentsHelper(requireContext(), this).register()

        views.btnUpload.setOnClickListener {
            requireActivity().finish()
        }

        views.btnChooseFile.setOnClickListener {
            onTypeSelected(AttachmentTypeSelectorView.Type.FILE)
        }

        views.ivRemoveAttachment.setOnClickListener {
            selectedAttachments = null // mutableListOf()
            views.tvPreviewAttachment.isVisible = false
            views.ivRemoveAttachment.isVisible = false
            views.btnUpload.isEnabled = false
        }

        views.btnChooseFile.setOnClickListener {
            onTypeSelected(AttachmentTypeSelectorView.Type.FILE)
        }
    }

    private val attachmentFileActivityResultLauncher = registerStartForActivityResult {
        if (it.resultCode == Activity.RESULT_OK)
            attachmentsHelper.onFileResult(it.data)
    }

    private fun launchAttachmentProcess(type: AttachmentTypeSelectorView.Type) {
        when (type) {
            AttachmentTypeSelectorView.Type.FILE -> attachmentsHelper.selectFile(attachmentFileActivityResultLauncher)
            else                                 -> Unit
        }.exhaustive
    }

    private val typeSelectedActivityResultLauncher = registerForPermissionsResult { allGranted ->
        if (allGranted) {
            val pendingType = attachmentsHelper.pendingType
            if (pendingType != null) {
                attachmentsHelper.pendingType = null
                launchAttachmentProcess(pendingType)
            }
        } else
            attachmentsHelper.pendingType = null
    }

    override fun onTypeSelected(type: AttachmentTypeSelectorView.Type) {
        if (checkPermissions(type.permissionsBit, requireActivity(), typeSelectedActivityResultLauncher))
            launchAttachmentProcess(type)
        else
            attachmentsHelper.pendingType = type
    }

    override fun onContentAttachmentsReady(attachments: List<ContentAttachmentData>) {
        if (attachments.isNotEmpty()) {
            selectedAttachments = attachments[0]
            views.tvPreviewAttachment.isVisible = true
            views.ivRemoveAttachment.isVisible = true
            views.tvPreviewAttachment.text = "Document Attached"
            views.btnUpload.isEnabled = true
        }
    }

    override fun onAttachmentsProcessFailed() {
    }
}
