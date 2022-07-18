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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.cioinfotech.cychat.core.platform.VectorBaseDialogFragment
import com.cioinfotech.cychat.databinding.FragmentProfileFullScreenBinding
import com.cioinfotech.cychat.features.home.AvatarRenderer
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.util.toMatrixItem

class ProfileFullScreenFragment(private val room: RoomSummary?, private var avatarRenderer: AvatarRenderer?, private val url: String? = null) : VectorBaseDialogFragment<FragmentProfileFullScreenBinding>() {
    override fun invalidate() {}

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentProfileFullScreenBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        views.ivBack.setOnClickListener {
            requireDialog().dismiss()
        }
        room?.let {
            views.tvTitle.text = it.displayName
            if (!room.toMatrixItem().avatarUrl.isNullOrEmpty())
                views.ivProfile.showImage(avatarRenderer?.getURL(room.toMatrixItem())?.toUri())
        }
        url?.let {
            views.ivProfile.showImage(url.toUri())
        }
    }
}
