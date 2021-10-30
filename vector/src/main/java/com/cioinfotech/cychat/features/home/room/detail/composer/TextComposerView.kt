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

package com.cioinfotech.cychat.features.home.room.detail.composer

import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.text.Editable
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.extensions.setTextIfDifferent
import com.cioinfotech.cychat.databinding.ComposerLayoutBinding

/**
 * Encapsulate the timeline composer UX.
 *
 */
class TextComposerView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {

    interface Callback : ComposerEditText.Callback {
        fun onCloseRelatedMessage()
        fun onSendMessage(text: CharSequence)
        fun onAddAttachment()
//        fun onSendAudio()
    }

    val views: ComposerLayoutBinding

    var callback: Callback? = null

    private var currentConstraintSetId: Int = -1

    private val animationDuration = 100L
    private var isExpanded = false
    val text: Editable?
        get() = views.composerEditText.text

    init {
        inflate(context, R.layout.composer_layout, this)
        views = ComposerLayoutBinding.bind(this)

        collapse(false)

        views.composerEditText.callback = object : ComposerEditText.Callback {
            override fun onRichContentSelected(contentUri: Uri): Boolean {
                return callback?.onRichContentSelected(contentUri) ?: false
            }

            override fun onTextChanged(text: CharSequence) {
                callback?.onTextChanged(text)
            }
        }
        views.composerRelatedMessageCloseButton.setOnClickListener {
            collapse()
            callback?.onCloseRelatedMessage()
        }

        views.sendButton.setOnClickListener {
//            if (isKeyboardTouchable) {
//                if (text.isNullOrEmpty() && !isExpanded) {
//                    callback?.onSendAudio()
//                } else {
            val textMessage = text?.toString()?.trim()?.toSpannable() ?: ""
            callback?.onSendMessage(textMessage)
//                }
//            }
        }

        views.attachmentButton.setOnClickListener {
            callback?.onAddAttachment()
        }

//        views.composerEditText.doOnTextChanged { text, _, _, _ ->
//            if (text.isNullOrEmpty() && !isExpanded) {
//                views.sendButton.setImageResource(R.drawable.ic_microphone)
//            } else {
//                views.sendButton.setImageResource(R.drawable.ic_send)
//            }
//        }
    }

    fun collapse(animate: Boolean = true, transitionComplete: (() -> Unit)? = null) {
        if (currentConstraintSetId == R.layout.composer_layout_constraint_set_compact) {
            // ignore we good
            return
        }
        isExpanded = false
        currentConstraintSetId = R.layout.composer_layout_constraint_set_compact
        applyNewConstraintSet(animate, transitionComplete)
    }

    fun expand(animate: Boolean = true, transitionComplete: (() -> Unit)? = null) {
        if (currentConstraintSetId == R.layout.composer_layout_constraint_set_expanded) {
            // ignore we good
            return
        }
        currentConstraintSetId = R.layout.composer_layout_constraint_set_expanded
        applyNewConstraintSet(animate, transitionComplete)
    }

    fun setTextIfDifferent(text: CharSequence?): Boolean {
        return views.composerEditText.setTextIfDifferent(text)
    }

    private fun applyNewConstraintSet(animate: Boolean, transitionComplete: (() -> Unit)?) {
        // val wasSendButtonInvisible = views.sendButton.isInvisible
        if (animate) {
            configureAndBeginTransition(transitionComplete)
        }
        ConstraintSet().also {
            it.clone(context, currentConstraintSetId)
            it.applyTo(this)
        }
    }

    private fun configureAndBeginTransition(transitionComplete: (() -> Unit)? = null) {
        val transition = TransitionSet().apply {
            ordering = TransitionSet.ORDERING_SEQUENTIAL
            addTransition(ChangeBounds())
            addTransition(Fade(Fade.IN))
            duration = animationDuration
            addListener(object : Transition.TransitionListener {
                override fun onTransitionEnd(transition: Transition) {
                    transitionComplete?.invoke()
                }

                override fun onTransitionResume(transition: Transition) {}

                override fun onTransitionPause(transition: Transition) {}

                override fun onTransitionCancel(transition: Transition) {}

                override fun onTransitionStart(transition: Transition) {}
            })
        }
        TransitionManager.beginDelayedTransition((parent as? ViewGroup ?: this), transition)
    }

    fun setRoomEncrypted(isEncrypted: Boolean) {
//        if (isKeyboardTouchable)
        if (isEncrypted) {
            views.composerEditText.setHint(R.string.room_message_placeholder)
//            views.composerShieldImageView.render(roomEncryptionTrustLevel)
        } else {
            views.composerEditText.setHint(R.string.room_message_placeholder)
//            views.composerShieldImageView.render(null)
        }
//        else
//            views.composerEditText.setHint(R.string.no_one_present_in_chat)
    }

    fun setKeyBoardTouch(isEnabled: Boolean) {
//        isKeyboardTouchable = isEnabled
        views.composerEditText.isFocusable = isEnabled
        views.composerEditText.isClickable = isEnabled

        if (!isEnabled)
            views.sendButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this.context, R.color.emoji_gray70))
    }
}
