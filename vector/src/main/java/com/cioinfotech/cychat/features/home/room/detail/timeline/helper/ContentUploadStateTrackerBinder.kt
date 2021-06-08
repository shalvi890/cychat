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

package com.cioinfotech.cychat.features.home.room.detail.timeline.helper

import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.ActiveSessionHolder
import com.cioinfotech.cychat.core.di.ScreenScope
import com.cioinfotech.cychat.core.error.ErrorFormatter
import com.cioinfotech.cychat.core.utils.TextUtils
import com.cioinfotech.cychat.features.home.room.detail.timeline.MessageColorProvider
import org.matrix.android.sdk.api.session.content.ContentUploadStateTracker
import org.matrix.android.sdk.api.session.room.send.SendState
import javax.inject.Inject

@ScreenScope
class ContentUploadStateTrackerBinder @Inject constructor(private val activeSessionHolder: ActiveSessionHolder,
                                                          private val messageColorProvider: MessageColorProvider,
                                                          private val errorFormatter: ErrorFormatter) {

    private val updateListeners = mutableMapOf<String, ContentUploadStateTracker.UpdateListener>()

    fun bind(eventId: String,
             isLocalFile: Boolean,
             progressLayout: ViewGroup) {
        activeSessionHolder.getSafeActiveSession()?.also { session ->
            val uploadStateTracker = session.contentUploadProgressTracker()
            val updateListener = ContentMediaProgressUpdater(progressLayout, isLocalFile, messageColorProvider, errorFormatter)
            updateListeners[eventId] = updateListener
            uploadStateTracker.track(eventId, updateListener)
        }
    }

    fun unbind(eventId: String) {
        activeSessionHolder.getSafeActiveSession()?.also { session ->
            val uploadStateTracker = session.contentUploadProgressTracker()
            updateListeners[eventId]?.also {
                uploadStateTracker.untrack(eventId, it)
            }
        }
    }

    fun clear() {
        activeSessionHolder.getSafeActiveSession()?.also {
            it.contentUploadProgressTracker().clear()
        }
    }
}

private class ContentMediaProgressUpdater(private val progressLayout: ViewGroup,
                                          private val isLocalFile: Boolean,
                                          private val messageColorProvider: MessageColorProvider,
                                          private val errorFormatter: ErrorFormatter) : ContentUploadStateTracker.UpdateListener {

    override fun onUpdate(state: ContentUploadStateTracker.State) {
        when (state) {
            is ContentUploadStateTracker.State.Idle                -> handleIdle()
            is ContentUploadStateTracker.State.EncryptingThumbnail -> handleEncryptingThumbnail()
            is ContentUploadStateTracker.State.UploadingThumbnail  -> handleProgressThumbnail(state)
            is ContentUploadStateTracker.State.Encrypting          -> handleEncrypting(state)
            is ContentUploadStateTracker.State.Uploading           -> handleProgress(state)
            is ContentUploadStateTracker.State.Failure             -> handleFailure(/*state*/)
            is ContentUploadStateTracker.State.Success             -> handleSuccess()
        }
    }

    private fun handleIdle() {
        if (isLocalFile) {
            progressLayout.isVisible = true
            val progressBar = progressLayout.findViewById<ProgressBar>(R.id.mediaProgressBar)
            val progressTextView = progressLayout.findViewById<TextView>(R.id.mediaProgressTextView)
            progressBar?.isVisible = true
            progressBar?.isIndeterminate = true
            progressBar?.progress = 0
            progressTextView?.text = progressLayout.context.getString(R.string.send_file_step_idle)
            progressTextView?.setTextColor(messageColorProvider.getMessageTextColor(SendState.UNSENT))
        } else {
            progressLayout.isVisible = false
        }
    }

    private fun handleEncryptingThumbnail() {
        doHandleEncrypting(R.string.send_file_step_encrypting_thumbnail, 0, 0)
    }

    private fun handleProgressThumbnail(state: ContentUploadStateTracker.State.UploadingThumbnail) {
        doHandleProgress(R.string.send_file_step_sending_thumbnail, state.current, state.total)
    }

    private fun handleEncrypting(state: ContentUploadStateTracker.State.Encrypting) {
        doHandleEncrypting(R.string.send_file_step_encrypting_file, state.current, state.total)
    }

    private fun handleProgress(state: ContentUploadStateTracker.State.Uploading) {
        doHandleProgress(R.string.send_file_step_sending_file, state.current, state.total)
    }

    private fun doHandleEncrypting(resId: Int, current: Long, total: Long) {
        progressLayout.visibility = View.VISIBLE
        val percent = if (total > 0) (100L * (current.toFloat() / total.toFloat())) else 0f
        val progressBar = progressLayout.findViewById<ProgressBar>(R.id.mediaProgressBar)
        val progressTextView = progressLayout.findViewById<TextView>(R.id.mediaProgressTextView)
        progressBar?.isIndeterminate = false
        progressBar?.progress = percent.toInt()
        progressTextView.isVisible = true
        progressTextView?.text = progressLayout.context.getString(resId)
        progressTextView?.setTextColor(messageColorProvider.getMessageTextColor(SendState.ENCRYPTING))
    }

    private fun doHandleProgress(resId: Int, current: Long, total: Long) {
        progressLayout.visibility = View.VISIBLE
        val percent = 100L * (current.toFloat() / total.toFloat())
        val progressBar = progressLayout.findViewById<ProgressBar>(R.id.mediaProgressBar)
        val progressTextView = progressLayout.findViewById<TextView>(R.id.mediaProgressTextView)
        progressBar?.isVisible = true
        progressBar?.isIndeterminate = false
        progressBar?.progress = percent.toInt()
        progressTextView.isVisible = true
        progressTextView?.text = progressLayout.context.getString(resId,
                TextUtils.formatFileSize(progressLayout.context, current, true),
                TextUtils.formatFileSize(progressLayout.context, total, true))
        progressTextView?.setTextColor(messageColorProvider.getMessageTextColor(SendState.SENDING))
    }

    private fun handleFailure(/*state: ContentUploadStateTracker.State.Failure*/) {
        progressLayout.visibility = View.VISIBLE
        val progressBar = progressLayout.findViewById<ProgressBar>(R.id.mediaProgressBar)
        val progressTextView = progressLayout.findViewById<TextView>(R.id.mediaProgressTextView)
        progressBar?.isVisible = false
        // Do not show the message it's too technical for users, and unfortunate when upload is cancelled
        // in the middle by turning airplane mode for example
        progressTextView.isVisible = false
//        progressTextView?.text = errorFormatter.toHumanReadable(state.throwable)
//        progressTextView?.setTextColor(messageColorProvider.getMessageTextColor(SendState.UNDELIVERED))
    }

    private fun handleSuccess() {
        progressLayout.visibility = View.GONE
    }
}