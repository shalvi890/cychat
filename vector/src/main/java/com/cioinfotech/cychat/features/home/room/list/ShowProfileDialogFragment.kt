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

package com.cioinfotech.cychat.features.home.room.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.platform.VectorBaseDialogFragment
import com.cioinfotech.cychat.databinding.FragmentShowProfileDialogBinding
import com.cioinfotech.cychat.features.home.AvatarRenderer
import com.cioinfotech.cychat.features.navigation.Navigator
import com.cioinfotech.cychat.features.share.IncomingShareController
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.util.toMatrixItem

class ShowProfileDialogFragment(
        private val room: RoomSummary,
        private val avatarRenderer: AvatarRenderer,
        private val navigator: Navigator,
        private val onRoomClicked: RoomListListener? = null,
        private val callback: IncomingShareController.Callback? = null,
) : VectorBaseDialogFragment<FragmentShowProfileDialogBinding>() {

    companion object {
        fun getInstance(room: RoomSummary,
                        avatarRenderer: AvatarRenderer,
                        navigator: Navigator,
                        onRoomClicked: RoomListListener? = null,
                        callback: IncomingShareController.Callback? = null) = ShowProfileDialogFragment(room, avatarRenderer, navigator, onRoomClicked, callback)
    }

    override fun invalidate() {}

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentShowProfileDialogBinding.inflate(
            inflater, container, false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        views.ivMessage.setOnClickListener {
            onRoomClicked?.onRoomClicked(room)
            callback?.onRoomClicked(room)
            requireDialog().dismiss()
        }
        views.root.setOnClickListener { requireDialog().dismiss() }
        views.ivInfo.setOnClickListener {
            navigator.openRoomProfile(requireActivity(), room.roomId)
        }
        views.tvTitle.text = room.displayName
        if (room.toMatrixItem().avatarUrl == "")
            avatarRenderer.render(room.toMatrixItem(), views.ivProfile)
        else {
            Glide.with(requireContext())
                    .load(avatarRenderer.getURL(room.toMatrixItem()))
                    .placeholder(showCircularProgressDrawable(requireContext()))
                    .error(R.mipmap.ic_launcher)
                    .into(views.ivProfile)
            views.ivProfile.setOnClickListener {
                ProfileFullScreenFragment(room, avatarRenderer).show(childFragmentManager, "")
            }
        }
    }

    private fun showCircularProgressDrawable(context: Context): CircularProgressDrawable {
        return CircularProgressDrawable(context).apply {
            strokeWidth = 8f
            centerRadius = 48f
            start()
        }
    }
}
