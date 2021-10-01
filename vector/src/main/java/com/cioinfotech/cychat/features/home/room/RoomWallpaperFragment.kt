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

package com.cioinfotech.cychat.features.home.room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.cioinfotech.cychat.core.di.DefaultSharedPreferences
import com.cioinfotech.cychat.core.platform.VectorBaseDialogFragment
import com.cioinfotech.cychat.databinding.FragmentRoomWallpaperBinding
import com.cioinfotech.cychat.features.home.room.adapter.RoomWallpaperAdapter

class RoomWallpaperFragment(private val roomId: String, private val itemListener: RoomWallpaperAdapter.ClickListener) : VectorBaseDialogFragment<FragmentRoomWallpaperBinding>(), RoomWallpaperAdapter.ClickListener {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentRoomWallpaperBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        views.rvWallpaper.adapter = RoomWallpaperAdapter().apply {
            this.itemClickListener = this@RoomWallpaperFragment
        }
        views.btnDefault.setOnClickListener {
            itemListener.onItemClicked(-1)
            saveWallpaperToPreference(-1)
            dismiss()
        }
        views.roomToolbarAvatarImageView.setOnClickListener { dismiss() }
        views.rvWallpaper.layoutManager = GridLayoutManager(requireContext(), 3)
    }

    override fun invalidate() {}

    override fun onItemClicked(resource: Int) {
        saveWallpaperToPreference(resource)
        itemListener.onItemClicked(resource)
        dismiss()
    }

    fun saveWallpaperToPreference(resource: Int) {
        DefaultSharedPreferences.getInstance(requireContext()).edit().apply {
            if (resource == -1)
                remove(roomId)
            else
                putInt(roomId, resource)
            apply()
        }
    }
}
