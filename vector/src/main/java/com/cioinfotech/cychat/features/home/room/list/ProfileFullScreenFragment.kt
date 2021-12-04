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
import com.cioinfotech.cychat.databinding.FragmentProfileFullScreenBinding
import com.cioinfotech.cychat.features.home.AvatarRenderer
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.util.toMatrixItem

class ProfileFullScreenFragment(private val room: RoomSummary, private var avatarRenderer: AvatarRenderer) : VectorBaseDialogFragment<FragmentProfileFullScreenBinding>() {
    override fun invalidate() {}

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentProfileFullScreenBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        views.ivBack.setOnClickListener {
            requireDialog().dismiss()
        }
        views.tvTitle.text = room.displayName
        if (room.toMatrixItem().avatarUrl == "")
            avatarRenderer.render(room.toMatrixItem(), views.ivProfile)
        else
            Glide.with(requireContext())
                    .load(avatarRenderer.getURL(room.toMatrixItem()))
                    .placeholder(showCircularProgressDrawable(requireContext()))
                    .error(R.mipmap.ic_launcher)
                    .into(views.ivProfile)
    }

    private fun showCircularProgressDrawable(context: Context): CircularProgressDrawable {
        return CircularProgressDrawable(context).apply {
            strokeWidth = 8f
            centerRadius = 48f
            start()
        }
    }
}
