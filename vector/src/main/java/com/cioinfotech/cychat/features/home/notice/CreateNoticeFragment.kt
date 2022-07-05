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

package com.cioinfotech.cychat.features.home.notice

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.DefaultSharedPreferences
import com.cioinfotech.cychat.core.extensions.addFragmentToBackstack
import com.cioinfotech.cychat.core.extensions.exhaustive
import com.cioinfotech.cychat.core.extensions.registerStartForActivityResult
import com.cioinfotech.cychat.core.platform.VectorBaseFragment
import com.cioinfotech.cychat.core.utils.KeyboardStateUtils
import com.cioinfotech.cychat.core.utils.checkPermissions
import com.cioinfotech.cychat.core.utils.registerForPermissionsResult
import com.cioinfotech.cychat.databinding.FragmentCreateNoticeBinding
import com.cioinfotech.cychat.features.attachments.AttachmentTypeSelectorView
import com.cioinfotech.cychat.features.attachments.AttachmentsHelper
import com.cioinfotech.cychat.features.cycore.viewmodel.CyCoreViewModel
import com.cioinfotech.cychat.features.home.notice.model.Board
import com.cioinfotech.cychat.features.home.notice.model.EventModel
import com.cioinfotech.cychat.features.home.notice.model.Notice
import com.cioinfotech.cychat.features.login.OrgListFragment
import com.cioinfotech.cychat.features.login.adapter.OrgListAdapter
import com.cioinfotech.cychat.features.settings.VectorPreferences
import com.cioinfotech.lib.multipicker.utils.FilePathHelper
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.matrix.android.sdk.api.session.content.ContentAttachmentData
import org.matrix.android.sdk.internal.network.NetworkConstants
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateNoticeFragment : VectorBaseFragment<FragmentCreateNoticeBinding>(), AttachmentsHelper.Callback, AttachmentTypeSelectorView.Callback {

    private lateinit var cyCoreViewModel: CyCoreViewModel

    private var selectedBoard: Board? = null
    private var listOfBoards = mutableListOf<Board>()
    private var updateNotice: Notice? = null
    private var noticeId: Int = 0
    private var selectedImages: ContentAttachmentData? = null //mutableListOf<ContentAttachmentData>()
    private var selectedAttachments: ContentAttachmentData? = null// = mutableListOf<ContentAttachmentData>()
    private var isAttachmentsClicked = false
    private lateinit var attachmentsHelper: AttachmentsHelper
    private lateinit var attachmentTypeSelector: AttachmentTypeSelectorView
    private lateinit var keyboardStateUtils: KeyboardStateUtils
    private lateinit var pref: SharedPreferences
    private var totalCountOfAttachments = 0
    private var sentCountOfAttachments = 0
    private val myCalendar: Calendar = Calendar.getInstance()
    private var eventAdded: EventModel? = null

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCreateNoticeBinding {
        return FragmentCreateNoticeBinding.inflate(inflater, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pref = DefaultSharedPreferences.getInstance(requireContext())
        attachmentsHelper = AttachmentsHelper(requireContext(), this).register()
        keyboardStateUtils = KeyboardStateUtils(requireActivity())
        cyCoreViewModel = (requireActivity() as NoticeBoardActivity).cyCoreViewModel
        showLoading(null)
        cyCoreViewModel.selectedNotice.observe(viewLifecycleOwner) {
            it.let { notice ->
                updateNotice = notice
            }
        }
        (requireActivity() as NoticeBoardActivity).setToolbarTitle(getString(R.string.create_new_notice))
        cyCoreViewModel.getNoticeBoards()
        cyCoreViewModel.noticeBoardsLiveData.observe(viewLifecycleOwner) {
            listOfBoards = it.data.boards
            if (listOfBoards.isEmpty()) {
                Snackbar.make(requireView(), getString(R.string.no_notice_boards_found), BaseTransientBottomBar.LENGTH_SHORT).show()
                views.btnNotice.isEnabled = false
            }
            dismissLoadingDialog()
            setUpdateMode()
        }

        views.spinner.setOnClickListener {
            OrgListFragment.getInstance(object : OrgListAdapter.ItemClickListener {
                override fun onClick(name: String) {
                    for (tempOrg in listOfBoards)
                        if (name == tempOrg.bb_name) {
                            selectedBoard = tempOrg
                            break
                        }
                    views.btnNotice.isEnabled = true
                    views.spinner.setText(selectedBoard?.bb_name)
                }
            },
                    listOfBoards.map { org -> org.bb_name }.toMutableList(),
                    getString(R.string.search_board_by_name)).show(parentFragmentManager, "")
        }

        cyCoreViewModel.errorData.observe(viewLifecycleOwner) {
            dismissLoadingDialog()
            if (it != null) {
                Snackbar.make(requireView(), it.error ?: getString(R.string.something_went_wrong), BaseTransientBottomBar.LENGTH_SHORT).show()
//                when(it.error){
//
//                }
            }
        }

        cyCoreViewModel.postDetailsLiveData.observe(viewLifecycleOwner) {
            totalCountOfAttachments += (if (selectedImages != null) 1 else 0)
            totalCountOfAttachments += (if (selectedAttachments != null) 1 else 0)
//            for (media in selectedImages)
            selectedImages?.let { it1 -> createUploadMediaBody(it1, NetworkConstants.MEDIA_IMAGE, it.data.postID.toString()) }?.let { it2 -> cyCoreViewModel.uploadMedia(it2) }

//            for (media in selectedAttachments)
            selectedAttachments?.let { it1 -> createUploadMediaBody(it1, NetworkConstants.MEDIA_ATTACHMENT, it.data.postID.toString()) }?.let { it2 -> cyCoreViewModel.uploadMedia(it2) }

            if (totalCountOfAttachments == 0) {
                dismissLoadingDialog()
                sentCountOfAttachments = 0
                totalCountOfAttachments = 0
                Snackbar.make(requireView(), getString(R.string.new_notice_created), BaseTransientBottomBar.LENGTH_SHORT).show()
                requireActivity().finish()
            }
        }

        cyCoreViewModel.postUploadedLiveData.observe(viewLifecycleOwner) {
            sentCountOfAttachments++
            if (sentCountOfAttachments == totalCountOfAttachments) {
                dismissLoadingDialog()
                sentCountOfAttachments = 0
                totalCountOfAttachments = 0
                Snackbar.make(requireView(), getString(R.string.new_notice_created), BaseTransientBottomBar.LENGTH_SHORT).show()
                requireActivity().finish()
            }
        }

        views.btnNotice.setOnClickListener {
            if (listOfBoards.isEmpty())
                Snackbar.make(requireView(), getString(R.string.no_notice_boards_found), BaseTransientBottomBar.LENGTH_SHORT).show()
            else {
                showLoading(null)
                cyCoreViewModel.updatePostDetails(createRequestBody())
            }
        }

        views.tvAttachPhoto.setOnClickListener {
            isAttachmentsClicked = false
            if (!::attachmentTypeSelector.isInitialized) {
                attachmentTypeSelector = AttachmentTypeSelectorView(vectorBaseActivity, vectorBaseActivity.layoutInflater, this@CreateNoticeFragment, true)
            }
            attachmentTypeSelector.show(views.tvAttachPhoto, keyboardStateUtils.isKeyboardShowing)
        }

        views.tvAttachDocument.setOnClickListener {
            isAttachmentsClicked = true
            onTypeSelected(AttachmentTypeSelectorView.Type.FILE)
        }

        views.ivRemove.setOnClickListener {
            selectedImages = null//mutableListOf()
            views.ivPreview.isVisible = false
            views.ivRemove.isVisible = false
            views.ivPreview.setImageIcon(null)
        }

        views.ivRemoveAttachment.setOnClickListener {
            selectedAttachments = null // mutableListOf()
            views.tvPreviewAttachment.isVisible = false
            views.ivRemoveAttachment.isVisible = false
        }

        views.tvAttachEvent.setOnClickListener {
            addFragmentToBackstack(R.id.container, CreateEventFragment::class.java)
        }

        views.etEndDateAndTime.setOnClickListener {
            val datePicker = DatePickerDialog(
                    requireContext(), dateListener, myCalendar[Calendar.YEAR],
                    myCalendar[Calendar.MONTH],
                    myCalendar[Calendar.DAY_OF_MONTH]
            )
            datePicker.setTitle("Select End Date")
            datePicker.datePicker.touchables[0].performClick()
            datePicker.show()
        }

        cyCoreViewModel.eventLiveData.observe(viewLifecycleOwner) {
            eventAdded = it
            if (it != null) {
                views.clEvent.isVisible = true
                views.tvType.text = if (it.eventType == NetworkConstants.EVENT_ONLINE)
                    "Online Event"
                else "Live Event"

                views.tvStartTime.text = "Start Time: " + it.eventStart
                views.tvEndTime.text = "End Time: " + it.eventEnd
                views.tvVenue.text = "Venue: " + it.eventVenue
            } else
                views.clEvent.isVisible = false
        }
    }

    private var dateListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        myCalendar.set(Calendar.YEAR, year)
        myCalendar.set(Calendar.MONTH, monthOfYear)
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val currentTime = Calendar.getInstance()
        val mTimePicker = TimePickerDialog(
                requireContext(), { _, selectedHour, selectedMinute ->
            views.etEndDateAndTime.error = null
            var dateText = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(myCalendar.time)
            val newHour = if (selectedHour > 9) "$selectedHour" else "0$selectedHour"
            dateText += if (selectedMinute > 9)
                " $newHour:$selectedMinute:00"
            else
                " $newHour:0$selectedMinute:00"
            views.etEndDateAndTime.setText(dateText)
        }, currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE), true
        )
        mTimePicker.setTitle("Select End Time")
        mTimePicker.show()
    }

    private fun createUploadMediaBody(selectedAttachment: ContentAttachmentData, type: String, postId: String): MutableMap<String, RequestBody> {
        val partList = mutableMapOf<String, RequestBody>()
        partList[NetworkConstants.CLIENT_NAME] = NetworkConstants.CY_VERSE_ANDROID.toRequestBody("text/plain".toMediaTypeOrNull())
        partList[NetworkConstants.OP] = NetworkConstants.UPLOAD_MEDIA.toRequestBody("text/plain".toMediaTypeOrNull())
        partList[NetworkConstants.SERVICE_NAME] = NetworkConstants.EDIT_POSTS.toRequestBody("text/plain".toMediaTypeOrNull())
        partList[NetworkConstants.POST_ID] = postId.toRequestBody("text/plain".toMediaTypeOrNull())
        FilePathHelper.getRealPath(context, selectedAttachment.queryUri)?.let { fileUri ->
            val imgFile = File(fileUri.path!!)
            partList["media\"; filename=\"${selectedAttachment.name}"] = imgFile.asRequestBody("image/*".toMediaTypeOrNull())
        }
        partList[NetworkConstants.TYPE] = type.toRequestBody("text/plain".toMediaTypeOrNull())
        return partList
    }

    private fun createRequestBody(): MutableMap<String, RequestBody> {
        val partList = mutableMapOf<String, RequestBody>()
        partList[NetworkConstants.CLIENT_NAME] = NetworkConstants.CY_VERSE_ANDROID.toRequestBody("text/plain".toMediaTypeOrNull())
        partList[NetworkConstants.OP] = NetworkConstants.UPDATE_POST_DETAILS.toRequestBody("text/plain".toMediaTypeOrNull())
        partList[NetworkConstants.SERVICE_NAME] = NetworkConstants.EDIT_POSTS.toRequestBody("text/plain".toMediaTypeOrNull())
        partList[NetworkConstants.CLID] = NetworkConstants.CY_VERSE_API_CLID.toRequestBody("text/plain".toMediaTypeOrNull())
        partList[NetworkConstants.USER_ID] = pref.getString(NetworkConstants.USER_ID, null).toString().toRequestBody("text/plain".toMediaTypeOrNull())//(pref.getString(NetworkConstants.USER_ID, null) ?: "")
        partList[NetworkConstants.BOARD_ID] = (selectedBoard?.bb_id ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
        partList[NetworkConstants.POST_TITLE] = views.etTitle.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        partList[NetworkConstants.TEXT_AFTER] = views.etTextAfter.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        partList[NetworkConstants.TEXT_BEFORE] = views.etTextBefore.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        partList[NetworkConstants.POST_STATUS] = NetworkConstants.POST_STATUS_TYPE_PUBLISH.toRequestBody("text/plain".toMediaTypeOrNull())
        partList[NetworkConstants.POST_ID] = noticeId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        eventAdded?.let {
            partList[NetworkConstants.EVENT_VENUE] = it.eventVenue.toRequestBody("text/plain".toMediaTypeOrNull())
            partList[NetworkConstants.EVENT_START] = it.eventStart.toRequestBody("text/plain".toMediaTypeOrNull())
            partList[NetworkConstants.EVENT_END] = it.eventEnd.toRequestBody("text/plain".toMediaTypeOrNull())
            partList[NetworkConstants.POST_TYPE] = it.eventType.toRequestBody("text/plain".toMediaTypeOrNull())
            partList[NetworkConstants.TIMEZONE] = it.eventTzName.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        if (eventAdded == null)
            partList[NetworkConstants.TIMEZONE] = NetworkConstants.POST.toRequestBody("text/plain".toMediaTypeOrNull())
        return partList
    }

    fun setUpdateMode() {
        updateNotice?.let {
            for (board in listOfBoards)
                if (board.bb_name == it.boardName)
                    selectedBoard = board
            views.btnNotice.isEnabled = true
            views.btnNotice.text = getString(R.string.update_notice)
            views.spinner.setText(selectedBoard?.bb_name)
            views.etTitle.setText(it.title)
            views.etTextBefore.setText(it.textBefore)
            views.etTextAfter.setText(it.textAfter)
            noticeId = it.postid
            views.spinner.isClickable = false
            views.spinner.setOnClickListener(null)
        }
    }

    override fun onContentAttachmentsReady(attachments: List<ContentAttachmentData>) {
        if (attachments.isNotEmpty()) {
            if (isAttachmentsClicked) {
                selectedAttachments = attachments[0]//.toMutableList()
                views.tvPreviewAttachment.isVisible = true
                views.ivRemoveAttachment.isVisible = true
                views.tvPreviewAttachment.text = selectedAttachments?.name
            } else {
                selectedImages = attachments[0]//.toMutableList()
                selectedImages?.queryUri.let {
                    Glide.with(requireContext())
                            .asBitmap()
                            .load(it)
                            .into(views.ivPreview)
                    views.ivPreview.isVisible = true
                    views.ivRemove.isVisible = true
                }
            }
        }
//        Toast.makeText(requireContext(), "Attachments Ready" + attachments.size, Toast.LENGTH_SHORT).show()
    }

//    private fun ImageView.render(url: String) {
//        GlideApp.with(this)
//                .load(url)
//                .placeholder(showCircularProgressDrawable(this.context))
//                .error(showCircularProgressDrawable(this.context))
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .dontAnimate()
//                .fitCenter()
////                .transform(RoundedCorners(DimensionConverter(this.context.resources).dpToPx(8)))
//                .into(this)
//    }

//    private fun showCircularProgressDrawable(context: Context): CircularProgressDrawable {
//        return CircularProgressDrawable(context).apply {
//            strokeWidth = 8f
//            centerRadius = 48f
//            start()
//        }
//    }

    override fun onAttachmentsProcessFailed() {
        Toast.makeText(requireContext(), "FAILED to load attachments", Toast.LENGTH_SHORT).show()
    }

    private val typeSelectedActivityResultLauncher = registerForPermissionsResult { allGranted ->
        if (allGranted) {
            val pendingType = attachmentsHelper.pendingType
            if (pendingType != null) {
                attachmentsHelper.pendingType = null
                launchAttachmentProcess(pendingType)
            }
        } else
            cleanUpAfterPermissionNotGranted()
    }

    private fun cleanUpAfterPermissionNotGranted() {
        attachmentsHelper.pendingType = null
    }

    private fun launchAttachmentProcess(type: AttachmentTypeSelectorView.Type) {
        when (type) {
            AttachmentTypeSelectorView.Type.CAMERA  -> attachmentsHelper.openCamera(
                    activity = requireActivity(),
                    vectorPreferences = VectorPreferences(requireContext()),
                    cameraActivityResultLauncher = attachmentPhotoActivityResultLauncher,
                    cameraVideoActivityResultLauncher = attachmentVideoActivityResultLauncher
            )
            AttachmentTypeSelectorView.Type.FILE    -> attachmentsHelper.selectFile(attachmentFileActivityResultLauncher)
            AttachmentTypeSelectorView.Type.GALLERY -> attachmentsHelper.selectGallery(attachmentImageActivityResultLauncher)
            else                                    -> Unit
        }.exhaustive
    }

    private val attachmentPhotoActivityResultLauncher = registerStartForActivityResult {
        if (it.resultCode == Activity.RESULT_OK)
            attachmentsHelper.onPhotoResult()
    }

    private val attachmentVideoActivityResultLauncher = registerStartForActivityResult {
        if (it.resultCode == Activity.RESULT_OK)
            attachmentsHelper.onRecordedVideoResult()
    }

    private val attachmentFileActivityResultLauncher = registerStartForActivityResult {
        if (it.resultCode == Activity.RESULT_OK)
            attachmentsHelper.onImageResult(it.data)
    }

    private val attachmentImageActivityResultLauncher = registerStartForActivityResult {
        if (it.resultCode == Activity.RESULT_OK)
            attachmentsHelper.onImageResult(it.data)
    }

    override fun onTypeSelected(type: AttachmentTypeSelectorView.Type) {
        if (checkPermissions(type.permissionsBit, requireActivity(), typeSelectedActivityResultLauncher))
            launchAttachmentProcess(type)
        else
            attachmentsHelper.pendingType = type
    }
}
